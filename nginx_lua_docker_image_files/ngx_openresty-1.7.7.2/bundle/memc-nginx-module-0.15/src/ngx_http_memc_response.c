
#line 1 "src/ngx_http_memc_response.rl"

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



#line 24 "src/ngx_http_memc_response.rl"

#line 29 "src/ngx_http_memc_response.c"
static const int memc_storage_start = 1;
static const int memc_storage_first_final = 54;
static const int memc_storage_error = 0;

static const int memc_storage_en_main = 1;


#line 25 "src/ngx_http_memc_response.rl"


#line 27 "src/ngx_http_memc_response.rl"

#line 42 "src/ngx_http_memc_response.c"
static const int memc_flush_all_start = 1;
static const int memc_flush_all_first_final = 30;
static const int memc_flush_all_error = 0;

static const int memc_flush_all_en_main = 1;


#line 28 "src/ngx_http_memc_response.rl"


#line 30 "src/ngx_http_memc_response.rl"

#line 55 "src/ngx_http_memc_response.c"
static const int memc_version_start = 1;
static const int memc_version_first_final = 39;
static const int memc_version_error = 0;

static const int memc_version_en_main = 1;


#line 31 "src/ngx_http_memc_response.rl"


#line 33 "src/ngx_http_memc_response.rl"

#line 68 "src/ngx_http_memc_response.c"
static const int memc_stats_start = 1;
static const int memc_stats_first_final = 43;
static const int memc_stats_error = 0;

static const int memc_stats_en_main = 1;


#line 34 "src/ngx_http_memc_response.rl"


#line 36 "src/ngx_http_memc_response.rl"

#line 81 "src/ngx_http_memc_response.c"
static const int memc_delete_start = 1;
static const int memc_delete_first_final = 45;
static const int memc_delete_error = 0;

static const int memc_delete_en_main = 1;


#line 37 "src/ngx_http_memc_response.rl"


#line 39 "src/ngx_http_memc_response.rl"

#line 94 "src/ngx_http_memc_response.c"
static const int memc_incr_decr_start = 1;
static const int memc_incr_decr_first_final = 40;
static const int memc_incr_decr_error = 0;

static const int memc_incr_decr_en_main = 1;


#line 40 "src/ngx_http_memc_response.rl"


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

            
#line 91 "src/ngx_http_memc_response.rl"
            
#line 156 "src/ngx_http_memc_response.c"
	{
	cs = memc_storage_start;
	}

