
/*
 * Copyright (C) Igor Sysoev
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_drizzle_module.h"
#include "ngx_http_drizzle_util.h"
#include "ngx_http_drizzle_handler.h"
#include "ngx_http_drizzle_processor.h"

#include <ngx_core.h>
#include <ngx_http.h>


static ngx_int_t ngx_http_upstream_dbd_reinit(ngx_http_request_t *r,
    ngx_http_upstream_t *u);
static void ngx_http_upstream_dbd_handler(ngx_event_t *ev);
static void ngx_http_upstream_dbd_connect(ngx_http_request_t *r,
    ngx_http_upstream_t *u);
static void ngx_http_upstream_dbd_cleanup(void *data);
static void ngx_http_upstream_dbd_wr_check_broken_connection(
    ngx_http_request_t *r);
static void ngx_http_upstream_dbd_rd_check_broken_connection(
    ngx_http_request_t *r);
static void ngx_http_upstream_dbd_check_broken_connection(ngx_http_request_t *r,
    ngx_event_t *ev);


ngx_int_t
ngx_http_drizzle_set_header(ngx_http_request_t *r, ngx_str_t *key,
    ngx_str_t *value)
{
    ngx_table_elt_t             *h;
    ngx_list_part_t             *part;
    ngx_uint_t                  i;

    dd("entered set_header");

    part = &r->headers_out.headers.part;
    h = part->elts;

    for (i = 0; /* void */; i++) {

        if (i >= part->nelts) {
            if (part->next == NULL) {
                break;
            }

            part = part->next;
            h = part->elts;
            i = 0;
        }

        if (h[i].key.len == key->len
            && ngx_strncasecmp(h[i].key.data, key->data, h[i].key.len) == 0)
        {
            if (value->len == 0) {
                h[i].hash = 0;
            }

            h[i].value = *value;

            return NGX_OK;
        }
    }

    if (value->len == 0) {
        return NGX_OK;
    }

    h = ngx_list_push(&r->headers_out.headers);

    if (h == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    h->hash = 1;
    h->key = *key;
    h->value = *value;

    return NGX_OK;
}


/* the following functions are copied directly from
   ngx_http_upstream.c in nginx 0.8.30, just because
   they're static. sigh. */

void
ngx_http_upstream_drizzle_finalize_request(ngx_http_request_t *r,
    ngx_http_upstream_t *u, ngx_int_t rc)
{
    ngx_time_t  *tp;

    dd("enter");

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
        dd("before free peer");
        u->peer.free(&u->peer, u->peer.data, 0);
        dd("after free peer");
    }

    dd("about to free peer 2, c: %p, r pool: %p", u->peer.connection, r->pool);

    if (u->peer.connection) {

#if 0 /* libdrizzle doesn't support SSL, was: (NGX_HTTP_SSL) */

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
                       "drizzle close http upstream connection: %d",
                       u->peer.connection->fd);

        dd("r pool: %p, c pool: %p", r->pool, u->peer.connection->pool);

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
        return;
    }

    r->connection->log->action = "sending to client";

    if (rc == 0) {
        rc = ngx_http_send_special(r, NGX_HTTP_LAST);
    }

    ngx_http_finalize_request(r, rc);
}


void
ngx_http_upstream_drizzle_next(ngx_http_request_t *r,
    ngx_http_upstream_t *u, ngx_int_t ft_type)
{
    ngx_uint_t  status, state;

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
        switch (ft_type) {

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
        ngx_http_upstream_drizzle_finalize_request(r, u,
                                              NGX_HTTP_CLIENT_CLOSED_REQUEST);
        return;
    }

    if (status) {
        u->state->status = status;

        if (u->peer.tries == 0 || !(u->conf->next_upstream & ft_type)) {
            ngx_http_upstream_drizzle_finalize_request(r, u, status);
            return;
        }
    }

    if (u->peer.connection) {
        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "close http upstream connection: %d",
                       u->peer.connection->fd);
#if 0 /* libdrizzle doesn't support SSL, was: (NGX_HTTP_SSL) */

        if (u->peer.connection->ssl) {
            u->peer.connection->ssl->no_wait_shutdown = 1;
            u->peer.connection->ssl->no_send_shutdown = 1;

            (void) ngx_ssl_shutdown(u->peer.connection);
        }
#endif

        dd("r pool: %p, c pool: %p", r->pool, u->peer.connection->pool);

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

    ngx_http_upstream_drizzle_finalize_request(r, u, status);
}


