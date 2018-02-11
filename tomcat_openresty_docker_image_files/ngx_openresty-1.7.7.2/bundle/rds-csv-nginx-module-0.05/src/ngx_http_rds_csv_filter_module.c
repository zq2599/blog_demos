
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_rds_csv_filter_module.h"
#include "ngx_http_rds_csv_util.h"
#include "ngx_http_rds_csv_processor.h"
#include "ngx_http_rds_csv_output.h"

#include <ngx_config.h>


#define ngx_http_rds_csv_content_type  "text/csv"
#define ngx_http_rds_csv_row_term  "\r\n"


static volatile ngx_cycle_t  *ngx_http_rds_csv_prev_cycle = NULL;


ngx_http_output_header_filter_pt  ngx_http_rds_csv_next_header_filter;
ngx_http_output_body_filter_pt    ngx_http_rds_csv_next_body_filter;


static void *ngx_http_rds_csv_create_loc_conf(ngx_conf_t *cf);
static char *ngx_http_rds_csv_merge_loc_conf(ngx_conf_t *cf, void *parent,
    void *child);
static ngx_int_t ngx_http_rds_csv_filter_init(ngx_conf_t *cf);
static char *ngx_http_rds_csv_row_terminator(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char *ngx_http_rds_csv_field_separator(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_rds_csv(ngx_conf_t *cf, ngx_command_t *cmd, void *conf);
static void * ngx_http_rds_csv_create_main_conf(ngx_conf_t *cf);


static ngx_command_t  ngx_http_rds_csv_commands[] = {

    { ngx_string("rds_csv"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF
          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_FLAG,
      ngx_http_rds_csv,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_rds_csv_loc_conf_t, enabled),
      NULL },

    { ngx_string("rds_csv_row_terminator"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF
          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE1,
      ngx_http_rds_csv_row_terminator,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_rds_csv_loc_conf_t, row_term),
      NULL },

    { ngx_string("rds_csv_field_separator"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF
          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE1,
      ngx_http_rds_csv_field_separator,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("rds_csv_field_name_header"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF
          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_FLAG,
      ngx_conf_set_flag_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_rds_csv_loc_conf_t, field_name_header),
      NULL },

    { ngx_string("rds_csv_content_type"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF
          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE1,
      ngx_conf_set_str_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_rds_csv_loc_conf_t, content_type),
      NULL },

    { ngx_string("rds_csv_buffer_size"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE1,
      ngx_conf_set_size_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_rds_csv_loc_conf_t, buf_size),
      NULL },

      ngx_null_command
};


static ngx_http_module_t  ngx_http_rds_csv_filter_module_ctx = {
    NULL,                                 /* preconfiguration */
    ngx_http_rds_csv_filter_init,         /* postconfiguration */

    ngx_http_rds_csv_create_main_conf,    /* create main configuration */
    NULL,                                 /* init main configuration */

    NULL,                                 /* create server configuration */
    NULL,                                 /* merge server configuration */

    ngx_http_rds_csv_create_loc_conf,     /* create location configuration */
    ngx_http_rds_csv_merge_loc_conf       /* merge location configuration */
};


ngx_module_t  ngx_http_rds_csv_filter_module = {
    NGX_MODULE_V1,
    &ngx_http_rds_csv_filter_module_ctx,  /* module context */
    ngx_http_rds_csv_commands,            /* module directives */
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
ngx_http_rds_csv_header_filter(ngx_http_request_t *r)
{
    ngx_http_rds_csv_ctx_t          *ctx;
    ngx_http_rds_csv_loc_conf_t     *conf;
    size_t                           len;
    u_char                          *p;

    /* XXX maybe we can generate stub JSON strings like
     * {"errcode":403,"error":"Permission denied"}
     * for HTTP error pages? */
    if ((r->headers_out.status < NGX_HTTP_OK)
        || (r->headers_out.status >= NGX_HTTP_SPECIAL_RESPONSE)
        || (r->headers_out.status == NGX_HTTP_NO_CONTENT)
        || (r->headers_out.status == NGX_HTTP_RESET_CONTENT))
    {
        ngx_http_set_ctx(r, NULL, ngx_http_rds_csv_filter_module);

        dd("status is not OK: %d, skipping", (int) r->headers_out.status);

        return ngx_http_rds_csv_next_header_filter(r);
    }

    /* r->headers_out.status = 0; */

    conf = ngx_http_get_module_loc_conf(r, ngx_http_rds_csv_filter_module);

    if (!conf->enabled) {
        return ngx_http_rds_csv_next_header_filter(r);
    }

    if (ngx_http_rds_csv_test_content_type(r) != NGX_OK) {
        return ngx_http_rds_csv_next_header_filter(r);
    }

    if (conf->content_type.len == sizeof(ngx_http_rds_csv_content_type) - 1
        && ngx_strncmp(conf->content_type.data, ngx_http_rds_csv_content_type,
                       sizeof(ngx_http_rds_csv_content_type) - 1) == 0)
    {
        /* MIME type is text/csv, we process Content-Type
         * according to RFC 4180 */

        len = sizeof(ngx_http_rds_csv_content_type) - 1
              + sizeof("; header=") - 1;

        if (conf->field_name_header) {
            len += sizeof("presence") - 1;

        } else {
            len += sizeof("absence") - 1;
        }

        p = ngx_palloc(r->pool, len);
        if (p == NULL) {
            return NGX_ERROR;
        }

        r->headers_out.content_type.len = len;
        r->headers_out.content_type_len = len;

        r->headers_out.content_type.data = p;

        p = ngx_copy(p, conf->content_type.data, conf->content_type.len);

        if (conf->field_name_header) {
            p = ngx_copy_literal(p, "; header=presence");

        } else {
            p = ngx_copy_literal(p, "; header=absence");
        }

        if (p - r->headers_out.content_type.data != (ssize_t) len) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "rds_csv: content type buffer error: %uz != %uz",
                          (size_t) (p - r->headers_out.content_type.data),
                          len);

            return NGX_ERROR;
        }

    } else {
        /* custom MIME-type, we just pass it through */

        r->headers_out.content_type = conf->content_type;
        r->headers_out.content_type_len = conf->content_type.len;
    }

    ctx = ngx_pcalloc(r->pool, sizeof(ngx_http_rds_csv_ctx_t));

    if (ctx == NULL) {
        return NGX_ERROR;
    }

    ctx->tag = (ngx_buf_tag_t) &ngx_http_rds_csv_filter_module;

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

    ngx_http_set_ctx(r, ctx, ngx_http_rds_csv_filter_module);

    ngx_http_clear_content_length(r);

    r->filter_need_in_memory = 1;

    /* we do postpone the header sending to the body filter */
    return NGX_OK;
}


static ngx_int_t
ngx_http_rds_csv_body_filter(ngx_http_request_t *r, ngx_chain_t *in)
{
    ngx_http_rds_csv_ctx_t    *ctx;
    ngx_int_t                   rc;

    if (in == NULL || r->header_only) {
        return ngx_http_rds_csv_next_body_filter(r, in);
    }

    ctx = ngx_http_get_module_ctx(r, ngx_http_rds_csv_filter_module);

    if (ctx == NULL) {
        return ngx_http_rds_csv_next_body_filter(r, in);
    }

    switch (ctx->state) {
    case state_expect_header:
        rc = ngx_http_rds_csv_process_header(r, in, ctx);
        break;

    case state_expect_col:
        rc = ngx_http_rds_csv_process_col(r, in, ctx);
        break;

    case state_expect_row:
        rc = ngx_http_rds_csv_process_row(r, in, ctx);
        break;

    case state_expect_field:
        rc = ngx_http_rds_csv_process_field(r, in, ctx);
        break;

    case state_expect_more_field_data:
        rc = ngx_http_rds_csv_process_more_field_data(r, in, ctx);
        break;

    case state_done:

        /* mark the remaining bufs as consumed */

        dd("discarding bufs");

        ngx_http_rds_csv_discard_bufs(r->pool, in);

        return NGX_OK;
        break;
    default:
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds_csv: invalid internal state: %d",
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

            dd("sending ERROR headers");

            ngx_http_rds_csv_next_header_filter(r);
            ngx_http_send_special(r, NGX_HTTP_LAST);

            return NGX_ERROR;
        }

        return NGX_ERROR;
    }

    dd("output bufs");

    return ngx_http_rds_csv_output_bufs(r, ctx);
}


static ngx_int_t
ngx_http_rds_csv_filter_init(ngx_conf_t *cf)
{
    int                             multi_http_blocks;
    ngx_http_rds_csv_main_conf_t   *rmcf;

    rmcf = ngx_http_conf_get_module_main_conf(cf,
                                              ngx_http_rds_csv_filter_module);

    if (ngx_http_rds_csv_prev_cycle != ngx_cycle) {
        ngx_http_rds_csv_prev_cycle = ngx_cycle;
        multi_http_blocks = 0;

    } else {
        multi_http_blocks = 1;
    }

    if (multi_http_blocks || rmcf->requires_filter) {
        ngx_http_rds_csv_next_header_filter = ngx_http_top_header_filter;
        ngx_http_top_header_filter = ngx_http_rds_csv_header_filter;

        ngx_http_rds_csv_next_body_filter = ngx_http_top_body_filter;
        ngx_http_top_body_filter = ngx_http_rds_csv_body_filter;
    }

    return NGX_OK;
}


static void *
ngx_http_rds_csv_create_loc_conf(ngx_conf_t *cf)
{
    ngx_http_rds_csv_loc_conf_t  *conf;

    conf = ngx_pcalloc(cf->pool, sizeof(ngx_http_rds_csv_loc_conf_t));
    if (conf == NULL) {
        return NULL;
    }

    /*
     * set by ngx_pcalloc():
     *
     *     conf->content_type = { 0, NULL };
     *     conf->row_term = { 0, NULL };
     */

    conf->enabled = NGX_CONF_UNSET;
    conf->field_sep = NGX_CONF_UNSET_UINT;
    conf->buf_size = NGX_CONF_UNSET_SIZE;
    conf->field_name_header = NGX_CONF_UNSET;

    return conf;
}


static char *
ngx_http_rds_csv_merge_loc_conf(ngx_conf_t *cf, void *parent, void *child)
{
    ngx_http_rds_csv_loc_conf_t *prev = parent;
    ngx_http_rds_csv_loc_conf_t *conf = child;

    ngx_conf_merge_value(conf->enabled, prev->enabled, 0);

    ngx_conf_merge_value(conf->field_name_header, prev->field_name_header, 1);

    ngx_conf_merge_uint_value(conf->field_sep, prev->field_sep,
                              (ngx_uint_t) ',');

    ngx_conf_merge_str_value(conf->row_term, prev->row_term,
                             ngx_http_rds_csv_row_term);

    ngx_conf_merge_str_value(conf->content_type, prev->content_type,
                             ngx_http_rds_csv_content_type);

    ngx_conf_merge_size_value(conf->buf_size, prev->buf_size,
                              (size_t) ngx_pagesize);

    return NGX_CONF_OK;
}


static char *
ngx_http_rds_csv_row_terminator(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf)
{
    ngx_http_rds_csv_loc_conf_t         *rlcf = conf;
    ngx_str_t                           *value;
    ngx_str_t                           *term;

    if (rlcf->row_term.len != 0) {
        return "is duplicate";
    }

    value = cf->args->elts;

    term = &value[1];

    if (term->len == 0) {
        return "takes empty string value";
    }

    if ((term->len == 1 && term->data[0] == '\n')
        || (term->len == 2 && term->data[0] == '\r' && term->data[1] == '\n'))
    {
        return ngx_conf_set_str_slot(cf, cmd, conf);
    }

    return "takes a value other than \"\\n\" and \"\\r\\n\"";
}


static char *
ngx_http_rds_csv_field_separator(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf)
{
    ngx_http_rds_csv_loc_conf_t         *rlcf = conf;
    ngx_str_t                           *value;
    ngx_str_t                           *sep;

    if (rlcf->field_sep != NGX_CONF_UNSET_UINT) {
        return "is duplicate";
    }

    value = cf->args->elts;

    sep = &value[1];

    if (sep->len != 1) {
        return "takes a string value not of length 1";
    }

    if (sep->data[0] == ',' || sep->data[0] == ';' || sep->data[0] == '\t') {
        rlcf->field_sep = (ngx_uint_t) (sep->data[0]);
        return NGX_CONF_OK;
    }

    return "takes a value other than \",\", \";\", and \"\\t\"";
}


static char *
ngx_http_rds_csv(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_rds_csv_main_conf_t     *rmcf;

    rmcf = ngx_http_conf_get_module_main_conf(cf,
                                              ngx_http_rds_csv_filter_module);

    rmcf->requires_filter = 1;

    return ngx_conf_set_flag_slot(cf, cmd, conf);
}


static void *
ngx_http_rds_csv_create_main_conf(ngx_conf_t *cf)
{
    ngx_http_rds_csv_main_conf_t    *rmcf;

    rmcf = ngx_pcalloc(cf->pool, sizeof(ngx_http_rds_csv_main_conf_t));
    if (rmcf == NULL) {
        return NULL;
    }

    /* set by ngx_pcalloc:
     *      rmcf->requires_filter = 0;
     */

    return rmcf;
}
