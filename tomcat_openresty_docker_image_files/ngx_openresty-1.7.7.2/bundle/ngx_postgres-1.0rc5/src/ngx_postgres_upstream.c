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

#include <nginx.h>
#include "ngx_postgres_ddebug.h"
#include "ngx_postgres_module.h"
#include "ngx_postgres_keepalive.h"
#include "ngx_postgres_processor.h"


ngx_int_t
ngx_postgres_upstream_init(ngx_conf_t *cf, ngx_http_upstream_srv_conf_t *uscf)
{
    ngx_postgres_upstream_srv_conf_t  *pgscf;
    ngx_postgres_upstream_server_t    *server;
    ngx_postgres_upstream_peers_t     *peers;
    ngx_uint_t                         i, j, n;

    dd("entering");

    uscf->peer.init = ngx_postgres_upstream_init_peer;

    pgscf = ngx_http_conf_upstream_srv_conf(uscf, ngx_postgres_module);

    if (pgscf->servers == NULL || pgscf->servers->nelts == 0) {
        ngx_log_error(NGX_LOG_ERR, cf->log, 0,
                      "postgres: no \"postgres_server\" defined"
                      " in upstream \"%V\" in %s:%ui",
                      &uscf->host, uscf->file_name, uscf->line);

        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }

    /* pgscf->servers != NULL */

    server = uscf->servers->elts;

    n = 0;

    for (i = 0; i < uscf->servers->nelts; i++) {
        n += server[i].naddrs;
    }

    peers = ngx_pcalloc(cf->pool, sizeof(ngx_postgres_upstream_peers_t)
            + sizeof(ngx_postgres_upstream_peer_t) * (n - 1));

    if (peers == NULL) {
        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }

    peers->single = (n == 1);
    peers->number = n;
    peers->name = &uscf->host;

    n = 0;

    for (i = 0; i < uscf->servers->nelts; i++) {
        for (j = 0; j < server[i].naddrs; j++) {
            peers->peer[n].sockaddr = server[i].addrs[j].sockaddr;
            peers->peer[n].socklen = server[i].addrs[j].socklen;
            peers->peer[n].name = server[i].addrs[j].name;
            peers->peer[n].port = server[i].port;
            peers->peer[n].dbname = server[i].dbname;
            peers->peer[n].user = server[i].user;
            peers->peer[n].password = server[i].password;

            peers->peer[n].host.data = ngx_pnalloc(cf->pool,
                                                   NGX_SOCKADDR_STRLEN);
            if (peers->peer[n].host.data == NULL) {
                dd("returning NGX_ERROR");
                return NGX_ERROR;
            }

            peers->peer[n].host.len = ngx_sock_ntop(peers->peer[n].sockaddr,
#if defined(nginx_version) && (nginx_version >= 1005003)
                                          peers->peer[n].socklen,
#endif
                                          peers->peer[n].host.data,
                                          NGX_SOCKADDR_STRLEN, 0);
            if (peers->peer[n].host.len == 0) {
                dd("returning NGX_ERROR");
                return NGX_ERROR;
            }

            n++;
        }
    }

    pgscf->peers = peers;
    pgscf->active_conns = 0;

    if (pgscf->max_cached) {
        dd("returning");
        return ngx_postgres_keepalive_init(cf->pool, pgscf);
    }

    dd("returning NGX_OK");
    return NGX_OK;
}

