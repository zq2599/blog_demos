
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_rds_json_filter_module.h"
#include "ngx_http_rds_json_util.h"
#include "ngx_http_rds_json_processor.h"
#include "ngx_http_rds_json_handler.h"
#include "ngx_http_rds_json_output.h"

#include <ngx_config.h>


#define ngx_http_rds_json_content_type  "application/json"

#define ngx_http_rds_json_errcode_default_key  "\"errcode\""
#define ngx_http_rds_json_errstr_default_key   "\"errstr\""


static volatile ngx_cycle_t  *ngx_http_rds_json_prev_cycle = NULL;


static ngx_conf_enum_t  ngx_http_rds_json_formats[] = {
    { ngx_string("normal"), json_format_normal },
    { ngx_string("compact"), json_format_compact },
    /* { ngx_string("pretty"),  json_format_pretty }, */
    { ngx_null_string, 0 }
};


ngx_http_output_header_filter_pt  ngx_http_rds_json_next_header_filter;
ngx_http_output_body_filter_pt    ngx_http_rds_json_next_body_filter;


static void * ngx_http_rds_json_create_main_conf(ngx_conf_t *cf);
static char *ngx_http_rds_json_ret(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf);
static void *ngx_http_rds_json_create_loc_conf(ngx_conf_t *cf);
static char *ngx_http_rds_json_merge_loc_conf(ngx_conf_t *cf, void *parent,
    void *child);
