
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef NGX_HTTP_MEMC_REQUEST_H
#define NGX_HTTP_MEMC_REQUEST_H

#include <ngx_core.h>
#include <ngx_http.h>

ngx_int_t ngx_http_memc_create_get_cmd_request(ngx_http_request_t *r);

ngx_int_t ngx_http_memc_create_storage_cmd_request(ngx_http_request_t *r);

ngx_int_t ngx_http_memc_create_noarg_cmd_request(ngx_http_request_t *r);

ngx_int_t ngx_http_memc_create_flush_all_cmd_request(ngx_http_request_t *r);

ngx_int_t ngx_http_memc_create_delete_cmd_request(ngx_http_request_t *r);

ngx_int_t ngx_http_memc_create_incr_decr_cmd_request(ngx_http_request_t *r);


#endif /* NGX_HTTP_MEMC_REQUEST_H */

