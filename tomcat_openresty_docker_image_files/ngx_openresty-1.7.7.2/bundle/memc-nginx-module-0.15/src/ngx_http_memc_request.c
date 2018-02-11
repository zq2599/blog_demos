
/*
 * Copyright (c) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_memc_request.h"
#include "ngx_http_memc_module.h"
#include "ngx_http_memc_util.h"


ngx_int_t
ngx_http_memc_create_storage_cmd_request(ngx_http_request_t *r)
{
    size_t                          len;
    off_t                           bytes;
    size_t                          bytes_len;
    uintptr_t                       escape;
    ngx_buf_t                      *b;
    ngx_chain_t                    *cl, *in;
    ngx_chain_t                   **ll;
    ngx_http_memc_ctx_t            *ctx;

    ngx_http_variable_value_t      *key_vv;
    ngx_http_variable_value_t      *flags_vv;
    ngx_http_variable_value_t      *exptime_vv;
    ngx_http_variable_value_t      *memc_value_vv;

    u_char                          bytes_buf[NGX_UINT32_LEN];

    /* TODO add support for the "cas" command */

    ctx = ngx_http_get_module_ctx(r, ngx_http_memc_module);

    /* prepare the "key" argument */

    key_vv = ctx->memc_key_vv;

    if (key_vv == NULL || key_vv->not_found || key_vv->len == 0) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "the \"$memc_key\" variable is not set");
        return NGX_ERROR;
    }

    escape = 2 * ngx_escape_uri(NULL, key_vv->data, key_vv->len,
            NGX_ESCAPE_MEMCACHED);

    /* prepare the "bytes" argument */

    if (ctx->memc_value_vv && !ctx->memc_value_vv->not_found) {
        dd("found variable $memc_value");

        memc_value_vv = ctx->memc_value_vv;

        bytes = memc_value_vv->len;

    } else if (r->request_body == NULL || r->request_body->bufs == NULL) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "neither the \"$memc_value\" variable "
                      "nor the request body is available");
        return NGX_HTTP_INTERNAL_SERVER_ERROR;

    } else {
        memc_value_vv = NULL;

        bytes = 0;
        for (cl = r->request_body->bufs; cl; cl = cl->next) {
            bytes += ngx_buf_size(cl->buf);
        }
    }

    bytes_len = ngx_snprintf(bytes_buf, sizeof(bytes_buf), "%O", bytes)
        - bytes_buf;

    /* prepare the "flags" argument */

    flags_vv = ctx->memc_flags_vv;

    if (flags_vv == NULL) {
        return NGX_ERROR;
    }

    if (flags_vv->not_found) {
        flags_vv->not_found = 0;
        flags_vv->valid = 1;
        flags_vv->no_cacheable = 0;
        flags_vv->len = sizeof("0") - 1;
        flags_vv->data = (u_char *) "0";
    } else if (flags_vv->len == 0) {
        flags_vv->len = sizeof("0") - 1;
        flags_vv->data = (u_char *) "0";
    }

    /* prepare the "exptime" argument */

    exptime_vv = ctx->memc_exptime_vv;

    if (exptime_vv == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    if (exptime_vv->not_found || exptime_vv->len == 0) {
        dd("setting exptime to its default value 0...");

        exptime_vv->not_found = 0;
        exptime_vv->valid = 1;
        exptime_vv->no_cacheable = 0;
        exptime_vv->len = sizeof("0") - 1;
        exptime_vv->data = (u_char *) "0";
    }

    /* calculate the total length of the command */

    len = ctx->cmd_str.len + sizeof(" ") - 1
        + key_vv->len + escape + sizeof(" ") - 1
        + flags_vv->len + sizeof(" ") - 1
        + exptime_vv->len + sizeof(" ") - 1
        + bytes_len
        + sizeof(CRLF) - 1;

    b = ngx_create_temp_buf(r->pool, len);
    if (b == NULL) {
        return NGX_ERROR;
    }

    cl = ngx_alloc_chain_link(r->pool);
    if (cl == NULL) {
        return NGX_ERROR;
    }

    cl->buf = b;
    cl->next = NULL;

    ll = &cl->next;

    r->upstream->request_bufs = cl;

    /* copy the memcached command over */

    b->last = ngx_copy(b->last, ctx->cmd_str.data, ctx->cmd_str.len);

    *b->last++ = ' ';

    /* copy the memcached key over */

    ctx->key.data = b->last;

    if (escape == 0) {
        b->last = ngx_copy(b->last, key_vv->data, key_vv->len);

    } else {
        b->last = (u_char *) ngx_escape_uri(b->last, key_vv->data, key_vv->len,
                NGX_ESCAPE_MEMCACHED);
    }

    ctx->key.len = b->last - ctx->key.data;

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "http memcached request: \"%V\"", &ctx->key);

    *b->last++ = ' ';

    /* copy the memcached flags over */

    b->last = ngx_copy(b->last, flags_vv->data, flags_vv->len);

    *b->last++ = ' ';

    /* copy the memcached exptime over */

    b->last = ngx_copy(b->last, exptime_vv->data, exptime_vv->len);

    *b->last++ = ' ';

    /* copy the memcached bytes over */

    b->last = ngx_copy(b->last, bytes_buf, bytes_len);

    *b->last++ = CR; *b->last++ = LF;

    if (memc_value_vv) {
        if (memc_value_vv->len) {
            dd("copy $memc_value to the request");
            b = ngx_calloc_buf(r->pool);

            if (b == NULL) {
                return NGX_ERROR;
            }

            b->memory = 1;

            b->start = b->pos = memc_value_vv->data;
            b->last  = b->end = b->start + memc_value_vv->len;

            cl = ngx_alloc_chain_link(r->pool);
            if (cl == NULL) {
                return NGX_ERROR;
            }

            cl->buf = b;
            cl->next = NULL;

            *ll = cl;
            ll = &cl->next;
        }

    } else {
        /* to preserve the r->request_body->bufs untouched */

        in = r->request_body->bufs;

        while (in) {
            cl = ngx_alloc_chain_link(r->pool);
            if (cl == NULL) {
                return NGX_ERROR;
            }

            cl->buf = ngx_calloc_buf(r->pool);
            if (cl->buf == NULL) {
                return NGX_ERROR;
            }

            cl->buf->memory = 1;
            *cl->buf = *in->buf;

            *ll = cl;
            ll = &cl->next;
            in = in->next;
        }
    }

    /* append the trailing CRLF */

    b = ngx_calloc_buf(r->pool);

    if (b == NULL) {
        return NGX_ERROR;
    }

    b->start = b->pos = (u_char *) CRLF;
    b->last  = b->end = b->start + sizeof(CRLF) - 1;

    b->memory = 1;

    cl = ngx_alloc_chain_link(r->pool);
    if (cl == NULL) {
        return NGX_ERROR;
    }

    cl->buf = b;
    cl->next = NULL;

    *ll = cl;

    return NGX_OK;
}