#line 92 "src/ngx_http_memc_response.rl"

        } else if (ctx->cmd == ngx_http_memc_cmd_flush_all) {
            dd("init memc_flush_all machine...");

            
#line 97 "src/ngx_http_memc_response.rl"
            
#line 169 "src/ngx_http_memc_response.c"
	{
	cs = memc_flush_all_start;
	}

#line 98 "src/ngx_http_memc_response.rl"

        } else if (ctx->cmd == ngx_http_memc_cmd_version) {
            dd("init memc_version machine...");

            
#line 103 "src/ngx_http_memc_response.rl"
            
#line 182 "src/ngx_http_memc_response.c"
	{
	cs = memc_version_start;
	}

#line 104 "src/ngx_http_memc_response.rl"

        } else if (ctx->cmd == ngx_http_memc_cmd_stats) {
            dd("init memc_stats machine...");

            
#line 109 "src/ngx_http_memc_response.rl"
            
#line 195 "src/ngx_http_memc_response.c"
	{
	cs = memc_stats_start;
	}

#line 110 "src/ngx_http_memc_response.rl"

        } else if (ctx->cmd == ngx_http_memc_cmd_delete) {
            dd("init memc_delete machine...");

            
#line 115 "src/ngx_http_memc_response.rl"
            
#line 208 "src/ngx_http_memc_response.c"
	{
	cs = memc_delete_start;
	}

#line 116 "src/ngx_http_memc_response.rl"

        } else if (ctx->cmd == ngx_http_memc_cmd_incr
                || ctx->cmd == ngx_http_memc_cmd_decr)
        {
            dd("init memc_incr_decr machine...");

            
#line 123 "src/ngx_http_memc_response.rl"
            
#line 223 "src/ngx_http_memc_response.c"
	{
	cs = memc_incr_decr_start;
	}

#line 124 "src/ngx_http_memc_response.rl"

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

    
#line 577 "src/ngx_http_memc_response.rl"
    
#line 578 "src/ngx_http_memc_response.rl"
    
#line 686 "src/ngx_http_memc_response.c"
	{
	if ( p == pe )
		goto _test_eof;
	switch ( cs )
	{
case 1:
	switch( (*p) ) {
		case 67: goto st2;
		case 69: goto st16;
		case 78: goto st28;
		case 83: goto st43;
	}
	goto st0;
st0:
cs = 0;
	goto _out;
st2:
	if ( ++p == pe )
		goto _test_eof2;
case 2:
	if ( (*p) == 76 )
		goto st3;
	goto st0;
st3:
	if ( ++p == pe )
		goto _test_eof3;
case 3:
	if ( (*p) == 73 )
		goto st4;
	goto st0;
st4:
	if ( ++p == pe )
		goto _test_eof4;
case 4:
	if ( (*p) == 69 )
		goto st5;
	goto st0;
st5:
	if ( ++p == pe )
		goto _test_eof5;
case 5:
	if ( (*p) == 78 )
		goto st6;
	goto st0;
st6:
	if ( ++p == pe )
		goto _test_eof6;
case 6:
	if ( (*p) == 84 )
		goto st7;
	goto st0;
st7:
	if ( ++p == pe )
		goto _test_eof7;
case 7:
	if ( (*p) == 95 )
		goto st8;
	goto st0;
st8:
	if ( ++p == pe )
		goto _test_eof8;
case 8:
	if ( (*p) == 69 )
		goto st9;
	goto st0;
st9:
	if ( ++p == pe )
		goto _test_eof9;
case 9:
	if ( (*p) == 82 )
		goto st10;
	goto st0;
st10:
	if ( ++p == pe )
		goto _test_eof10;
case 10:
	if ( (*p) == 82 )
		goto st11;
	goto st0;
st11:
	if ( ++p == pe )
		goto _test_eof11;
case 11:
	if ( (*p) == 79 )
		goto st12;
	goto st0;
st12:
	if ( ++p == pe )
		goto _test_eof12;
case 12:
	if ( (*p) == 82 )
		goto st13;
	goto st0;
st13:
	if ( ++p == pe )
		goto _test_eof13;
case 13:
	if ( (*p) == 32 )
		goto st14;
	goto st0;
st14:
	if ( ++p == pe )
		goto _test_eof14;
case 14:
	if ( (*p) == 13 )
		goto st15;
	goto st14;
st15:
	if ( ++p == pe )
		goto _test_eof15;
case 15:
	switch( (*p) ) {
		case 10: goto tr18;
		case 13: goto st15;
	}
	goto st14;
tr18:
#line 4 "src/memc_common.rl"
	{
        dd("caught error...");
        dd("machine state: %d", cs);

        *status_addr = NGX_HTTP_BAD_GATEWAY;
    }
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
	goto st54;
tr30:
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
	goto st54;
tr41:
#line 37 "src/memc_common.rl"
	{
        dd("status set to 404");

        *status_addr = NGX_HTTP_NOT_FOUND;
    }
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
	goto st54;
tr56:
#line 31 "src/memc_common.rl"
	{
        dd("status set to 201");

        *status_addr = NGX_HTTP_CREATED;
    }
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
	goto st54;
st54:
	if ( ++p == pe )
		goto _test_eof54;
case 54:
#line 854 "src/ngx_http_memc_response.c"
	goto st0;
st16:
	if ( ++p == pe )
		goto _test_eof16;
case 16:
	switch( (*p) ) {
		case 82: goto st17;
		case 88: goto st22;
	}
	goto st0;
st17:
	if ( ++p == pe )
		goto _test_eof17;
case 17:
	if ( (*p) == 82 )
		goto st18;
	goto st0;
st18:
	if ( ++p == pe )
		goto _test_eof18;
case 18:
	if ( (*p) == 79 )
		goto st19;
	goto st0;
st19:
	if ( ++p == pe )
		goto _test_eof19;
case 19:
	if ( (*p) == 82 )
		goto st20;
	goto st0;
st20:
	if ( ++p == pe )
		goto _test_eof20;
case 20:
	if ( (*p) == 13 )
		goto st21;
	goto st0;
st21:
	if ( ++p == pe )
		goto _test_eof21;
case 21:
	if ( (*p) == 10 )
		goto tr18;
	goto st0;
st22:
	if ( ++p == pe )
		goto _test_eof22;
case 22:
	if ( (*p) == 73 )
		goto st23;
	goto st0;
st23:
	if ( ++p == pe )
		goto _test_eof23;
case 23:
	if ( (*p) == 83 )
		goto st24;
	goto st0;
st24:
	if ( ++p == pe )
		goto _test_eof24;
case 24:
	if ( (*p) == 84 )
		goto st25;
	goto st0;
st25:
	if ( ++p == pe )
		goto _test_eof25;
case 25:
	if ( (*p) == 83 )
		goto st26;
	goto st0;
st26:
	if ( ++p == pe )
		goto _test_eof26;
case 26:
	if ( (*p) == 13 )
		goto st27;
	goto st0;
st27:
	if ( ++p == pe )
		goto _test_eof27;
case 27:
	if ( (*p) == 10 )
		goto tr30;
	goto st0;
st28:
	if ( ++p == pe )
		goto _test_eof28;
case 28:
	if ( (*p) == 79 )
		goto st29;
	goto st0;
st29:
	if ( ++p == pe )
		goto _test_eof29;
case 29:
	if ( (*p) == 84 )
		goto st30;
	goto st0;
st30:
	if ( ++p == pe )
		goto _test_eof30;
case 30:
	if ( (*p) == 95 )
		goto st31;
	goto st0;
st31:
	if ( ++p == pe )
		goto _test_eof31;
case 31:
	switch( (*p) ) {
		case 70: goto st32;
		case 83: goto st38;
	}
	goto st0;
st32:
	if ( ++p == pe )
		goto _test_eof32;
case 32:
	if ( (*p) == 79 )
		goto st33;
	goto st0;
st33:
	if ( ++p == pe )
		goto _test_eof33;
case 33:
	if ( (*p) == 85 )
		goto st34;
	goto st0;
st34:
	if ( ++p == pe )
		goto _test_eof34;
case 34:
	if ( (*p) == 78 )
		goto st35;
	goto st0;
st35:
	if ( ++p == pe )
		goto _test_eof35;
case 35:
	if ( (*p) == 68 )
		goto st36;
	goto st0;
st36:
	if ( ++p == pe )
		goto _test_eof36;
case 36:
	if ( (*p) == 13 )
		goto st37;
	goto st0;
st37:
	if ( ++p == pe )
		goto _test_eof37;
case 37:
	if ( (*p) == 10 )
		goto tr41;
	goto st0;
st38:
	if ( ++p == pe )
		goto _test_eof38;
case 38:
	if ( (*p) == 84 )
		goto st39;
	goto st0;
st39:
	if ( ++p == pe )
		goto _test_eof39;
case 39:
	if ( (*p) == 79 )
		goto st40;
	goto st0;
st40:
	if ( ++p == pe )
		goto _test_eof40;
case 40:
	if ( (*p) == 82 )
		goto st41;
	goto st0;
st41:
	if ( ++p == pe )
		goto _test_eof41;
case 41:
	if ( (*p) == 69 )
		goto st42;
	goto st0;
st42:
	if ( ++p == pe )
		goto _test_eof42;
case 42:
	if ( (*p) == 68 )
		goto st26;
	goto st0;
st43:
	if ( ++p == pe )
		goto _test_eof43;
case 43:
	switch( (*p) ) {
		case 69: goto st44;
		case 84: goto st48;
	}
	goto st0;
st44:
	if ( ++p == pe )
		goto _test_eof44;
case 44:
	if ( (*p) == 82 )
		goto st45;
	goto st0;
st45:
	if ( ++p == pe )
		goto _test_eof45;
case 45:
	if ( (*p) == 86 )
		goto st46;
	goto st0;
st46:
	if ( ++p == pe )
		goto _test_eof46;
case 46:
	if ( (*p) == 69 )
		goto st47;
	goto st0;
st47:
	if ( ++p == pe )
		goto _test_eof47;
case 47:
	if ( (*p) == 82 )
		goto st7;
	goto st0;
st48:
	if ( ++p == pe )
		goto _test_eof48;
case 48:
	if ( (*p) == 79 )
		goto st49;
	goto st0;
st49:
	if ( ++p == pe )
		goto _test_eof49;
case 49:
	if ( (*p) == 82 )
		goto st50;
	goto st0;
st50:
	if ( ++p == pe )
		goto _test_eof50;
case 50:
	if ( (*p) == 69 )
		goto st51;
	goto st0;
st51:
	if ( ++p == pe )
		goto _test_eof51;
case 51:
	if ( (*p) == 68 )
		goto st52;
	goto st0;
st52:
	if ( ++p == pe )
		goto _test_eof52;
case 52:
	if ( (*p) == 13 )
		goto st53;
	goto st0;
st53:
	if ( ++p == pe )
		goto _test_eof53;
case 53:
	if ( (*p) == 10 )
		goto tr56;
	goto st0;
	}
	_test_eof2: cs = 2; goto _test_eof; 
	_test_eof3: cs = 3; goto _test_eof; 
	_test_eof4: cs = 4; goto _test_eof; 
	_test_eof5: cs = 5; goto _test_eof; 
	_test_eof6: cs = 6; goto _test_eof; 
	_test_eof7: cs = 7; goto _test_eof; 
	_test_eof8: cs = 8; goto _test_eof; 
	_test_eof9: cs = 9; goto _test_eof; 
	_test_eof10: cs = 10; goto _test_eof; 
	_test_eof11: cs = 11; goto _test_eof; 
	_test_eof12: cs = 12; goto _test_eof; 
	_test_eof13: cs = 13; goto _test_eof; 
	_test_eof14: cs = 14; goto _test_eof; 
	_test_eof15: cs = 15; goto _test_eof; 
	_test_eof54: cs = 54; goto _test_eof; 
	_test_eof16: cs = 16; goto _test_eof; 
	_test_eof17: cs = 17; goto _test_eof; 
	_test_eof18: cs = 18; goto _test_eof; 
	_test_eof19: cs = 19; goto _test_eof; 
	_test_eof20: cs = 20; goto _test_eof; 
	_test_eof21: cs = 21; goto _test_eof; 
	_test_eof22: cs = 22; goto _test_eof; 
	_test_eof23: cs = 23; goto _test_eof; 
	_test_eof24: cs = 24; goto _test_eof; 
	_test_eof25: cs = 25; goto _test_eof; 
	_test_eof26: cs = 26; goto _test_eof; 
	_test_eof27: cs = 27; goto _test_eof; 
	_test_eof28: cs = 28; goto _test_eof; 
	_test_eof29: cs = 29; goto _test_eof; 
	_test_eof30: cs = 30; goto _test_eof; 
	_test_eof31: cs = 31; goto _test_eof; 
	_test_eof32: cs = 32; goto _test_eof; 
	_test_eof33: cs = 33; goto _test_eof; 
	_test_eof34: cs = 34; goto _test_eof; 
	_test_eof35: cs = 35; goto _test_eof; 
	_test_eof36: cs = 36; goto _test_eof; 
	_test_eof37: cs = 37; goto _test_eof; 
	_test_eof38: cs = 38; goto _test_eof; 
	_test_eof39: cs = 39; goto _test_eof; 
	_test_eof40: cs = 40; goto _test_eof; 
	_test_eof41: cs = 41; goto _test_eof; 
	_test_eof42: cs = 42; goto _test_eof; 
	_test_eof43: cs = 43; goto _test_eof; 
	_test_eof44: cs = 44; goto _test_eof; 
	_test_eof45: cs = 45; goto _test_eof; 
	_test_eof46: cs = 46; goto _test_eof; 
	_test_eof47: cs = 47; goto _test_eof; 
	_test_eof48: cs = 48; goto _test_eof; 
	_test_eof49: cs = 49; goto _test_eof; 
	_test_eof50: cs = 50; goto _test_eof; 
	_test_eof51: cs = 51; goto _test_eof; 
	_test_eof52: cs = 52; goto _test_eof; 
	_test_eof53: cs = 53; goto _test_eof; 

	_test_eof: {}
	_out: {}
	}

#line 579 "src/ngx_http_memc_response.rl"

    *cs_addr = cs;

    return p;
}