ngx_int_t
ngx_http_upstream_drizzle_test_connect(ngx_connection_t *c)
{
    int        err;
    socklen_t  len;

#if (NGX_HAVE_KQUEUE)

    if (ngx_event_flags & NGX_USE_KQUEUE_EVENT)  {
        if (c->write->pending_eof) {
            c->log->action = "connecting to upstream";
            (void) ngx_connection_error(c, c->write->kq_errno,
                                        "kevent() reported that connect() "
                                        "failed");
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

        if (getsockopt(c->fd, SOL_SOCKET, SO_ERROR, (void *) &err, &len)
            == -1)
        {
            err = ngx_errno;
        }

        if (err) {
            c->log->action = "connecting to upstream";
            (void) ngx_connection_error(c, err, "connect() failed");
            return NGX_ERROR;
        }
    }

    return NGX_OK;
}


void
ngx_http_upstream_dbd_init(ngx_http_request_t *r)
{
    ngx_connection_t     *c;

    c = r->connection;

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, c->log, 0,
                   "http init upstream, client timer: %d", c->read->timer_set);

    if (c->read->timer_set) {
        ngx_del_timer(c->read);
    }

    if (ngx_event_flags & NGX_USE_CLEAR_EVENT) {

        if (!c->write->active) {
            if (ngx_add_event(c->write, NGX_WRITE_EVENT, NGX_CLEAR_EVENT)
                == NGX_ERROR)
            {
                ngx_http_finalize_request(r, NGX_HTTP_INTERNAL_SERVER_ERROR);
                return;
            }
        }
    }

    ngx_http_upstream_dbd_init_request(r);
}


