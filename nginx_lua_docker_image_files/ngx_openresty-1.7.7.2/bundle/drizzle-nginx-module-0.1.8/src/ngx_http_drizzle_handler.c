
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_drizzle_module.h"
#include "ngx_http_drizzle_handler.h"
#include "ngx_http_drizzle_processor.h"
#include "ngx_http_drizzle_util.h"
#include "ngx_http_drizzle_upstream.h"
#include "ngx_http_drizzle_keepalive.h"


#ifdef _WIN32
/* import the POLLIN and POLLOUT flags */
#   ifndef WIN32_LEAN_AND_MEAN
#       define WIN32_LEAN_AND_MEAN
#   endif
#   include <winsock2.h>
#endif


/* for read/write event handlers */


static ngx_int_t ngx_http_drizzle_create_request(ngx_http_request_t *r);
static ngx_int_t ngx_http_drizzle_reinit_request(ngx_http_request_t *r);
static void ngx_http_drizzle_abort_request(ngx_http_request_t *r);
static void ngx_http_drizzle_finalize_request(ngx_http_request_t *r,
        ngx_int_t rc);
static ngx_int_t ngx_http_drizzle_process_header(ngx_http_request_t *r);
static ngx_int_t ngx_http_drizzle_input_filter_init(void *data);
static ngx_int_t ngx_http_drizzle_input_filter(void *data, ssize_t bytes);


ngx_int_t
ngx_http_drizzle_handler(ngx_http_request_t *r)
{
    ngx_http_upstream_t            *u;
    ngx_http_drizzle_loc_conf_t    *dlcf;
#if defined(nginx_version) && nginx_version < 8017
    ngx_http_drizzle_ctx_t         *dctx;
#endif
    ngx_http_core_loc_conf_t       *clcf;
    ngx_str_t                       target;
    ngx_url_t                       url;
    ngx_connection_t               *c;

    dd("request: %p", r);
    dd("subrequest in memory: %d", (int) r->subrequest_in_memory);
    dd("connection: %p", r->connection);
    dd("connection log: %p", r->connection->log);

    if (r->subrequest_in_memory) {
        /* TODO: add support for subrequest in memory by
         * emitting output into u->buffer instead */

        ngx_log_error(NGX_LOG_ALERT, r->connection->log, 0,
                      "ngx_http_drizzle_module does not support "
                      "subrequest in memory");

        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    dlcf = ngx_http_get_module_loc_conf(r, ngx_http_drizzle_module);

    if ((dlcf->default_query == NULL) && !(dlcf->methods_set & r->method)) {
        if (dlcf->methods_set != 0) {
            return NGX_HTTP_NOT_ALLOWED;
        }

        clcf = ngx_http_get_module_loc_conf(r, ngx_http_core_module);

        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "drizzle: missing \"drizzle_query\" in location \"%V\"",
                      &clcf->name);

        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    dd("XXX upstream already exists? %p", r->upstream);

#if defined(nginx_version) && \
    ((nginx_version >= 7063 && nginx_version < 8000) \
     || nginx_version >= 8007)

    dd("creating upstream.......");
    if (ngx_http_upstream_create(r) != NGX_OK) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    u = r->upstream;

#else /* 0.7.x < 0.7.63, 0.8.x < 0.8.7 */

    dd("XXX create upstream");
    u = ngx_pcalloc(r->pool, sizeof(ngx_http_upstream_t));
    if (u == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    u->peer.log = r->connection->log;
    u->peer.log_error = NGX_ERROR_ERR;
#  if (NGX_THREADS)
    u->peer.lock = &r->connection->lock;
#  endif

    r->upstream = u;

#endif

    if (dlcf->complex_target) {
        /* variables used in the drizzle_pass directive */
        if (ngx_http_complex_value(r, dlcf->complex_target, &target)
                != NGX_OK)
        {
            dd("failed to compile");
            return NGX_ERROR;
        }

        if (target.len == 0) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                    "drizzle: handler: empty \"drizzle_pass\" target");
            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }

        url.host = target;
        url.port = 0;
        url.no_resolve = 1;

        dlcf->upstream.upstream = ngx_http_upstream_drizzle_add(r, &url);

        if (dlcf->upstream.upstream == NULL) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                   "drizzle: upstream \"%V\" not found", &target);

            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }
    }