static u_char *
parse_memc_flush_all(int *cs_addr, u_char *p, u_char *pe,
    ngx_uint_t *status_addr, unsigned *done_addr)
{
    int cs = *cs_addr;

    
#line 593 "src/ngx_http_memc_response.rl"
    
#line 594 "src/ngx_http_memc_response.rl"
    
#line 1206 "src/ngx_http_memc_response.c"
	{
	if ( p == pe )
		goto _test_eof;
	switch ( cs )
	{
case 1:
	switch( (*p) ) {
		case 67: goto st2;
		case 69: goto st16;
		case 79: goto st22;
		case 83: goto st25;
	}
	goto st0;
st0:
cs = 0;
	goto _out;
st2:
	if ( ++p == pe )
		goto _test_eof2;
case 2:
	if ( (*p) == 76 )
		goto st3;
	goto st0;
st3:
	if ( ++p == pe )
		goto _test_eof3;
case 3:
	if ( (*p) == 73 )
		goto st4;
	goto st0;
st4:
	if ( ++p == pe )
		goto _test_eof4;
case 4:
	if ( (*p) == 69 )
		goto st5;
	goto st0;
st5:
	if ( ++p == pe )
		goto _test_eof5;
case 5:
	if ( (*p) == 78 )
		goto st6;
	goto st0;
st6:
	if ( ++p == pe )
		goto _test_eof6;
case 6:
	if ( (*p) == 84 )
		goto st7;
	goto st0;
st7:
	if ( ++p == pe )
		goto _test_eof7;
case 7:
	if ( (*p) == 95 )
		goto st8;
	goto st0;
st8:
	if ( ++p == pe )
		goto _test_eof8;
case 8:
	if ( (*p) == 69 )
		goto st9;
	goto st0;
st9:
	if ( ++p == pe )
		goto _test_eof9;
case 9:
	if ( (*p) == 82 )
		goto st10;
	goto st0;
st10:
	if ( ++p == pe )
		goto _test_eof10;
case 10:
	if ( (*p) == 82 )
		goto st11;
	goto st0;
st11:
	if ( ++p == pe )
		goto _test_eof11;
case 11:
	if ( (*p) == 79 )
		goto st12;
	goto st0;
st12:
	if ( ++p == pe )
		goto _test_eof12;
case 12:
	if ( (*p) == 82 )
		goto st13;
	goto st0;
st13:
	if ( ++p == pe )
		goto _test_eof13;
case 13:
	if ( (*p) == 32 )
		goto st14;
	goto st0;
st14:
	if ( ++p == pe )
		goto _test_eof14;
case 14:
	if ( (*p) == 13 )
		goto st15;
	goto st14;
st15:
	if ( ++p == pe )
		goto _test_eof15;
case 15:
	switch( (*p) ) {
		case 10: goto tr18;
		case 13: goto st15;
	}
	goto st14;
tr18:
#line 4 "src/memc_common.rl"
	{
        dd("caught error...");
        dd("machine state: %d", cs);

        *status_addr = NGX_HTTP_BAD_GATEWAY;
    }
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
	goto st30;
tr26:
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
	goto st30;
st30:
	if ( ++p == pe )
		goto _test_eof30;
case 30:
#line 1348 "src/ngx_http_memc_response.c"
	goto st0;
st16:
	if ( ++p == pe )
		goto _test_eof16;
case 16:
	if ( (*p) == 82 )
		goto st17;
	goto st0;
st17:
	if ( ++p == pe )
		goto _test_eof17;
case 17:
	if ( (*p) == 82 )
		goto st18;
	goto st0;
st18:
	if ( ++p == pe )
		goto _test_eof18;
case 18:
	if ( (*p) == 79 )
		goto st19;
	goto st0;
st19:
	if ( ++p == pe )
		goto _test_eof19;
case 19:
	if ( (*p) == 82 )
		goto st20;
	goto st0;
st20:
	if ( ++p == pe )
		goto _test_eof20;
case 20:
	if ( (*p) == 13 )
		goto st21;
	goto st0;
st21:
	if ( ++p == pe )
		goto _test_eof21;
case 21:
	if ( (*p) == 10 )
		goto tr18;
	goto st0;
st22:
	if ( ++p == pe )
		goto _test_eof22;
case 22:
	if ( (*p) == 75 )
		goto st23;
	goto st0;
st23:
	if ( ++p == pe )
		goto _test_eof23;
case 23:
	if ( (*p) == 13 )
		goto st24;
	goto st0;
st24:
	if ( ++p == pe )
		goto _test_eof24;
case 24:
	if ( (*p) == 10 )
		goto tr26;
	goto st0;
st25:
	if ( ++p == pe )
		goto _test_eof25;
case 25:
	if ( (*p) == 69 )
		goto st26;
	goto st0;
st26:
	if ( ++p == pe )
		goto _test_eof26;
case 26:
	if ( (*p) == 82 )
		goto st27;
	goto st0;
st27:
	if ( ++p == pe )
		goto _test_eof27;
case 27:
	if ( (*p) == 86 )
		goto st28;
	goto st0;
st28:
	if ( ++p == pe )
		goto _test_eof28;
case 28:
	if ( (*p) == 69 )
		goto st29;
	goto st0;
st29:
	if ( ++p == pe )
		goto _test_eof29;
case 29:
	if ( (*p) == 82 )
		goto st7;
	goto st0;
	}
	_test_eof2: cs = 2; goto _test_eof; 
	_test_eof3: cs = 3; goto _test_eof; 
	_test_eof4: cs = 4; goto _test_eof; 
	_test_eof5: cs = 5; goto _test_eof; 
	_test_eof6: cs = 6; goto _test_eof; 
	_test_eof7: cs = 7; goto _test_eof; 
	_test_eof8: cs = 8; goto _test_eof; 
	_test_eof9: cs = 9; goto _test_eof; 
	_test_eof10: cs = 10; goto _test_eof; 
	_test_eof11: cs = 11; goto _test_eof; 
	_test_eof12: cs = 12; goto _test_eof; 
	_test_eof13: cs = 13; goto _test_eof; 
	_test_eof14: cs = 14; goto _test_eof; 
	_test_eof15: cs = 15; goto _test_eof; 
	_test_eof30: cs = 30; goto _test_eof; 
	_test_eof16: cs = 16; goto _test_eof; 
	_test_eof17: cs = 17; goto _test_eof; 
	_test_eof18: cs = 18; goto _test_eof; 
	_test_eof19: cs = 19; goto _test_eof; 
	_test_eof20: cs = 20; goto _test_eof; 
	_test_eof21: cs = 21; goto _test_eof; 
	_test_eof22: cs = 22; goto _test_eof; 
	_test_eof23: cs = 23; goto _test_eof; 
	_test_eof24: cs = 24; goto _test_eof; 
	_test_eof25: cs = 25; goto _test_eof; 
	_test_eof26: cs = 26; goto _test_eof; 
	_test_eof27: cs = 27; goto _test_eof; 
	_test_eof28: cs = 28; goto _test_eof; 
	_test_eof29: cs = 29; goto _test_eof; 

	_test_eof: {}
	_out: {}
	}

#line 595 "src/ngx_http_memc_response.rl"

    *cs_addr = cs;

    return p;
}