static ngx_int_t ngx_http_rds_json_filter_init(ngx_conf_t *cf);
static char * ngx_http_rds_json_root(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf);
static char * ngx_http_rds_json_success_property(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_rds_json_user_property(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_rds_json_errcode_key(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf);
static char * ngx_http_rds_json_errstr_key(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf);
static char * ngx_http_rds_json(ngx_conf_t *cf, ngx_command_t *cmd, void *conf);


static ngx_command_t  ngx_http_rds_json_commands[] = {

    { ngx_string("rds_json"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF
          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_FLAG,
      ngx_http_rds_json,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_rds_json_loc_conf_t, enabled),
      NULL },

    { ngx_string("rds_json_root"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF
          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE1,
      ngx_http_rds_json_root,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("rds_json_success_property"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF
          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE1,
      ngx_http_rds_json_success_property,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("rds_json_user_property"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF
          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE2,
      ngx_http_rds_json_user_property,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("rds_json_errcode_key"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF
          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE1,
      ngx_http_rds_json_errcode_key,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("rds_json_errstr_key"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF
          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE1,
      ngx_http_rds_json_errstr_key,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("rds_json_format"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF
          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE1,
      ngx_conf_set_enum_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_rds_json_loc_conf_t, format),
      &ngx_http_rds_json_formats },

    { ngx_string("rds_json_content_type"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF
          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE1,
      ngx_conf_set_str_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_rds_json_loc_conf_t, content_type),
      NULL },

    { ngx_string("rds_json_ret"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_2MORE,
      ngx_http_rds_json_ret,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("rds_json_buffer_size"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE1,
      ngx_conf_set_size_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_rds_json_loc_conf_t, buf_size),
      NULL },

      ngx_null_command
};


static ngx_http_module_t  ngx_http_rds_json_filter_module_ctx = {
    NULL,                                  /* preconfiguration */
    ngx_http_rds_json_filter_init,         /* postconfiguration */

    ngx_http_rds_json_create_main_conf,    /* create main configuration */
    NULL,                                  /* init main configuration */

    NULL,                                  /* create server configuration */
    NULL,                                  /* merge server configuration */

    ngx_http_rds_json_create_loc_conf,     /* create location configuration */
    ngx_http_rds_json_merge_loc_conf       /* merge location configuration */
};


ngx_module_t  ngx_http_rds_json_filter_module = {
    NGX_MODULE_V1,
    &ngx_http_rds_json_filter_module_ctx,  /* module context */
    ngx_http_rds_json_commands,            /* module directives */
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


static ngx_int_t
ngx_http_rds_json_header_filter(ngx_http_request_t *r)
{
    ngx_http_rds_json_ctx_t         *ctx;
    ngx_http_rds_json_loc_conf_t    *conf;

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "rds json header filter, \"%V\"", &r->uri);

    /* XXX maybe we can generate stub JSON strings like
     * {"errcode":403,"error":"Permission denied"}
     * for HTTP error pages? */

    if ((r->headers_out.status < NGX_HTTP_OK)
        || (r->headers_out.status >= NGX_HTTP_SPECIAL_RESPONSE)
        || (r->headers_out.status == NGX_HTTP_NO_CONTENT)
        || (r->headers_out.status == NGX_HTTP_RESET_CONTENT))
    {
        ngx_http_set_ctx(r, NULL, ngx_http_rds_json_filter_module);

        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "rds json: skipped due to bad status: %ui",
                       r->headers_out.status);

        return ngx_http_rds_json_next_header_filter(r);
    }

    /* r->headers_out.status = 0; */

    conf = ngx_http_get_module_loc_conf(r, ngx_http_rds_json_filter_module);

    if (!conf->enabled) {
        ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "rds json: skipped because not enabled in the current "
                       "location");

        return ngx_http_rds_json_next_header_filter(r);
    }

    if (ngx_http_rds_json_test_content_type(r) != NGX_OK) {
        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "rds json: skipped due to unmatched Content-Type "
                       "response header \"%V\"",
                       &r->headers_out.content_type);

        return ngx_http_rds_json_next_header_filter(r);
    }

    r->headers_out.content_type = conf->content_type;
    r->headers_out.content_type_len = conf->content_type.len;

    ctx = ngx_pcalloc(r->pool, sizeof(ngx_http_rds_json_ctx_t));

    if (ctx == NULL) {
        return NGX_ERROR;
    }

    ctx->tag = (ngx_buf_tag_t) &ngx_http_rds_json_filter_module;

    ctx->state = state_expect_header;

    ctx->header_sent = 0;

    ctx->last_out = &ctx->out;

    /* set by ngx_pcalloc
     *      ctx->out       = NULL
     *      ctx->busy_bufs = NULL
     *      ctx->free_bufs = NULL
     *      ctx->cached = (ngx_buf_t) 0
     *      ctx->postponed = (ngx_buf_t) 0
     *      ctx->avail_out = 0
     *      ctx->col_names = NULL
     *      ctx->col_count = 0
     *      ctx->cur_col = 0
     *      ctx->field_offset = 0
     *      ctx->field_total = 0
     *      ctx->field_data_rest = 0
     */

    ngx_http_set_ctx(r, ctx, ngx_http_rds_json_filter_module);

    ngx_http_clear_content_length(r);

    r->filter_need_in_memory = 1;

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "rds json header filter postponed header sending");

    /* we do postpone the header sending to the body filter */
    return NGX_OK;
}


static ngx_int_t
ngx_http_rds_json_body_filter(ngx_http_request_t *r, ngx_chain_t *in)
{
    ngx_http_rds_json_ctx_t    *ctx;
    ngx_int_t                   rc;

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "rds json body filter, \"%V\"", &r->uri);

    if (in == NULL || r->header_only) {
        return ngx_http_rds_json_next_body_filter(r, in);
    }

    ctx = ngx_http_get_module_ctx(r, ngx_http_rds_json_filter_module);

    if (ctx == NULL) {
        return ngx_http_rds_json_next_body_filter(r, in);
    }

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "rds json body filter postponed header sending");

    switch (ctx->state) {
    case state_expect_header:
        rc = ngx_http_rds_json_process_header(r, in, ctx);
        break;

    case state_expect_col:
        rc = ngx_http_rds_json_process_col(r, in, ctx);
        break;

    case state_expect_row:
        rc = ngx_http_rds_json_process_row(r, in, ctx);
        break;

    case state_expect_field:
        rc = ngx_http_rds_json_process_field(r, in, ctx);
        break;

    case state_expect_more_field_data:
        rc = ngx_http_rds_json_process_more_field_data(r, in, ctx);
        break;

    case state_done:

        ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "rds json body filter discarding unexpected trailing "
                       "buffers");

        /* mark the remaining bufs as consumed */

        ngx_http_rds_json_discard_bufs(r->pool, in);

        return NGX_OK;

    default:
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds_json: invalid internal state: %d",
                      ctx->state);

        rc = NGX_ERROR;

        break;
    }

    dd("body filter rc: %d", (int) rc);

    if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
        ctx->state = state_done;

        if (!ctx->header_sent) {
            ctx->header_sent = 1;

            if (rc == NGX_ERROR) {
                rc = NGX_HTTP_INTERNAL_SERVER_ERROR;
            }

            r->headers_out.status = rc;

            ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                           "rds json body filter sending error page headers");

            ngx_http_rds_json_next_header_filter(r);
            ngx_http_send_special(r, NGX_HTTP_LAST);

            return NGX_ERROR;
        }

        return NGX_ERROR;
    }

    dd("output bufs");

    return ngx_http_rds_json_output_bufs(r, ctx);
}


