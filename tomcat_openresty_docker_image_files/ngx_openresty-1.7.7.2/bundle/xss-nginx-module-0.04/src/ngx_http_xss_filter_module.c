
/*
 * Copyright (C) agentzh
 */

#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_xss_filter_module.h"
#include "ngx_http_xss_util.h"

#include <nginx.h>
#include <ngx_config.h>


#define ngx_http_xss_default_output_type "application/x-javascript"


static ngx_str_t  ngx_http_xss_default_types[] = {
    ngx_string("application/json"),
    ngx_null_string
};


static void *ngx_http_xss_create_loc_conf(ngx_conf_t *cf);
static char *ngx_http_xss_merge_loc_conf(ngx_conf_t *cf, void *parent,
    void *child);
static ngx_int_t ngx_http_xss_filter_init(ngx_conf_t *cf);
static void * ngx_http_xss_create_main_conf(ngx_conf_t *cf);
static char * ngx_http_xss_get(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf);


static volatile ngx_cycle_t  *ngx_http_xss_prev_cycle = NULL;


static ngx_command_t  ngx_http_xss_commands[] = {

    { ngx_string("xss_get"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF
          |NGX_HTTP_LIF_CONF|NGX_CONF_FLAG,
      ngx_http_xss_get,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_xss_loc_conf_t, get_enabled),
      NULL },

    { ngx_string("xss_callback_arg"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF
          |NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_str_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_xss_loc_conf_t, callback_arg),
      NULL },

    { ngx_string("xss_input_types"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF
          |NGX_CONF_1MORE|NGX_HTTP_LIF_CONF,
      ngx_http_types_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_xss_loc_conf_t, input_types_keys),
      &ngx_http_xss_default_types[0] },

    { ngx_string("xss_check_status"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF
          |NGX_CONF_FLAG|NGX_HTTP_LIF_CONF,
      ngx_conf_set_flag_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_xss_loc_conf_t, check_status),
      NULL },

    { ngx_string("xss_override_status"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF
          |NGX_CONF_FLAG|NGX_HTTP_LIF_CONF,
      ngx_conf_set_flag_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_xss_loc_conf_t, override_status),
      NULL },

    { ngx_string("xss_output_type"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF
          |NGX_CONF_1MORE|NGX_HTTP_LIF_CONF,
      ngx_conf_set_str_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_xss_loc_conf_t, output_type),
      NULL },

      ngx_null_command
};


static ngx_http_module_t  ngx_http_xss_filter_module_ctx = {
    NULL,                                  /* preconfiguration */
    ngx_http_xss_filter_init,              /* postconfiguration */

    ngx_http_xss_create_main_conf,         /* create main configuration */
    NULL,                                  /* init main configuration */

    NULL,                                  /* create server configuration */
    NULL,                                  /* merge server configuration */

    ngx_http_xss_create_loc_conf,          /* create location configuration */
    ngx_http_xss_merge_loc_conf            /* merge location configuration */
};


ngx_module_t  ngx_http_xss_filter_module = {
    NGX_MODULE_V1,
    &ngx_http_xss_filter_module_ctx,       /* module context */
    ngx_http_xss_commands,                 /* module directives */
    NGX_HTTP_MODULE,                       /* module type */
    NULL,                                  /* init master */
    NULL,                                  /* init module */
    NULL,                                  /* init process */
    NULL,                                  /* init thread */
    NULL,                                  /* exit thread */
    NULL,                                  /* exit process */
    NULL,                                  /* exit master */
    NGX_MODULE_V1_PADDING
};


static ngx_http_output_header_filter_pt  ngx_http_next_header_filter;
static ngx_http_output_body_filter_pt    ngx_http_next_body_filter;