static u_char *
parse_memc_version(int *cs_addr, u_char *p, u_char *pe,
    ngx_uint_t *status_addr, unsigned *done_addr)
{
    int cs = *cs_addr;

    
#line 609 "src/ngx_http_memc_response.rl"
    
#line 610 "src/ngx_http_memc_response.rl"
    
#line 1502 "src/ngx_http_memc_response.c"
	{
	if ( p == pe )
		goto _test_eof;
	switch ( cs )
	{
case 1:
	switch( (*p) ) {
		case 67: goto st2;
		case 69: goto st16;
		case 83: goto st22;
		case 86: goto st27;
	}
	goto st0;
st0:
cs = 0;
	goto _out;
st2:
	if ( ++p == pe )
		goto _test_eof2;
case 2:
	if ( (*p) == 76 )
		goto st3;
	goto st0;
st3:
	if ( ++p == pe )
		goto _test_eof3;
case 3:
	if ( (*p) == 73 )
		goto st4;
	goto st0;
st4:
	if ( ++p == pe )
		goto _test_eof4;
case 4:
	if ( (*p) == 69 )
		goto st5;
	goto st0;
st5:
	if ( ++p == pe )
		goto _test_eof5;
case 5:
	if ( (*p) == 78 )
		goto st6;
	goto st0;
st6:
	if ( ++p == pe )
		goto _test_eof6;
case 6:
	if ( (*p) == 84 )
		goto st7;
	goto st0;
st7:
	if ( ++p == pe )
		goto _test_eof7;
case 7:
	if ( (*p) == 95 )
		goto st8;
	goto st0;
st8:
	if ( ++p == pe )
		goto _test_eof8;
case 8:
	if ( (*p) == 69 )
		goto st9;
	goto st0;
st9:
	if ( ++p == pe )
		goto _test_eof9;
case 9:
	if ( (*p) == 82 )
		goto st10;
	goto st0;
st10:
	if ( ++p == pe )
		goto _test_eof10;
case 10:
	if ( (*p) == 82 )
		goto st11;
	goto st0;
st11:
	if ( ++p == pe )
		goto _test_eof11;
case 11:
	if ( (*p) == 79 )
		goto st12;
	goto st0;
st12:
	if ( ++p == pe )
		goto _test_eof12;
case 12:
	if ( (*p) == 82 )
		goto st13;
	goto st0;
st13:
	if ( ++p == pe )
		goto _test_eof13;
case 13:
	if ( (*p) == 32 )
		goto st14;
	goto st0;
st14:
	if ( ++p == pe )
		goto _test_eof14;
case 14:
	if ( (*p) == 13 )
		goto st15;
	goto st14;
st15:
	if ( ++p == pe )
		goto _test_eof15;
case 15:
	switch( (*p) ) {
		case 10: goto tr18;
		case 13: goto st15;
	}
	goto st14;
tr18:
#line 4 "src/memc_common.rl"
	{
        dd("caught error...");
        dd("machine state: %d", cs);

        *status_addr = NGX_HTTP_BAD_GATEWAY;
    }
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
	goto st39;
st39:
	if ( ++p == pe )
		goto _test_eof39;
case 39:
#line 1637 "src/ngx_http_memc_response.c"
	goto st0;
st16:
	if ( ++p == pe )
		goto _test_eof16;
case 16:
	if ( (*p) == 82 )
		goto st17;
	goto st0;
st17:
	if ( ++p == pe )
		goto _test_eof17;
case 17:
	if ( (*p) == 82 )
		goto st18;
	goto st0;
st18:
	if ( ++p == pe )
		goto _test_eof18;
case 18:
	if ( (*p) == 79 )
		goto st19;
	goto st0;
st19:
	if ( ++p == pe )
		goto _test_eof19;
case 19:
	if ( (*p) == 82 )
		goto st20;
	goto st0;
st20:
	if ( ++p == pe )
		goto _test_eof20;
case 20:
	if ( (*p) == 13 )
		goto st21;
	goto st0;
st21:
	if ( ++p == pe )
		goto _test_eof21;
case 21:
	if ( (*p) == 10 )
		goto tr18;
	goto st0;
st22:
	if ( ++p == pe )
		goto _test_eof22;
case 22:
	if ( (*p) == 69 )
		goto st23;
	goto st0;
st23:
	if ( ++p == pe )
		goto _test_eof23;
case 23:
	if ( (*p) == 82 )
		goto st24;
	goto st0;
st24:
	if ( ++p == pe )
		goto _test_eof24;
case 24:
	if ( (*p) == 86 )
		goto st25;
	goto st0;
st25:
	if ( ++p == pe )
		goto _test_eof25;
case 25:
	if ( (*p) == 69 )
		goto st26;
	goto st0;
st26:
	if ( ++p == pe )
		goto _test_eof26;
case 26:
	if ( (*p) == 82 )
		goto st7;
	goto st0;
st27:
	if ( ++p == pe )
		goto _test_eof27;
case 27:
	if ( (*p) == 69 )
		goto st28;
	goto st0;
st28:
	if ( ++p == pe )
		goto _test_eof28;
case 28:
	if ( (*p) == 82 )
		goto st29;
	goto st0;
st29:
	if ( ++p == pe )
		goto _test_eof29;
case 29:
	if ( (*p) == 83 )
		goto st30;
	goto st0;
st30:
	if ( ++p == pe )
		goto _test_eof30;
case 30:
	if ( (*p) == 73 )
		goto st31;
	goto st0;
st31:
	if ( ++p == pe )
		goto _test_eof31;
case 31:
	if ( (*p) == 79 )
		goto st32;
	goto st0;
st32:
	if ( ++p == pe )
		goto _test_eof32;
case 32:
	if ( (*p) == 78 )
		goto st33;
	goto st0;
st33:
	if ( ++p == pe )
		goto _test_eof33;
case 33:
	if ( (*p) == 32 )
		goto st34;
	goto st0;
st34:
	if ( ++p == pe )
		goto _test_eof34;
case 34:
	if ( (*p) == 13 )
		goto st37;
	goto st35;
st35:
	if ( ++p == pe )
		goto _test_eof35;
case 35:
	if ( (*p) == 13 )
		goto st36;
	goto st35;
st36:
	if ( ++p == pe )
		goto _test_eof36;
case 36:
	switch( (*p) ) {
		case 10: goto tr38;
		case 13: goto st36;
	}
	goto st35;
tr38:
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
	goto st40;
st40:
	if ( ++p == pe )
		goto _test_eof40;
case 40:
#line 1799 "src/ngx_http_memc_response.c"
	if ( (*p) == 13 )
		goto st36;
	goto st35;
st37:
	if ( ++p == pe )
		goto _test_eof37;
case 37:
	switch( (*p) ) {
		case 10: goto st38;
		case 13: goto st36;
	}
	goto st35;
st38:
	if ( ++p == pe )
		goto _test_eof38;
case 38:
	goto st35;
	}
	_test_eof2: cs = 2; goto _test_eof; 
	_test_eof3: cs = 3; goto _test_eof; 
	_test_eof4: cs = 4; goto _test_eof; 
	_test_eof5: cs = 5; goto _test_eof; 
	_test_eof6: cs = 6; goto _test_eof; 
	_test_eof7: cs = 7; goto _test_eof; 
	_test_eof8: cs = 8; goto _test_eof; 
	_test_eof9: cs = 9; goto _test_eof; 
	_test_eof10: cs = 10; goto _test_eof; 
	_test_eof11: cs = 11; goto _test_eof; 
	_test_eof12: cs = 12; goto _test_eof; 
	_test_eof13: cs = 13; goto _test_eof; 
	_test_eof14: cs = 14; goto _test_eof; 
	_test_eof15: cs = 15; goto _test_eof; 
	_test_eof39: cs = 39; goto _test_eof; 
	_test_eof16: cs = 16; goto _test_eof; 
	_test_eof17: cs = 17; goto _test_eof; 
	_test_eof18: cs = 18; goto _test_eof; 
	_test_eof19: cs = 19; goto _test_eof; 
	_test_eof20: cs = 20; goto _test_eof; 
	_test_eof21: cs = 21; goto _test_eof; 
	_test_eof22: cs = 22; goto _test_eof; 
	_test_eof23: cs = 23; goto _test_eof; 
	_test_eof24: cs = 24; goto _test_eof; 
	_test_eof25: cs = 25; goto _test_eof; 
	_test_eof26: cs = 26; goto _test_eof; 
	_test_eof27: cs = 27; goto _test_eof; 
	_test_eof28: cs = 28; goto _test_eof; 
	_test_eof29: cs = 29; goto _test_eof; 
	_test_eof30: cs = 30; goto _test_eof; 
	_test_eof31: cs = 31; goto _test_eof; 
	_test_eof32: cs = 32; goto _test_eof; 
	_test_eof33: cs = 33; goto _test_eof; 
	_test_eof34: cs = 34; goto _test_eof; 
	_test_eof35: cs = 35; goto _test_eof; 
	_test_eof36: cs = 36; goto _test_eof; 
	_test_eof40: cs = 40; goto _test_eof; 
	_test_eof37: cs = 37; goto _test_eof; 
	_test_eof38: cs = 38; goto _test_eof; 

	_test_eof: {}
	_out: {}
	}

#line 611 "src/ngx_http_memc_response.rl"

    *cs_addr = cs;

    return p;
}