static ngx_int_t
ngx_http_rds_json_filter_init(ngx_conf_t *cf)
{
    int                              multi_http_blocks;
    ngx_http_rds_json_main_conf_t   *rmcf;

    rmcf = ngx_http_conf_get_module_main_conf(cf,
                                              ngx_http_rds_json_filter_module);

    if (ngx_http_rds_json_prev_cycle != ngx_cycle) {
        ngx_http_rds_json_prev_cycle = ngx_cycle;
        multi_http_blocks = 0;

    } else {
        multi_http_blocks = 1;
    }

    dd("setting next filter: %d", (int) rmcf->requires_filters);

    if (multi_http_blocks || rmcf->requires_filters) {
        dd("register filters");

        ngx_http_rds_json_next_header_filter = ngx_http_top_header_filter;
        ngx_http_top_header_filter = ngx_http_rds_json_header_filter;

        ngx_http_rds_json_next_body_filter = ngx_http_top_body_filter;
        ngx_http_top_body_filter = ngx_http_rds_json_body_filter;
    }

    return NGX_OK;
}


static void *
ngx_http_rds_json_create_loc_conf(ngx_conf_t *cf)
{
    ngx_http_rds_json_loc_conf_t  *conf;

    conf = ngx_pcalloc(cf->pool, sizeof(ngx_http_rds_json_loc_conf_t));
    if (conf == NULL) {
        return NULL;
    }

    /*
     * set by ngx_pcalloc():
     *
     *     conf->content_type = { 0, NULL };
     *     conf->root         = { 0, NULL };
     *     conf->success      = { 0, NULL };
     *     conf->user_props   = NULL;
     *     conf->errcode      = { 0, NULL };
     *     conf->errstr       = NULL;
     */

    conf->enabled = NGX_CONF_UNSET;
    conf->format = NGX_CONF_UNSET_UINT;

    conf->buf_size = NGX_CONF_UNSET_SIZE;

#if 0
    conf->buf_size = 1;
#endif

    return conf;
}


static char *
ngx_http_rds_json_merge_loc_conf(ngx_conf_t *cf, void *parent, void *child)
{
    ngx_http_rds_json_loc_conf_t *prev = parent;
    ngx_http_rds_json_loc_conf_t *conf = child;

    ngx_conf_merge_value(conf->enabled, prev->enabled, 0);

    ngx_conf_merge_uint_value(conf->format, prev->format, json_format_normal);

    ngx_conf_merge_str_value(conf->root, prev->root, "");

    ngx_conf_merge_str_value(conf->success, prev->success, "");

    if (conf->user_props == NULL) {
        conf->user_props = prev->user_props;
    }

    if (conf->root.len == 0 && (conf->success.len || conf->user_props)) {
        ngx_str_set(&conf->root, "\"data\"");
    }

    ngx_conf_merge_str_value(conf->errcode_key, prev->errcode_key,
                             "\"errcode\"");

    ngx_conf_merge_str_value(conf->errstr_key, prev->errstr_key,
                             "\"errstr\"");

    ngx_conf_merge_str_value(conf->content_type, prev->content_type,
                             ngx_http_rds_json_content_type);

    ngx_conf_merge_size_value(conf->buf_size, prev->buf_size,
                              (size_t) ngx_pagesize);

    return NGX_CONF_OK;
}


