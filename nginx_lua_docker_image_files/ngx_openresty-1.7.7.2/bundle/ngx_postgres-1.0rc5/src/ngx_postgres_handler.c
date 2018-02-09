/*
 * Copyright (c) 2010, FRiCKLE Piotr Sikora <info@frickle.com>
 * Copyright (c) 2009-2010, Xiaozhe Wang <chaoslawful@gmail.com>
 * Copyright (c) 2009-2010, Yichun Zhang <agentzh@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#ifndef DDEBUG
#define DDEBUG 0
#endif

#include "ngx_postgres_ddebug.h"
#include "ngx_postgres_handler.h"
#include "ngx_postgres_module.h"
#include "ngx_postgres_output.h"
#include "ngx_postgres_processor.h"
#include "ngx_postgres_util.h"


ngx_int_t
ngx_postgres_handler(ngx_http_request_t *r)
{
    ngx_postgres_loc_conf_t   *pglcf;
    ngx_postgres_ctx_t        *pgctx;
    ngx_http_core_loc_conf_t  *clcf;
    ngx_http_upstream_t       *u;
    ngx_connection_t          *c;
    ngx_str_t                  host;
    ngx_url_t                  url;
    ngx_int_t                  rc;

    dd("entering");

    if (r->subrequest_in_memory) {
        /* TODO: add support for subrequest in memory by
         * emitting output into u->buffer instead */

        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "postgres: ngx_postgres module does not support"
                      " subrequests in memory");

        dd("returning NGX_HTTP_INTERNAL_SERVER_ERROR");
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    pglcf = ngx_http_get_module_loc_conf(r, ngx_postgres_module);

    if ((pglcf->query.def == NULL) && !(pglcf->query.methods_set & r->method)) {
        if (pglcf->query.methods_set != 0) {
            dd("returning NGX_HTTP_NOT_ALLOWED");
            return NGX_HTTP_NOT_ALLOWED;
        }

        clcf = ngx_http_get_module_loc_conf(r, ngx_http_core_module);

        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "postgres: missing \"postgres_query\" in location \"%V\"",
                      &clcf->name);

        dd("returning NGX_HTTP_INTERNAL_SERVER_ERROR");
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    rc = ngx_http_discard_request_body(r);
    if (rc != NGX_OK) {
        dd("returning rc:%d", (int) rc);
        return rc;
    }

#if defined(nginx_version) \
    && (((nginx_version >= 7063) && (nginx_version < 8000)) \
        || (nginx_version >= 8007))

    if (ngx_http_upstream_create(r) != NGX_OK) {
        dd("returning NGX_HTTP_INTERNAL_SERVER_ERROR");
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    u = r->upstream;

#else /* 0.7.x < 0.7.63, 0.8.x < 0.8.7 */

    u = ngx_pcalloc(r->pool, sizeof(ngx_http_upstream_t));
    if (u == NULL) {
        dd("returning NGX_HTTP_INTERNAL_SERVER_ERROR");
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    u->peer.log = r->connection->log;
    u->peer.log_error = NGX_ERROR_ERR;
#  if (NGX_THREADS)
    u->peer.lock = &r->connection->lock;
#  endif
    r->upstream = u;
#endif

    if (pglcf->upstream_cv) {
        /* use complex value */
        if (ngx_http_complex_value(r, pglcf->upstream_cv, &host) != NGX_OK) {
            dd("returning NGX_HTTP_INTERNAL_SERVER_ERROR");
            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }

        if (host.len == 0) {
            clcf = ngx_http_get_module_loc_conf(r, ngx_http_core_module);

            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "postgres: empty \"postgres_pass\" (was: \"%V\")"
                          " in location \"%V\"", &pglcf->upstream_cv->value,
                          &clcf->name);

            dd("returning NGX_HTTP_INTERNAL_SERVER_ERROR");
            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }

        ngx_memzero(&url, sizeof(ngx_url_t));

        url.host = host;
        url.no_resolve = 1;

        pglcf->upstream.upstream = ngx_postgres_find_upstream(r, &url);
        if (pglcf->upstream.upstream == NULL) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "postgres: upstream name \"%V\" not found", &host);

            dd("returning NGX_ERROR");
            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }
    }

    pgctx = ngx_pcalloc(r->pool, sizeof(ngx_postgres_ctx_t));
    if (pgctx == NULL) {
        dd("returning NGX_HTTP_INTERNAL_SERVER_ERROR");
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    /*
     * set by ngx_pcalloc():
     *
     *     pgctx->response = NULL
     *     pgctx->var_query = { 0, NULL }
     *     pgctx->variables = NULL
     *     pgctx->status = 0
     */

    pgctx->var_cols = NGX_ERROR;
    pgctx->var_rows = NGX_ERROR;
    pgctx->var_affected = NGX_ERROR;

    if (pglcf->variables != NULL) {
        pgctx->variables = ngx_array_create(r->pool, pglcf->variables->nelts,
                                            sizeof(ngx_str_t));
        if (pgctx->variables == NULL) {
            dd("returning NGX_HTTP_INTERNAL_SERVER_ERROR");
            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }

        /* fake ngx_array_push'ing */
        pgctx->variables->nelts = pglcf->variables->nelts;

        ngx_memzero(pgctx->variables->elts,
                    pgctx->variables->nelts * pgctx->variables->size);
    }

    ngx_http_set_ctx(r, pgctx, ngx_postgres_module);

    u->schema.len = sizeof("postgres://") - 1;
    u->schema.data = (u_char *) "postgres://";

    u->output.tag = (ngx_buf_tag_t) &ngx_postgres_module;

    u->conf = &pglcf->upstream;

    u->create_request = ngx_postgres_create_request;
    u->reinit_request = ngx_postgres_reinit_request;
    u->process_header = ngx_postgres_process_header;
    u->abort_request = ngx_postgres_abort_request;
    u->finalize_request = ngx_postgres_finalize_request;

    /* we bypass the upstream input filter mechanism in
     * ngx_http_upstream_process_headers */

    u->input_filter_init = ngx_postgres_input_filter_init;
    u->input_filter = ngx_postgres_input_filter;
    u->input_filter_ctx = NULL;

#if defined(nginx_version) && (nginx_version >= 8011)
    r->main->count++;
#endif

    ngx_http_upstream_init(r);

    /* override the read/write event handler to our own */
    u->write_event_handler = ngx_postgres_wev_handler;
    u->read_event_handler = ngx_postgres_rev_handler;

    /* a bit hack-ish way to return error response (clean-up part) */
    if ((u->peer.connection) && (u->peer.connection->fd == 0)) {
        c = u->peer.connection;
        u->peer.connection = NULL;

        if (c->write->timer_set) {
            ngx_del_timer(c->write);
        }

#if defined(nginx_version) && (nginx_version >= 1001004)
        if (c->pool) {
            ngx_destroy_pool(c->pool);
        }
#endif

        ngx_free_connection(c);

        ngx_postgres_upstream_finalize_request(r, u,
#if defined(nginx_version) && (nginx_version >= 8017)
                                               NGX_HTTP_SERVICE_UNAVAILABLE);
#else
            pgctx->status ? pgctx->status : NGX_HTTP_INTERNAL_SERVER_ERROR);
#endif
    }

    dd("returning NGX_DONE");
    return NGX_DONE;
}