static ngx_int_t
ngx_http_xss_header_filter(ngx_http_request_t *r)
{
    ngx_http_xss_ctx_t          *ctx;
    ngx_http_xss_loc_conf_t     *xlcf;
    ngx_str_t                    callback;
    u_char                      *p, *src, *dst;

    if (r != r->main) {
        ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "xss skipped in subrequests");

        return ngx_http_next_header_filter(r);
    }

    xlcf = ngx_http_get_module_loc_conf(r, ngx_http_xss_filter_module);

    if (!xlcf->get_enabled) {
        return ngx_http_next_header_filter(r);
    }

    if (r->method != NGX_HTTP_GET) {
        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "xss skipped due to the unmatched request method: %V",
                       &r->method_name);

        return ngx_http_next_header_filter(r);
    }

    if (xlcf->check_status) {

        if (r->headers_out.status != NGX_HTTP_OK
            && r->headers_out.status != NGX_HTTP_CREATED)
        {
            ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                           "xss skipped due to unmatched response status "
                           "\"%ui\"", r->headers_out.status);

            return ngx_http_next_header_filter(r);
        }
    }

    if (xlcf->callback_arg.len == 0) {

        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "xss: xss_get is enabled but no xss_callback_arg "
                      "specified");

        return ngx_http_next_header_filter(r);
    }

    if (ngx_http_test_content_type(r, &xlcf->input_types) == NULL) {

        ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "xss skipped due to unmatched Content-Type response "
                       "header");

        return ngx_http_next_header_filter(r);
    }

    if (ngx_http_arg(r, xlcf->callback_arg.data, xlcf->callback_arg.len,
                     &callback)
        != NGX_OK)
    {
        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "xss skipped: no GET argument \"%V\" specified in "
                       "the request", &xlcf->callback_arg);

        return ngx_http_next_header_filter(r);
    }

    p = ngx_palloc(r->pool, callback.len);
    if (p == NULL) {
        return NGX_ERROR;
    }

    src = callback.data; dst = p;

    ngx_unescape_uri(&dst, &src, callback.len, NGX_UNESCAPE_URI_COMPONENT);

    if (src != callback.data + callback.len) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "xss: unescape uri: input data not consumed completely");

        return NGX_ERROR;
    }

    callback.data = p;
    callback.len = dst - p;

    if (ngx_http_xss_test_callback(callback.data, callback.len) != NGX_OK) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "xss: bad callback argument: \"%V\"", &callback);

        return ngx_http_next_header_filter(r);
    }

    ctx = ngx_pcalloc(r->pool, sizeof(ngx_http_xss_ctx_t));
    if (ctx == NULL) {
        return NGX_ERROR;
    }

    /*
     * set by ngx_pcalloc():
     *
     *     ctx->callback = { 0, NULL };
     *     ctx->before_body_sent = 0;
     */

    ctx->callback = callback;

    ngx_http_set_ctx(r, ctx, ngx_http_xss_filter_module);

    r->headers_out.content_type = xlcf->output_type;
    r->headers_out.content_type_len = xlcf->output_type.len;
    r->headers_out.content_type_lowcase = NULL;

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "xss output Content-Type header \"%V\"",
                   &xlcf->output_type);

    ngx_http_clear_content_length(r);
    ngx_http_clear_accept_ranges(r);

    if (xlcf->override_status
        && r->headers_out.status >= NGX_HTTP_SPECIAL_RESPONSE)
    {
        r->headers_out.status = NGX_HTTP_OK;
    }

    return ngx_http_next_header_filter(r);
}


static ngx_int_t
ngx_http_xss_body_filter(ngx_http_request_t *r, ngx_chain_t *in)
{
    ngx_uint_t                 last;
    ngx_chain_t               *cl, *orig_in;
    ngx_chain_t              **ll = NULL;
    ngx_http_xss_ctx_t        *ctx;
    size_t                     len;
    ngx_buf_t                 *b;

    if (in == NULL || r->header_only) {
        return ngx_http_next_body_filter(r, in);
    }

    ctx = ngx_http_get_module_ctx(r, ngx_http_xss_filter_module);

    if (ctx == NULL) {
        return ngx_http_next_body_filter(r, in);
    }

    orig_in = in;

    if (!ctx->before_body_sent) {
        ctx->before_body_sent = 1;

        dd("callback: %.*s", (int) ctx->callback.len,
           ctx->callback.data);

        len = ctx->callback.len + sizeof("(") - 1;

        b = ngx_create_temp_buf(r->pool, len);
        if (b == NULL) {
            return NGX_ERROR;
        }

        b->last = ngx_copy(b->last, ctx->callback.data, ctx->callback.len);

        *b->last++ = '(';

        cl = ngx_alloc_chain_link(r->pool);
        if (cl == NULL) {
            return NGX_ERROR;
        }

        cl->buf = b;
        cl->next = in;
        in = cl;
    }

    last = 0;

    for (cl = orig_in; cl; cl = cl->next) {
        if (cl->buf->last_buf) {
            cl->buf->last_buf = 0;
            cl->buf->sync = 1;
            ll = &cl->next;
            last = 1;
        }
    }

    if (last) {
        len = sizeof(");") - 1;

        b = ngx_create_temp_buf(r->pool, len);
        if (b == NULL) {
            return NGX_ERROR;
        }

        *b->last++ = ')';
        *b->last++ = ';';

        b->last_buf = 1;

        cl = ngx_alloc_chain_link(r->pool);
        if (cl == NULL) {
            return NGX_ERROR;
        }

        cl->buf = b;
        cl->next = NULL;
        *ll = cl;

        ngx_http_set_ctx(r, NULL, ngx_http_xss_filter_module);
    }

    return ngx_http_next_body_filter(r, in);
}


