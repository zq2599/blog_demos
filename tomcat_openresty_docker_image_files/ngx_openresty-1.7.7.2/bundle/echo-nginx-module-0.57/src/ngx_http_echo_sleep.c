
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_echo_sleep.h"
#include "ngx_http_echo_handler.h"

#include <nginx.h>
#include <ngx_log.h>


/* event handler for echo_sleep */

static void ngx_http_echo_post_sleep(ngx_http_request_t *r);
static void ngx_http_echo_sleep_cleanup(void *data);


ngx_int_t
ngx_http_echo_exec_echo_sleep(ngx_http_request_t *r,
    ngx_http_echo_ctx_t *ctx, ngx_array_t *computed_args)
{
    ngx_str_t                   *computed_arg;
    ngx_str_t                   *computed_arg_elts;
    ngx_int_t                    delay; /* in msec */
    ngx_http_cleanup_t          *cln;

    computed_arg_elts = computed_args->elts;
    computed_arg = &computed_arg_elts[0];

    delay = ngx_atofp(computed_arg->data, computed_arg->len, 3);

    if (delay == NGX_ERROR) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                   "invalid sleep duration \"%V\"", &computed_arg_elts[0]);

        return NGX_HTTP_BAD_REQUEST;
    }

    dd("adding timer with delay %lu ms, r:%.*s", (unsigned long) delay,
            (int) r->uri.len, r->uri.data);

    ngx_add_timer(&ctx->sleep, (ngx_msec_t) delay);

    /* we don't check broken downstream connections
     * ourselves so even if the client shuts down
     * the connection prematurely, nginx will still
     * go on waiting for our timers to get properly
     * expired. However, we'd still register a
     * cleanup handler for completeness. */

    cln = ngx_http_cleanup_add(r, 0);
    if (cln == NULL) {
        return NGX_ERROR;
    }

    cln->handler = ngx_http_echo_sleep_cleanup;
    cln->data = r;

    return NGX_AGAIN;
}


static void
ngx_http_echo_post_sleep(ngx_http_request_t *r)
{
    ngx_http_echo_ctx_t         *ctx;
    /* ngx_int_t                    rc; */

    dd("post sleep, r:%.*s", (int) r->uri.len, r->uri.data);

    ctx = ngx_http_get_module_ctx(r, ngx_http_echo_module);

    if (ctx == NULL) {
        return;
    }

    ctx->waiting = 0;
    ctx->done = 1;

    dd("sleep: after get module ctx");

    dd("timed out? %d", ctx->sleep.timedout);
    dd("timer set? %d", ctx->sleep.timer_set);

    if (!ctx->sleep.timedout) {
        dd("HERE reached!");
        return;
    }

    ctx->sleep.timedout = 0;

    if (ctx->sleep.timer_set) {
        dd("deleting timer for echo_sleep");

        ngx_del_timer(&ctx->sleep);
    }

    /* r->write_event_handler = ngx_http_request_empty_handler; */

    ngx_http_echo_wev_handler(r);
}


void
ngx_http_echo_sleep_event_handler(ngx_event_t *ev)
{
    ngx_connection_t        *c;
    ngx_http_request_t      *r;
    ngx_http_log_ctx_t      *ctx;

    r = ev->data;
    c = r->connection;

    if (c->destroyed) {
        return;
    }

    if (c->error) {
        ngx_http_finalize_request(r, NGX_ERROR);
        return;
    }

    ctx = c->log->data;
    ctx->current_request = r;

    /* XXX when r->done == 1 we should do cleaning immediately
     * and delete our timer and then quit. */

    ngx_log_debug2(NGX_LOG_DEBUG_HTTP, c->log, 0,
            "echo sleep handler: \"%V?%V\"", &r->uri, &r->args);

    /*
    if (r->done) {
        return;
    }
    */

    ngx_http_echo_post_sleep(r);

#if defined(nginx_version)

    dd("before run posted requests");

    ngx_http_run_posted_requests(c);

    dd("after run posted requests");

#endif
}


ngx_int_t
ngx_http_echo_exec_echo_blocking_sleep(ngx_http_request_t *r,
    ngx_http_echo_ctx_t *ctx, ngx_array_t *computed_args)
{
    ngx_str_t                   *computed_arg;
    ngx_str_t                   *computed_arg_elts;
    ngx_int_t                    delay; /* in msec */

    computed_arg_elts = computed_args->elts;
    computed_arg = &computed_arg_elts[0];

    delay = ngx_atofp(computed_arg->data, computed_arg->len, 3);

    if (delay == NGX_ERROR) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                   "invalid sleep duration \"%V\"", &computed_arg_elts[0]);
        return NGX_HTTP_BAD_REQUEST;
    }

    dd("blocking delay: %lu ms", (unsigned long) delay);

    ngx_msleep((ngx_msec_t) delay);

    return NGX_OK;
}


static void
ngx_http_echo_sleep_cleanup(void *data)
{
    ngx_http_request_t      *r = data;
    ngx_http_echo_ctx_t         *ctx;

    dd("echo sleep cleanup");

    ctx = ngx_http_get_module_ctx(r, ngx_http_echo_module);
    if (ctx == NULL) {
        return;
    }

    if (ctx->sleep.timer_set) {
        dd("cleanup: deleting timer for echo_sleep");

        ngx_del_timer(&ctx->sleep);
        return;
    }

    dd("cleanup: timer not set");
}
