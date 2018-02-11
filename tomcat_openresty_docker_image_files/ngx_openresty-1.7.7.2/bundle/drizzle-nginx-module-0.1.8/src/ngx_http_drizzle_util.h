#ifndef NGX_HTTP_DRIZZLE_UTIL_H
#define NGX_HTTP_DRIZZLE_UTIL_H

#include "ddebug.h"

#include <nginx.h>
#include <ngx_core.h>
#include <ngx_http.h>

#define ngx_http_drizzle_strcmp_const(a, b) \
        ngx_strncmp(a, b, sizeof(b) - 1)

#ifndef ngx_copy_const_str
#define ngx_copy_const_str(p, s)  ngx_copy(p, s, sizeof(s) - 1)
#endif


void ngx_http_upstream_dbd_init(ngx_http_request_t *r);
void ngx_http_upstream_dbd_init_request(ngx_http_request_t *r);
ngx_int_t ngx_http_drizzle_set_header(ngx_http_request_t *r, ngx_str_t *key,
        ngx_str_t *value);
void ngx_http_upstream_drizzle_finalize_request(ngx_http_request_t *r,
    ngx_http_upstream_t *u, ngx_int_t rc);
void ngx_http_upstream_drizzle_next(ngx_http_request_t *r,
    ngx_http_upstream_t *u, ngx_int_t rc);
ngx_int_t ngx_http_upstream_drizzle_test_connect(ngx_connection_t *c);
ngx_int_t ngx_http_drizzle_set_thread_id_variable(ngx_http_request_t *r,
        ngx_http_upstream_t *u);
ngx_uint_t ngx_http_drizzle_queue_size(ngx_queue_t *queue);
size_t ngx_http_drizzle_get_num_size(uint64_t i);


#define ngx_http_drizzle_nelems(x) (sizeof(x) / sizeof(x[0]))


#endif /* NGX_HTTP_DRIZZLE_UTIL_H */