#if defined(nginx_version) && nginx_version < 8017
    dctx = ngx_pcalloc(r->pool, sizeof(ngx_http_drizzle_ctx_t));
    if (dctx == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    ngx_http_set_ctx(r, dctx, ngx_http_drizzle_module);
#endif

    u->schema.len = sizeof("drizzle://") - 1;
    u->schema.data = (u_char *) "drizzle://";

    u->output.tag = (ngx_buf_tag_t) &ngx_http_drizzle_module;

    dd("drizzle tag: %p", (void *) u->output.tag);

    u->conf = &dlcf->upstream;

    u->create_request = ngx_http_drizzle_create_request;
    u->reinit_request = ngx_http_drizzle_reinit_request;
    u->process_header = ngx_http_drizzle_process_header;
    u->abort_request = ngx_http_drizzle_abort_request;
    u->finalize_request = ngx_http_drizzle_finalize_request;

    /* we bypass the upstream input filter mechanism in
     * ngx_http_upstream_process_headers */

    u->input_filter_init = ngx_http_drizzle_input_filter_init;
    u->input_filter = ngx_http_drizzle_input_filter;
    u->input_filter_ctx = NULL;

#if defined(nginx_version) && nginx_version >= 8011
    r->main->count++;
#endif

    dd("XXX connect timeout: %d", (int) dlcf->upstream.connect_timeout);

    ngx_http_upstream_dbd_init(r);

    /* override the read/write event handler to our own */
    u->write_event_handler = ngx_http_drizzle_wev_handler;
    u->read_event_handler  = ngx_http_drizzle_rev_handler;

    /* a bit hack-ish way to return error response (clean-up part) */
    if ((u->peer.connection) && (u->peer.connection->fd == 0)) {
        c = u->peer.connection;
        u->peer.connection = NULL;

        if (c->write->timer_set) {
            ngx_del_timer(c->write);
        }

        ngx_free_connection(c);

        ngx_http_upstream_drizzle_finalize_request(r, u,
#if defined(nginx_version) && (nginx_version >= 8017)
            NGX_HTTP_SERVICE_UNAVAILABLE);
#else
            dctx->status ? dctx->status : NGX_HTTP_INTERNAL_SERVER_ERROR);
#endif
    }

    return NGX_DONE;
}


void
ngx_http_drizzle_wev_handler(ngx_http_request_t *r, ngx_http_upstream_t *u)
{
    ngx_connection_t            *c;

    dd("drizzle wev handler");

    /* just to ensure u->reinit_request always gets called for
     * upstream_next */
    u->request_sent = 1;

    c = u->peer.connection;

    if (c->write->timedout) {
        dd("drizzle connection write timeout");

        ngx_http_drizzle_set_thread_id_variable(r, u);

        ngx_http_upstream_drizzle_next(r, u,
                NGX_HTTP_UPSTREAM_FT_TIMEOUT);
        return;
    }

    if (ngx_http_upstream_drizzle_test_connect(c) != NGX_OK) {
        dd("drizzle connection is broken");

        ngx_http_upstream_drizzle_next(r, u, NGX_HTTP_UPSTREAM_FT_ERROR);
        return;
    }

    ngx_http_drizzle_set_libdrizzle_ready(r);

    (void) ngx_http_drizzle_process_events(r);
}