ngx_int_t
ngx_http_memc_create_get_cmd_request(ngx_http_request_t *r)
{
    size_t                          len;
    uintptr_t                       escape;
    ngx_buf_t                      *b;
    ngx_chain_t                    *cl;
    ngx_http_memc_ctx_t            *ctx;
    ngx_http_variable_value_t      *vv;

    ctx = ngx_http_get_module_ctx(r, ngx_http_memc_module);

    vv = ctx->memc_key_vv;

    if (vv == NULL || vv->not_found || vv->len == 0) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "the \"$memc_key\" variable is not set");
        return NGX_ERROR;
    }

    escape = 2 * ngx_escape_uri(NULL, vv->data, vv->len, NGX_ESCAPE_MEMCACHED);

    len = sizeof("get ") - 1 + vv->len + escape + sizeof(CRLF) - 1;

    b = ngx_create_temp_buf(r->pool, len);
    if (b == NULL) {
        return NGX_ERROR;
    }

    cl = ngx_alloc_chain_link(r->pool);
    if (cl == NULL) {
        return NGX_ERROR;
    }

    cl->buf = b;
    cl->next = NULL;

    r->upstream->request_bufs = cl;

    *b->last++ = 'g'; *b->last++ = 'e'; *b->last++ = 't'; *b->last++ = ' ';

    ctx->key.data = b->last;

    if (escape == 0) {
        b->last = ngx_copy(b->last, vv->data, vv->len);

    } else {
        b->last = (u_char *) ngx_escape_uri(b->last, vv->data, vv->len,
                                            NGX_ESCAPE_MEMCACHED);
    }

    ctx->key.len = b->last - ctx->key.data;

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "http memcached request: \"%V\"", &ctx->key);

    *b->last++ = CR; *b->last++ = LF;

    ctx->parser_state = NGX_ERROR;

    return NGX_OK;
}

