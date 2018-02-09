#ifndef NGX_HTTP_DRIZZLE_PROCESSOR_H
#define NGX_HTTP_DRIZZLE_PROCESSOR_H

#include "ngx_http_drizzle_module.h"
#include "ngx_http_drizzle_upstream.h"
#include <ngx_http.h>
#include <ngx_core.h>


ngx_int_t ngx_http_drizzle_process_events(ngx_http_request_t *r);

void ngx_http_upstream_drizzle_done(ngx_http_request_t *r,
        ngx_http_upstream_t *u, ngx_http_upstream_drizzle_peer_data_t *dp,
        ngx_int_t rc);

#endif /* NGX_HTTP_DRIZZLE_PROCESSOR_H */

