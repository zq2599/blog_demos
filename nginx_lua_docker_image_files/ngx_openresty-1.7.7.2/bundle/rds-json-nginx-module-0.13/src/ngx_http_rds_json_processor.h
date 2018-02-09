
/*
 * Copyright (C) agentzh
 */

#ifndef NGX_HTTP_RDS_JSON_PROCESSOR_H
#define NGX_HTTP_RDS_JSON_PROCESSOR_H


#include "ngx_http_rds_json_filter_module.h"

#include <ngx_core.h>
#include <ngx_http.h>
#include <nginx.h>


ngx_int_t ngx_http_rds_json_process_header(ngx_http_request_t *r,
        ngx_chain_t *in, ngx_http_rds_json_ctx_t *ctx);

ngx_int_t ngx_http_rds_json_process_col(ngx_http_request_t *r,
        ngx_chain_t *in, ngx_http_rds_json_ctx_t *ctx);

ngx_int_t ngx_http_rds_json_process_row(ngx_http_request_t *r,
        ngx_chain_t *in, ngx_http_rds_json_ctx_t *ctx);

ngx_int_t ngx_http_rds_json_process_field(ngx_http_request_t *r,
        ngx_chain_t *in, ngx_http_rds_json_ctx_t *ctx);

ngx_int_t ngx_http_rds_json_process_more_field_data(ngx_http_request_t *r,
        ngx_chain_t *in, ngx_http_rds_json_ctx_t *ctx);


#endif /* NGX_HTTP_RDS_JSON_PROCESSOR_H */