ngx_int_t
ngx_http_memc_create_noarg_cmd_request(ngx_http_request_t *r)
{
    size_t                          len;
    ngx_buf_t                      *b;
    ngx_http_memc_ctx_t            *ctx;
    ngx_chain_t                    *cl;

    ctx = ngx_http_get_module_ctx(r, ngx_http_memc_module);

    len = ctx->cmd_str.len + sizeof(CRLF) - 1;

    b = ngx_create_temp_buf(r->pool, len);
    if (b == NULL) {
        return NGX_ERROR;
    }

    cl = ngx_alloc_chain_link(r->pool);
    if (cl == NULL) {
        return NGX_ERROR;
    }

    cl->buf = b;
    cl->next = NULL;

    r->upstream->request_bufs = cl;

    b->last = ngx_copy(b->last, ctx->cmd_str.data, ctx->cmd_str.len);

    *b->last++ = CR; *b->last++ = LF;

    return NGX_OK;
}


ngx_int_t
ngx_http_memc_create_flush_all_cmd_request(ngx_http_request_t *r)
{
    size_t                          len;
    ngx_buf_t                      *b;
    ngx_http_memc_ctx_t            *ctx;
    ngx_chain_t                    *cl;
    ngx_http_variable_value_t      *exptime_vv;

    ctx = ngx_http_get_module_ctx(r, ngx_http_memc_module);

    /* prepare the (optional) "exptime" argument */

    exptime_vv = ctx->memc_exptime_vv;

    if (exptime_vv == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    len = ctx->cmd_str.len;

    if (!exptime_vv->not_found && exptime_vv->len) {
        dd("found exptime: %s", exptime_vv->data);

        len += sizeof(' ') + exptime_vv->len;
    }

    len += sizeof(CRLF) - 1;

    b = ngx_create_temp_buf(r->pool, len);
    if (b == NULL) {
        return NGX_ERROR;
    }

    cl = ngx_alloc_chain_link(r->pool);
    if (cl == NULL) {
        return NGX_ERROR;
    }

    cl->buf = b;
    cl->next = NULL;

    r->upstream->request_bufs = cl;

    b->last = ngx_copy(b->last, ctx->cmd_str.data, ctx->cmd_str.len);

    if (!exptime_vv->not_found && exptime_vv->len) {
        *b->last++ = ' ';
        b->last = ngx_copy(b->last, exptime_vv->data, exptime_vv->len);
    }

    *b->last++ = CR; *b->last++ = LF;

    return NGX_OK;
}


ngx_int_t
ngx_http_memc_create_delete_cmd_request(ngx_http_request_t *r)
{
    size_t                          len;
    ngx_buf_t                      *b;
    ngx_http_memc_ctx_t            *ctx;
    ngx_chain_t                    *cl;
    uintptr_t                       escape;
    ngx_http_variable_value_t      *key_vv;
    ngx_http_variable_value_t      *exptime_vv;

    ctx = ngx_http_get_module_ctx(r, ngx_http_memc_module);

    /* prepare the "key" argument */

    key_vv = ctx->memc_key_vv;

    if (key_vv == NULL || key_vv->not_found || key_vv->len == 0) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "the \"$memc_key\" variable is not set");
        return NGX_ERROR;
    }

    escape = 2 * ngx_escape_uri(NULL, key_vv->data, key_vv->len,
            NGX_ESCAPE_MEMCACHED);

    /* prepare the (optional) "exptime" argument */

    exptime_vv = ctx->memc_exptime_vv;

    if (exptime_vv == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    len = ctx->cmd_str.len + sizeof(' ') + key_vv->len + escape;

    if (!exptime_vv->not_found && exptime_vv->len) {
        dd("found exptime: %s", exptime_vv->data);

        len += sizeof(' ') + exptime_vv->len;
    }

    len += sizeof(CRLF) - 1;

    b = ngx_create_temp_buf(r->pool, len);
    if (b == NULL) {
        return NGX_ERROR;
    }

    cl = ngx_alloc_chain_link(r->pool);
    if (cl == NULL) {
        return NGX_ERROR;
    }

    cl->buf = b;
    cl->next = NULL;

    r->upstream->request_bufs = cl;

    b->last = ngx_copy(b->last, ctx->cmd_str.data, ctx->cmd_str.len);

    *b->last++ = ' ';

    if (escape == 0) {
        b->last = ngx_copy(b->last, key_vv->data, key_vv->len);

    } else {
        b->last = (u_char *) ngx_escape_uri(b->last, key_vv->data, key_vv->len,
                                            NGX_ESCAPE_MEMCACHED);
    }

    if (!exptime_vv->not_found && exptime_vv->len) {
        *b->last++ = ' ';
        b->last = ngx_copy(b->last, exptime_vv->data, exptime_vv->len);
    }

    *b->last++ = CR; *b->last++ = LF;

    return NGX_OK;
}