void
ngx_http_upstream_dbd_init_request(ngx_http_request_t *r)
{
    ngx_str_t                      *host;
    ngx_uint_t                      i;
    ngx_resolver_ctx_t             *ctx, temp;
    ngx_http_cleanup_t             *cln;
    ngx_http_upstream_t            *u;
    ngx_http_core_loc_conf_t       *clcf;
    ngx_http_upstream_srv_conf_t   *uscf, **uscfp;
    ngx_http_upstream_main_conf_t  *umcf;

#if defined(nginx_version) && nginx_version >= 8011
    if (r->aio) {
        return;
    }
#endif

    u = r->upstream;

#if 0 && (NGX_HTTP_CACHE)

    if (u->conf->cache) {
        ngx_int_t  rc;

        rc = ngx_http_upstream_cache(r, u);

        if (rc == NGX_BUSY) {
            r->write_event_handler = ngx_http_upstream_init_request;
            return;
        }

        r->write_event_handler = ngx_http_request_empty_handler;

        if (rc == NGX_DONE) {
            return;
        }

        if (rc != NGX_DECLINED) {
            ngx_http_finalize_request(r, rc);
            return;
        }
    }

#endif

    u->store = (u->conf->store || u->conf->store_lengths);

    if (!u->store && !r->post_action && !u->conf->ignore_client_abort) {
        r->read_event_handler =
                             ngx_http_upstream_dbd_rd_check_broken_connection;
        r->write_event_handler =
                             ngx_http_upstream_dbd_wr_check_broken_connection;
    }

    if (r->request_body) {
        u->request_bufs = r->request_body->bufs;
    }

    if (u->create_request(r) != NGX_OK) {
        ngx_http_finalize_request(r, NGX_HTTP_INTERNAL_SERVER_ERROR);
        return;
    }

#if 0
#if defined(nginx_version) && nginx_version >= 8022
    u->peer.local = u->conf->local;
#endif
#endif

    clcf = ngx_http_get_module_loc_conf(r, ngx_http_core_module);

#if defined(nginx_version) && nginx_version >= 8011
    u->output.alignment = clcf->directio_alignment;
#endif

    u->output.pool = r->pool;
    u->output.bufs.num = 1;
    u->output.bufs.size = clcf->client_body_buffer_size;
    u->output.output_filter = ngx_chain_writer;
    u->output.filter_ctx = &u->writer;

    u->writer.pool = r->pool;

    if (r->upstream_states == NULL) {

        r->upstream_states = ngx_array_create(r->pool, 1,
                                           sizeof(ngx_http_upstream_state_t));
        if (r->upstream_states == NULL) {
            ngx_http_finalize_request(r, NGX_HTTP_INTERNAL_SERVER_ERROR);
            return;
        }

    } else {

        u->state = ngx_array_push(r->upstream_states);
        if (u->state == NULL) {
            ngx_http_upstream_drizzle_finalize_request(r, u,
                                              NGX_HTTP_INTERNAL_SERVER_ERROR);
            return;
        }

        ngx_memzero(u->state, sizeof(ngx_http_upstream_state_t));
    }

    cln = ngx_http_cleanup_add(r, 0);
    if (cln == NULL) {
        ngx_http_finalize_request(r, NGX_HTTP_INTERNAL_SERVER_ERROR);
        return;
    }

    cln->handler = ngx_http_upstream_dbd_cleanup;
    cln->data = r;
    u->cleanup = &cln->handler;

    if (u->resolved == NULL) {

        uscf = u->conf->upstream;

    } else {

        if (u->resolved->sockaddr) {

            if (ngx_http_upstream_create_round_robin_peer(r, u->resolved)
                != NGX_OK)
            {
                ngx_http_upstream_drizzle_finalize_request(r, u,
                                              NGX_HTTP_INTERNAL_SERVER_ERROR);
                return;
            }

            ngx_http_upstream_dbd_connect(r, u);

            return;
        }

        host = &u->resolved->host;

        umcf = ngx_http_get_module_main_conf(r, ngx_http_upstream_module);

        uscfp = umcf->upstreams.elts;

        for (i = 0; i < umcf->upstreams.nelts; i++) {

            uscf = uscfp[i];

            if (uscf->host.len == host->len
                && ((uscf->port == 0 && u->resolved->no_port)
                     || uscf->port == u->resolved->port)
                && ngx_memcmp(uscf->host.data, host->data, host->len) == 0)
            {
                goto found;
            }
        }

        temp.name = *host;

        ctx = ngx_resolve_start(clcf->resolver, &temp);
        if (ctx == NULL) {
            ngx_http_upstream_drizzle_finalize_request(r, u,
                                              NGX_HTTP_INTERNAL_SERVER_ERROR);
            return;
        }

        if (ctx == NGX_NO_RESOLVER) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "no resolver defined to resolve %V", host);

            ngx_http_upstream_drizzle_finalize_request(r, u,
                    NGX_HTTP_BAD_GATEWAY);

            return;
        }

#if 0
        ctx->name = *host;
        ctx->type = NGX_RESOLVE_A;
        ctx->handler = ngx_http_upstream_resolve_handler;
        ctx->data = r;
        ctx->timeout = clcf->resolver_timeout;

        u->resolved->ctx = ctx;

        if (ngx_resolve_name(ctx) != NGX_OK) {
            u->resolved->ctx = NULL;
            ngx_http_upstream_drizzle_finalize_request(r, u,
                                               NGX_HTTP_INTERNAL_SERVER_ERROR);
            return;
        }
#endif
        return;
    }

found:

    if (uscf->peer.init(r, uscf) != NGX_OK) {
        ngx_http_upstream_drizzle_finalize_request(r, u,
                                              NGX_HTTP_INTERNAL_SERVER_ERROR);
        return;
    }

    ngx_http_upstream_dbd_connect(r, u);
}