static char *
ngx_http_rds_json_ret(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_rds_json_loc_conf_t        *jlcf = conf;
    ngx_http_core_loc_conf_t            *clcf;
    ngx_str_t                           *value;
    ngx_http_compile_complex_value_t     ccv;
    ngx_uint_t                           i;
    u_char                               c;

    value = cf->args->elts;

    jlcf->errcode = value[1];

    if (jlcf->errcode.len == 0) {
        ngx_log_error(NGX_LOG_ERR, cf->log, 0,
                      "rds_json: rds_json_ret: the errcode argument is empty");

        return NGX_CONF_ERROR;
    }

    /* check the validaty of the errcode argument */
    for (i = 0; i < jlcf->errcode.len; i++) {
        c = jlcf->errcode.data[i];

        if (c < '0' || c > '9') {
            ngx_log_error(NGX_LOG_ERR, cf->log, 0,
                          "rds_json: rds_json_ret: invalid errcode argument: "
                          "\"%V\"", &jlcf->errcode);

            return NGX_CONF_ERROR;
        }
    }

    jlcf->errstr = ngx_palloc(cf->pool, sizeof(ngx_http_complex_value_t));
    if (jlcf->errstr == NULL) {
        return NGX_CONF_ERROR;
    }

    if (value[2].len == 0) {
        ngx_memzero(jlcf->errstr, sizeof(ngx_http_complex_value_t));
        goto done;
    }

    ngx_memzero(&ccv, sizeof(ngx_http_compile_complex_value_t));

    ccv.cf = cf;
    ccv.value = &value[2];
    ccv.complex_value = jlcf->errstr;

    if (ngx_http_compile_complex_value(&ccv) != NGX_OK) {
        return NGX_CONF_ERROR;
    }

done:

    clcf = ngx_http_conf_get_module_loc_conf(cf,
            ngx_http_core_module);
    if (clcf == NULL) {
        return NGX_CONF_ERROR;
    }

    clcf->handler = ngx_http_rds_json_ret_handler;

    return NGX_CONF_OK;
}


static char *
ngx_http_rds_json_root(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_rds_json_loc_conf_t        *jlcf = conf;
    ngx_str_t                           *value;
    uintptr_t                            escape;
    u_char                              *p;

    value = cf->args->elts;

    if (jlcf->root.len) {
        return "is duplicate";
    }

    if (value[1].len == 0) {
        return "takes an empty value";
    }

    escape = ngx_http_rds_json_escape_json_str(NULL, value[1].data,
                                               value[1].len);

    jlcf->root.len = value[1].len + escape + sizeof("\"\"") - 1;

    p = ngx_palloc(cf->pool, jlcf->root.len);
    if (p == NULL) {
        return NGX_CONF_ERROR;
    }

    jlcf->root.data = p;

    *p++ = '"';

    if (escape == 0) {
        p = ngx_copy(p, value[1].data, value[1].len);

    } else {
        p = (u_char *) ngx_http_rds_json_escape_json_str(p, value[1].data,
                                                         value[1].len);
    }

    *p++ = '"';

    if (p - jlcf->root.data != (ssize_t) jlcf->root.len) {
        return "sees buffer error";
    }

    return NGX_CONF_OK;
}


static char *
ngx_http_rds_json_success_property(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf)
{
    ngx_http_rds_json_loc_conf_t        *jlcf = conf;
    ngx_str_t                           *value;
    uintptr_t                            escape;
    u_char                              *p;

    value = cf->args->elts;

    if (jlcf->success.len) {
        return "is duplicate";
    }

    if (value[1].len == 0) {
        return "takes an empty value";
    }

    escape = ngx_http_rds_json_escape_json_str(NULL, value[1].data,
                                               value[1].len);

    jlcf->success.len = value[1].len + escape + sizeof("\"\"") - 1;

    p = ngx_palloc(cf->pool, jlcf->success.len);
    if (p == NULL) {
        return NGX_CONF_ERROR;
    }

    jlcf->success.data = p;

    *p++ = '"';

    if (escape == 0) {
        p = ngx_copy(p, value[1].data, value[1].len);

    } else {
        p = (u_char *) ngx_http_rds_json_escape_json_str(p, value[1].data,
                                                         value[1].len);
    }

    *p++ = '"';

    if (p - jlcf->success.data != (ssize_t) jlcf->success.len) {
        return "sees buffer error";
    }

    return NGX_CONF_OK;
}


