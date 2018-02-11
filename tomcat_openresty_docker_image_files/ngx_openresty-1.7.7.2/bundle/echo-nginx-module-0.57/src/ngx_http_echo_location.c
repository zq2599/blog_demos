#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_echo_util.h"
#include "ngx_http_echo_location.h"
#include "ngx_http_echo_handler.h"

#include <nginx.h>


static ngx_int_t ngx_http_echo_adjust_subrequest(ngx_http_request_t *sr);


ngx_int_t
ngx_http_echo_exec_echo_location_async(ngx_http_request_t *r,
    ngx_http_echo_ctx_t *ctx, ngx_array_t *computed_args)
{
    ngx_int_t                    rc;
    ngx_http_request_t          *sr; /* subrequest object */
    ngx_str_t                   *computed_arg_elts;
    ngx_str_t                    location;
    ngx_str_t                   *url_args;
    ngx_str_t                    args;
    ngx_uint_t                   flags = 0;

    dd_enter();

    computed_arg_elts = computed_args->elts;

    location = computed_arg_elts[0];

    if (location.len == 0) {
        return NGX_ERROR;
    }

    if (computed_args->nelts > 1) {
        url_args = &computed_arg_elts[1];
    } else {
        url_args = NULL;
    }

    args.data = NULL;
    args.len = 0;

    if (ngx_http_parse_unsafe_uri(r, &location, &args, &flags) != NGX_OK) {
        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "echo_location_async sees unsafe uri: \"%V\"",
                       &location);
        return NGX_ERROR;
    }

    if (args.len > 0 && url_args == NULL) {
        url_args = &args;
    }

    rc = ngx_http_echo_send_header_if_needed(r, ctx);
    if (rc == NGX_ERROR || rc > NGX_OK || r->header_only) {
        return rc;
    }

    rc = ngx_http_subrequest(r, &location, url_args, &sr, NULL, 0);

    if (rc != NGX_OK) {
        return NGX_ERROR;
    }

    rc = ngx_http_echo_adjust_subrequest(sr);
    if (rc != NGX_OK) {
        return NGX_ERROR;
    }

    return NGX_OK;
}


ngx_int_t
ngx_http_echo_exec_echo_location(ngx_http_request_t *r,
    ngx_http_echo_ctx_t *ctx, ngx_array_t *computed_args)
{
    ngx_int_t                            rc;
    ngx_http_request_t                  *sr; /* subrequest object */
    ngx_str_t                           *computed_arg_elts;
    ngx_str_t                            location;
    ngx_str_t                           *url_args;
    ngx_http_post_subrequest_t          *psr;
    ngx_str_t                            args;
    ngx_uint_t                           flags = 0;
    ngx_http_echo_ctx_t                 *sr_ctx;

    computed_arg_elts = computed_args->elts;

    location = computed_arg_elts[0];

    if (location.len == 0) {
        return NGX_ERROR;
    }

    if (computed_args->nelts > 1) {
        url_args = &computed_arg_elts[1];

    } else {
        url_args = NULL;
    }

    args.data = NULL;
    args.len = 0;

    if (ngx_http_parse_unsafe_uri(r, &location, &args, &flags) != NGX_OK) {
        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "echo_location sees unsafe uri: \"%V\"",
                       &location);
        return NGX_ERROR;
    }

    if (args.len > 0 && url_args == NULL) {
        url_args = &args;
    }

    rc = ngx_http_echo_send_header_if_needed(r, ctx);
    if (rc == NGX_ERROR || rc > NGX_OK || r->header_only) {
        return rc;
    }

    sr_ctx = ngx_http_echo_create_ctx(r);

    psr = ngx_palloc(r->pool, sizeof(ngx_http_post_subrequest_t));
    if (psr == NULL) {
        return NGX_ERROR;
    }

    psr->handler = ngx_http_echo_post_subrequest;
    psr->data = sr_ctx;

    rc = ngx_http_subrequest(r, &location, url_args, &sr, psr, 0);

    if (rc != NGX_OK) {
        return NGX_ERROR;
    }

    rc = ngx_http_echo_adjust_subrequest(sr);

    if (rc != NGX_OK) {
        return NGX_ERROR;
    }

    return NGX_AGAIN;
}


static ngx_int_t
ngx_http_echo_adjust_subrequest(ngx_http_request_t *sr)
{
    ngx_http_core_main_conf_t   *cmcf;
    ngx_http_request_t          *r;

    /* we do not inherit the parent request's variables */
    cmcf = ngx_http_get_module_main_conf(sr, ngx_http_core_module);

    r = sr->parent;

    sr->header_in = r->header_in;

    /* XXX work-around a bug in ngx_http_subrequest */
    if (r->headers_in.headers.last == &r->headers_in.headers.part) {
        sr->headers_in.headers.last = &sr->headers_in.headers.part;
    }

    sr->variables = ngx_pcalloc(sr->pool, cmcf->variables.nelts
                                * sizeof(ngx_http_variable_value_t));

    if (sr->variables == NULL) {
        return NGX_ERROR;
    }

    return NGX_OK;
}