static void
ngx_http_upstream_dbd_connect(ngx_http_request_t *r, ngx_http_upstream_t *u)
{
    ngx_int_t          rc;
    ngx_time_t        *tp;
    ngx_connection_t  *c;

    r->connection->log->action = "connecting to upstream";

    if (u->state && u->state->response_sec) {
        tp = ngx_timeofday();
        u->state->response_sec = tp->sec - u->state->response_sec;
        u->state->response_msec = tp->msec - u->state->response_msec;
    }

    u->state = ngx_array_push(r->upstream_states);
    if (u->state == NULL) {
        ngx_http_upstream_drizzle_finalize_request(r, u,
                                              NGX_HTTP_INTERNAL_SERVER_ERROR);
        return;
    }

    ngx_memzero(u->state, sizeof(ngx_http_upstream_state_t));

    tp = ngx_timeofday();
    u->state->response_sec = tp->sec;
    u->state->response_msec = tp->msec;

    rc = ngx_event_connect_peer(&u->peer);

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "http upstream connect: %i", rc);

    if (rc == NGX_ERROR) {
        ngx_http_upstream_drizzle_finalize_request(r, u,
                                              NGX_HTTP_INTERNAL_SERVER_ERROR);
        return;
    }

    u->state->peer = u->peer.name;

    if (rc == NGX_BUSY) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0, "no live upstreams");
        ngx_http_upstream_drizzle_next(r, u, NGX_HTTP_UPSTREAM_FT_NOLIVE);
        return;
    }

    if (rc == NGX_DECLINED) {
        ngx_http_upstream_drizzle_next(r, u, NGX_HTTP_UPSTREAM_FT_ERROR);
        return;
    }

    /* rc == NGX_OK || rc == NGX_AGAIN */

    c = u->peer.connection;

    c->data = r;

    c->write->handler = ngx_http_upstream_dbd_handler;
    c->read->handler = ngx_http_upstream_dbd_handler;

    u->write_event_handler = ngx_http_drizzle_wev_handler;
    u->read_event_handler = ngx_http_drizzle_rev_handler;

    c->sendfile &= r->connection->sendfile;
    u->output.sendfile = c->sendfile;

    c->pool = r->pool;
    c->log = r->connection->log;
    c->read->log = c->log;
    c->write->log = c->log;

    /* init or reinit the ngx_output_chain() and ngx_chain_writer() contexts */

    u->writer.out = NULL;
    u->writer.last = &u->writer.out;
    u->writer.connection = c;
    u->writer.limit = 0;

    if (u->request_sent) {
        if (ngx_http_upstream_dbd_reinit(r, u) != NGX_OK) {
            ngx_http_upstream_drizzle_finalize_request(r, u,
                                              NGX_HTTP_INTERNAL_SERVER_ERROR);
            return;
        }
    }

    if (r->request_body
        && r->request_body->buf
        && r->request_body->temp_file
        && r == r->main)
    {
        /*
         * the r->request_body->buf can be reused for one request only,
         * the subrequests should allocate their own temporay bufs
         */

        u->output.free = ngx_alloc_chain_link(r->pool);
        if (u->output.free == NULL) {
            ngx_http_upstream_drizzle_finalize_request(r, u,
                                              NGX_HTTP_INTERNAL_SERVER_ERROR);
            return;
        }

        u->output.free->buf = r->request_body->buf;
        u->output.free->next = NULL;
        u->output.allocated = 1;

        r->request_body->buf->pos = r->request_body->buf->start;
        r->request_body->buf->last = r->request_body->buf->start;
        r->request_body->buf->tag = u->output.tag;
    }

    u->request_sent = 0;

    if (rc == NGX_AGAIN) {
        ngx_add_timer(c->write, u->conf->connect_timeout);
        return;
    }

#if 0 /* libdrizzle doesn't support SSL, was: (NGX_HTTP_SSL) */

    if (u->ssl && c->ssl == NULL) {
        ngx_http_upstream_ssl_init_connection(r, u, c);
        return;
    }

#endif

#if 0
    ngx_http_upstream_send_request(r, u);
#endif

    dd("connection error: %d", c->error);

    ngx_http_drizzle_set_libdrizzle_ready(r);

    (void) ngx_http_drizzle_process_events(r);
}


static void
ngx_http_upstream_dbd_rd_check_broken_connection(ngx_http_request_t *r)
{
    ngx_http_upstream_dbd_check_broken_connection(r, r->connection->read);
}


static void
ngx_http_upstream_dbd_wr_check_broken_connection(ngx_http_request_t *r)
{
    ngx_http_upstream_dbd_check_broken_connection(r, r->connection->write);
}