static u_char *
parse_memc_stats(int *cs_addr, u_char *p, u_char *pe, ngx_uint_t *status_addr,
    unsigned *done_addr)
{
    int cs = *cs_addr;

    
#line 625 "src/ngx_http_memc_response.rl"
    
#line 626 "src/ngx_http_memc_response.rl"
    
#line 1881 "src/ngx_http_memc_response.c"
	{
	if ( p == pe )
		goto _test_eof;
	switch ( cs )
	{
case 1:
	switch( (*p) ) {
		case 67: goto st2;
		case 69: goto st16;
		case 83: goto st25;
	}
	goto st0;
st0:
cs = 0;
	goto _out;
st2:
	if ( ++p == pe )
		goto _test_eof2;
case 2:
	if ( (*p) == 76 )
		goto st3;
	goto st0;
st3:
	if ( ++p == pe )
		goto _test_eof3;
case 3:
	if ( (*p) == 73 )
		goto st4;
	goto st0;
st4:
	if ( ++p == pe )
		goto _test_eof4;
case 4:
	if ( (*p) == 69 )
		goto st5;
	goto st0;
st5:
	if ( ++p == pe )
		goto _test_eof5;
case 5:
	if ( (*p) == 78 )
		goto st6;
	goto st0;
st6:
	if ( ++p == pe )
		goto _test_eof6;
case 6:
	if ( (*p) == 84 )
		goto st7;
	goto st0;
st7:
	if ( ++p == pe )
		goto _test_eof7;
case 7:
	if ( (*p) == 95 )
		goto st8;
	goto st0;
st8:
	if ( ++p == pe )
		goto _test_eof8;
case 8:
	if ( (*p) == 69 )
		goto st9;
	goto st0;
st9:
	if ( ++p == pe )
		goto _test_eof9;
case 9:
	if ( (*p) == 82 )
		goto st10;
	goto st0;
st10:
	if ( ++p == pe )
		goto _test_eof10;
case 10:
	if ( (*p) == 82 )
		goto st11;
	goto st0;
st11:
	if ( ++p == pe )
		goto _test_eof11;
case 11:
	if ( (*p) == 79 )
		goto st12;
	goto st0;
st12:
	if ( ++p == pe )
		goto _test_eof12;
case 12:
	if ( (*p) == 82 )
		goto st13;
	goto st0;
st13:
	if ( ++p == pe )
		goto _test_eof13;
case 13:
	if ( (*p) == 32 )
		goto st14;
	goto st0;
st14:
	if ( ++p == pe )
		goto _test_eof14;
case 14:
	if ( (*p) == 13 )
		goto st15;
	goto st14;
st15:
	if ( ++p == pe )
		goto _test_eof15;
case 15:
	switch( (*p) ) {
		case 10: goto tr17;
		case 13: goto st15;
	}
	goto st14;
tr17:
#line 4 "src/memc_common.rl"
	{
        dd("caught error...");
        dd("machine state: %d", cs);

        *status_addr = NGX_HTTP_BAD_GATEWAY;
    }
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
	goto st43;
tr22:
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
	goto st43;
st43:
	if ( ++p == pe )
		goto _test_eof43;
case 43:
#line 2022 "src/ngx_http_memc_response.c"
	goto st0;
st16:
	if ( ++p == pe )
		goto _test_eof16;
case 16:
	switch( (*p) ) {
		case 78: goto st17;
		case 82: goto st20;
	}
	goto st0;
st17:
	if ( ++p == pe )
		goto _test_eof17;
case 17:
	if ( (*p) == 68 )
		goto st18;
	goto st0;
st18:
	if ( ++p == pe )
		goto _test_eof18;
case 18:
	if ( (*p) == 13 )
		goto st19;
	goto st0;
st19:
	if ( ++p == pe )
		goto _test_eof19;
case 19:
	if ( (*p) == 10 )
		goto tr22;
	goto st0;
st20:
	if ( ++p == pe )
		goto _test_eof20;
case 20:
	if ( (*p) == 82 )
		goto st21;
	goto st0;
st21:
	if ( ++p == pe )
		goto _test_eof21;
case 21:
	if ( (*p) == 79 )
		goto st22;
	goto st0;
st22:
	if ( ++p == pe )
		goto _test_eof22;
case 22:
	if ( (*p) == 82 )
		goto st23;
	goto st0;
st23:
	if ( ++p == pe )
		goto _test_eof23;
case 23:
	if ( (*p) == 13 )
		goto st24;
	goto st0;
st24:
	if ( ++p == pe )
		goto _test_eof24;
case 24:
	if ( (*p) == 10 )
		goto tr17;
	goto st0;
st25:
	if ( ++p == pe )
		goto _test_eof25;
case 25:
	switch( (*p) ) {
		case 69: goto st26;
		case 84: goto st30;
	}
	goto st0;
st26:
	if ( ++p == pe )
		goto _test_eof26;
case 26:
	if ( (*p) == 82 )
		goto st27;
	goto st0;
st27:
	if ( ++p == pe )
		goto _test_eof27;
case 27:
	if ( (*p) == 86 )
		goto st28;
	goto st0;
st28:
	if ( ++p == pe )
		goto _test_eof28;
case 28:
	if ( (*p) == 69 )
		goto st29;
	goto st0;
st29:
	if ( ++p == pe )
		goto _test_eof29;
case 29:
	if ( (*p) == 82 )
		goto st7;
	goto st0;
st30:
	if ( ++p == pe )
		goto _test_eof30;
case 30:
	if ( (*p) == 65 )
		goto st31;
	goto st0;
st31:
	if ( ++p == pe )
		goto _test_eof31;
case 31:
	if ( (*p) == 84 )
		goto st32;
	goto st0;
st32:
	if ( ++p == pe )
		goto _test_eof32;
case 32:
	if ( (*p) == 32 )
		goto st33;
	goto st0;
st33:
	if ( ++p == pe )
		goto _test_eof33;
case 33:
	if ( (*p) == 13 )
		goto st41;
	goto st34;
st34:
	if ( ++p == pe )
		goto _test_eof34;
case 34:
	if ( (*p) == 13 )
		goto st35;
	goto st34;
st35:
	if ( ++p == pe )
		goto _test_eof35;
case 35:
	switch( (*p) ) {
		case 10: goto st36;
		case 13: goto st35;
	}
	goto st34;
st36:
	if ( ++p == pe )
		goto _test_eof36;
case 36:
	switch( (*p) ) {
		case 13: goto st35;
		case 69: goto st37;
	}
	goto st34;
st37:
	if ( ++p == pe )
		goto _test_eof37;
case 37:
	switch( (*p) ) {
		case 13: goto st35;
		case 78: goto st38;
	}
	goto st34;
st38:
	if ( ++p == pe )
		goto _test_eof38;
case 38:
	switch( (*p) ) {
		case 13: goto st35;
		case 68: goto st39;
	}
	goto st34;
st39:
	if ( ++p == pe )
		goto _test_eof39;
case 39:
	if ( (*p) == 13 )
		goto st40;
	goto st34;
st40:
	if ( ++p == pe )
		goto _test_eof40;
case 40:
	switch( (*p) ) {
		case 10: goto tr43;
		case 13: goto st35;
	}
	goto st34;
tr43:
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
	goto st44;
st44:
	if ( ++p == pe )
		goto _test_eof44;
case 44:
#line 2224 "src/ngx_http_memc_response.c"
	switch( (*p) ) {
		case 13: goto st35;
		case 69: goto st37;
	}
	goto st34;
st41:
	if ( ++p == pe )
		goto _test_eof41;
case 41:
	switch( (*p) ) {
		case 10: goto st42;
		case 13: goto st35;
	}
	goto st34;
st42:
	if ( ++p == pe )
		goto _test_eof42;
case 42:
	goto st34;
	}
	_test_eof2: cs = 2; goto _test_eof; 
	_test_eof3: cs = 3; goto _test_eof; 
	_test_eof4: cs = 4; goto _test_eof; 
	_test_eof5: cs = 5; goto _test_eof; 
	_test_eof6: cs = 6; goto _test_eof; 
	_test_eof7: cs = 7; goto _test_eof; 
	_test_eof8: cs = 8; goto _test_eof; 
	_test_eof9: cs = 9; goto _test_eof; 
	_test_eof10: cs = 10; goto _test_eof; 
	_test_eof11: cs = 11; goto _test_eof; 
	_test_eof12: cs = 12; goto _test_eof; 
	_test_eof13: cs = 13; goto _test_eof; 
	_test_eof14: cs = 14; goto _test_eof; 
	_test_eof15: cs = 15; goto _test_eof; 
	_test_eof43: cs = 43; goto _test_eof; 
	_test_eof16: cs = 16; goto _test_eof; 
	_test_eof17: cs = 17; goto _test_eof; 
	_test_eof18: cs = 18; goto _test_eof; 
	_test_eof19: cs = 19; goto _test_eof; 
	_test_eof20: cs = 20; goto _test_eof; 
	_test_eof21: cs = 21; goto _test_eof; 
	_test_eof22: cs = 22; goto _test_eof; 
	_test_eof23: cs = 23; goto _test_eof; 
	_test_eof24: cs = 24; goto _test_eof; 
	_test_eof25: cs = 25; goto _test_eof; 
	_test_eof26: cs = 26; goto _test_eof; 
	_test_eof27: cs = 27; goto _test_eof; 
	_test_eof28: cs = 28; goto _test_eof; 
	_test_eof29: cs = 29; goto _test_eof; 
	_test_eof30: cs = 30; goto _test_eof; 
	_test_eof31: cs = 31; goto _test_eof; 
	_test_eof32: cs = 32; goto _test_eof; 
	_test_eof33: cs = 33; goto _test_eof; 
	_test_eof34: cs = 34; goto _test_eof; 
	_test_eof35: cs = 35; goto _test_eof; 
	_test_eof36: cs = 36; goto _test_eof; 
	_test_eof37: cs = 37; goto _test_eof; 
	_test_eof38: cs = 38; goto _test_eof; 
	_test_eof39: cs = 39; goto _test_eof; 
	_test_eof40: cs = 40; goto _test_eof; 
	_test_eof44: cs = 44; goto _test_eof; 
	_test_eof41: cs = 41; goto _test_eof; 
	_test_eof42: cs = 42; goto _test_eof; 

	_test_eof: {}
	_out: {}
	}

#line 627 "src/ngx_http_memc_response.rl"

    *cs_addr = cs;

    return p;
}


