#ifndef ECHO_FOREACH_H
#define ECHO_FOREACH_H

#include "ngx_http_echo_module.h"

ngx_int_t ngx_http_echo_exec_echo_foreach_split(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx, ngx_array_t *computed_args);

ngx_int_t ngx_http_echo_exec_echo_end(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx);

ngx_int_t ngx_http_echo_it_variable(ngx_http_request_t *r,
        ngx_http_variable_value_t *v, uintptr_t data);

#endif /* ECHO_FOREACH_H */