static void
ngx_http_upstream_dbd_check_broken_connection(ngx_http_request_t *r,
    ngx_event_t *ev)
{
    int                  n;
    char                 buf[1];
    ngx_err_t            err;
    ngx_int_t            event;
    ngx_connection_t     *c;
    ngx_http_upstream_t  *u;

    ngx_log_debug2(NGX_LOG_DEBUG_HTTP, ev->log, 0,
                   "http upstream check client, write event:%d, \"%V\"",
                   ev->write, &r->uri);

    c = r->connection;
    u = r->upstream;

    if (c->error) {
        if ((ngx_event_flags & NGX_USE_LEVEL_EVENT) && ev->active) {

            event = ev->write ? NGX_WRITE_EVENT : NGX_READ_EVENT;

            if (ngx_del_event(ev, event, 0) != NGX_OK) {
                ngx_http_upstream_drizzle_finalize_request(r, u,
                                              NGX_HTTP_INTERNAL_SERVER_ERROR);
                return;
            }
        }

        if (!u->cacheable) {
            ngx_http_upstream_drizzle_finalize_request(r, u,
                                              NGX_HTTP_CLIENT_CLOSED_REQUEST);
        }

        return;
    }

#if (NGX_HAVE_KQUEUE)

    if (ngx_event_flags & NGX_USE_KQUEUE_EVENT) {

        if (!ev->pending_eof) {
            return;
        }

        ev->eof = 1;
        c->error = 1;

        if (ev->kq_errno) {
            ev->error = 1;
        }

        if (!u->cacheable && u->peer.connection) {
            ngx_log_error(NGX_LOG_INFO, ev->log, ev->kq_errno,
                          "kevent() reported that client closed prematurely "
                          "connection, so upstream connection is closed too");
            ngx_http_upstream_drizzle_finalize_request(r, u,
                                               NGX_HTTP_CLIENT_CLOSED_REQUEST);
            return;
        }

        ngx_log_error(NGX_LOG_INFO, ev->log, ev->kq_errno,
                      "kevent() reported that client closed "
                      "prematurely connection");

        if (u->peer.connection == NULL) {
            ngx_http_upstream_drizzle_finalize_request(r, u,
                                               NGX_HTTP_CLIENT_CLOSED_REQUEST);
        }

        return;
    }

#endif

    n = recv(c->fd, buf, 1, MSG_PEEK);

    err = ngx_socket_errno;

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, ev->log, err,
                   "http upstream recv(): %d", n);

    if (ev->write && (n >= 0 || err == NGX_EAGAIN)) {
        return;
    }

    if ((ngx_event_flags & NGX_USE_LEVEL_EVENT) && ev->active) {

        event = ev->write ? NGX_WRITE_EVENT : NGX_READ_EVENT;

        if (ngx_del_event(ev, event, 0) != NGX_OK) {
            ngx_http_upstream_drizzle_finalize_request(r, u,
                                               NGX_HTTP_INTERNAL_SERVER_ERROR);
            return;
        }
    }

    if (n > 0) {
        return;
    }

    if (n == -1) {
        if (err == NGX_EAGAIN) {
            return;
        }

        ev->error = 1;

    } else { /* n == 0 */
        err = 0;
    }

    ev->eof = 1;
    c->error = 1;

    if (!u->cacheable && u->peer.connection) {
        ngx_log_error(NGX_LOG_INFO, ev->log, err,
                      "client closed prematurely connection, "
                      "so upstream connection is closed too");
        ngx_http_upstream_drizzle_finalize_request(r, u,
                                           NGX_HTTP_CLIENT_CLOSED_REQUEST);
        return;
    }

    ngx_log_error(NGX_LOG_INFO, ev->log, err,
                  "client closed prematurely connection");

    if (u->peer.connection == NULL) {
        ngx_http_upstream_drizzle_finalize_request(r, u,
                                           NGX_HTTP_CLIENT_CLOSED_REQUEST);
    }
}


static void
ngx_http_upstream_dbd_cleanup(void *data)
{
    ngx_http_request_t *r = data;

    ngx_http_upstream_t  *u;

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "cleanup http upstream request: \"%V\"", &r->uri);

    u = r->upstream;

    if (u->resolved && u->resolved->ctx) {
        ngx_resolve_name_done(u->resolved->ctx);
        u->resolved->ctx = NULL;
    }

    ngx_http_upstream_drizzle_finalize_request(r, u, NGX_DONE);
}


