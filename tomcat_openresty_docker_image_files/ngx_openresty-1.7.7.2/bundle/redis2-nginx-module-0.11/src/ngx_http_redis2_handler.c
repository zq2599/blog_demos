#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_redis2_handler.h"
#include "ngx_http_redis2_reply.h"
#include "ngx_http_redis2_util.h"


static ngx_int_t ngx_http_redis2_create_request(ngx_http_request_t *r);
static ngx_int_t ngx_http_redis2_reinit_request(ngx_http_request_t *r);
static ngx_int_t ngx_http_redis2_process_header(ngx_http_request_t *r);
static ngx_int_t ngx_http_redis2_filter_init(void *data);
static ngx_int_t ngx_http_redis2_filter(void *data, ssize_t bytes);
static void ngx_http_redis2_abort_request(ngx_http_request_t *r);
static void ngx_http_redis2_finalize_request(ngx_http_request_t *r,
    ngx_int_t rc);


ngx_int_t
ngx_http_redis2_handler(ngx_http_request_t *r)
{
    ngx_int_t                        rc;
    ngx_http_upstream_t             *u;
    ngx_http_redis2_ctx_t           *ctx;
    ngx_http_redis2_loc_conf_t      *rlcf;
    ngx_str_t                        target;
    ngx_url_t                        url;

    if (ngx_http_set_content_type(r) != NGX_OK) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    if (ngx_http_upstream_create(r) != NGX_OK) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    u = r->upstream;

    rlcf = ngx_http_get_module_loc_conf(r, ngx_http_redis2_module);

    if (rlcf->complex_target) {
        /* variables used in the redis2_pass directive */

        if (ngx_http_complex_value(r, rlcf->complex_target, &target)
                != NGX_OK)
        {
            return NGX_ERROR;
        }

        if (target.len == 0) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                    "handler: empty \"redis2_pass\" target");
            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }

        url.host = target;
        url.port = 0;
        url.no_resolve = 1;

        rlcf->upstream.upstream = ngx_http_redis2_upstream_add(r, &url);

        if (rlcf->upstream.upstream == NULL) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                   "redis2: upstream \"%V\" not found", &target);

            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }
    }

    ngx_str_set(&u->schema, "redis2://");
    u->output.tag = (ngx_buf_tag_t) &ngx_http_redis2_module;

    u->conf = &rlcf->upstream;

    u->create_request = ngx_http_redis2_create_request;
    u->reinit_request = ngx_http_redis2_reinit_request;
    u->process_header = ngx_http_redis2_process_header;
    u->abort_request = ngx_http_redis2_abort_request;
    u->finalize_request = ngx_http_redis2_finalize_request;

    ctx = ngx_pcalloc(r->pool, sizeof(ngx_http_redis2_ctx_t));
    if (ctx == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    ctx->request = r;
    ctx->state = NGX_ERROR;

    ngx_http_set_ctx(r, ctx, ngx_http_redis2_module);

    u->input_filter_init = ngx_http_redis2_filter_init;
    u->input_filter = ngx_http_redis2_filter;
    u->input_filter_ctx = ctx;

    rc = ngx_http_read_client_request_body(r, ngx_http_upstream_init);

    if (rc >= NGX_HTTP_SPECIAL_RESPONSE) {
        return rc;
    }

    return NGX_DONE;
}


