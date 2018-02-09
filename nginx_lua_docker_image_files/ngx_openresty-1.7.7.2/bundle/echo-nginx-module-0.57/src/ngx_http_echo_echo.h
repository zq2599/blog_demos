#ifndef ECHO_ECHO_H
#define ECHO_ECHO_H

#include "ngx_http_echo_module.h"

ngx_int_t ngx_http_echo_echo_init(ngx_conf_t *cf);

ngx_int_t ngx_http_echo_exec_echo_sync(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx);

ngx_int_t ngx_http_echo_exec_echo(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx, ngx_array_t *computed_args,
        ngx_flag_t in_filter, ngx_array_t *opts);

ngx_int_t ngx_http_echo_exec_echo_request_body(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx);

ngx_int_t ngx_http_echo_exec_echo_flush(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx);

ngx_int_t ngx_http_echo_exec_echo_duplicate(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx, ngx_array_t *computed_args);

#endif /* ECHO_ECHO_H */