static u_char *
parse_memc_delete(int *cs_addr, u_char *p, u_char *pe, ngx_uint_t *status_addr,
    unsigned *done_addr)
{
    int cs = *cs_addr;

    
#line 641 "src/ngx_http_memc_response.rl"
    
#line 642 "src/ngx_http_memc_response.rl"
    
#line 2312 "src/ngx_http_memc_response.c"
	{
	if ( p == pe )
		goto _test_eof;
	switch ( cs )
	{
case 1:
	switch( (*p) ) {
		case 67: goto st2;
		case 68: goto st16;
		case 69: goto st24;
		case 78: goto st30;
		case 83: goto st40;
	}
	goto st0;
st0:
cs = 0;
	goto _out;
st2:
	if ( ++p == pe )
		goto _test_eof2;
case 2:
	if ( (*p) == 76 )
		goto st3;
	goto st0;
st3:
	if ( ++p == pe )
		goto _test_eof3;
case 3:
	if ( (*p) == 73 )
		goto st4;
	goto st0;
st4:
	if ( ++p == pe )
		goto _test_eof4;
case 4:
	if ( (*p) == 69 )
		goto st5;
	goto st0;
st5:
	if ( ++p == pe )
		goto _test_eof5;
case 5:
	if ( (*p) == 78 )
		goto st6;
	goto st0;
st6:
	if ( ++p == pe )
		goto _test_eof6;
case 6:
	if ( (*p) == 84 )
		goto st7;
	goto st0;
st7:
	if ( ++p == pe )
		goto _test_eof7;
case 7:
	if ( (*p) == 95 )
		goto st8;
	goto st0;
st8:
	if ( ++p == pe )
		goto _test_eof8;
case 8:
	if ( (*p) == 69 )
		goto st9;
	goto st0;
st9:
	if ( ++p == pe )
		goto _test_eof9;
case 9:
	if ( (*p) == 82 )
		goto st10;
	goto st0;
st10:
	if ( ++p == pe )
		goto _test_eof10;
case 10:
	if ( (*p) == 82 )
		goto st11;
	goto st0;
st11:
	if ( ++p == pe )
		goto _test_eof11;
case 11:
	if ( (*p) == 79 )
		goto st12;
	goto st0;
st12:
	if ( ++p == pe )
		goto _test_eof12;
case 12:
	if ( (*p) == 82 )
		goto st13;
	goto st0;
st13:
	if ( ++p == pe )
		goto _test_eof13;
case 13:
	if ( (*p) == 32 )
		goto st14;
	goto st0;
st14:
	if ( ++p == pe )
		goto _test_eof14;
case 14:
	if ( (*p) == 13 )
		goto st15;
	goto st14;
st15:
	if ( ++p == pe )
		goto _test_eof15;
case 15:
	switch( (*p) ) {
		case 10: goto tr19;
		case 13: goto st15;
	}
	goto st14;
tr19:
#line 4 "src/memc_common.rl"
	{
        dd("caught error...");
        dd("machine state: %d", cs);

        *status_addr = NGX_HTTP_BAD_GATEWAY;
    }
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
	goto st45;
tr27:
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
	goto st45;
tr42:
#line 37 "src/memc_common.rl"
	{
        dd("status set to 404");

        *status_addr = NGX_HTTP_NOT_FOUND;
    }
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
	goto st45;
st45:
	if ( ++p == pe )
		goto _test_eof45;
case 45:
#line 2468 "src/ngx_http_memc_response.c"
	goto st0;
st16:
	if ( ++p == pe )
		goto _test_eof16;
case 16:
	if ( (*p) == 69 )
		goto st17;
	goto st0;
st17:
	if ( ++p == pe )
		goto _test_eof17;
case 17:
	if ( (*p) == 76 )
		goto st18;
	goto st0;
st18:
	if ( ++p == pe )
		goto _test_eof18;
case 18:
	if ( (*p) == 69 )
		goto st19;
	goto st0;
st19:
	if ( ++p == pe )
		goto _test_eof19;
case 19:
	if ( (*p) == 84 )
		goto st20;
	goto st0;
st20:
	if ( ++p == pe )
		goto _test_eof20;
case 20:
	if ( (*p) == 69 )
		goto st21;
	goto st0;
st21:
	if ( ++p == pe )
		goto _test_eof21;
case 21:
	if ( (*p) == 68 )
		goto st22;
	goto st0;
st22:
	if ( ++p == pe )
		goto _test_eof22;
case 22:
	if ( (*p) == 13 )
		goto st23;
	goto st0;
st23:
	if ( ++p == pe )
		goto _test_eof23;
case 23:
	if ( (*p) == 10 )
		goto tr27;
	goto st0;
st24:
	if ( ++p == pe )
		goto _test_eof24;
case 24:
	if ( (*p) == 82 )
		goto st25;
	goto st0;
st25:
	if ( ++p == pe )
		goto _test_eof25;
case 25:
	if ( (*p) == 82 )
		goto st26;
	goto st0;
st26:
	if ( ++p == pe )
		goto _test_eof26;
case 26:
	if ( (*p) == 79 )
		goto st27;
	goto st0;
st27:
	if ( ++p == pe )
		goto _test_eof27;
case 27:
	if ( (*p) == 82 )
		goto st28;
	goto st0;
st28:
	if ( ++p == pe )
		goto _test_eof28;
case 28:
	if ( (*p) == 13 )
		goto st29;
	goto st0;
st29:
	if ( ++p == pe )
		goto _test_eof29;
case 29:
	if ( (*p) == 10 )
		goto tr19;
	goto st0;
st30:
	if ( ++p == pe )
		goto _test_eof30;
case 30:
	if ( (*p) == 79 )
		goto st31;
	goto st0;
st31:
	if ( ++p == pe )
		goto _test_eof31;
case 31:
	if ( (*p) == 84 )
		goto st32;
	goto st0;
st32:
	if ( ++p == pe )
		goto _test_eof32;
case 32:
	if ( (*p) == 95 )
		goto st33;
	goto st0;
st33:
	if ( ++p == pe )
		goto _test_eof33;
case 33:
	if ( (*p) == 70 )
		goto st34;
	goto st0;
st34:
	if ( ++p == pe )
		goto _test_eof34;
case 34:
	if ( (*p) == 79 )
		goto st35;
	goto st0;
st35:
	if ( ++p == pe )
		goto _test_eof35;
case 35:
	if ( (*p) == 85 )
		goto st36;
	goto st0;
st36:
	if ( ++p == pe )
		goto _test_eof36;
case 36:
	if ( (*p) == 78 )
		goto st37;
	goto st0;
st37:
	if ( ++p == pe )
		goto _test_eof37;
case 37:
	if ( (*p) == 68 )
		goto st38;
	goto st0;
st38:
	if ( ++p == pe )
		goto _test_eof38;
case 38:
	if ( (*p) == 13 )
		goto st39;
	goto st0;
st39:
	if ( ++p == pe )
		goto _test_eof39;
case 39:
	if ( (*p) == 10 )
		goto tr42;
	goto st0;
st40:
	if ( ++p == pe )
		goto _test_eof40;
case 40:
	if ( (*p) == 69 )
		goto st41;
	goto st0;
st41:
	if ( ++p == pe )
		goto _test_eof41;
case 41:
	if ( (*p) == 82 )
		goto st42;
	goto st0;
st42:
	if ( ++p == pe )
		goto _test_eof42;
case 42:
	if ( (*p) == 86 )
		goto st43;
	goto st0;
st43:
	if ( ++p == pe )
		goto _test_eof43;
case 43:
	if ( (*p) == 69 )
		goto st44;
	goto st0;
st44:
	if ( ++p == pe )
		goto _test_eof44;
case 44:
	if ( (*p) == 82 )
		goto st7;
	goto st0;
	}
	_test_eof2: cs = 2; goto _test_eof; 
	_test_eof3: cs = 3; goto _test_eof; 
	_test_eof4: cs = 4; goto _test_eof; 
	_test_eof5: cs = 5; goto _test_eof; 
	_test_eof6: cs = 6; goto _test_eof; 
	_test_eof7: cs = 7; goto _test_eof; 
	_test_eof8: cs = 8; goto _test_eof; 
	_test_eof9: cs = 9; goto _test_eof; 
	_test_eof10: cs = 10; goto _test_eof; 
	_test_eof11: cs = 11; goto _test_eof; 
	_test_eof12: cs = 12; goto _test_eof; 
	_test_eof13: cs = 13; goto _test_eof; 
	_test_eof14: cs = 14; goto _test_eof; 
	_test_eof15: cs = 15; goto _test_eof; 
	_test_eof45: cs = 45; goto _test_eof; 
	_test_eof16: cs = 16; goto _test_eof; 
	_test_eof17: cs = 17; goto _test_eof; 
	_test_eof18: cs = 18; goto _test_eof; 
	_test_eof19: cs = 19; goto _test_eof; 
	_test_eof20: cs = 20; goto _test_eof; 
	_test_eof21: cs = 21; goto _test_eof; 
	_test_eof22: cs = 22; goto _test_eof; 
	_test_eof23: cs = 23; goto _test_eof; 
	_test_eof24: cs = 24; goto _test_eof; 
	_test_eof25: cs = 25; goto _test_eof; 
	_test_eof26: cs = 26; goto _test_eof; 
	_test_eof27: cs = 27; goto _test_eof; 
	_test_eof28: cs = 28; goto _test_eof; 
	_test_eof29: cs = 29; goto _test_eof; 
	_test_eof30: cs = 30; goto _test_eof; 
	_test_eof31: cs = 31; goto _test_eof; 
	_test_eof32: cs = 32; goto _test_eof; 
	_test_eof33: cs = 33; goto _test_eof; 
	_test_eof34: cs = 34; goto _test_eof; 
	_test_eof35: cs = 35; goto _test_eof; 
	_test_eof36: cs = 36; goto _test_eof; 
	_test_eof37: cs = 37; goto _test_eof; 
	_test_eof38: cs = 38; goto _test_eof; 
	_test_eof39: cs = 39; goto _test_eof; 
	_test_eof40: cs = 40; goto _test_eof; 
	_test_eof41: cs = 41; goto _test_eof; 
	_test_eof42: cs = 42; goto _test_eof; 
	_test_eof43: cs = 43; goto _test_eof; 
	_test_eof44: cs = 44; goto _test_eof; 

	_test_eof: {}
	_out: {}
	}

#line 643 "src/ngx_http_memc_response.rl"

    *cs_addr = cs;

    return p;
}


