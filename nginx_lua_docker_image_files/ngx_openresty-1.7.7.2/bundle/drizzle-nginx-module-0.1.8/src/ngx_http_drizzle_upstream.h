#ifndef NGX_HTTP_DRIZZLE_UPSTREAM_H
#define NGX_HTTP_DRIZZLE_UPSTREAM_H


#include <ngx_core.h>
#include <ngx_http.h>
#include <nginx.h>
#include "ngx_http_drizzle_module.h"


typedef enum {
    drizzle_keepalive_overflow_ignore = 0,
    drizzle_keepalive_overflow_reject

} ngx_http_drizzle_keepalive_overflow_t;


typedef enum {
    ngx_http_drizzle_protocol = 0,
    ngx_http_mysql_protocol

} ngx_http_upstream_drizzle_protocol_t;


typedef struct {

#if defined(nginx_version) && nginx_version >= 8022
    ngx_addr_t                      *addrs;
#else
    ngx_peer_addr_t                 *addrs;
#endif

    ngx_uint_t                       naddrs;
    in_port_t                        port;
    ngx_str_t                        user;
    ngx_str_t                        password;
    ngx_str_t                        dbname;
    ngx_str_t                        set_names_query;

    ngx_http_upstream_drizzle_protocol_t      protocol;

} ngx_http_upstream_drizzle_server_t;


typedef struct {
    struct sockaddr                *sockaddr;
    socklen_t                       socklen;
    ngx_str_t                       name;
    in_port_t                       port;
    ngx_str_t                       user;
    ngx_str_t                       password;
    ngx_str_t                       dbname;
    ngx_str_t                      *set_names_query;
    u_char                         *host;

    ngx_http_upstream_drizzle_protocol_t      protocol;

} ngx_http_upstream_drizzle_peer_t;


typedef struct {
    unsigned                            single;
    ngx_uint_t                          number;
    ngx_str_t                          *name;

    ngx_http_upstream_drizzle_peer_t    peer[1];

} ngx_http_upstream_drizzle_peers_t;


typedef struct {
    ngx_http_upstream_drizzle_peers_t   *peers;

    /* TODO: we might need "tried" from round robin peer data */
    ngx_uint_t                           current;

    /* of ngx_http_upstream_drizzle_server_t */
    ngx_array_t                         *servers;

    drizzle_st                           drizzle;
    ngx_pool_t                          *pool;

    /* keepalive related fields */
    unsigned                             single;
    ngx_queue_t                          free;
    ngx_queue_t                          cache;

    ngx_uint_t                           active_conns;

    ngx_uint_t                           max_cached;
    ngx_http_drizzle_keepalive_overflow_t    overflow;

} ngx_http_upstream_drizzle_srv_conf_t;


typedef struct {
    ngx_http_drizzle_loc_conf_t            *loc_conf;
    ngx_http_upstream_drizzle_srv_conf_t   *srv_conf;

    ngx_http_upstream_t                    *upstream;
    ngx_http_request_t                     *request;

    ngx_str_t                               dbname;
    ngx_str_t                               query;

    ngx_http_drizzle_state_t                state;

    drizzle_con_st                         *drizzle_con;
    drizzle_result_st                       drizzle_res;
    drizzle_column_st                       drizzle_col;
    uint64_t                                drizzle_row;

    ngx_str_t                               name;

    ngx_chain_t                           **last_out;

    ngx_buf_t                              *out_buf;
    ngx_buf_t                               cached;
    ngx_buf_t                               postponed;
    size_t                                  avail_out;
    ngx_str_t                               charset;
    ngx_str_t                              *set_names_query;

    struct sockaddr                         sockaddr;

    /* how many times this connection has been successfully used */
    ngx_uint_t                              used;

    unsigned                                failed:1;
    unsigned                                seen_stream_end:1;
    unsigned                                has_set_names:1;
    unsigned                                enable_charset:1;
} ngx_http_upstream_drizzle_peer_data_t;


char * ngx_http_upstream_drizzle_server(ngx_conf_t *cf, ngx_command_t *cmd,
        void *conf);
void * ngx_http_upstream_drizzle_create_srv_conf(ngx_conf_t *cf);
ngx_flag_t ngx_http_upstream_drizzle_is_my_peer(
        const ngx_peer_connection_t *peer);
void ngx_http_upstream_drizzle_free_connection(ngx_log_t *log,
        ngx_connection_t *c, drizzle_con_st *dc,
        ngx_http_upstream_drizzle_srv_conf_t *dscf);
ngx_http_upstream_srv_conf_t * ngx_http_upstream_drizzle_add(
        ngx_http_request_t *r, ngx_url_t *url);


#endif /* NGX_HTTP_DRIZZLE_UPSTREAM_H */
