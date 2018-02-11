
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_srcache_store.h"
#include "ngx_http_srcache_fetch.h"
#include "ngx_http_srcache_util.h"
#include <assert.h>


static ngx_int_t ngx_http_srcache_store_post_subrequest(ngx_http_request_t *r,
    void *data, ngx_int_t rc);
static ngx_int_t ngx_http_srcache_store_subrequest(ngx_http_request_t *r,
        ngx_http_srcache_ctx_t *ctx);
static ngx_int_t ngx_http_srcache_header_filter(ngx_http_request_t *r);
static ngx_int_t ngx_http_srcache_body_filter(ngx_http_request_t *r,
        ngx_chain_t *in);


ngx_http_output_header_filter_pt  ngx_http_srcache_next_header_filter;
ngx_http_output_body_filter_pt    ngx_http_srcache_next_body_filter;


static ngx_int_t
ngx_http_srcache_header_filter(ngx_http_request_t *r)
{
    ngx_http_srcache_ctx_t          *ctx, *pr_ctx;
    ngx_http_srcache_loc_conf_t     *slcf;
    ngx_http_post_subrequest_t      *ps;
    ngx_str_t                        skip;
    ngx_uint_t                      *sp; /* status pointer */

    dd_enter();
    dd("srcache header filter");

    ctx = ngx_http_get_module_ctx(r, ngx_http_srcache_filter_module);

    if (r != r->main && ctx == NULL) {
        ps = r->post_subrequest;
        if (ps != NULL
            && (ps->handler == ngx_http_srcache_fetch_post_subrequest
                || ps->handler == ngx_http_srcache_store_post_subrequest)
            && ps->data != NULL)
        {
            /* the subrequest ctx has been cleared by
             * ngx_http_internal_redirect, resume it from the post_subrequest
             * data
             */
            dd("resumed ctx from post_subrequest");
            ctx = ps->data;
            ngx_http_set_ctx(r, ctx, ngx_http_srcache_filter_module);
        }
    }

    if (ctx == NULL || ctx->from_cache) {
        dd("bypass: %.*s", (int) r->uri.len, r->uri.data);
        return ngx_http_srcache_next_header_filter(r);
    }

    if (ctx->in_fetch_subrequest) {
        dd("in fetch subrequest");

        pr_ctx = ngx_http_get_module_ctx(r->parent,
                                         ngx_http_srcache_filter_module);

        if (pr_ctx == NULL) {
            dd("parent ctx is null");

            ctx->ignore_body = 1;

            return NGX_ERROR;
        }

        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "srcache_fetch: subrequest returned status %ui",
                       r->headers_out.status);

        if (r->headers_out.status != NGX_HTTP_OK) {
            dd("ignoring body because status == %d",
               (int) r->headers_out.status);

            ctx->ignore_body = 1;

            pr_ctx->waiting_subrequest = 0;
            /* pr_ctx->fetch_error = 1; */
            r->header_sent = 1;

            if (r->method == NGX_HTTP_HEAD) {
                r->header_only = 1;
            }

            return NGX_OK;
        }

        ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "srcache_fetch decides to send the response in cache");

        r->filter_need_in_memory = 1;
        pr_ctx->from_cache = 1;
        ctx->parsing_cached_headers = 1;
        r->header_sent = 1;

        if (r->method == NGX_HTTP_HEAD) {
            r->header_only = 1;
        }

        return NGX_OK;
    }

    if (ctx->in_store_subrequest) {
        dd("in store subreuqest");
        ctx->ignore_body = 1;

        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "srcache_store: subrequest returned status %ui",
                       r->headers_out.status);

        r->header_sent = 1;

        if (r->method == NGX_HTTP_HEAD) {
            r->header_only = 1;
        }

        return NGX_OK;
    }

    slcf = ngx_http_get_module_loc_conf(r, ngx_http_srcache_filter_module);

    if (slcf->store == NULL) {
        dd("slcf->store is NULL");
        return ngx_http_srcache_next_header_filter(r);
    }

    /* slcf->store != NULL */

#if 1
    if (!(r->method & (slcf->cache_methods & ~NGX_HTTP_HEAD))) {
        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "srcache_store skipped due to request method %V",
                       &r->method_name);

        return ngx_http_srcache_next_header_filter(r);
    }
