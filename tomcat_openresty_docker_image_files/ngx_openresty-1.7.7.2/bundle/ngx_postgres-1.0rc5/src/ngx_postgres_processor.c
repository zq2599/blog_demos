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
#include "ngx_postgres_output.h"
#include "ngx_postgres_processor.h"
#include "ngx_postgres_util.h"
#include "ngx_postgres_variable.h"


void
ngx_postgres_process_events(ngx_http_request_t *r)
{
    ngx_postgres_upstream_peer_data_t  *pgdt;
    ngx_connection_t                   *pgxc;
    ngx_http_upstream_t                *u;
    ngx_int_t                           rc;

    dd("entering");

    u = r->upstream;
    pgxc = u->peer.connection;
    pgdt = u->peer.data;

    if (!ngx_postgres_upstream_is_my_peer(&u->peer)) {
        ngx_log_error(NGX_LOG_ERR, pgxc->log, 0,
                      "postgres: trying to connect to something that"
                      " is not PostgreSQL database");

        goto failed;
    }

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, pgxc->log, 0,
                   "postgres: process events");

    switch (pgdt->state) {
    case state_db_connect:
        dd("state_db_connect");
        rc = ngx_postgres_upstream_connect(r, pgxc, pgdt);
        break;
    case state_db_send_query:
        dd("state_db_send_query");
        rc = ngx_postgres_upstream_send_query(r, pgxc, pgdt);
        break;
    case state_db_get_result:
        dd("state_db_get_result");
        rc = ngx_postgres_upstream_get_result(r, pgxc, pgdt);
        break;
    case state_db_get_ack:
        dd("state_db_get_ack");
        rc = ngx_postgres_upstream_get_ack(r, pgxc, pgdt);
        break;
    case state_db_idle:
        dd("state_db_idle, re-using keepalive connection");
        pgxc->log->action = "sending query to PostgreSQL database";
        pgdt->state = state_db_send_query;
        rc = ngx_postgres_upstream_send_query(r, pgxc, pgdt);
        break;
    default:
        dd("unknown state:%d", pgdt->state);
        ngx_log_error(NGX_LOG_ERR, pgxc->log, 0,
                      "postgres: unknown state:%d", pgdt->state);

        goto failed;
    }

    if (rc >= NGX_HTTP_SPECIAL_RESPONSE) {
        ngx_postgres_upstream_finalize_request(r, u, rc);
    } else if (rc == NGX_ERROR) {
        goto failed;
    }

    dd("returning");
    return;

failed:

    ngx_postgres_upstream_next(r, u, NGX_HTTP_UPSTREAM_FT_ERROR);

    dd("returning");
}