void
ngx_http_drizzle_rev_handler(ngx_http_request_t *r, ngx_http_upstream_t *u)
{
    ngx_connection_t            *c;

    dd("drizzle rev handler");

    /* just to ensure u->reinit_request always gets called for
     * upstream_next */
    u->request_sent = 1;

    c = u->peer.connection;

    if (c->read->timedout) {
        dd("drizzle connection read timeout");
        ngx_http_drizzle_set_thread_id_variable(r, u);

        ngx_http_upstream_drizzle_next(r, u,
                NGX_HTTP_UPSTREAM_FT_TIMEOUT);
        return;
    }

    if (ngx_http_upstream_drizzle_test_connect(c) != NGX_OK) {
        dd("drizzle connection is broken");

        ngx_http_upstream_drizzle_next(r, u, NGX_HTTP_UPSTREAM_FT_ERROR);
        return;
    }

    ngx_http_drizzle_set_libdrizzle_ready(r);

    (void) ngx_http_drizzle_process_events(r);
}


static ngx_int_t
ngx_http_drizzle_create_request(ngx_http_request_t *r)
{
    r->upstream->request_bufs = NULL;

    return NGX_OK;
}


static ngx_int_t
ngx_http_drizzle_reinit_request(ngx_http_request_t *r)
{
    ngx_http_upstream_t         *u;

    u = r->upstream;

    /* override the read/write event handler to our own */
    u->write_event_handler = ngx_http_drizzle_wev_handler;
    u->read_event_handler  = ngx_http_drizzle_rev_handler;

    return NGX_OK;
}


static void
ngx_http_drizzle_abort_request(ngx_http_request_t *r)
{
}


static void
ngx_http_drizzle_finalize_request(ngx_http_request_t *r,
    ngx_int_t rc)
{
}


static ngx_int_t
ngx_http_drizzle_process_header(ngx_http_request_t *r)
{
    ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
           "ngx_http_drizzle_process_header should not be called"
           " by the upstream");

    return NGX_ERROR;
}


static ngx_int_t
ngx_http_drizzle_input_filter_init(void *data)
{
    ngx_http_request_t          *r = data;

    ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
           "ngx_http_drizzle_input_filter_init should not be called"
           " by the upstream");

    return NGX_ERROR;
}


static ngx_int_t
ngx_http_drizzle_input_filter(void *data, ssize_t bytes)
{
    ngx_http_request_t          *r = data;

    ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
           "ngx_http_drizzle_input_filter should not be called"
           " by the upstream");

    return NGX_ERROR;
}


void
ngx_http_drizzle_set_libdrizzle_ready(ngx_http_request_t *r)
{
    ngx_http_upstream_drizzle_peer_data_t       *dp;
    drizzle_con_st                              *dc;
#if 1
    short                                        revents = 0;
#endif

    dp = r->upstream->peer.data;

    dc = dp->drizzle_con;

#if 0
    /* libdrizzle uses standard poll() event constants
     * and depends on drizzle_con_wait() to set them.
     * we can directly call drizzle_con_wait() here to
     * set those drizzle internal event states, because
     * epoll() and other underlying event mechamism used
     * by the nginx core can play well enough with poll().
     * */

    (void) drizzle_con_wait(dc->drizzle);
#endif

#if 1
    revents |= POLLOUT;
    revents |= POLLIN;

    /* drizzle_con_set_revents() isn't declared external in libdrizzle-0.4.0, */
    /* so we have to do its job all by ourselves... */

    dc->options |= DRIZZLE_CON_IO_READY;
    dc->revents = revents;
    dc->events &= (short) ~revents;
#endif
}


