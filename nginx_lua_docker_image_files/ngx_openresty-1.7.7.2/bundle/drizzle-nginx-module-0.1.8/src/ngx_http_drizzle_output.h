#ifndef NGX_HTTP_DRIZZLE_OUTPUT_H
#define NGX_HTTP_DRIZZLE_OUTPUT_H

#include "ngx_http_drizzle_upstream.h"

ngx_int_t ngx_http_drizzle_output_result_header(ngx_http_request_t *r,
        drizzle_result_st *res);

ngx_int_t ngx_http_drizzle_output_col(ngx_http_request_t *r,
        drizzle_column_st *res);

ngx_int_t ngx_http_drizzle_output_row(ngx_http_request_t *r, uint64_t row);

ngx_int_t ngx_http_drizzle_output_field(ngx_http_request_t *r, size_t offset,
        size_t len, size_t total, drizzle_field_t field);

ngx_int_t ngx_http_drizzle_output_bufs(ngx_http_request_t *r,
        ngx_http_upstream_drizzle_peer_data_t *dp);

#endif /* NGX_HTTP_DRIZZLE_OUTPUT_H */