ngx_int_t
ngx_postgres_upstream_connect(ngx_http_request_t *r, ngx_connection_t *pgxc,
    ngx_postgres_upstream_peer_data_t *pgdt)
{
    PostgresPollingStatusType  pgrc;

    dd("entering");

    pgrc = PQconnectPoll(pgdt->pgconn);

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, pgxc->log, 0,
                   "postgres: polling while connecting, rc:%d", (int) pgrc);

    if (pgrc == PGRES_POLLING_READING || pgrc == PGRES_POLLING_WRITING) {

        /*
         * Fix for Linux issue found by chaoslawful (via agentzh):
         * "According to the source of libpq (around fe-connect.c:1215), during
         *  the state switch from CONNECTION_STARTED to CONNECTION_MADE, there's
         *  no socket read/write operations (just a plain getsockopt call and a
         *  getsockname call). Therefore, for edge-triggered event model, we
         *  have to call PQconnectPoll one more time (immediately) when we see
         *  CONNECTION_MADE is returned, or we're very likely to wait for a
         *  writable event that has already appeared and will never appear
         *  again :)"
         */
        if (PQstatus(pgdt->pgconn) == CONNECTION_MADE && pgxc->write->ready) {
            dd("re-polling on connection made");

            pgrc = PQconnectPoll(pgdt->pgconn);
            dd("re-polling rc:%d", (int) pgrc);

            ngx_log_debug1(NGX_LOG_DEBUG_HTTP, pgxc->log, 0,
                           "postgres: re-polling while connecting, rc:%d",
                           (int) pgrc);

            if (pgrc == PGRES_POLLING_READING || pgrc == PGRES_POLLING_WRITING)
            {
                ngx_log_debug1(NGX_LOG_DEBUG_HTTP, pgxc->log, 0,
                               "postgres: busy while connecting, rc:%d",
                               (int) pgrc);

                dd("returning NGX_AGAIN");
                return NGX_AGAIN;
            }

            goto done;
        }

#if defined(DDEBUG) && (DDEBUG)
        switch (PQstatus(pgdt->pgconn)) {
        case CONNECTION_NEEDED:
             dd("connecting (waiting for connect()))");
             break;
        case CONNECTION_STARTED:
             dd("connecting (waiting for connection to be made)");
             break;
        case CONNECTION_MADE:
             dd("connecting (connection established)");
             break;
        case CONNECTION_AWAITING_RESPONSE:
             dd("connecting (credentials sent, waiting for response)");
             break;
        case CONNECTION_AUTH_OK:
             dd("connecting (authenticated)");
             break;
        case CONNECTION_SETENV:
             dd("connecting (negotiating envinroment)");
             break;
        case CONNECTION_SSL_STARTUP:
             dd("connecting (negotiating SSL)");
             break;
        default:
             /*
              * This cannot happen, PQconnectPoll would return
              * PGRES_POLLING_FAILED in that case.
              */
             dd("connecting (unknown state:%d)", (int) PQstatus(pgdt->pgconn));

             dd("returning NGX_ERROR");
             return NGX_ERROR;
        }
#endif /* DDEBUG */

        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, pgxc->log, 0,
                       "postgres: busy while connecting, rc:%d", (int) pgrc);

        dd("returning NGX_AGAIN");
        return NGX_AGAIN;
    }

done:

    /* remove connection timeout from new connection */
    if (pgxc->write->timer_set) {
        ngx_del_timer(pgxc->write);
    }

    if (pgrc != PGRES_POLLING_OK) {
        dd("connection failed");
        ngx_log_error(NGX_LOG_ERR, pgxc->log, 0,
                      "postgres: connection failed: %s",
                      PQerrorMessage(pgdt->pgconn));

        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }

    dd("connected successfully");
    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, pgxc->log, 0,
                   "postgres: connected successfully");

    pgxc->log->action = "sending query to PostgreSQL database";
    pgdt->state = state_db_send_query;

    dd("returning");
    return ngx_postgres_upstream_send_query(r, pgxc, pgdt);
}

ngx_int_t
ngx_postgres_upstream_send_query(ngx_http_request_t *r, ngx_connection_t *pgxc,
    ngx_postgres_upstream_peer_data_t *pgdt)
{
    ngx_postgres_loc_conf_t  *pglcf;
    ngx_int_t                 pgrc;
    u_char                   *query;

    dd("entering");

    pglcf = ngx_http_get_module_loc_conf(r, ngx_postgres_module);

    query = ngx_pnalloc(r->pool, pgdt->query.len + 1);
    if (query == NULL) {
        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }

    (void) ngx_cpystrn(query, pgdt->query.data, pgdt->query.len + 1);

    dd("sending query: %s", query);
    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, pgxc->log, 0,
                   "postgres: sending query: \"%s\"", query);

    if (pglcf->output_binary) {
        pgrc = PQsendQueryParams(pgdt->pgconn, (const char *) query,
                                 0, NULL, NULL, NULL, NULL, /* binary */ 1);
    } else {
        pgrc = PQsendQuery(pgdt->pgconn, (const char *) query);
    }

    if (pgrc == 0) {
        dd("sending query failed");
        ngx_log_error(NGX_LOG_ERR, pgxc->log, 0,
                      "postgres: sending query failed: %s",
                      PQerrorMessage(pgdt->pgconn));

        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }

    /* set result timeout */
    ngx_add_timer(pgxc->read, r->upstream->conf->read_timeout);

    dd("query sent successfully");
    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, pgxc->log, 0,
                   "postgres: query sent successfully");

    pgxc->log->action = "waiting for result from PostgreSQL database";
    pgdt->state = state_db_get_result;

    dd("returning NGX_DONE");
    return NGX_DONE;
}

