#ifndef ECHO_TIMER_H
#define ECHO_TIMER_H

#include "ngx_http_echo_module.h"

ngx_int_t ngx_http_echo_timer_elapsed_variable(ngx_http_request_t *r,
        ngx_http_variable_value_t *v, uintptr_t data);

ngx_int_t ngx_http_echo_exec_echo_reset_timer(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx);

#endif /* ECHO_TIMER_H */

