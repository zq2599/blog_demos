#ifndef ECHO_SLEEP_H
#define ECHO_SLEEP_H

#include "ngx_http_echo_module.h"

ngx_int_t ngx_http_echo_exec_echo_sleep(
        ngx_http_request_t *r, ngx_http_echo_ctx_t *ctx,
        ngx_array_t *computed_args);

ngx_int_t ngx_http_echo_exec_echo_blocking_sleep(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx, ngx_array_t *computed_args);

void ngx_http_echo_sleep_event_handler(ngx_event_t *ev);

#endif /* ECHO_SLEEP_H */