ngx_int_t
ngx_postgres_upstream_init_peer(ngx_http_request_t *r,
    ngx_http_upstream_srv_conf_t *uscf)
{
    ngx_postgres_upstream_peer_data_t  *pgdt;
    ngx_postgres_upstream_srv_conf_t   *pgscf;
    ngx_postgres_loc_conf_t            *pglcf;
    ngx_postgres_ctx_t                 *pgctx;
    ngx_http_core_loc_conf_t           *clcf;
    ngx_http_upstream_t                *u;
    ngx_postgres_mixed_t               *query;
    ngx_str_t                           sql;
    ngx_uint_t                          i;

    dd("entering");

    pgdt = ngx_pcalloc(r->pool, sizeof(ngx_postgres_upstream_peer_data_t));
    if (pgdt == NULL) {
        goto failed;
    }

    u = r->upstream;

    pgdt->upstream = u;
    pgdt->request = r;

    pgscf = ngx_http_conf_upstream_srv_conf(uscf, ngx_postgres_module);
    pglcf = ngx_http_get_module_loc_conf(r, ngx_postgres_module);
    pgctx = ngx_http_get_module_ctx(r, ngx_postgres_module);

    pgdt->srv_conf = pgscf;
    pgdt->loc_conf = pglcf;

    u->peer.data = pgdt;
    u->peer.get = ngx_postgres_upstream_get_peer;
    u->peer.free = ngx_postgres_upstream_free_peer;

    if (pglcf->query.methods_set & r->method) {
        /* method-specific query */
        dd("using method-specific query");

        query = pglcf->query.methods->elts;
        for (i = 0; i < pglcf->query.methods->nelts; i++) {
            if (query[i].key & r->method) {
                query = &query[i];
                break;
            }
        }

        if (i == pglcf->query.methods->nelts) {
            goto failed;
        }
    } else {
        /* default query */
        dd("using default query");

        query = pglcf->query.def;
    }

    if (query->cv) {
        /* complex value */
        dd("using complex value");

        if (ngx_http_complex_value(r, query->cv, &sql) != NGX_OK) {
            goto failed;
        }

        if (sql.len == 0) {
            clcf = ngx_http_get_module_loc_conf(r, ngx_http_core_module);

            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "postgres: empty \"postgres_query\" (was: \"%V\")"
                          " in location \"%V\"", &query->cv->value,
                          &clcf->name);

            goto failed;
        }

        pgdt->query = sql;
    } else {
        /* simple value */
        dd("using simple value");

        pgdt->query = query->sv;
    }

    /* set $postgres_query */
    pgctx->var_query = pgdt->query;

    dd("returning NGX_OK");
    return NGX_OK;

failed:

#if defined(nginx_version) && (nginx_version >= 8017)
    dd("returning NGX_ERROR");
    return NGX_ERROR;
#else
    r->upstream->peer.data = NULL;

    dd("returning NGX_OK (NGX_ERROR)");
    return NGX_OK;
#endif
}