void
ngx_postgres_wev_handler(ngx_http_request_t *r, ngx_http_upstream_t *u)
{
    ngx_connection_t  *pgxc;

    dd("entering");

    /* just to ensure u->reinit_request always gets called for
     * upstream_next */
    u->request_sent = 1;

    pgxc = u->peer.connection;

    if (pgxc->write->timedout) {
        dd("postgres connection write timeout");

        ngx_postgres_upstream_next(r, u, NGX_HTTP_UPSTREAM_FT_TIMEOUT);

        dd("returning");
        return;
    }

    if (ngx_postgres_upstream_test_connect(pgxc) != NGX_OK) {
        dd("postgres connection is broken");

        ngx_postgres_upstream_next(r, u, NGX_HTTP_UPSTREAM_FT_ERROR);

        dd("returning");
        return;
    }

    ngx_postgres_process_events(r);

    dd("returning");
}

void
ngx_postgres_rev_handler(ngx_http_request_t *r, ngx_http_upstream_t *u)
{
    ngx_connection_t  *pgxc;

    dd("entering");

    /* just to ensure u->reinit_request always gets called for
     * upstream_next */
    u->request_sent = 1;

    pgxc = u->peer.connection;

    if (pgxc->read->timedout) {
        dd("postgres connection read timeout");

        ngx_postgres_upstream_next(r, u, NGX_HTTP_UPSTREAM_FT_TIMEOUT);

        dd("returning");
        return;
    }

    if (ngx_postgres_upstream_test_connect(pgxc) != NGX_OK) {
        dd("postgres connection is broken");

        ngx_postgres_upstream_next(r, u, NGX_HTTP_UPSTREAM_FT_ERROR);

        dd("returning");
        return;
    }

    ngx_postgres_process_events(r);

    dd("returning");
}

ngx_int_t
ngx_postgres_create_request(ngx_http_request_t *r)
{
    dd("entering");

    r->upstream->request_bufs = NULL;

    dd("returning NGX_OK");
    return NGX_OK;
}

ngx_int_t
ngx_postgres_reinit_request(ngx_http_request_t *r)
{
    ngx_http_upstream_t  *u;

    dd("entering");

    u = r->upstream;

    /* override the read/write event handler to our own */
    u->write_event_handler = ngx_postgres_wev_handler;
    u->read_event_handler = ngx_postgres_rev_handler;

    dd("returning NGX_OK");
    return NGX_OK;
}

void
ngx_postgres_abort_request(ngx_http_request_t *r)
{
    dd("entering & returning (dummy function)");
}

void
ngx_postgres_finalize_request(ngx_http_request_t *r, ngx_int_t rc)
{
    ngx_postgres_ctx_t  *pgctx;

    dd("entering");

    if (rc == NGX_OK) {
        pgctx = ngx_http_get_module_ctx(r, ngx_postgres_module);

        ngx_postgres_output_chain(r, pgctx->response);
    }

    dd("returning");
}

ngx_int_t
ngx_postgres_process_header(ngx_http_request_t *r)
{
    dd("entering");

    ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                  "postgres: ngx_postgres_process_header should not"
                  " be called by the upstream");

    dd("returning NGX_ERROR");
    return NGX_ERROR;
}

ngx_int_t
ngx_postgres_input_filter_init(void *data)
{
    ngx_http_request_t  *r = data;

    dd("entering");

    ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                  "postgres: ngx_postgres_input_filter_init should not"
                  " be called by the upstream");

    dd("returning NGX_ERROR");
    return NGX_ERROR;
}

ngx_int_t
ngx_postgres_input_filter(void *data, ssize_t bytes)
{
    ngx_http_request_t  *r = data;

    dd("entering");

    ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                  "postgres: ngx_postgres_input_filter should not"
                  " be called by the upstream");

    dd("returning NGX_ERROR");
    return NGX_ERROR;
}