static ngx_int_t
ngx_http_redis2_create_request(ngx_http_request_t *r)
{
    ngx_buf_t                       *b;
    ngx_chain_t                     *cl;
    ngx_http_redis2_loc_conf_t      *rlcf;
    ngx_str_t                        query;
    ngx_str_t                        query_count;
    ngx_int_t                        rc;
    ngx_http_redis2_ctx_t           *ctx;
    ngx_int_t                        n;

    ctx = ngx_http_get_module_ctx(r, ngx_http_redis2_module);

    rlcf = ngx_http_get_module_loc_conf(r, ngx_http_redis2_module);

    if (rlcf->queries) {
        ctx->query_count = rlcf->queries->nelts;

        rc = ngx_http_redis2_build_query(r, rlcf->queries, &b);
        if (rc != NGX_OK) {
            return rc;
        }

    } else if (rlcf->literal_query.len == 0) {
        if (rlcf->complex_query == NULL) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                    "no redis2 query specified or the query is empty");

            return NGX_ERROR;
        }

        if (ngx_http_complex_value(r, rlcf->complex_query, &query)
                != NGX_OK)
        {
            return NGX_ERROR;
        }

        if (query.len == 0) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                    "the redis query is empty");

            return NGX_ERROR;
        }

        if (rlcf->complex_query_count == NULL) {
            ctx->query_count = 1;

        } else {
            if (ngx_http_complex_value(r, rlcf->complex_query_count,
                    &query_count) != NGX_OK)
            {
                return NGX_ERROR;
            }

            if (query_count.len == 0) {
                ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                        "the N argument to redis2_raw_queries is empty");

                return NGX_ERROR;
            }

            n = ngx_atoi(query_count.data, query_count.len);
            if (n == NGX_ERROR || n == 0) {
                ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                        "the N argument to redis2_raw_queries is invalid");

                return NGX_ERROR;
            }

            ctx->query_count = n;
        }

        b = ngx_create_temp_buf(r->pool, query.len);
        if (b == NULL) {
            return NGX_ERROR;
        }

        b->last = ngx_copy(b->pos, query.data, query.len);

    } else {
        ctx->query_count = 1;

        b = ngx_calloc_buf(r->pool);
        if (b == NULL) {
            return NGX_ERROR;
        }

        b->pos = rlcf->literal_query.data;
        b->last = b->pos + rlcf->literal_query.len;
        b->memory = 1;
    }

    cl = ngx_alloc_chain_link(r->pool);
    if (cl == NULL) {
        return NGX_ERROR;
    }

    cl->buf = b;
    cl->next = NULL;

    r->upstream->request_bufs = cl;

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "http redis2 request: \"%V\"", &rlcf->literal_query);

    return NGX_OK;
}


static ngx_int_t
ngx_http_redis2_reinit_request(ngx_http_request_t *r)
{
    return NGX_OK;
}


static ngx_int_t
ngx_http_redis2_process_header(ngx_http_request_t *r)
{
    ngx_http_upstream_t         *u;
    ngx_http_redis2_ctx_t       *ctx;
    ngx_buf_t                   *b;
    u_char                       chr;
    ngx_str_t                    buf;

    u = r->upstream;
    b = &u->buffer;

    if (b->last - b->pos < (ssize_t) sizeof(u_char)) {
        return NGX_AGAIN;
    }

    ctx = ngx_http_get_module_ctx(r, ngx_http_redis2_module);

    /* the first char is the response header */

    chr = *b->pos;

    dd("response header: %c (ascii %d)", chr, chr);

    switch (chr) {
        case '+':
        case '-':
        case ':':
        case '$':
        case '*':
            ctx->filter = ngx_http_redis2_process_reply;
            break;

        default:
            buf.data = b->pos;
            buf.len = b->last - b->pos;

            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "redis2 sent invalid response: \"%V\"", &buf);

            return NGX_HTTP_UPSTREAM_INVALID_HEADER;
    }

    u->headers_in.status_n = NGX_HTTP_OK;
    u->state->status = NGX_HTTP_OK;

    return NGX_OK;
}


static ngx_int_t
ngx_http_redis2_filter_init(void *data)
{
#if 0
    ngx_http_redis2_ctx_t  *ctx = data;

    ngx_http_upstream_t  *u;

    u = ctx->request->upstream;
#endif

    return NGX_OK;
}


static ngx_int_t
ngx_http_redis2_filter(void *data, ssize_t bytes)
{
    ngx_http_redis2_ctx_t  *ctx = data;

    return ctx->filter(ctx, bytes);
}


static void
ngx_http_redis2_abort_request(ngx_http_request_t *r)
{
    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "abort http redis2 request");
    return;
}


static void
ngx_http_redis2_finalize_request(ngx_http_request_t *r, ngx_int_t rc)
{
    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "finalize http redis2 request");
    return;
}

