#ifndef NGX_SET_SECURE_RANDOM_H
#define NGX_SET_SECURE_RANDOM_H

#include <ngx_core.h>
#include <ngx_config.h>
#include <ngx_http.h>

ngx_int_t ngx_http_set_misc_set_secure_random_alphanum(ngx_http_request_t *r,
        ngx_str_t *res, ngx_http_variable_value_t *v);

ngx_int_t ngx_http_set_misc_set_secure_random_lcalpha(ngx_http_request_t *r,
        ngx_str_t *res, ngx_http_variable_value_t *v);

#endif /* NGX_SET_SECURE_RANDOM_H */

