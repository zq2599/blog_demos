
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef NGX_HTTP_RDS_CSV_PROCESSOR_H
#define NGX_HTTP_RDS_CSV_PROCESSOR_H


#include "ngx_http_rds_csv_filter_module.h"

#include <ngx_core.h>
#include <ngx_http.h>
#include <nginx.h>


ngx_int_t ngx_http_rds_csv_process_header(ngx_http_request_t *r,
    ngx_chain_t *in, ngx_http_rds_csv_ctx_t *ctx);

ngx_int_t ngx_http_rds_csv_process_col(ngx_http_request_t *r,
    ngx_chain_t *in, ngx_http_rds_csv_ctx_t *ctx);

ngx_int_t ngx_http_rds_csv_process_row(ngx_http_request_t *r,
    ngx_chain_t *in, ngx_http_rds_csv_ctx_t *ctx);

ngx_int_t ngx_http_rds_csv_process_field(ngx_http_request_t *r,
    ngx_chain_t *in, ngx_http_rds_csv_ctx_t *ctx);

ngx_int_t ngx_http_rds_csv_process_more_field_data(ngx_http_request_t *r,
    ngx_chain_t *in, ngx_http_rds_csv_ctx_t *ctx);


#endif /* NGX_HTTP_RDS_CSV_PROCESSOR_H */
