#ifndef NGX_HTTP_MEMC_HANDLER_H
#define NGX_HTTP_MEMC_HANDLER_H

#include <ngx_core.h>
#include <ngx_http.h>


ngx_int_t ngx_http_memc_handler(ngx_http_request_t *r);
ngx_int_t ngx_http_memc_init(ngx_conf_t *cf);


#endif /* NGX_HTTP_MEMC_HANDLER_H */

