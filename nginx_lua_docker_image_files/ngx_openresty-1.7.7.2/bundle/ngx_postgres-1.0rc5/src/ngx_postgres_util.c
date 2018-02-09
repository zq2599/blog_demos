/*
 * Copyright (c) 2010, FRiCKLE Piotr Sikora <info@frickle.com>
 * Copyright (c) 2009-2010, Yichun Zhang <agentzh@gmail.com>
 * Copyright (C) 2002-2010, Igor Sysoev
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
 * THIS SOFTWARE IS PROVIDED BY AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

#ifndef DDEBUG
#define DDEBUG 0
#endif

#include <nginx.h>

#include "ngx_postgres_ddebug.h"
#include "ngx_postgres_util.h"


/*
 * All functions in this file are copied directly from ngx_http_upstream.c,
 * beacuse they are declared as static there.
 */


void
ngx_postgres_upstream_finalize_request(ngx_http_request_t *r,
    ngx_http_upstream_t *u, ngx_int_t rc)
{
    ngx_time_t  *tp;

    dd("entering");

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "finalize http upstream request: %i", rc);

    if (u->cleanup) {
        *u->cleanup = NULL;
    }

    if (u->resolved && u->resolved->ctx) {
        ngx_resolve_name_done(u->resolved->ctx);
        u->resolved->ctx = NULL;
    }

    if (u->state && u->state->response_sec) {
        tp = ngx_timeofday();
        u->state->response_sec = tp->sec - u->state->response_sec;
        u->state->response_msec = tp->msec - u->state->response_msec;

        if (u->pipe) {
            u->state->response_length = u->pipe->read_length;
        }
    }

    if (u->finalize_request) {
        u->finalize_request(r, rc);
    }

    if (u->peer.free) {
        u->peer.free(&u->peer, u->peer.data, 0);
    }

    if (u->peer.connection) {

#if 0 /* we don't support SSL at this time, was: (NGX_HTTP_SSL) */

        /* TODO: do not shutdown persistent connection */

        if (u->peer.connection->ssl) {

            /*
             * We send the "close notify" shutdown alert to the upstream only
             * and do not wait its "close notify" shutdown alert.
             * It is acceptable according to the TLS standard.
             */

            u->peer.connection->ssl->no_wait_shutdown = 1;

            (void) ngx_ssl_shutdown(u->peer.connection);
        }
#endif

        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "close http upstream connection: %d",
                       u->peer.connection->fd);

#if defined(nginx_version) && (nginx_version >= 1001004)
        if (u->peer.connection->pool) {
            ngx_destroy_pool(u->peer.connection->pool);
        }
#endif

        ngx_close_connection(u->peer.connection);
    }

    u->peer.connection = NULL;

    if (u->pipe && u->pipe->temp_file) {
        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "http upstream temp fd: %d",
                       u->pipe->temp_file->file.fd);
    }

    if (u->header_sent
        && (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE))
    {
        rc = 0;
    }

    if (rc == NGX_DECLINED) {
        dd("returning");
        return;
    }

    r->connection->log->action = "sending to client";

    if (rc == 0) {
        rc = ngx_http_send_special(r, NGX_HTTP_LAST);
    }

    ngx_http_finalize_request(r, rc);

    dd("returning");
}

void
ngx_postgres_upstream_next(ngx_http_request_t *r,
    ngx_http_upstream_t *u, ngx_int_t ft_type)
{
    ngx_uint_t  status, state;

    dd("entering");

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "http next upstream, %xi", ft_type);

#if 0
    ngx_http_busy_unlock(u->conf->busy_lock, &u->busy_lock);
#endif

    if (ft_type == NGX_HTTP_UPSTREAM_FT_HTTP_404) {
        state = NGX_PEER_NEXT;
    } else {
        state = NGX_PEER_FAILED;
    }

    if (ft_type != NGX_HTTP_UPSTREAM_FT_NOLIVE) {
        u->peer.free(&u->peer, u->peer.data, state);
    }

    if (ft_type == NGX_HTTP_UPSTREAM_FT_TIMEOUT) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, NGX_ETIMEDOUT,
                      "upstream timed out");
    }

    if (u->peer.cached && ft_type == NGX_HTTP_UPSTREAM_FT_ERROR) {
        status = 0;

    } else {
        switch(ft_type) {

        case NGX_HTTP_UPSTREAM_FT_TIMEOUT:
            status = NGX_HTTP_GATEWAY_TIME_OUT;
            break;

        case NGX_HTTP_UPSTREAM_FT_HTTP_500:
            status = NGX_HTTP_INTERNAL_SERVER_ERROR;
            break;

        case NGX_HTTP_UPSTREAM_FT_HTTP_404:
            status = NGX_HTTP_NOT_FOUND;
            break;

        /*
         * NGX_HTTP_UPSTREAM_FT_BUSY_LOCK and NGX_HTTP_UPSTREAM_FT_MAX_WAITING
         * never reach here
         */

        default:
            status = NGX_HTTP_BAD_GATEWAY;
        }
    }

    if (r->connection->error) {
        ngx_postgres_upstream_finalize_request(r, u,
                                               NGX_HTTP_CLIENT_CLOSED_REQUEST);

        dd("returning");
        return;
    }

    if (status) {
        u->state->status = status;

        if (u->peer.tries == 0 || !(u->conf->next_upstream & ft_type)) {
            ngx_postgres_upstream_finalize_request(r, u, status);

            dd("returning");
            return;
        }
    }

    if (u->peer.connection) {
        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "close http upstream connection: %d",
                       u->peer.connection->fd);

