/*
 * Copyright (c) 2010, FRiCKLE Piotr Sikora <info@frickle.com>
 * Copyright (c) 2009-2010, Yichun Zhang <agentzh@gmail.com>
 * Copyright (C) 2008, Maxim Dounin
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

#include "ngx_postgres_ddebug.h"
#include "ngx_postgres_keepalive.h"


ngx_int_t
ngx_postgres_keepalive_init(ngx_pool_t *pool,
    ngx_postgres_upstream_srv_conf_t *pgscf)
{
    ngx_postgres_keepalive_cache_t  *cached;
    ngx_uint_t                       i;

    dd("entering");

    cached = ngx_pcalloc(pool,
                 sizeof(ngx_postgres_keepalive_cache_t) * pgscf->max_cached);
    if (cached == NULL) {
        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }

    ngx_queue_init(&pgscf->cache);
    ngx_queue_init(&pgscf->free);

    for (i = 0; i < pgscf->max_cached; i++) {
        ngx_queue_insert_head(&pgscf->free, &cached[i].queue);
        cached[i].srv_conf = pgscf;
    }

    dd("returning NGX_OK");
    return NGX_OK;
}

ngx_int_t
ngx_postgres_keepalive_get_peer_single(ngx_peer_connection_t *pc,
    ngx_postgres_upstream_peer_data_t *pgp,
    ngx_postgres_upstream_srv_conf_t *pgscf)
{
    ngx_postgres_keepalive_cache_t  *item;
    ngx_queue_t                     *q;
    ngx_connection_t                *c;

    dd("entering");

    if (!ngx_queue_empty(&pgscf->cache)) {
        dd("non-empty queue");

        q = ngx_queue_head(&pgscf->cache);
        ngx_queue_remove(q);

        item = ngx_queue_data(q, ngx_postgres_keepalive_cache_t, queue);
        c = item->connection;

        ngx_queue_insert_head(&pgscf->free, q);

        c->idle = 0;
        c->log = pc->log;
#if defined(nginx_version) && (nginx_version >= 1001004)
        c->pool->log = pc->log;
#endif
        c->read->log = pc->log;
        c->write->log = pc->log;

        pgp->name.data = item->name.data;
        pgp->name.len = item->name.len;

        pgp->sockaddr = item->sockaddr;

        pgp->pgconn = item->pgconn;

        pc->connection = c;
        pc->cached = 1;

        pc->name = &pgp->name;

        pc->sockaddr = &pgp->sockaddr;
        pc->socklen = item->socklen;

        dd("returning NGX_DONE");

        return NGX_DONE;
    }

    dd("returning NGX_DECLINED");
    return NGX_DECLINED;
}

ngx_int_t
ngx_postgres_keepalive_get_peer_multi(ngx_peer_connection_t *pc,
    ngx_postgres_upstream_peer_data_t *pgp,
    ngx_postgres_upstream_srv_conf_t *pgscf)
{
    ngx_postgres_keepalive_cache_t  *item;
    ngx_queue_t                     *q, *cache;
    ngx_connection_t                *c;

    dd("entering");

    cache = &pgscf->cache;

    for (q = ngx_queue_head(cache);
         q != ngx_queue_sentinel(cache);
         q = ngx_queue_next(q))
    {
        item = ngx_queue_data(q, ngx_postgres_keepalive_cache_t, queue);
        c = item->connection;

        if (ngx_memn2cmp((u_char *) &item->sockaddr, (u_char *) pc->sockaddr,
                item->socklen, pc->socklen) == 0)
        {
            ngx_queue_remove(q);
            ngx_queue_insert_head(&pgscf->free, q);

            c->idle = 0;
            c->log = pc->log;
#if defined(nginx_version) && (nginx_version >= 1001004)
            c->pool->log = pc->log;
#endif
            c->read->log = pc->log;
            c->write->log = pc->log;

            pc->connection = c;
            pc->cached = 1;

            /* we do not need to resume the peer name
             * because we already take the right value outside */

            pgp->pgconn = item->pgconn;

            dd("returning NGX_DONE");
            return NGX_DONE;
        }
    }

    dd("returning NGX_DECLINED");
    return NGX_DECLINED;
}

