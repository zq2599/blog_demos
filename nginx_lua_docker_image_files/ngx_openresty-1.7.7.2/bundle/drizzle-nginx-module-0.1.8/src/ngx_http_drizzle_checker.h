#ifndef NGX_HTTP_DRIZZLE_CHECKER_H
#define NGX_HTTP_DRIZZLE_CHECKER_H

#include "ngx_http_drizzle_module.h"

ngx_int_t ngx_http_drizzle_check_int(ngx_str_t *value, void *data);

ngx_int_t ngx_http_drizzle_check_float(ngx_str_t *value, void *data);

ngx_int_t ngx_http_drizzle_check_bool(ngx_str_t *value, void *data);

ngx_int_t ngx_http_drizzle_check_col(ngx_str_t *value, void *data);

ngx_int_t ngx_http_drizzle_check_table(ngx_str_t *value, void *data);

ngx_int_t ngx_http_drizzle_check_keyword(ngx_str_t *value, void *data);

#endif /* NGX_HTTP_DRIZZLE_CHECKER_H */

