#ifndef NGX_HTTP_DRIZZLE_HANDLER_H
#define NGX_HTTP_DRIZZLE_HANDLER_H

#include <ngx_core.h>
#include <ngx_http.h>


void ngx_http_drizzle_set_libdrizzle_ready(ngx_http_request_t *r);

ngx_int_t ngx_http_drizzle_handler(ngx_http_request_t *r);

void ngx_http_drizzle_rev_handler(ngx_http_request_t *r,
        ngx_http_upstream_t *u);

void ngx_http_drizzle_wev_handler(ngx_http_request_t *r,
        ngx_http_upstream_t *u);

ngx_int_t ngx_http_drizzle_status_handler(ngx_http_request_t *r);


#endif /* NGX_HTTP_DRIZZLE_HANDLER_H */
