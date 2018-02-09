
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef NGX_HTTP_MEMC_RESPONSE_H
#define NGX_HTTP_MEMC_RESPONSE_H

#include <ngx_core.h>
#include <ngx_http.h>

ngx_int_t ngx_http_memc_process_get_cmd_header(ngx_http_request_t *r);

ngx_int_t ngx_http_memc_get_cmd_filter_init(void *data);

ngx_int_t ngx_http_memc_get_cmd_filter(void *data, ssize_t bytes);

ngx_int_t ngx_http_memc_process_simple_header(ngx_http_request_t *r);

ngx_int_t ngx_http_memc_empty_filter_init(void *data);

ngx_int_t ngx_http_memc_empty_filter(void *data, ssize_t bytes);

ngx_int_t ngx_http_memc_process_flush_all_cmd_header(ngx_http_request_t *r);

#endif /* NGX_HTTP_MEMC_RESPONSE_H */

