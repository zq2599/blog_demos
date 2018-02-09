#ifndef NGX_HTTP_SET_BASE32
#define NGX_HTTP_SET_BASE32


#include <ngx_core.h>
#include <ngx_config.h>
#include <ngx_http.h>


ngx_int_t ngx_http_set_misc_encode_base32(ngx_http_request_t *r,
        ngx_str_t *res, ngx_http_variable_value_t *v);

ngx_int_t ngx_http_set_misc_decode_base32(ngx_http_request_t *r,
        ngx_str_t *res, ngx_http_variable_value_t *v);


#endif /* NGX_HTTP_SET_BASE32 */

