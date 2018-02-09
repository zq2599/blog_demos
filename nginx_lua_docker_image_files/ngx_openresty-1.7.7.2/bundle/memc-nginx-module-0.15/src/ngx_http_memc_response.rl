
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_memc_response.h"
#include "ngx_http_memc_module.h"


#ifdef s_char
#undef s_char
#endif

#define s_char signed char


%% machine memc_storage;
%% write data;

%% machine memc_flush_all;
%% write data;

%% machine memc_version;
%% write data;

%% machine memc_stats;
%% write data;

%% machine memc_delete;
%% write data;

%% machine memc_incr_decr;
%% write data;


u_char  ngx_http_memc_end[] = CRLF "END" CRLF;


static u_char * parse_memc_storage(int *cs_addr, u_char *p, u_char *pe,
        ngx_uint_t *status_addr, unsigned *done_addr);
static u_char * parse_memc_flush_all(int *cs_addr, u_char *p, u_char *pe,
        ngx_uint_t *status_addr, unsigned *done_addr);
static u_char * parse_memc_version(int *cs_addr, u_char *p, u_char *pe,
        ngx_uint_t *status_addr, unsigned *done_addr);
static u_char * parse_memc_stats(int *cs_addr, u_char *p, u_char *pe,
        ngx_uint_t *status_addr, unsigned *done_addr);
static u_char * parse_memc_delete(int *cs_addr, u_char *p, u_char *pe,
        ngx_uint_t *status_addr, unsigned *done_addr);
static u_char * parse_memc_incr_decr(int *cs_addr, u_char *p, u_char *pe,
        ngx_uint_t *status_addr, unsigned *done_addr);
static ngx_int_t ngx_http_memc_write_simple_response(ngx_http_request_t *r,
        ngx_http_upstream_t *u, ngx_http_memc_ctx_t *ctx,
        ngx_uint_t status, ngx_str_t *resp);


ngx_int_t
ngx_http_memc_process_simple_header(ngx_http_request_t *r)
{
    ngx_int_t                rc;
    int                      cs;
    s_char                  *p;
    s_char                  *pe;
    s_char                  *orig;
    ngx_str_t                resp;
    ngx_http_upstream_t     *u;
    ngx_http_memc_ctx_t     *ctx;
    ngx_uint_t               status;
    unsigned                 done = 0;
    int                      error_state;
    int                      final_state;

    status = NGX_HTTP_OK;

    dd("process simple cmd header");

    ctx = ngx_http_get_module_ctx(r, ngx_http_memc_module);

    if (ctx->parser_state == NGX_ERROR) {
        dd("reinit state");

        if (ctx->is_storage_cmd) {
            dd("init memc_storage machine...");

            %% machine memc_storage;
            %% write init;

        } else if (ctx->cmd == ngx_http_memc_cmd_flush_all) {
            dd("init memc_flush_all machine...");

            %% machine memc_flush_all;
            %% write init;

        } else if (ctx->cmd == ngx_http_memc_cmd_version) {
            dd("init memc_version machine...");

            %% machine memc_version;
            %% write init;

        } else if (ctx->cmd == ngx_http_memc_cmd_stats) {
            dd("init memc_stats machine...");

            %% machine memc_stats;
            %% write init;

        } else if (ctx->cmd == ngx_http_memc_cmd_delete) {
            dd("init memc_delete machine...");

            %% machine memc_delete;
            %% write init;

        } else if (ctx->cmd == ngx_http_memc_cmd_incr
                || ctx->cmd == ngx_http_memc_cmd_decr)
        {
            dd("init memc_incr_decr machine...");

            %% machine memc_incr_decr;
            %% write init;

        } else {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
              "unrecognized memcached command in "
              "ngx_http_memc_process_simple_header: \"%V\"",
              &ctx->cmd_str);

            return NGX_ERROR; /* this results in 500 status */
        }

    } else {
        cs = ctx->parser_state;
    }

    u = r->upstream;

    orig = (s_char *) u->buffer.pos;

    p  = (s_char *) u->buffer.pos;
    pe = (s_char *) u->buffer.last;

    dd("buffer len: %d", (int) (pe - p));

    if (ctx->is_storage_cmd) {
        error_state = memc_storage_error;
        final_state = memc_storage_first_final;

        p = (s_char *) parse_memc_storage(&cs, (u_char *) p, (u_char *) pe,
                &status, &done);

    } else if (ctx->cmd == ngx_http_memc_cmd_flush_all) {
        error_state = memc_flush_all_error;
        final_state = memc_flush_all_first_final;

        p = (s_char *) parse_memc_flush_all(&cs, (u_char *) p, (u_char *) pe,
                &status, &done);

    } else if (ctx->cmd == ngx_http_memc_cmd_version) {
        error_state = memc_version_error;
        final_state = memc_version_first_final;

        p = (s_char *) parse_memc_version(&cs, (u_char *) p, (u_char *) pe,
                &status, &done);

    } else if (ctx->cmd == ngx_http_memc_cmd_stats) {
        error_state = memc_stats_error;
        final_state = memc_stats_first_final;

        p = (s_char *) parse_memc_stats(&cs, (u_char *) p, (u_char *) pe,
                &status, &done);

    } else if (ctx->cmd == ngx_http_memc_cmd_delete) {
        error_state = memc_delete_error;
        final_state = memc_delete_first_final;

        p = (s_char *) parse_memc_delete(&cs, (u_char *) p, (u_char *) pe,
                &status, &done);

    } else if (ctx->cmd == ngx_http_memc_cmd_incr
            || ctx->cmd == ngx_http_memc_cmd_decr)
    {
        error_state = memc_incr_decr_error;
        final_state = memc_incr_decr_first_final;

        p = (s_char *) parse_memc_incr_decr(&cs, (u_char *) p, (u_char *) pe,
                &status, &done);

    } else {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
          "unrecognized memcached command in "
          "ngx_http_memc_process_simple_header: \"%V\"",
          &ctx->cmd_str);

        return NGX_ERROR; /* this results in 500 status */
    }

    ctx->parser_state = cs;

    resp.data = u->buffer.start;
    resp.len  = (u_char *) p - resp.data;

    u->buffer.pos = (u_char *) p;

    dd("machine state: %d (done: %d)", cs, done);
    dd("memcached response: (len: %d) %s", (int) resp.len, resp.data);

    if (done || cs >= final_state) {
        dd("memcached response parsed (resp.len: %d)", (int) resp.len);

        rc = ngx_http_memc_write_simple_response(r, u, ctx, status, &resp);

        return rc;
    }

    if (cs == error_state) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                  "memcached sent invalid response for command \"%V\" "
                  "at pos %O: %V", &ctx->cmd_str, (off_t) (p - orig), &resp);

        status = NGX_HTTP_BAD_GATEWAY;
        u->headers_in.status_n = status;
        u->state->status = status;

        /* u->headers_in.status_n will be the final status */
        return NGX_OK;
    }

    dd("we need more data to proceed (returned NGX_AGAIN)");

    return NGX_AGAIN;
}


