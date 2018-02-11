#ifndef ECHO_LOCATION_H
#define ECHO_LOCATION_H

#include "ngx_http_echo_module.h"

ngx_int_t ngx_http_echo_exec_echo_location_async(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx, ngx_array_t *computed_args);

ngx_int_t ngx_http_echo_exec_echo_location(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx, ngx_array_t *computed_args);

#endif /* ECHO_LOCATION_H */