#endif

    if (!slcf->ignore_content_encoding
        && r->headers_out.content_encoding
        && r->headers_out.content_encoding->value.len)
    {
        ngx_log_error(NGX_LOG_WARN, r->connection->log, 0,
                      "srcache_store skipped due to response header "
                      "\"Content-Encoding: %V\" (maybe you forgot to disable "
                      "compression on the backend?)",
                      &r->headers_out.content_encoding->value);

        return ngx_http_srcache_next_header_filter(r);
    }

    if (slcf->resp_cache_control
        && ngx_http_srcache_response_no_cache(r, slcf, ctx) == NGX_OK)
    {
        ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "srcache_store skipped due to response header "
                       "Cache-Control");

        return ngx_http_srcache_next_header_filter(r);
    }

    if (slcf->store_skip != NULL
        && ngx_http_complex_value(r, slcf->store_skip, &skip) == NGX_OK
        && skip.len
        && (skip.len != 1 || skip.data[0] != '0'))
    {
        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "srcache_store skipped due to the true value fed into "
                       "srcache_store_skip: \"%V\"", &skip);

        ctx->store_skip = 1;

        return ngx_http_srcache_next_header_filter(r);
    }

    dd("error page: %d", (int) r->error_page);

    if (slcf->store_statuses != NULL) {

        sp = (ngx_uint_t *) slcf->store_statuses;

        while (r->headers_out.status < *sp) {
            sp++;
        }

        if (*sp == 0 || r->headers_out.status > *sp) {
            ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                           "srcache_store bypassed because of unmatched "
                           "status code %ui with srcache_store_statuses",
                           r->headers_out.status);

            return ngx_http_srcache_next_header_filter(r);
        }

        /* r->headers_out.status == (ngx_uint_t) *sp */

    } else {

        if (r->headers_out.status != NGX_HTTP_OK
            && r->headers_out.status != NGX_HTTP_MOVED_TEMPORARILY
            && r->headers_out.status != NGX_HTTP_MOVED_PERMANENTLY)
        {
            ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                           "srcache_store bypassed because of unmatched status "
                           "code %i (only 200, 301, or 302 are accepted by "
                           "default)", r->headers_out.status);

            return ngx_http_srcache_next_header_filter(r);
        }
    }

    if (slcf->store_max_size != 0
        && r->headers_out.content_length_n > 0
        && r->headers_out.content_length_n + 15
           /* just an approxiation for the response header size */
           > (off_t) slcf->store_max_size)
    {
        ngx_log_debug2(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "srcache_store bypassed because of too large "
                       "Content-Length response header: %O (limit is: %z)",
                       r->headers_out.content_length_n, slcf->store_max_size);

        return ngx_http_srcache_next_header_filter(r);
    }

    dd("try to save the response header");

    if (r != r->main) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "ngx_srcache not working in subrequests (yet)");

        /* not allowd in subrquests */
        return NGX_ERROR;
    }

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "srcache_store decides to store the response");

    r->filter_need_in_memory = 1;

    ctx->http_status = r->headers_out.status;
    ctx->store_response = 1;

    if (r->method == NGX_HTTP_HEAD) {
        r->header_only = 1;
    }

    /* store the response header to ctx->body_to_cache */
    if (ngx_http_srcache_store_response_header(r, ctx) == NGX_ERROR) {
        return NGX_ERROR;
    }

    return ngx_http_srcache_next_header_filter(r);
}