ngx_int_t
ngx_postgres_upstream_get_peer(ngx_peer_connection_t *pc, void *data)
{
    ngx_postgres_upstream_peer_data_t  *pgdt = data;
    ngx_postgres_upstream_srv_conf_t   *pgscf;
#if defined(nginx_version) && (nginx_version < 8017)
    ngx_postgres_ctx_t                 *pgctx;
#endif
    ngx_postgres_upstream_peers_t      *peers;
    ngx_postgres_upstream_peer_t       *peer;
    ngx_connection_t                   *pgxc = NULL;
    int                                 fd;
    ngx_event_t                        *rev, *wev;
    ngx_int_t                           rc;
    u_char                             *connstring, *last;
    size_t                              len;

    dd("entering");

#if defined(nginx_version) && (nginx_version < 8017)
    if (data == NULL) {
        goto failed;
    }

    pgctx = ngx_http_get_module_ctx(pgdt->request, ngx_postgres_module);
#endif

    pgscf = pgdt->srv_conf;

    pgdt->failed = 0;

    if (pgscf->max_cached && pgscf->single) {
        rc = ngx_postgres_keepalive_get_peer_single(pc, pgdt, pgscf);
        if (rc != NGX_DECLINED) {
            /* re-use keepalive peer */
            dd("re-using keepalive peer (single)");

            pgdt->state = state_db_send_query;

            ngx_postgres_process_events(pgdt->request);

            dd("returning NGX_AGAIN");
            return NGX_AGAIN;
        }
    }

    peers = pgscf->peers;

    if (pgscf->current > peers->number - 1) {
        pgscf->current = 0;
    }

    peer = &peers->peer[pgscf->current++];

    pgdt->name.len = peer->name.len;
    pgdt->name.data = peer->name.data;

    pgdt->sockaddr = *peer->sockaddr;

    pc->name = &pgdt->name;
    pc->sockaddr = &pgdt->sockaddr;
    pc->socklen = peer->socklen;
    pc->cached = 0;

    if ((pgscf->max_cached) && (!pgscf->single)) {
        rc = ngx_postgres_keepalive_get_peer_multi(pc, pgdt, pgscf);
        if (rc != NGX_DECLINED) {
            /* re-use keepalive peer */
            dd("re-using keepalive peer (multi)");

            pgdt->state = state_db_send_query;

            ngx_postgres_process_events(pgdt->request);

            dd("returning NGX_AGAIN");
            return NGX_AGAIN;
        }
    }

    if ((pgscf->reject) && (pgscf->active_conns >= pgscf->max_cached)) {
        ngx_log_error(NGX_LOG_INFO, pc->log, 0,
                      "postgres: keepalive connection pool is full,"
                      " rejecting request to upstream \"%V\"", &peer->name);

        /* a bit hack-ish way to return error response (setup part) */
        pc->connection = ngx_get_connection(0, pc->log);

#if defined(nginx_version) && (nginx_version < 8017)
        pgctx->status = NGX_HTTP_SERVICE_UNAVAILABLE;
#endif

        dd("returning NGX_AGAIN (NGX_HTTP_SERVICE_UNAVAILABLE)");
        return NGX_AGAIN;
    }

    /* sizeof("...") - 1 + 1 (for spaces and '\0' omitted */
    len = sizeof("hostaddr=") + peer->host.len
        + sizeof("port=") + sizeof("65535") - 1
        + sizeof("dbname=") + peer->dbname.len
        + sizeof("user=") + peer->user.len
        + sizeof("password=") + peer->password.len
        + sizeof("sslmode=disable");

    connstring = ngx_pnalloc(pgdt->request->pool, len);
    if (connstring == NULL) {
#if defined(nginx_version) && (nginx_version >= 8017)
        dd("returning NGX_ERROR");
        return NGX_ERROR;
#else
        goto failed;
#endif
    }

    /* TODO add unix sockets */
    last = ngx_snprintf(connstring, len - 1,
                        "hostaddr=%V port=%d dbname=%V user=%V password=%V"
                        " sslmode=disable",
                        &peer->host, peer->port, &peer->dbname, &peer->user,
                        &peer->password);
    *last = '\0';

    dd("PostgreSQL connection string: %s", connstring);

    /*
     * internal checks in PQsetnonblocking are taking care of any
     * PQconnectStart failures, so we don't need to check them here.
     */

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, pc->log, 0,
                   "postgres: connecting");

    pgdt->pgconn = PQconnectStart((const char *)connstring);
    if (PQsetnonblocking(pgdt->pgconn, 1) == -1) {
        ngx_log_error(NGX_LOG_ERR, pc->log, 0,
                      "postgres: connection failed: %s in upstream \"%V\"",
                      PQerrorMessage(pgdt->pgconn), &peer->name);

        PQfinish(pgdt->pgconn);
        pgdt->pgconn = NULL;

#if defined(nginx_version) && (nginx_version >= 8017)
        dd("returning NGX_DECLINED");
        return NGX_DECLINED;
#else
        pgctx->status = NGX_HTTP_BAD_GATEWAY;
        goto failed;
#endif
    }

#if defined(DDEBUG) && (DDEBUG > 1)
    PQtrace(pgdt->pgconn, stderr);
#endif

    dd("connection status:%d", (int) PQstatus(pgdt->pgconn));

    /* take spot in keepalive connection pool */
    pgscf->active_conns++;

    /* add the file descriptor (fd) into an nginx connection structure */

    fd = PQsocket(pgdt->pgconn);
    if (fd == -1) {
        ngx_log_error(NGX_LOG_ERR, pc->log, 0,
                      "postgres: failed to get connection fd");

        goto invalid;
    }

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, pc->log, 0,
                   "postgres: connection fd:%d", fd);

    pgxc = pc->connection = ngx_get_connection(fd, pc->log);

    if (pgxc == NULL) {
        ngx_log_error(NGX_LOG_ERR, pc->log, 0,
                      "postgres: failed to get a free nginx connection");

        goto invalid;
    }

    pgxc->log = pc->log;
    pgxc->log_error = pc->log_error;
    pgxc->number = ngx_atomic_fetch_add(ngx_connection_counter, 1);

    rev = pgxc->read;
    wev = pgxc->write;

    rev->log = pc->log;
    wev->log = pc->log;

    /* register the connection with postgres connection fd into the
     * nginx event model */

    if (ngx_event_flags & NGX_USE_RTSIG_EVENT) {
        dd("NGX_USE_RTSIG_EVENT");
        if (ngx_add_conn(pgxc) != NGX_OK) {
            goto bad_add;
        }

    } else if (ngx_event_flags & NGX_USE_CLEAR_EVENT) {
        dd("NGX_USE_CLEAR_EVENT");
        if (ngx_add_event(rev, NGX_READ_EVENT, NGX_CLEAR_EVENT) != NGX_OK) {
            goto bad_add;
        }

        if (ngx_add_event(wev, NGX_WRITE_EVENT, NGX_CLEAR_EVENT) != NGX_OK) {
            goto bad_add;
        }

    } else {
        dd("NGX_USE_LEVEL_EVENT");
        if (ngx_add_event(rev, NGX_READ_EVENT, NGX_LEVEL_EVENT) != NGX_OK) {
            goto bad_add;
        }

        if (ngx_add_event(wev, NGX_WRITE_EVENT, NGX_LEVEL_EVENT) != NGX_OK) {
            goto bad_add;
        }
    }

    pgxc->log->action = "connecting to PostgreSQL database";
    pgdt->state = state_db_connect;

    dd("returning NGX_AGAIN");
    return NGX_AGAIN;