ngx_int_t
ngx_postgres_upstream_get_result(ngx_http_request_t *r, ngx_connection_t *pgxc,
    ngx_postgres_upstream_peer_data_t *pgdt)
{
    ExecStatusType   pgrc;
    PGresult        *res;
    ngx_int_t        rc;

    dd("entering");

    /* remove connection timeout from re-used keepalive connection */
    if (pgxc->write->timer_set) {
        ngx_del_timer(pgxc->write);
    }

    if (!PQconsumeInput(pgdt->pgconn)) {
        ngx_log_error(NGX_LOG_ERR, pgxc->log, 0,
                      "postgres: failed to consume input: %s",
                      PQerrorMessage(pgdt->pgconn));

        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }

    if (PQisBusy(pgdt->pgconn)) {
        ngx_log_debug0(NGX_LOG_DEBUG_HTTP, pgxc->log, 0,
                       "postgres: busy while receiving result");

        dd("returning NGX_AGAIN");
        return NGX_AGAIN;
    }

    dd("receiving result");

    res = PQgetResult(pgdt->pgconn);
    if (res == NULL) {
        dd("receiving result failed");
        ngx_log_error(NGX_LOG_ERR, pgxc->log, 0,
                      "postgres: failed to receive result: %s",
                      PQerrorMessage(pgdt->pgconn));

        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }

    pgrc = PQresultStatus(res);
    if ((pgrc != PGRES_COMMAND_OK) && (pgrc != PGRES_TUPLES_OK)) {
        dd("receiving result failed");
        ngx_log_error(NGX_LOG_ERR, pgxc->log, 0,
                      "postgres: failed to receive result: %s: %s",
                      PQresStatus(pgrc),
                      PQerrorMessage(pgdt->pgconn));

        PQclear(res);

        dd("returning NGX_HTTP_INTERNAL_SERVER_ERROR");
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    dd("result received successfully, cols:%d rows:%d",
       PQnfields(res), PQntuples(res));

    ngx_log_debug2(NGX_LOG_DEBUG_HTTP, pgxc->log, 0,
                   "postgres: result received successfully, cols:%d rows:%d",
                   PQnfields(res), PQntuples(res));

    pgxc->log->action = "processing result from PostgreSQL database";
    rc = ngx_postgres_process_response(r, res);

    PQclear(res);

    if (rc != NGX_DONE) {
        dd("returning rc:%d", (int) rc);
        return rc;
    }

    dd("result processed successfully");

    pgxc->log->action = "waiting for ACK from PostgreSQL database";
    pgdt->state = state_db_get_ack;

    dd("returning");
    return ngx_postgres_upstream_get_ack(r, pgxc, pgdt);
}

ngx_int_t
ngx_postgres_process_response(ngx_http_request_t *r, PGresult *res)
{
    ngx_postgres_loc_conf_t      *pglcf;
    ngx_postgres_ctx_t           *pgctx;
    ngx_postgres_rewrite_conf_t  *pgrcf;
    ngx_postgres_variable_t      *pgvar;
    ngx_str_t                    *store;
    char                         *affected;
    size_t                        affected_len;
    ngx_uint_t                    i;
    ngx_int_t                     rc;

    dd("entering");

    pglcf = ngx_http_get_module_loc_conf(r, ngx_postgres_module);
    pgctx = ngx_http_get_module_ctx(r, ngx_postgres_module);

    /* set $postgres_columns */
    pgctx->var_cols = PQnfields(res);

    /* set $postgres_rows */
    pgctx->var_rows = PQntuples(res);

    /* set $postgres_affected */
    if (ngx_strncmp(PQcmdStatus(res), "SELECT", sizeof("SELECT") - 1)) {
        affected = PQcmdTuples(res);
        affected_len = ngx_strlen(affected);
        if (affected_len) {
            pgctx->var_affected = ngx_atoi((u_char *) affected, affected_len);
        }
    }

    if (pglcf->rewrites) {
        /* process rewrites */
        pgrcf = pglcf->rewrites->elts;
        for (i = 0; i < pglcf->rewrites->nelts; i++) {
            rc = pgrcf[i].handler(r, &pgrcf[i]);
            if (rc != NGX_DECLINED) {
                if (rc >= NGX_HTTP_SPECIAL_RESPONSE) {
                    dd("returning NGX_DONE, status %d", (int) rc);
                    pgctx->status = rc;
                    return NGX_DONE;
                }

                pgctx->status = rc;
                break;
            }
        }
    }

    if (pglcf->variables) {
        /* set custom variables */
        pgvar = pglcf->variables->elts;
        store = pgctx->variables->elts;

        for (i = 0; i < pglcf->variables->nelts; i++) {
            store[i] = ngx_postgres_variable_set_custom(r, res, &pgvar[i]);
            if ((store[i].len == 0) && (pgvar[i].value.required)) {
                dd("returning NGX_DONE, status NGX_HTTP_INTERNAL_SERVER_ERROR");
                pgctx->status = NGX_HTTP_INTERNAL_SERVER_ERROR;
                return NGX_DONE;
            }
        }
    }

    if (pglcf->output_handler) {
        /* generate output */
        dd("returning");
        return pglcf->output_handler(r, res);
    }

    dd("returning NGX_DONE");
    return NGX_DONE;
}

ngx_int_t
ngx_postgres_upstream_get_ack(ngx_http_request_t *r, ngx_connection_t *pgxc,
    ngx_postgres_upstream_peer_data_t *pgdt)
{
    PGresult  *res;

    dd("entering");

    if (!PQconsumeInput(pgdt->pgconn)) {
        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }

    if (PQisBusy(pgdt->pgconn)) {
        dd("returning NGX_AGAIN");
        return NGX_AGAIN;
    }

    /* remove result timeout */
    if (pgxc->read->timer_set) {
        ngx_del_timer(pgxc->read);
    }

    dd("receiving ACK (ready for next query)");

    res = PQgetResult(pgdt->pgconn);
    if (res != NULL) {
        dd("receiving ACK failed");
        ngx_log_error(NGX_LOG_ERR, pgxc->log, 0,
                      "postgres: receiving ACK failed: multiple queries(?)");

        PQclear(res);

        dd("returning NGX_HTTP_INTERNAL_SERVER_ERROR");
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    dd("ACK received successfully");

    pgxc->log->action = "being idle on PostgreSQL database";
    pgdt->state = state_db_idle;

    dd("returning");
    return ngx_postgres_upstream_done(r, r->upstream, pgdt);
}

ngx_int_t
ngx_postgres_upstream_done(ngx_http_request_t *r, ngx_http_upstream_t *u,
    ngx_postgres_upstream_peer_data_t *pgdt)
{
    ngx_postgres_ctx_t  *pgctx;

    dd("entering");

    /* flag for keepalive */
    u->headers_in.status_n = NGX_HTTP_OK;

    pgctx = ngx_http_get_module_ctx(r, ngx_postgres_module);

    if (pgctx->status >= NGX_HTTP_SPECIAL_RESPONSE) {
        ngx_postgres_upstream_finalize_request(r, u, pgctx->status);
    } else {
        ngx_postgres_upstream_finalize_request(r, u, NGX_OK);
    }

    dd("returning NGX_DONE");
    return NGX_DONE;
}