ngx_int_t
ngx_http_memc_empty_filter_init(void *data)
{
    ngx_http_memc_ctx_t  *ctx = data;
    ngx_http_upstream_t  *u;

    u = ctx->request->upstream;

    u->length = 0;

    /* to persuade ngx_http_upstream_keepalive (if any)
       to cache the connection if the status is neither
       200 nor 404. */
    if (u->headers_in.status_n == NGX_HTTP_CREATED) {
        u->headers_in.status_n = NGX_HTTP_OK;
    }

    return NGX_OK;
}

ngx_int_t
ngx_http_memc_empty_filter(void *data, ssize_t bytes)
{
    ngx_http_memc_ctx_t  *ctx = data;
    ngx_http_upstream_t  *u;

    u = ctx->request->upstream;

    /* recover the buffer for subrequests in memory */
    u->buffer.last += ctx->body_length;

    return NGX_OK;
}


ngx_int_t
ngx_http_memc_get_cmd_filter_init(void *data)
{
    ngx_http_memc_ctx_t  *ctx = data;

    ngx_http_upstream_t  *u;

    u = ctx->request->upstream;

    dd("filter init: u->length: %d", (int) u->length);

    u->length = u->headers_in.content_length_n + NGX_HTTP_MEMC_END;

    dd("filter init (2): u->length: %d", (int) u->length);

    return NGX_OK;
}