ngx_int_t
ngx_http_drizzle_status_handler(ngx_http_request_t *r)
{
    ngx_http_upstream_main_conf_t  *umcf;
    ngx_http_upstream_srv_conf_t  **uscfp;
    ngx_http_upstream_srv_conf_t   *uscf;
    ngx_uint_t                      i, n;
    ngx_chain_t                    *cl;
    ngx_buf_t                      *b;
    size_t                          len;
    ngx_int_t                       rc;
    ngx_queue_t                    *q;

    ngx_http_drizzle_keepalive_cache_t      *item;
    ngx_http_upstream_drizzle_srv_conf_t    *dscf;

    umcf = ngx_http_get_module_main_conf(r, ngx_http_upstream_module);

    uscfp = umcf->upstreams.elts;

    /* calculate the output buffer length */

    len = 0;

    if (ngx_process == NGX_PROCESS_WORKER) {
        len += sizeof("worker process: \n\n") - 1
             + ngx_http_drizzle_get_num_size(ngx_pid);
    }

    n = 0;

    for (i = 0; i < umcf->upstreams.nelts; i++) {
        uscf = uscfp[i];

        if (uscf->srv_conf == NULL) {
            /* skip implicit upstream specified directly by the fastcgi_pass,
             * proxy_pass, and similar directives */

            continue;
        }

        dscf = ngx_http_conf_upstream_srv_conf(uscf, ngx_http_drizzle_module);

        if (dscf == NULL || dscf->servers == NULL) {
            continue;
        }

        if (n != 0) {
            len += sizeof("\n") - 1;
        }

        n++;

        len += sizeof("upstream \n") - 1
             + uscf->host.len
             + sizeof("  active connections: \n") - 1
             + ngx_http_drizzle_get_num_size(dscf->active_conns)
             + sizeof("  connection pool capacity: \n") - 1
             + ngx_http_drizzle_get_num_size(dscf->max_cached);

        if (dscf->max_cached) {
            /* dump overflow flag for the connection pool */

            switch (dscf->overflow) {
                case drizzle_keepalive_overflow_ignore:
                    len += sizeof("  overflow: ignore\n") - 1;
                    break;

                case drizzle_keepalive_overflow_reject:
                    len += sizeof("  overflow: reject\n") - 1;
                    break;

                default:
                    len += sizeof("  overflow: N/A\n") - 1;
                    break;
            }

            /* dump the lengths of the "cache" and "free" queues in the pool */

            len += sizeof("  cached connection queue: \n") - 1
                 + ngx_http_drizzle_get_num_size(
                         ngx_http_drizzle_queue_size(&dscf->cache)
                   )
                 + sizeof("  free'd connection queue: \n") - 1
                 + ngx_http_drizzle_get_num_size(
                         ngx_http_drizzle_queue_size(&dscf->free)
                   )

            /* dump how many times that each individual connection in the
             * pool has been successfully used in the "cache" queue */

                 + sizeof("  cached connection successfully used count:\n") - 1;

            for (q = ngx_queue_head(&dscf->cache);
                 q != ngx_queue_sentinel(&dscf->cache);
                 q = ngx_queue_next(q))
            {
                item = ngx_queue_data(q, ngx_http_drizzle_keepalive_cache_t,
                        queue);

                len += sizeof(" ") - 1
                     + ngx_http_drizzle_get_num_size(item->used);
            }

            /* dump how many times that each individual connection in the
             * pool has been successfully used in the "free" queue */

            len += sizeof("  free'd connection successfully used count:\n") - 1;

            for (q = ngx_queue_head(&dscf->free);
                 q != ngx_queue_sentinel(&dscf->free);
                 q = ngx_queue_next(q))
            {
                item = ngx_queue_data(q, ngx_http_drizzle_keepalive_cache_t,
                        queue);

                len += sizeof(" ") - 1
                     + ngx_http_drizzle_get_num_size(item->used);
            }
        }

        len += sizeof("  servers: \n") - 1
             + ngx_http_drizzle_get_num_size(dscf->servers->nelts)
             + sizeof("  peers: \n") - 1
             + ngx_http_drizzle_get_num_size(dscf->peers->number);
    }

    /* allocate the output buffer */

    b = ngx_create_temp_buf(r->pool, len);
    if (b == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    /* fill in the output buffer with the actual data */

    if (ngx_process == NGX_PROCESS_WORKER) {
        b->last = ngx_sprintf(b->last, "worker process: %P\n\n", ngx_pid);
    }

    n = 0;

    for (i = 0; i < umcf->upstreams.nelts; i++) {
        uscf = uscfp[i];

        if (uscf->srv_conf == NULL) {
            /* skip implicit upstream specified directly by the fastcgi_pass,
             * proxy_pass, and similar directives */

            continue;
        }

        dscf = ngx_http_conf_upstream_srv_conf(uscf, ngx_http_drizzle_module);

        if (dscf == NULL || dscf->servers == NULL) {
            continue;
        }

        if (n != 0) {
            *b->last++ = '\n';
        }

        n++;

        b->last = ngx_copy_const_str(b->last, "upstream ");
        b->last = ngx_copy(b->last, uscf->host.data, uscf->host.len);

        *b->last++ = '\n';

        b->last = ngx_sprintf(b->last, "  active connections: %uD\n",
                dscf->active_conns);

        b->last = ngx_sprintf(b->last, "  connection pool capacity: %uD\n",
                dscf->max_cached);

        if (dscf->max_cached) {
            /* dump overflow flag for the connection pool */

            switch (dscf->overflow) {
                case drizzle_keepalive_overflow_ignore:
                    b->last = ngx_copy_const_str(b->last,
                            "  overflow: ignore\n");
                    break;

                case drizzle_keepalive_overflow_reject:
                    b->last = ngx_copy_const_str(b->last,
                            "  overflow: reject\n");
                    break;

                default:
                    b->last = ngx_copy_const_str(b->last, "  overflow: N/A\n");
                    break;
            }

            /* dump the lengths of the "cache" and "free" queues in the pool */

            b->last = ngx_sprintf(b->last, "  cached connection queue: %uD\n",
                    ngx_http_drizzle_queue_size(&dscf->cache));

            b->last = ngx_sprintf(b->last, "  free'd connection queue: %uD\n",
                    ngx_http_drizzle_queue_size(&dscf->free));

            /* dump how many times that each individual connection in the
             * pool has been successfully used in the "cache" queue */

            b->last = ngx_copy_const_str(b->last,
                    "  cached connection successfully used count:");

            for (q = ngx_queue_head(&dscf->cache);
                 q != ngx_queue_sentinel(&dscf->cache);
                 q = ngx_queue_next(q))
            {
                item = ngx_queue_data(q, ngx_http_drizzle_keepalive_cache_t,
                        queue);
                b->last = ngx_sprintf(b->last, " %uD", item->used);
            }

            *b->last++ = '\n';

            /* dump how many times that each individual connection in the
             * pool has been successfully used in the "free" queue */

            b->last = ngx_copy_const_str(b->last,
                    "  free'd connection successfully used count:");

            for (q = ngx_queue_head(&dscf->free);
                 q != ngx_queue_sentinel(&dscf->free);
                 q = ngx_queue_next(q))
            {
                item = ngx_queue_data(q, ngx_http_drizzle_keepalive_cache_t,
                        queue);
                b->last = ngx_sprintf(b->last, " %uD", item->used);
            }

            *b->last++ = '\n';
        }

        b->last = ngx_sprintf(b->last, "  servers: %uD\n",
                dscf->servers->nelts);

        b->last = ngx_sprintf(b->last, "  peers: %uD\n", dscf->peers->number);
    }

    if (b->last != b->end) {
        ngx_log_error(NGX_LOG_ALERT, r->connection->log, 0,
                      "drizzle_status output buffer error: %O != %O",
                      (off_t) (b->last - b->pos),
                      (off_t) (b->end - b->pos));

        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    if (r == r->main) {
        b->last_buf = 1;
    }

    cl = ngx_alloc_chain_link(r->pool);
    if (cl == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    cl->buf = b;
    cl->next = NULL;

    r->headers_out.status = NGX_HTTP_OK;

    rc = ngx_http_send_header(r);

    if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
        return rc;
    }

    return ngx_http_output_filter(r, cl);
}