static ngx_int_t
ngx_http_srcache_body_filter(ngx_http_request_t *r, ngx_chain_t *in)
{
    ngx_http_srcache_ctx_t      *ctx, *pr_ctx;
    ngx_int_t                    rc;
    ngx_str_t                    skip;
    ngx_chain_t                 *cl;
    ngx_http_srcache_loc_conf_t *slcf;
    size_t                       len;
    unsigned                     last;

    dd_enter();

    if (in == NULL) {
        return ngx_http_srcache_next_body_filter(r, NULL);
    }

    ctx = ngx_http_get_module_ctx(r, ngx_http_srcache_filter_module);

    if (ctx == NULL || ctx->from_cache || ctx->store_skip) {
        dd("bypass: %.*s", (int) r->uri.len, r->uri.data);
        return ngx_http_srcache_next_body_filter(r, in);
    }

    if (ctx->ignore_body || ctx->in_store_subrequest/* || ctx->fetch_error */) {
        dd("ignore body: ignore body %d, in store sr %d",
           (int) ctx->ignore_body, (int) ctx->in_store_subrequest);
        ngx_http_srcache_discard_bufs(r->pool, in);
        return NGX_OK;
    }

    if (ctx->in_fetch_subrequest) {
        if (ctx->parsing_cached_headers) {

            /* parse the cached response's headers and
             * set r->parent->headers_out */

            if (ctx->process_header == NULL) {
                dd("restore parent request header");
                ctx->process_header = ngx_http_srcache_process_status_line;
                r->state = 0; /* sw_start */
            }

            for (cl = in; cl; cl = cl->next) {
                if (ngx_buf_in_memory(cl->buf)) {
                    dd("old pos %p, last %p", cl->buf->pos, cl->buf->last);

                    rc = ctx->process_header(r, cl->buf);

                    if (rc == NGX_AGAIN) {
                        dd("AGAIN/OK: new pos %p, last %p",
                           cl->buf->pos, cl->buf->last);

                        continue;
                    }

                    if (rc == NGX_ERROR) {
                        r->state = 0; /* sw_start */
                        ctx->parsing_cached_headers = 0;
                        ctx->ignore_body = 1;
                        ngx_http_srcache_discard_bufs(r->pool, cl);
                        pr_ctx = ngx_http_get_module_ctx(r->parent,
                                              ngx_http_srcache_filter_module);

                        if (pr_ctx == NULL) {
                            return NGX_ERROR;
                        }

                        pr_ctx->from_cache = 0;

                        return NGX_OK;
                    }

                    /* rc == NGX_OK */

                    dd("OK: new pos %p, last %p", cl->buf->pos, cl->buf->last);
                    dd("buf left: %.*s", (int) (cl->buf->last - cl->buf->pos),
                       cl->buf->pos);

                    ctx->parsing_cached_headers = 0;

                    break;
                }
            }

            if (cl == NULL) {
                return NGX_OK;
            }

            if (cl->buf->pos == cl->buf->last) {
                cl = cl->next;
            }

            if (cl == NULL) {
                return NGX_OK;
            }

            in = cl;
        }

        dd("save the cached response body for parent");

        pr_ctx = ngx_http_get_module_ctx(r->parent,
                                         ngx_http_srcache_filter_module);

        if (pr_ctx == NULL) {
            return NGX_ERROR;
        }

        rc = ngx_http_srcache_add_copy_chain(r->pool,
                                             &pr_ctx->body_from_cache, in,
                                             &last);

        if (rc != NGX_OK) {
            return NGX_ERROR;
        }

        if (last) {
            ctx->seen_subreq_eof = 1;
        }

        ngx_http_srcache_discard_bufs(r->pool, in);

        return NGX_OK;
    }

    if (ctx->store_response) {
        dd("storing the response: %p", in);

        slcf = ngx_http_get_module_loc_conf(r, ngx_http_srcache_filter_module);

        if (r->headers_out.status == NGX_HTTP_PARTIAL_CONTENT
            && ctx->http_status == NGX_HTTP_OK)
        {
            u_char *p;

            if (!slcf->store_ranges) {
                ctx->store_response = 0;
                goto done;
            }

            dd("fix 206 status code");

            /* handle 206 Partial Content generated by the range filter */
            cl = ctx->body_to_cache;
            assert(cl && cl->buf && cl->buf->last - cl->buf->pos > 12);
            p = cl->buf->pos + sizeof("HTTP/1.x 20") - 1;
            *p = '6';

            ctx->http_status = NGX_HTTP_PARTIAL_CONTENT;
        }

        for (cl = in; cl; cl = cl->next) {
            if (ngx_buf_in_memory(cl->buf)) {
                len = ngx_buf_size(cl->buf);
                ctx->response_length += len;
                ctx->response_body_length += len;
            }
        }

        if (slcf->store_max_size != 0
            && ctx->response_length > slcf->store_max_size)
        {
            ngx_log_debug2(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                           "srcache_store bypassed because response body "
                           "exceeded maximum size: %z (limit is: %z)",
                           ctx->response_length, slcf->store_max_size);

            ctx->store_response = 0;

            goto done;
        }

        rc = ngx_http_srcache_add_copy_chain(r->pool, &ctx->body_to_cache,
                                             in, &last);

        if (rc != NGX_OK) {
            ctx->store_response = 0;
            goto done;
        }

        if (last && r == r->main) {

#if 1
            if (r->headers_out.content_length_n >
                (off_t) ctx->response_body_length)
            {
                ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                              "srcache_store: skipped because response body "
                              "truncated: %O > %uz",
                              r->headers_out.content_length_n,
                              ctx->response_body_length);

                ctx->store_response = 0;
                goto done;
            }

            if (r->headers_out.status >= NGX_HTTP_SPECIAL_RESPONSE
                && r->headers_out.status != ctx->http_status)
            {
                /* data truncation or body receive timeout */

                ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                              "srcache_store: skipped due to new error status "
                              "code %ui (old: %ui)",
                              r->headers_out.status, ctx->http_status);

                ctx->store_response = 0;
                goto done;
            }
#endif

            if (slcf->store_skip != NULL
                && ngx_http_complex_value(r, slcf->store_skip, &skip) == NGX_OK
                && skip.len
                && (skip.len != 1 || skip.data[0] != '0'))
            {
                ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                               "srcache_store skipped due to the true value in "
                               "srcache_store_skip: \"%V\"", &skip);

                ctx->store_response = 0;
                goto done;
            }

            rc = ngx_http_srcache_store_subrequest(r, ctx);

            if (rc != NGX_OK) {
                ctx->store_response = 0;
                goto done;
            }
        }

    } else {
        dd("NO store response");
    }

