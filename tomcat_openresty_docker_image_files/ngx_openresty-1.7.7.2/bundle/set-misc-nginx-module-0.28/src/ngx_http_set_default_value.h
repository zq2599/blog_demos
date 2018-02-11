#include <ngx_core.h>
#include <ngx_config.h>
#include <ngx_http.h>

#ifndef NGX_HTTP_SET_DEFAULT_VALUE
#define NGX_HTTP_SET_DEFAULT_VALUE


char * ngx_http_set_if_empty(ngx_conf_t *cf, ngx_command_t *cmd,
        void *conf);


ngx_int_t ngx_http_set_misc_set_if_empty(ngx_http_request_t *r,
        ngx_str_t *res, ngx_http_variable_value_t *v);


#endif /* NGX_HTTP_SET_DEFAULT_VALUE */