void
ngx_postgres_keepalive_free_peer(ngx_peer_connection_t *pc,
    ngx_postgres_upstream_peer_data_t *pgp,
    ngx_postgres_upstream_srv_conf_t *pgscf, ngx_uint_t  state)
{
    ngx_postgres_keepalive_cache_t  *item;
    ngx_queue_t                     *q;
    ngx_connection_t                *c;
    ngx_http_upstream_t             *u;

    dd("entering");

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, pc->log, 0,
                   "postgres: free keepalive peer");

    if (state & NGX_PEER_FAILED) {
        pgp->failed = 1;
    }

    u = pgp->upstream;

    if ((!pgp->failed) && (pc->connection != NULL)
        && (u->headers_in.status_n == NGX_HTTP_OK))
    {
        c = pc->connection;

        if (c->read->timer_set) {
            ngx_del_timer(c->read);
        }

        if (c->write->timer_set) {
            ngx_del_timer(c->write);
        }

        if (c->write->active && (ngx_event_flags & NGX_USE_LEVEL_EVENT)) {
            if (ngx_del_event(c->write, NGX_WRITE_EVENT, 0) != NGX_OK) {
                return;
            }
        }

        pc->connection = NULL;

        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, pc->log, 0,
                       "postgres: free keepalive peer: saving connection %p",
                       c);

        if (ngx_queue_empty(&pgscf->free)) {
            /* connection pool is already full */

            q = ngx_queue_last(&pgscf->cache);
            ngx_queue_remove(q);

            item = ngx_queue_data(q, ngx_postgres_keepalive_cache_t,
                                  queue);

            ngx_postgres_upstream_free_connection(pc->log, item->connection,
                                                  item->pgconn, pgscf);

        } else {
            q = ngx_queue_head(&pgscf->free);
            ngx_queue_remove(q);

            item = ngx_queue_data(q, ngx_postgres_keepalive_cache_t,
                                  queue);
        }

        item->connection = c;
        ngx_queue_insert_head(&pgscf->cache, q);

        c->write->handler = ngx_postgres_keepalive_dummy_handler;
        c->read->handler = ngx_postgres_keepalive_close_handler;

        c->data = item;
        c->idle = 1;
        c->log = ngx_cycle->log;
#if defined(nginx_version) && (nginx_version >= 1001004)
        c->pool->log = ngx_cycle->log;
#endif
        c->read->log = ngx_cycle->log;
        c->write->log = ngx_cycle->log;

        item->socklen = pc->socklen;
        ngx_memcpy(&item->sockaddr, pc->sockaddr, pc->socklen);

        item->pgconn = pgp->pgconn;

        item->name.data = pgp->name.data;
        item->name.len = pgp->name.len;
    }

    dd("returning");
}

void
ngx_postgres_keepalive_dummy_handler(ngx_event_t *ev)
{
    dd("entering & returning (dummy handler)");
}

void
ngx_postgres_keepalive_close_handler(ngx_event_t *ev)
{
    ngx_postgres_upstream_srv_conf_t  *pgscf;
    ngx_postgres_keepalive_cache_t    *item;
    ngx_connection_t                  *c;
    PGresult                          *res;

    dd("entering");

    c = ev->data;
    item = c->data;

    if (c->close) {
        goto close;
    }

    if (PQconsumeInput(item->pgconn) && !PQisBusy(item->pgconn)) {
        res = PQgetResult(item->pgconn);
        if (res == NULL) {
            dd("returning");
            return;
        }

        PQclear(res);

        dd("received result on idle keepalive connection");
        ngx_log_error(NGX_LOG_ERR, c->log, 0,
                      "postgres: received result on idle keepalive connection");
    }

close:

    pgscf = item->srv_conf;

    ngx_postgres_upstream_free_connection(ev->log, c, item->pgconn, pgscf);

    ngx_queue_remove(&item->queue);
    ngx_queue_insert_head(&pgscf->free, &item->queue);

    dd("returning");
}

void
ngx_postgres_keepalive_cleanup(void *data)
{
    ngx_postgres_upstream_srv_conf_t  *pgscf = data;
    ngx_postgres_keepalive_cache_t    *item;
    ngx_queue_t                       *q;

    dd("entering");

    /* ngx_queue_empty is broken when used on unitialized queue */
    if (pgscf->cache.prev == NULL) {
        dd("returning");
        return;
    }

    /* just to be on the safe-side */
    pgscf->max_cached = 0;

    while (!ngx_queue_empty(&pgscf->cache)) {
        q = ngx_queue_head(&pgscf->cache);
        ngx_queue_remove(q);

        item = ngx_queue_data(q, ngx_postgres_keepalive_cache_t,
                              queue);

        dd("postgres: disconnecting %p", item->connection);

        ngx_postgres_upstream_free_connection(item->connection->log,
                                              item->connection,
                                              item->pgconn, pgscf);
    }

    dd("returning");
}