done:
    return ngx_http_srcache_next_body_filter(r, in);
}




static ngx_int_t
ngx_http_srcache_store_subrequest(ngx_http_request_t *r,
    ngx_http_srcache_ctx_t *ctx)
{
    ngx_http_srcache_ctx_t         *sr_ctx;
    ngx_str_t                       args;
    ngx_uint_t                      flags = 0;
    ngx_http_request_t             *sr;
    ngx_int_t                       rc;
    ngx_http_request_body_t        *rb = NULL;
    ngx_http_srcache_loc_conf_t    *conf;
    ngx_http_post_subrequest_t     *psr;

    ngx_http_srcache_parsed_request_t  *parsed_sr;

    dd("store subrequest");

    conf = ngx_http_get_module_loc_conf(r, ngx_http_srcache_filter_module);

    if (conf->store == NULL) {
        dd("conf store is NULL");
        return NGX_ERROR;
    }

    parsed_sr = ngx_palloc(r->pool, sizeof(ngx_http_srcache_parsed_request_t));
    if (parsed_sr == NULL) {
        return NGX_ERROR;
    }

    parsed_sr->method      = conf->store->method;
    parsed_sr->method_name = conf->store->method_name;

    if (ctx->body_to_cache) {
        dd("found body to cache (len %d)", (int) ctx->response_length);

        rb = ngx_pcalloc(r->pool, sizeof(ngx_http_request_body_t));

        if (rb == NULL) {
            return NGX_ERROR;
        }

        rb->bufs = ctx->body_to_cache;
        rb->buf = ctx->body_to_cache->buf;

        parsed_sr->request_body = rb;

    } else {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "srcache_store: no request body for the subrequest");

        return NGX_ERROR;
    }

    parsed_sr->content_length_n = ctx->response_length;

    if (ngx_http_complex_value(r, &conf->store->location,
                               &parsed_sr->location) != NGX_OK)
    {
        return NGX_ERROR;
    }

    if (parsed_sr->location.len == 0) {
        return NGX_ERROR;
    }

    if (ngx_http_complex_value(r, &conf->store->args, &parsed_sr->args)
        != NGX_OK)
    {
        return NGX_ERROR;
    }

    args.data = NULL;
    args.len = 0;

    if (ngx_http_parse_unsafe_uri(r, &parsed_sr->location, &args, &flags)
        != NGX_OK)
    {
        return NGX_ERROR;
    }

    if (args.len > 0 && parsed_sr->args.len == 0) {
        parsed_sr->args = args;
    }

    dd("firing the store subrequest");

    dd("store location: %.*s", (int) parsed_sr->location.len,
            parsed_sr->location.data);

    dd("store args: %.*s", (int) parsed_sr->args.len,
       parsed_sr->args.data);

    sr_ctx = ngx_pcalloc(r->pool, sizeof(ngx_http_srcache_ctx_t));

    if (sr_ctx == NULL) {
        return NGX_ERROR;
    }

    sr_ctx->in_store_subrequest = 1;

    psr = ngx_palloc(r->pool, sizeof(ngx_http_post_subrequest_t));
    if (psr == NULL) {
        return NGX_ERROR;
    }

    psr->handler = ngx_http_srcache_store_post_subrequest;
    psr->data = sr_ctx;

    rc = ngx_http_subrequest(r, &parsed_sr->location, &parsed_sr->args,
                             &sr, psr, flags);

    if (rc != NGX_OK) {
        return NGX_ERROR;
    }

    rc = ngx_http_srcache_adjust_subrequest(sr, parsed_sr);

    if (rc != NGX_OK) {
        return NGX_ERROR;
    }

    ngx_http_set_ctx(sr, sr_ctx, ngx_http_srcache_filter_module);

    return NGX_OK;
}


static ngx_int_t
ngx_http_srcache_store_post_subrequest(ngx_http_request_t *r, void *data,
    ngx_int_t rc)
{
    if (rc == NGX_ERROR
        || rc >= NGX_HTTP_SPECIAL_RESPONSE
        || r->headers_out.status >= NGX_HTTP_SPECIAL_RESPONSE)
    {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "srcache_store subrequest failed: rc=%i status=%ui",
                      rc, r->headers_out.status);
    }

    return NGX_OK;
}


ngx_int_t
ngx_http_srcache_filter_init(ngx_conf_t *cf)
{
    ngx_http_srcache_next_header_filter = ngx_http_top_header_filter;
    ngx_http_top_header_filter = ngx_http_srcache_header_filter;

    ngx_http_srcache_next_body_filter = ngx_http_top_body_filter;
    ngx_http_top_body_filter = ngx_http_srcache_body_filter;

    return NGX_OK;
}

/* vi:set ft=c ts=4 sw=4 et fdm=marker: */
