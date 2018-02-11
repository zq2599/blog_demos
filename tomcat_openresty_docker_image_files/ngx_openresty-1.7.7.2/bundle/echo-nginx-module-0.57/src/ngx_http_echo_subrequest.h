#ifndef ECHO_SUBREQUEST_H
#define ECHO_SUBREQUEST_H

#include "ngx_http_echo_module.h"

ngx_int_t ngx_http_echo_exec_echo_subrequest(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx, ngx_array_t *computed_args);

ngx_int_t ngx_http_echo_exec_echo_subrequest_async(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx, ngx_array_t *computed_args);

ngx_int_t ngx_http_echo_exec_abort_parent(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx);

ngx_int_t ngx_http_echo_exec_exec(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx, ngx_array_t *computed_args);

#endif /* ECHO_SUBREQUEST_H */

