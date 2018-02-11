#ifndef ECHO_REQUEST_INFO_H
#define ECHO_REQUEST_INFO_H

#include "ngx_http_echo_module.h"

ngx_int_t ngx_http_echo_exec_echo_read_request_body(
        ngx_http_request_t* r, ngx_http_echo_ctx_t *ctx);

ngx_int_t ngx_http_echo_request_method_variable(ngx_http_request_t *r,
        ngx_http_variable_value_t *v, uintptr_t data);

ngx_int_t ngx_http_echo_client_request_method_variable(ngx_http_request_t *r,
        ngx_http_variable_value_t *v, uintptr_t data);

ngx_int_t ngx_http_echo_request_body_variable(ngx_http_request_t *r,
        ngx_http_variable_value_t *v, uintptr_t data);

ngx_int_t ngx_http_echo_client_request_headers_variable(ngx_http_request_t *r,
        ngx_http_variable_value_t *v, uintptr_t data);

ngx_int_t ngx_http_echo_cacheable_request_uri_variable(ngx_http_request_t *r,
        ngx_http_variable_value_t *v, uintptr_t data);

ngx_int_t ngx_http_echo_request_uri_variable(ngx_http_request_t *r,
        ngx_http_variable_value_t *v, uintptr_t data);

ngx_int_t ngx_http_echo_response_status_variable(ngx_http_request_t *r,
        ngx_http_variable_value_t *v, uintptr_t data);

#endif /* ECHO_REQUEST_INFO_H */