#if 0 /* we don't support SSL at this time, was: (NGX_HTTP_SSL) */

        if (u->peer.connection->ssl) {
            u->peer.connection->ssl->no_wait_shutdown = 1;
            u->peer.connection->ssl->no_send_shutdown = 1;

            (void) ngx_ssl_shutdown(u->peer.connection);
        }
#endif

#if defined(nginx_version) && (nginx_version >= 1001004)
        if (u->peer.connection->pool) {
            ngx_destroy_pool(u->peer.connection->pool);
        }
#endif

        ngx_close_connection(u->peer.connection);
    }

#if 0
    if (u->conf->busy_lock && !u->busy_locked) {
        ngx_http_upstream_busy_lock(p);
        return;
    }
#endif

    /* TODO: ngx_http_upstream_connect(r, u); */
    if (status == 0) {
        status = NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    dd("returning");
    return ngx_postgres_upstream_finalize_request(r, u, status);
}

ngx_int_t
ngx_postgres_upstream_test_connect(ngx_connection_t *c)
{
    int        err;
    socklen_t  len;

    dd("entering");

#if (NGX_HAVE_KQUEUE)

    if (ngx_event_flags & NGX_USE_KQUEUE_EVENT)  {
        if (c->write->pending_eof) {
            c->log->action = "connecting to upstream";
            (void) ngx_connection_error(c, c->write->kq_errno,
                       "kevent() reported that connect() failed");

            dd("returning NGX_ERROR");
            return NGX_ERROR;
        }

    } else
#endif
    {
        err = 0;
        len = sizeof(int);

        /*
         * BSDs and Linux return 0 and set a pending error in err
         * Solaris returns -1 and sets errno
         */

        if (getsockopt(c->fd, SOL_SOCKET, SO_ERROR, (void *) &err, &len) == -1)
        {
            err = ngx_errno;
        }

        if (err) {
            c->log->action = "connecting to upstream";
            (void) ngx_connection_error(c, err, "connect() failed");

            dd("returning NGX_ERROR");
            return NGX_ERROR;
        }
    }

    dd("returning NGX_OK");
    return NGX_OK;
}

ngx_int_t
ngx_postgres_rewrite_var(ngx_http_request_t *r, ngx_http_variable_value_t *v,
    uintptr_t data)
{
    ngx_http_variable_t              *var;
    ngx_http_core_main_conf_t        *cmcf;
    ngx_postgres_rewrite_loc_conf_t  *rlcf;

    rlcf = ngx_http_get_module_loc_conf(r, ngx_http_rewrite_module);

    if (rlcf->uninitialized_variable_warn == 0) {
        *v = ngx_http_variable_null_value;
        return NGX_OK;
    }

    cmcf = ngx_http_get_module_main_conf(r, ngx_http_core_module);

    var = cmcf->variables.elts;

    /*
     * the ngx_http_rewrite_module sets variables directly in r->variables,
     * and they should be handled by ngx_http_get_indexed_variable(),
     * so the handler is called only if the variable is not initialized
     */

    ngx_log_error(NGX_LOG_WARN, r->connection->log, 0,
                  "using uninitialized \"%V\" variable", &var[data].name);

    *v = ngx_http_variable_null_value;

    return NGX_OK;
}

char *
ngx_postgres_rewrite_value(ngx_conf_t *cf, ngx_postgres_rewrite_loc_conf_t *lcf,
    ngx_str_t *value)
{
    ngx_int_t                              n;
    ngx_http_script_compile_t              sc;
    ngx_http_script_value_code_t          *val;
    ngx_http_script_complex_value_code_t  *complex;

    n = ngx_http_script_variables_count(value);

    if (n == 0) {
        val = ngx_http_script_start_code(cf->pool, &lcf->codes,
                                         sizeof(ngx_http_script_value_code_t));
        if (val == NULL) {
            return NGX_CONF_ERROR;
        }

        n = ngx_atoi(value->data, value->len);

        if (n == NGX_ERROR) {
            n = 0;
        }

        val->code = ngx_http_script_value_code;
        val->value = (uintptr_t) n;
        val->text_len = (uintptr_t) value->len;
        val->text_data = (uintptr_t) value->data;

        return NGX_CONF_OK;
    }

    complex = ngx_http_script_start_code(cf->pool, &lcf->codes,
                                 sizeof(ngx_http_script_complex_value_code_t));
    if (complex == NULL) {
        return NGX_CONF_ERROR;
    }

    complex->code = ngx_http_script_complex_value_code;
    complex->lengths = NULL;

    ngx_memzero(&sc, sizeof(ngx_http_script_compile_t));

    sc.cf = cf;
    sc.source = value;
    sc.lengths = &complex->lengths;
    sc.values = &lcf->codes;
    sc.variables = n;
    sc.complete_lengths = 1;

    if (ngx_http_script_compile(&sc) != NGX_OK) {
        return NGX_CONF_ERROR;
    }

    return NGX_CONF_OK;
}
