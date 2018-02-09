#ifndef NGX_HTTP_DRIZZLE_KEEPALIVE_H
#define NGX_HTTP_DRIZZLE_KEEPALIVE_H


#include "ngx_http_drizzle_upstream.h"
#include <ngx_core.h>
#include <ngx_http.h>
#include <nginx.h>


typedef struct {
    ngx_http_upstream_drizzle_srv_conf_t  *srv_conf;

    ngx_queue_t                          queue;

    ngx_connection_t                    *connection;

    socklen_t                            socklen;
    struct sockaddr                      sockaddr;
    drizzle_con_st                      *drizzle_con;
    ngx_str_t                            name;

    /* how many times this connection has been successfully used */
    ngx_uint_t                           used;

    unsigned                             has_set_names:1;

} ngx_http_drizzle_keepalive_cache_t;


char * ngx_http_upstream_drizzle_keepalive(ngx_conf_t *cf, ngx_command_t *cmd,
        void *conf);

ngx_int_t ngx_http_drizzle_keepalive_init(ngx_pool_t *pool,
        ngx_http_upstream_drizzle_srv_conf_t *dscf);

ngx_int_t ngx_http_drizzle_keepalive_get_peer_single(ngx_peer_connection_t *pc,
        ngx_http_upstream_drizzle_peer_data_t *dp,
        ngx_http_upstream_drizzle_srv_conf_t *dscf);

ngx_int_t ngx_http_drizzle_keepalive_get_peer_multi(ngx_peer_connection_t *pc,
        ngx_http_upstream_drizzle_peer_data_t *dp,
        ngx_http_upstream_drizzle_srv_conf_t *dscf);

void ngx_http_drizzle_keepalive_free_peer(ngx_peer_connection_t *pc,
        ngx_http_upstream_drizzle_peer_data_t *dp,
        ngx_http_upstream_drizzle_srv_conf_t *dscf,
        ngx_uint_t  state);

#endif /* NGX_HTTP_DRIZZLE_KEEPALIVE_H */