static char *
ngx_http_rds_json_user_property(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf)
{
    u_char                              *p;
    ngx_str_t                           *value;
    uintptr_t                            escape;
    ngx_http_rds_json_property_t        *prop;
    ngx_http_rds_json_loc_conf_t        *jlcf = conf;
    ngx_http_compile_complex_value_t     ccv;

    value = cf->args->elts;

    if (value[1].len == 0) {
        return "takes an empty key";
    }

    if (value[2].len == 0) {
        return "takes an empty value";
    }

    if (jlcf->user_props == NULL) {
        jlcf->user_props = ngx_array_create(cf->pool, 4,
                                        sizeof(ngx_http_rds_json_property_t));

        if (jlcf->user_props == NULL) {
            return NGX_CONF_ERROR;
        }
    }

    prop = ngx_array_push(jlcf->user_props);
    if (prop == NULL) {
        return NGX_CONF_ERROR;
    }

    /* process the user property key */

    escape = ngx_http_rds_json_escape_json_str(NULL, value[1].data,
                                               value[1].len);

    prop->key.len = value[1].len + escape + sizeof("\"\"") - 1;

    p = ngx_palloc(cf->pool, prop->key.len);
    if (p == NULL) {
        return NGX_CONF_ERROR;
    }

    prop->key.data = p;

    *p++ = '"';

    if (escape == 0) {
        p = ngx_copy(p, value[1].data, value[1].len);

    } else {
        p = (u_char *) ngx_http_rds_json_escape_json_str(p, value[1].data,
                                                         value[1].len);
    }

    *p++ = '"';

    if (p - prop->key.data != (ssize_t) prop->key.len) {
        return "sees buffer error";
    }

    /* process the user property value */

    ngx_memzero(&ccv, sizeof(ngx_http_compile_complex_value_t));
    ccv.cf = cf;
    ccv.value = &value[2];
    ccv.complex_value = &prop->value;

    if (ngx_http_compile_complex_value(&ccv) != NGX_OK) {
        return NGX_CONF_ERROR;
    }

    return NGX_CONF_OK;
}


static char *
ngx_http_rds_json_errcode_key(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_rds_json_loc_conf_t        *jlcf = conf;
    u_char                              *p;
    ngx_str_t                           *value;
    uintptr_t                            escape;

    value = cf->args->elts;

    if (jlcf->errcode_key.len) {
        return "is duplicate";
    }

    if (value[1].len == 0) {
        return "takes an empty value";
    }

    escape = ngx_http_rds_json_escape_json_str(NULL, value[1].data,
                                               value[1].len);

    jlcf->errcode_key.len = value[1].len + escape + sizeof("\"\"") - 1;

    p = ngx_palloc(cf->pool, jlcf->errcode_key.len);
    if (p == NULL) {
        return NGX_CONF_ERROR;
    }

    jlcf->errcode_key.data = p;

    *p++ = '"';

    if (escape == 0) {
        p = ngx_copy(p, value[1].data, value[1].len);

    } else {
        p = (u_char *) ngx_http_rds_json_escape_json_str(p, value[1].data,
                                                         value[1].len);
    }

    *p++ = '"';

    if (p - jlcf->errcode_key.data != (ssize_t) jlcf->errcode_key.len) {
        return "sees buffer error";
    }

    return NGX_CONF_OK;
}


static char *
ngx_http_rds_json_errstr_key(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_rds_json_loc_conf_t        *jlcf = conf;
    ngx_str_t                           *value;
    uintptr_t                            escape;
    u_char                              *p;

    value = cf->args->elts;

    if (jlcf->errstr_key.len) {
        return "is duplicate";
    }

    if (value[1].len == 0) {
        return "takes an empty value";
    }

    escape = ngx_http_rds_json_escape_json_str(NULL, value[1].data,
                                               value[1].len);

    jlcf->errstr_key.len = value[1].len + escape + sizeof("\"\"") - 1;

    p = ngx_palloc(cf->pool, jlcf->errstr_key.len);
    if (p == NULL) {
        return NGX_CONF_ERROR;
    }

    jlcf->errstr_key.data = p;

    *p++ = '"';

    if (escape == 0) {
        p = ngx_copy(p, value[1].data, value[1].len);

    } else {
        p = (u_char *) ngx_http_rds_json_escape_json_str(p, value[1].data,
                                                         value[1].len);
    }

    *p++ = '"';

    if (p - jlcf->errstr_key.data != (ssize_t) jlcf->errstr_key.len) {
        return "sees buffer error";
    }

    return NGX_CONF_OK;
}


static char *
ngx_http_rds_json(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_rds_json_main_conf_t     *rmcf;

    rmcf = ngx_http_conf_get_module_main_conf(cf,
                                              ngx_http_rds_json_filter_module);

    dd("set filter used to 1");

    rmcf->requires_filters = 1;

    return ngx_conf_set_flag_slot(cf, cmd, conf);
}


static void *
ngx_http_rds_json_create_main_conf(ngx_conf_t *cf)
{
    ngx_http_rds_json_main_conf_t    *rmcf;

    rmcf = ngx_pcalloc(cf->pool, sizeof(ngx_http_rds_json_main_conf_t));
    if (rmcf == NULL) {
        return NULL;
    }

    /* set by ngx_pcalloc:
     *      rmcf->requires_filters = 0;
     */

    return rmcf;
}