ngx_int_t
ngx_http_memc_get_cmd_filter(void *data, ssize_t bytes)
{
    ngx_http_memc_ctx_t  *ctx = data;

    u_char               *last;
    ngx_buf_t            *b;
    ngx_chain_t          *cl, **ll;
    ngx_http_upstream_t  *u;

    u = ctx->request->upstream;
    b = &u->buffer;

    if (u->length == ctx->rest) {

        if (ngx_strncmp(b->last,
                        ngx_http_memc_end + NGX_HTTP_MEMC_END - ctx->rest,
                        bytes) != 0)
        {
            ngx_log_error(NGX_LOG_ERR, ctx->request->connection->log, 0,
                          "memcached sent invalid trailer");

            u->length = 0;
            ctx->rest = 0;

            return NGX_OK;
        }

        u->length -= bytes;
        ctx->rest -= bytes;

#if defined(nginx_version) && nginx_version >= 1001004
        if (u->length == 0) {
            u->keepalive = 1;
        }
#endif

        return NGX_OK;
    }

    for (cl = u->out_bufs, ll = &u->out_bufs; cl; cl = cl->next) {
        ll = &cl->next;
    }

    cl = ngx_chain_get_free_buf(ctx->request->pool, &u->free_bufs);
    if (cl == NULL) {
        return NGX_ERROR;
    }

    cl->buf->flush = 1;
    cl->buf->memory = 1;

    *ll = cl;

    last = b->last;
    cl->buf->pos = last;
    b->last += bytes;
    cl->buf->last = b->last;
    cl->buf->tag = u->output.tag;

    ngx_log_debug4(NGX_LOG_DEBUG_HTTP, ctx->request->connection->log, 0,
                   "memcached filter bytes:%z size:%z length:%z rest:%z",
                   bytes, b->last - b->pos, u->length, ctx->rest);

    if (bytes <= (ssize_t) (u->length - NGX_HTTP_MEMC_END)) {
        u->length -= bytes;
        return NGX_OK;
    }

    last += u->length - NGX_HTTP_MEMC_END;

    if (ngx_strncmp(last, ngx_http_memc_end, b->last - last) != 0) {
        ngx_log_error(NGX_LOG_ERR, ctx->request->connection->log, 0,
                      "memcached sent invalid trailer");

#if defined(nginx_version) && nginx_version >= 1001004
        b->last = last;
        cl->buf->last = last;
        u->length = 0;
        ctx->rest = 0;

        return NGX_OK;
#endif
    }

    ctx->rest -= b->last - last;
    b->last = last;
    cl->buf->last = last;
    u->length = ctx->rest;

#if defined(nginx_version) && nginx_version >= 1001004
    if (u->length == 0) {
        u->keepalive = 1;
    }
#endif

    return NGX_OK;
}


ngx_int_t
ngx_http_memc_process_get_cmd_header(ngx_http_request_t *r)
{
    ngx_http_memc_loc_conf_t        *conf;
    u_char                          *p, *len;
    ngx_str_t                        line;
    ngx_http_upstream_t             *u;
    ngx_http_memc_ctx_t             *ctx;
    ngx_http_variable_value_t       *flags_vv;

    u = r->upstream;

    dd("process header: u->length: %u", (unsigned) u->length);

    for (p = u->buffer.pos; p < u->buffer.last; p++) {
        if (*p == LF) {
            goto found;
        }
    }

    return NGX_AGAIN;

found:

    *p = '\0';

    line.len = p - u->buffer.pos - 1;
    line.data = u->buffer.pos;

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "memcached: \"%V\"", &line);

    p = u->buffer.pos;

    ctx = ngx_http_get_module_ctx(r, ngx_http_memc_module);

    if (ngx_strncmp(p, "VALUE ", sizeof("VALUE ") - 1) == 0) {

        p += sizeof("VALUE ") - 1;

        if (ngx_strncmp(p, ctx->key.data, ctx->key.len) != 0) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "memcached sent invalid key in response \"%V\" "
                          "for key \"%V\"",
                          &line, &ctx->key);

            return NGX_HTTP_UPSTREAM_INVALID_HEADER;
        }

        p += ctx->key.len;

        if (*p++ != ' ') {
            goto no_valid;
        }

        /* save flags */

        flags_vv = ctx->memc_flags_vv;

        if (flags_vv == NULL) {
            return NGX_ERROR;
        }

        if (flags_vv->not_found) {
            flags_vv->not_found = 0;
            flags_vv->valid = 1;
            flags_vv->no_cacheable = 0;
        }

        flags_vv->data = p;

        while (*p) {
            if (*p++ == ' ') {
                flags_vv->len = p - 1 - flags_vv->data;
                conf = ngx_http_get_module_loc_conf(r, ngx_http_memc_module);

                if (conf->flags_to_last_modified) {
                    r->headers_out.last_modified_time =
                            ngx_atotm(flags_vv->data, flags_vv->len);
                }

                goto length;
            }
        }

        goto no_valid;

    length:

        len = p;

        while (*p && *p++ != CR) { /* void */ }

