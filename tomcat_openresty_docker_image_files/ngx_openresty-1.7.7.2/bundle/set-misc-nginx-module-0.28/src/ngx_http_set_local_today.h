#include <ngx_core.h>
#include <ngx_config.h>
#include <ngx_http.h>


ngx_int_t ngx_http_set_local_today(ngx_http_request_t *r, ngx_str_t *res,
        ngx_http_variable_value_t *v);

ngx_int_t ngx_http_set_formatted_gmt_time(ngx_http_request_t *r, ngx_str_t *res,
        ngx_http_variable_value_t *v);

ngx_int_t ngx_http_set_formatted_local_time(ngx_http_request_t *r,
        ngx_str_t *res, ngx_http_variable_value_t *v);
