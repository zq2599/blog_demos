#ifndef NGX_HTTP_REDIS2_MODULE_H
#define NGX_HTTP_REDIS2_MODULE_H


#include <ngx_config.h>
#include <ngx_core.h>
#include <ngx_http.h>


extern ngx_module_t  ngx_http_redis2_module;

typedef struct {
    ngx_http_upstream_conf_t   upstream;
    ngx_str_t                  literal_query; /* for redis2_literal_raw_query */
    ngx_http_complex_value_t  *complex_query; /* for redis2_raw_query */
    ngx_http_complex_value_t  *complex_query_count; /* for redis2_raw_query */
    ngx_http_complex_value_t  *complex_target; /* for redis2_pass */
    ngx_array_t               *queries; /* for redis2_query */

} ngx_http_redis2_loc_conf_t;


typedef struct ngx_http_redis2_ctx_s  ngx_http_redis2_ctx_t;

typedef ngx_int_t (*ngx_http_redis2_filter_handler_ptr)
    (ngx_http_redis2_ctx_t *ctx, ssize_t bytes);


struct ngx_http_redis2_ctx_s {
    ngx_int_t                  query_count;
    ngx_http_request_t        *request;
    int                        state;
    size_t                     chunk_size;
    size_t                     chunk_bytes_read;
    size_t                     chunks_read;
    size_t                     chunk_count;

    ngx_http_redis2_filter_handler_ptr  filter;
};


#endif /* NGX_HTTP_REDIS2_MODULE_H */