#if defined(nginx_version) && nginx_version >= 1001004
        u->headers_in.content_length_n = ngx_atoof(len, p - len - 1);
        if (u->headers_in.content_length_n == -1) {
#else
        r->headers_out.content_length_n = ngx_atoof(len, p - len - 1);
        if (r->headers_out.content_length_n == -1) {
#endif
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "memcached sent invalid length in response \"%V\" "
                          "for key \"%V\"",
                          &line, &ctx->key);
            return NGX_HTTP_UPSTREAM_INVALID_HEADER;
        }

        u->headers_in.status_n = NGX_HTTP_OK;
        u->state->status = NGX_HTTP_OK;
        u->buffer.pos = p + 1;

        return NGX_OK;
    }

    if (ngx_strcmp(p, "END\x0d") == 0) {
        ngx_log_error(NGX_LOG_INFO, r->connection->log, 0,
                      "key: \"%V\" was not found by memcached", &ctx->key);

        u->headers_in.status_n = NGX_HTTP_NOT_FOUND;
        u->state->status = NGX_HTTP_NOT_FOUND;

#if defined(nginx_version) && nginx_version >= 1001004
        u->keepalive = 1;
#endif

        return NGX_OK;
    }

no_valid:

    ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                  "memcached sent invalid response: \"%V\"", &line);

    return NGX_HTTP_UPSTREAM_INVALID_HEADER;
}


static ngx_int_t
ngx_http_memc_write_simple_response(ngx_http_request_t *r,
    ngx_http_upstream_t *u, ngx_http_memc_ctx_t *ctx, ngx_uint_t status,
    ngx_str_t *resp)
{
    ngx_chain_t             *cl, **ll;

    for (cl = u->out_bufs, ll = &u->out_bufs; cl; cl = cl->next) {
        ll = &cl->next;
    }

    cl = ngx_chain_get_free_buf(r->pool, &u->free_bufs);
    if (cl == NULL) {
        return NGX_ERROR;
    }

    cl->buf->flush = 1;
    cl->buf->memory = 1;
    cl->buf->pos = resp->data;
    cl->buf->last = cl->buf->pos + resp->len;

    *ll = cl;

    /* for subrequests in memory */
    u->buffer.pos = resp->data;
    u->buffer.last = resp->data + resp->len;
    ctx->body_length = resp->len;

#if defined(nginx_version) && nginx_version >= 1001004
    u->headers_in.content_length_n = resp->len;
    u->keepalive = 1;
#else
    r->headers_out.content_length_n = resp->len;
#endif

    u->headers_in.status_n = status;
    u->state->status = status;

    return NGX_OK;
}


static u_char *
parse_memc_storage(int *cs_addr, u_char *p, u_char *pe,
    ngx_uint_t *status_addr, unsigned *done_addr)
{
    int cs = *cs_addr;

    %% machine memc_storage;
    %% include "memc_storage.rl";
    %% write exec;

    *cs_addr = cs;

    return p;
}


static u_char *
parse_memc_flush_all(int *cs_addr, u_char *p, u_char *pe,
    ngx_uint_t *status_addr, unsigned *done_addr)
{
    int cs = *cs_addr;

    %% machine memc_flush_all;
    %% include "memc_flush_all.rl";
    %% write exec;

    *cs_addr = cs;

    return p;
}


static u_char *
parse_memc_version(int *cs_addr, u_char *p, u_char *pe,
    ngx_uint_t *status_addr, unsigned *done_addr)
{
    int cs = *cs_addr;

    %% machine memc_version;
    %% include "memc_version.rl";
    %% write exec;

    *cs_addr = cs;

    return p;
}


static u_char *
parse_memc_stats(int *cs_addr, u_char *p, u_char *pe, ngx_uint_t *status_addr,
    unsigned *done_addr)
{
    int cs = *cs_addr;

    %% machine memc_stats;
    %% include "memc_stats.rl";
    %% write exec;

    *cs_addr = cs;

    return p;
}


static u_char *
parse_memc_delete(int *cs_addr, u_char *p, u_char *pe, ngx_uint_t *status_addr,
    unsigned *done_addr)
{
    int cs = *cs_addr;

    %% machine memc_delete;
    %% include "memc_delete.rl";
    %% write exec;

    *cs_addr = cs;

    return p;
}


static u_char *
parse_memc_incr_decr(int *cs_addr, u_char *p, u_char *pe,
    ngx_uint_t *status_addr, unsigned *done_addr)
{
    int cs = *cs_addr;

    %% machine memc_incr_decr;
    %% include "memc_incr_decr.rl";
    %% write exec;

    *cs_addr = cs;

    return p;
}