bad_add:

    ngx_log_error(NGX_LOG_ERR, pc->log, 0,
                  "postgres: failed to add nginx connection");

invalid:

    ngx_postgres_upstream_free_connection(pc->log, pc->connection,
                                          pgdt->pgconn, pgscf);

#if defined(nginx_version) && (nginx_version >= 8017)
    dd("returning NGX_ERROR");
    return NGX_ERROR;
#else

failed:

    /* a bit hack-ish way to return error response (setup part) */
    pc->connection = ngx_get_connection(0, pc->log);

    dd("returning NGX_AGAIN (NGX_ERROR)");
    return NGX_AGAIN;
#endif
}

void
ngx_postgres_upstream_free_peer(ngx_peer_connection_t *pc,
    void *data, ngx_uint_t state)
{
    ngx_postgres_upstream_peer_data_t  *pgdt = data;
    ngx_postgres_upstream_srv_conf_t   *pgscf;

    dd("entering");

#if defined(nginx_version) && (nginx_version < 8017)
    if (data == NULL) {
        dd("returning");
        return;
    }
#endif

    pgscf = pgdt->srv_conf;

    if (pgscf->max_cached) {
        ngx_postgres_keepalive_free_peer(pc, pgdt, pgscf, state);
    }

    if (pc->connection) {
        dd("free connection to PostgreSQL database");

        ngx_postgres_upstream_free_connection(pc->log, pc->connection,
                pgdt->pgconn, pgscf);

        pgdt->pgconn = NULL;
        pc->connection = NULL;
    }

    dd("returning");
}

ngx_flag_t
ngx_postgres_upstream_is_my_peer(const ngx_peer_connection_t *peer)
{
    dd("entering & returning");
    return (peer->get == ngx_postgres_upstream_get_peer);
}

void
ngx_postgres_upstream_free_connection(ngx_log_t *log, ngx_connection_t *c,
    PGconn *pgconn, ngx_postgres_upstream_srv_conf_t *pgscf)
{
    ngx_event_t  *rev, *wev;

    dd("entering");

    PQfinish(pgconn);

    if (c) {
        rev = c->read;
        wev = c->write;

        if (rev->timer_set) {
            ngx_del_timer(rev);
        }

        if (wev->timer_set) {
            ngx_del_timer(wev);
        }

        if (ngx_del_conn) {
           ngx_del_conn(c, NGX_CLOSE_EVENT);
        } else {
            if (rev->active || rev->disabled) {
                ngx_del_event(rev, NGX_READ_EVENT, NGX_CLOSE_EVENT);
            }

            if (wev->active || wev->disabled) {
                ngx_del_event(wev, NGX_WRITE_EVENT, NGX_CLOSE_EVENT);
            }
        }

#if defined(nginx_version) && nginx_version >= 1007005
        if (rev->posted) {
#else
        if (rev->prev) {
#endif
            ngx_delete_posted_event(rev);
        }

#if defined(nginx_version) && nginx_version >= 1007005
        if (wev->posted) {
#else
        if (wev->prev) {
#endif
            ngx_delete_posted_event(wev);
        }

        rev->closed = 1;
        wev->closed = 1;

#if defined(nginx_version) && (nginx_version >= 1001004)
        if (c->pool) {
            ngx_destroy_pool(c->pool);
        }
#endif

        ngx_free_connection(c);

        c->fd = (ngx_socket_t) -1;
    }

    /* free spot in keepalive connection pool */
    pgscf->active_conns--;

    dd("returning");
}
