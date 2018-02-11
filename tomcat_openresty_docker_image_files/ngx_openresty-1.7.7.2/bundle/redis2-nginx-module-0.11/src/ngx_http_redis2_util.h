#ifndef NGX_HTTP_REDIS2_UTIL_H
#define NGX_HTTP_REDIS2_UTIL_H


#include "ngx_http_redis2_module.h"


#ifndef ngx_str_set
#define ngx_str_set(str, text)                                               \
    (str)->len = sizeof(text) - 1; (str)->data = (u_char *) text
#endif


char * ngx_http_redis2_set_complex_value_slot(ngx_conf_t *cf,
        ngx_command_t *cmd, void *conf);
ngx_http_upstream_srv_conf_t * ngx_http_redis2_upstream_add(
        ngx_http_request_t *r, ngx_url_t *url);
ngx_int_t ngx_http_redis2_build_query(ngx_http_request_t *r,
        ngx_array_t *queries, ngx_buf_t **b);

#endif /* NGX_HTTP_REDIS2_UTIL_H */