static ngx_int_t
ngx_http_xss_filter_init(ngx_conf_t *cf)
{
    int                             multi_http_blocks;
    ngx_http_xss_main_conf_t       *xmcf;

    xmcf = ngx_http_conf_get_module_main_conf(cf, ngx_http_xss_filter_module);

    if (ngx_http_xss_prev_cycle != ngx_cycle) {
        ngx_http_xss_prev_cycle = ngx_cycle;
        multi_http_blocks = 0;

    } else {
        multi_http_blocks = 1;
    }

    if (multi_http_blocks || xmcf->requires_filter) {
        ngx_http_next_header_filter = ngx_http_top_header_filter;
        ngx_http_top_header_filter = ngx_http_xss_header_filter;

        ngx_http_next_body_filter = ngx_http_top_body_filter;
        ngx_http_top_body_filter = ngx_http_xss_body_filter;
    }

    return NGX_OK;
}


static void *
ngx_http_xss_create_loc_conf(ngx_conf_t *cf)
{
    ngx_http_xss_loc_conf_t  *conf;

    conf = ngx_pcalloc(cf->pool, sizeof(ngx_http_xss_loc_conf_t));
    if (conf == NULL) {
        return NULL;
    }

    /*
     * set by ngx_pcalloc():
     *
     *     conf->callback_arg = { 0, NULL };
     *     conf->input_types = { NULL };
     *     conf->input_types_keys = NULL;
     *     conf->output_type = { 0, NULL };
     */

    conf->get_enabled = NGX_CONF_UNSET;
    conf->check_status = NGX_CONF_UNSET;
    conf->override_status = NGX_CONF_UNSET;

    return conf;
}


static char *
ngx_http_xss_merge_loc_conf(ngx_conf_t *cf, void *parent, void *child)
{
    ngx_http_xss_loc_conf_t *prev = parent;
    ngx_http_xss_loc_conf_t *conf = child;

    ngx_conf_merge_str_value(conf->callback_arg, prev->callback_arg, "");

    ngx_conf_merge_value(conf->get_enabled, prev->get_enabled, 0);

    ngx_conf_merge_value(conf->check_status, prev->check_status, 1);

    ngx_conf_merge_value(conf->override_status, prev->override_status, 1);

#if defined(nginx_version) && nginx_version >= 8029
    if (ngx_http_merge_types(cf, &conf->input_types_keys, &conf->input_types,
                             &prev->input_types_keys, &prev->input_types,
                             ngx_http_xss_default_types)
        != NGX_OK)
#else /* 0.7.x or 0.8.x < 0.8.29 */
    if (ngx_http_merge_types(cf, conf->input_types_keys, &conf->input_types,
                             prev->input_types_keys, &prev->input_types,
                             ngx_http_xss_default_types)
        != NGX_OK)
#endif
    {
        return NGX_CONF_ERROR;
    }

    ngx_conf_merge_str_value(conf->output_type, prev->output_type,
                             ngx_http_xss_default_output_type);

    return NGX_CONF_OK;
}


static void *
ngx_http_xss_create_main_conf(ngx_conf_t *cf)
{
    ngx_http_xss_main_conf_t    *conf;

    conf = ngx_pcalloc(cf->pool, sizeof(ngx_http_xss_main_conf_t));
    if (conf == NULL) {
        return NULL;
    }

    /* set by ngx_pcalloc:
     *      conf->requires_filter = 0;
     */

    return conf;
}


static char *
ngx_http_xss_get(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_xss_main_conf_t       *xmcf;

    xmcf = ngx_http_conf_get_module_main_conf(cf,
                                              ngx_http_xss_filter_module);

    xmcf->requires_filter = 1;

    return ngx_conf_set_flag_slot(cf, cmd, conf);
}