static u_char *
parse_memc_incr_decr(int *cs_addr, u_char *p, u_char *pe,
    ngx_uint_t *status_addr, unsigned *done_addr)
{
    int cs = *cs_addr;

    
#line 657 "src/ngx_http_memc_response.rl"
    
#line 658 "src/ngx_http_memc_response.rl"
    
#line 2742 "src/ngx_http_memc_response.c"
	{
	if ( p == pe )
		goto _test_eof;
	switch ( cs )
	{
case 1:
	switch( (*p) ) {
		case 67: goto tr2;
		case 69: goto tr3;
		case 78: goto tr4;
		case 83: goto tr5;
	}
	if ( 48 <= (*p) && (*p) <= 57 )
		goto tr0;
	goto st0;
st0:
cs = 0;
	goto _out;
tr0:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st2;
st2:
	if ( ++p == pe )
		goto _test_eof2;
case 2:
#line 2772 "src/ngx_http_memc_response.c"
	switch( (*p) ) {
		case 13: goto tr6;
		case 32: goto tr7;
	}
	if ( 48 <= (*p) && (*p) <= 57 )
		goto tr0;
	goto st0;
tr6:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st3;
st3:
	if ( ++p == pe )
		goto _test_eof3;
case 3:
#line 2791 "src/ngx_http_memc_response.c"
	if ( (*p) == 10 )
		goto tr8;
	goto st0;
tr8:
#line 31 "src/memc_common.rl"
	{
        dd("status set to 201");

        *status_addr = NGX_HTTP_CREATED;
    }
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st40;
tr22:
#line 4 "src/memc_common.rl"
	{
        dd("caught error...");
        dd("machine state: %d", cs);

        *status_addr = NGX_HTTP_BAD_GATEWAY;
    }
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st40;
tr37:
#line 37 "src/memc_common.rl"
	{
        dd("status set to 404");

        *status_addr = NGX_HTTP_NOT_FOUND;
    }
#line 21 "src/memc_common.rl"
	{
        dd("done it!");
        *done_addr = 1;
    }
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st40;
st40:
	if ( ++p == pe )
		goto _test_eof40;
case 40:
#line 2854 "src/ngx_http_memc_response.c"
	goto st0;
tr7:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st4;
st4:
	if ( ++p == pe )
		goto _test_eof4;
case 4:
#line 2867 "src/ngx_http_memc_response.c"
	switch( (*p) ) {
		case 13: goto tr6;
		case 32: goto tr7;
	}
	goto st0;
tr2:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st5;
st5:
	if ( ++p == pe )
		goto _test_eof5;
case 5:
#line 2884 "src/ngx_http_memc_response.c"
	if ( (*p) == 76 )
		goto tr9;
	goto st0;
tr9:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st6;
st6:
	if ( ++p == pe )
		goto _test_eof6;
case 6:
#line 2899 "src/ngx_http_memc_response.c"
	if ( (*p) == 73 )
		goto tr10;
	goto st0;
tr10:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st7;
st7:
	if ( ++p == pe )
		goto _test_eof7;
case 7:
#line 2914 "src/ngx_http_memc_response.c"
	if ( (*p) == 69 )
		goto tr11;
	goto st0;
tr11:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st8;
st8:
	if ( ++p == pe )
		goto _test_eof8;
case 8:
#line 2929 "src/ngx_http_memc_response.c"
	if ( (*p) == 78 )
		goto tr12;
	goto st0;
tr12:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st9;
st9:
	if ( ++p == pe )
		goto _test_eof9;
case 9:
#line 2944 "src/ngx_http_memc_response.c"
	if ( (*p) == 84 )
		goto tr13;
	goto st0;
tr13:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st10;
st10:
	if ( ++p == pe )
		goto _test_eof10;
case 10:
#line 2959 "src/ngx_http_memc_response.c"
	if ( (*p) == 95 )
		goto tr14;
	goto st0;
tr14:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st11;
st11:
	if ( ++p == pe )
		goto _test_eof11;
case 11:
#line 2974 "src/ngx_http_memc_response.c"
	if ( (*p) == 69 )
		goto tr15;
	goto st0;
tr15:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st12;
st12:
	if ( ++p == pe )
		goto _test_eof12;
case 12:
#line 2989 "src/ngx_http_memc_response.c"
	if ( (*p) == 82 )
		goto tr16;
	goto st0;
tr16:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st13;
st13:
	if ( ++p == pe )
		goto _test_eof13;
case 13:
#line 3004 "src/ngx_http_memc_response.c"
	if ( (*p) == 82 )
		goto tr17;
	goto st0;
tr17:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st14;
st14:
	if ( ++p == pe )
		goto _test_eof14;
case 14:
#line 3019 "src/ngx_http_memc_response.c"
	if ( (*p) == 79 )
		goto tr18;
	goto st0;
tr18:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st15;
st15:
	if ( ++p == pe )
		goto _test_eof15;
case 15:
#line 3034 "src/ngx_http_memc_response.c"
	if ( (*p) == 82 )
		goto tr19;
	goto st0;
tr19:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st16;
st16:
	if ( ++p == pe )
		goto _test_eof16;
case 16:
#line 3049 "src/ngx_http_memc_response.c"
	if ( (*p) == 32 )
		goto tr20;
	goto st0;
tr20:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st17;
st17:
	if ( ++p == pe )
		goto _test_eof17;
case 17:
#line 3064 "src/ngx_http_memc_response.c"
	if ( (*p) == 13 )
		goto tr21;
	goto tr20;
tr21:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st18;
st18:
	if ( ++p == pe )
		goto _test_eof18;
case 18:
#line 3079 "src/ngx_http_memc_response.c"
	switch( (*p) ) {
		case 10: goto tr22;
		case 13: goto tr21;
	}
	goto tr20;
tr3:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st19;
st19:
	if ( ++p == pe )
		goto _test_eof19;
case 19:
#line 3096 "src/ngx_http_memc_response.c"
	if ( (*p) == 82 )
		goto tr23;
	goto st0;
tr23:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st20;
st20:
	if ( ++p == pe )
		goto _test_eof20;
case 20:
#line 3111 "src/ngx_http_memc_response.c"
	if ( (*p) == 82 )
		goto tr24;
	goto st0;
tr24:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st21;
st21:
	if ( ++p == pe )
		goto _test_eof21;
case 21:
#line 3126 "src/ngx_http_memc_response.c"
	if ( (*p) == 79 )
		goto tr25;
	goto st0;
tr25:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st22;
st22:
	if ( ++p == pe )
		goto _test_eof22;
case 22:
#line 3141 "src/ngx_http_memc_response.c"
	if ( (*p) == 82 )
		goto tr26;
	goto st0;
tr26:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st23;
st23:
	if ( ++p == pe )
		goto _test_eof23;
case 23:
#line 3156 "src/ngx_http_memc_response.c"
	if ( (*p) == 13 )
		goto tr27;
	goto st0;
tr27:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st24;
st24:
	if ( ++p == pe )
		goto _test_eof24;
case 24:
#line 3171 "src/ngx_http_memc_response.c"
	if ( (*p) == 10 )
		goto tr22;
	goto st0;
tr4:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st25;
st25:
	if ( ++p == pe )
		goto _test_eof25;
case 25:
#line 3186 "src/ngx_http_memc_response.c"
	if ( (*p) == 79 )
		goto tr28;
	goto st0;
tr28:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st26;
st26:
	if ( ++p == pe )
		goto _test_eof26;
case 26:
#line 3201 "src/ngx_http_memc_response.c"
	if ( (*p) == 84 )
		goto tr29;
	goto st0;
tr29:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st27;
st27:
	if ( ++p == pe )
		goto _test_eof27;
case 27:
#line 3216 "src/ngx_http_memc_response.c"
	if ( (*p) == 95 )
		goto tr30;
	goto st0;
tr30:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st28;
st28:
	if ( ++p == pe )
		goto _test_eof28;
case 28:
#line 3231 "src/ngx_http_memc_response.c"
	if ( (*p) == 70 )
		goto tr31;
	goto st0;
tr31:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st29;
st29:
	if ( ++p == pe )
		goto _test_eof29;
case 29:
#line 3246 "src/ngx_http_memc_response.c"
	if ( (*p) == 79 )
		goto tr32;
	goto st0;
tr32:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st30;
st30:
	if ( ++p == pe )
		goto _test_eof30;
case 30:
#line 3261 "src/ngx_http_memc_response.c"
	if ( (*p) == 85 )
		goto tr33;
	goto st0;
tr33:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st31;
st31:
	if ( ++p == pe )
		goto _test_eof31;
case 31:
#line 3276 "src/ngx_http_memc_response.c"
	if ( (*p) == 78 )
		goto tr34;
	goto st0;
tr34:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st32;
st32:
	if ( ++p == pe )
		goto _test_eof32;
case 32:
#line 3291 "src/ngx_http_memc_response.c"
	if ( (*p) == 68 )
		goto tr35;
	goto st0;
tr35:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st33;
st33:
	if ( ++p == pe )
		goto _test_eof33;
case 33:
#line 3306 "src/ngx_http_memc_response.c"
	if ( (*p) == 13 )
		goto tr36;
	goto st0;
tr36:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st34;
st34:
	if ( ++p == pe )
		goto _test_eof34;
case 34:
#line 3321 "src/ngx_http_memc_response.c"
	if ( (*p) == 10 )
		goto tr37;
	goto st0;
tr5:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st35;
st35:
	if ( ++p == pe )
		goto _test_eof35;
case 35:
#line 3336 "src/ngx_http_memc_response.c"
	if ( (*p) == 69 )
		goto tr38;
	goto st0;
tr38:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st36;
st36:
	if ( ++p == pe )
		goto _test_eof36;
case 36:
#line 3351 "src/ngx_http_memc_response.c"
	if ( (*p) == 82 )
		goto tr39;
	goto st0;
tr39:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st37;
st37:
	if ( ++p == pe )
		goto _test_eof37;
case 37:
#line 3366 "src/ngx_http_memc_response.c"
	if ( (*p) == 86 )
		goto tr40;
	goto st0;
tr40:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st38;
st38:
	if ( ++p == pe )
		goto _test_eof38;
case 38:
#line 3381 "src/ngx_http_memc_response.c"
	if ( (*p) == 69 )
		goto tr41;
	goto st0;
tr41:
#line 26 "src/memc_common.rl"
	{
        dd("state %d, left %d, reading char '%c'", cs,
        (int) (pe - p), *p);
    }
	goto st39;
st39:
	if ( ++p == pe )
		goto _test_eof39;
case 39:
#line 3396 "src/ngx_http_memc_response.c"
	if ( (*p) == 82 )
		goto tr13;
	goto st0;
	}
	_test_eof2: cs = 2; goto _test_eof; 
	_test_eof3: cs = 3; goto _test_eof; 
	_test_eof40: cs = 40; goto _test_eof; 
	_test_eof4: cs = 4; goto _test_eof; 
	_test_eof5: cs = 5; goto _test_eof; 
	_test_eof6: cs = 6; goto _test_eof; 
	_test_eof7: cs = 7; goto _test_eof; 
	_test_eof8: cs = 8; goto _test_eof; 
	_test_eof9: cs = 9; goto _test_eof; 
	_test_eof10: cs = 10; goto _test_eof; 
	_test_eof11: cs = 11; goto _test_eof; 
	_test_eof12: cs = 12; goto _test_eof; 
	_test_eof13: cs = 13; goto _test_eof; 
	_test_eof14: cs = 14; goto _test_eof; 
	_test_eof15: cs = 15; goto _test_eof; 
	_test_eof16: cs = 16; goto _test_eof; 
	_test_eof17: cs = 17; goto _test_eof; 
	_test_eof18: cs = 18; goto _test_eof; 
	_test_eof19: cs = 19; goto _test_eof; 
	_test_eof20: cs = 20; goto _test_eof; 
	_test_eof21: cs = 21; goto _test_eof; 
	_test_eof22: cs = 22; goto _test_eof; 
	_test_eof23: cs = 23; goto _test_eof; 
	_test_eof24: cs = 24; goto _test_eof; 
	_test_eof25: cs = 25; goto _test_eof; 
	_test_eof26: cs = 26; goto _test_eof; 
	_test_eof27: cs = 27; goto _test_eof; 
	_test_eof28: cs = 28; goto _test_eof; 
	_test_eof29: cs = 29; goto _test_eof; 
	_test_eof30: cs = 30; goto _test_eof; 
	_test_eof31: cs = 31; goto _test_eof; 
	_test_eof32: cs = 32; goto _test_eof; 
	_test_eof33: cs = 33; goto _test_eof; 
	_test_eof34: cs = 34; goto _test_eof; 
	_test_eof35: cs = 35; goto _test_eof; 
	_test_eof36: cs = 36; goto _test_eof; 
	_test_eof37: cs = 37; goto _test_eof; 
	_test_eof38: cs = 38; goto _test_eof; 
	_test_eof39: cs = 39; goto _test_eof; 

	_test_eof: {}
	_out: {}
	}

#line 659 "src/ngx_http_memc_response.rl"

    *cs_addr = cs;

    return p;
}