static void
ngx_http_upstream_dbd_handler(ngx_event_t *ev)
{
    ngx_connection_t     *c;
    ngx_http_request_t   *r;
    ngx_http_log_ctx_t   *ctx;
    ngx_http_upstream_t  *u;

    c = ev->data;
    r = c->data;

    u = r->upstream;
    c = r->connection;

    ctx = c->log->data;
    ctx->current_request = r;

    ngx_log_debug2(NGX_LOG_DEBUG_HTTP, c->log, 0,
                   "http upstream request: \"%V?%V\"", &r->uri, &r->args);

    dd("upstream ev write:%d, ready:%d", (int) ev->write, (int) ev->ready);

    if (ev->write) {
        u->write_event_handler(r, u);

    } else {
        u->read_event_handler(r, u);
    }

    ngx_http_run_posted_requests(c);
}


static ngx_int_t
ngx_http_upstream_dbd_reinit(ngx_http_request_t *r, ngx_http_upstream_t *u)
{
    ngx_chain_t  *cl;

    if (u->reinit_request(r) != NGX_OK) {
        return NGX_ERROR;
    }

    ngx_memzero(&u->headers_in, sizeof(ngx_http_upstream_headers_in_t));

    if (ngx_list_init(&u->headers_in.headers, r->pool, 8,
                      sizeof(ngx_table_elt_t))
        != NGX_OK)
    {
        return NGX_ERROR;
    }

    /* reinit the request chain */

    for (cl = u->request_bufs; cl; cl = cl->next) {
        cl->buf->pos = cl->buf->start;
        cl->buf->file_pos = 0;
    }

    /* reinit the subrequest's ngx_output_chain() context */

    if (r->request_body && r->request_body->temp_file
        && r != r->main && u->output.buf)
    {
        u->output.free = ngx_alloc_chain_link(r->pool);
        if (u->output.free == NULL) {
            return NGX_ERROR;
        }

        u->output.free->buf = u->output.buf;
        u->output.free->next = NULL;

        u->output.buf->pos = u->output.buf->start;
        u->output.buf->last = u->output.buf->start;
    }

    u->output.buf = NULL;
    u->output.in = NULL;
    u->output.busy = NULL;

    /* reinit u->buffer */

    u->buffer.pos = u->buffer.start;

#if 0 && (NGX_HTTP_CACHE)

    if (r->cache) {
        u->buffer.pos += r->cache->header_start;
    }

#endif

    u->buffer.last = u->buffer.pos;

    return NGX_OK;
}


ngx_int_t
ngx_http_drizzle_set_thread_id_variable(ngx_http_request_t *r,
    ngx_http_upstream_t *u)
{
    ngx_http_drizzle_loc_conf_t    *dlcf;
    size_t                          size;
    ngx_http_variable_value_t      *vv;
    uint32_t                        tid;
    drizzle_con_st                 *dc;

    ngx_http_upstream_drizzle_peer_data_t       *dp;

    dp = r->upstream->peer.data;
    if (dp == NULL) {
        return NGX_ERROR;
    }

    dc = dp->drizzle_con;
    if (dc == NULL) {
        return NGX_ERROR;
    }

    tid = drizzle_con_thread_id(dc);

    dd("tid = %d", (int) tid);

    if (tid == 0) {
        /* invalid thread id */
        return NGX_OK;
    }

    size = ngx_http_drizzle_get_num_size(tid);

    dlcf = ngx_http_get_module_loc_conf(r, ngx_http_drizzle_module);

    vv = ngx_http_get_indexed_variable(r, dlcf->tid_var_index);

    if (vv == NULL) {
        return NGX_ERROR;
    }

    vv->not_found = 0;
    vv->valid = 1;
    vv->no_cacheable = 0;

    vv->data = ngx_palloc(r->pool, size);
    if (vv->data == NULL) {
        return NGX_ERROR;
    }

    vv->len = size;

    ngx_sprintf(vv->data, "%uD", tid);

    dd("$drizzle_thread_id set");

    return NGX_OK;
}


size_t
ngx_http_drizzle_get_num_size(uint64_t i)
{
    size_t          n = 0;

    do {
        i = i / 10;
        n++;
    } while (i > 0);

    return n;
}


ngx_uint_t
ngx_http_drizzle_queue_size(ngx_queue_t *queue)
{
    ngx_queue_t     *q;
    ngx_uint_t       n = 0;

   for (q = ngx_queue_head(queue);
         q != ngx_queue_sentinel(queue);
         q = ngx_queue_next(q))
    {
        n++;
    }

    return n;
}