ngx_int_t
ngx_http_memc_create_incr_decr_cmd_request(ngx_http_request_t *r)
{
    size_t                          len;
    ngx_buf_t                      *b;
    ngx_http_memc_ctx_t            *ctx;
    ngx_chain_t                    *cl;
    uintptr_t                       escape;
    ngx_http_variable_value_t      *key_vv;
    ngx_http_variable_value_t      *value_vv;

    ctx = ngx_http_get_module_ctx(r, ngx_http_memc_module);

    /* prepare the "key" argument */

    key_vv = ctx->memc_key_vv;

    if (key_vv == NULL || key_vv->not_found || key_vv->len == 0) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "the \"$memc_key\" variable is not set");
        return NGX_ERROR;
    }

    escape = 2 * ngx_escape_uri(NULL, key_vv->data, key_vv->len,
            NGX_ESCAPE_MEMCACHED);

    /* prepare the "value" argument */

    value_vv = ctx->memc_value_vv;

    /* XXX validate if $memc_value_vv is a valid uint64 string */

    len = ctx->cmd_str.len + sizeof(' ') + key_vv->len + escape
        + sizeof(' ') + value_vv->len + sizeof(CRLF) - 1;

    b = ngx_create_temp_buf(r->pool, len);
    if (b == NULL) {
        return NGX_ERROR;
    }

    cl = ngx_alloc_chain_link(r->pool);
    if (cl == NULL) {
        return NGX_ERROR;
    }

    cl->buf = b;
    cl->next = NULL;

    r->upstream->request_bufs = cl;

    b->last = ngx_copy(b->last, ctx->cmd_str.data, ctx->cmd_str.len);

    *b->last++ = ' ';

    if (escape == 0) {
        b->last = ngx_copy(b->last, key_vv->data, key_vv->len);

    } else {
        b->last = (u_char *) ngx_escape_uri(b->last, key_vv->data, key_vv->len,
                                            NGX_ESCAPE_MEMCACHED);
    }

    *b->last++ = ' ';

    b->last = ngx_copy(b->last, value_vv->data, value_vv->len);

    *b->last++ = CR; *b->last++ = LF;

    return NGX_OK;
}
