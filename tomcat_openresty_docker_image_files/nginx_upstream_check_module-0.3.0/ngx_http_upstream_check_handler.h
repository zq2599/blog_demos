#ifndef _NGX_HTTP_UPSTREAM_CHECK_HANDLER_H_INCLUDED_
#define _NGX_HTTP_UPSTREAM_CHECK_HANDLER_H_INCLUDED_


#include <ngx_config.h>
#include <ngx_core.h>
#include <ngx_event.h>
#include <ngx_event_connect.h>
#include <ngx_event_pipe.h>
#include <ngx_http.h>
#include "ngx_http_upstream_check_module.h"


typedef struct {
    u_char major;
    u_char minor;
} ssl_protocol_version_t;

typedef struct {
    u_char                 msg_type;
    ssl_protocol_version_t version;
    uint16_t               length;

    u_char                 handshake_type;
    u_char                 handshake_length[3];
    ssl_protocol_version_t hello_version;

    time_t                 time;
    u_char                 random[28];

    u_char                 others[0];
} __attribute__((packed)) server_ssl_hello_t;

typedef struct {
    u_char                 packet_length[3];
    u_char                 packet_number;

    u_char                 protocol_version;
    u_char                 others[0];
} __attribute__((packed)) mysql_handshake_init_t;

typedef struct {
    uint16_t               preamble;
    uint16_t               length;
    u_char                 type;
} __attribute__((packed)) ajp_raw_packet_t;

typedef struct {
    ngx_buf_t          send;
    ngx_buf_t          recv;

    ngx_uint_t         state;
    ngx_http_status_t  status;
} ngx_http_check_ctx;

/* state */
#define NGX_HTTP_CHECK_CONNECT_DONE     0x0001
#define NGX_HTTP_CHECK_SEND_DONE        0x0002
#define NGX_HTTP_CHECK_RECV_DONE        0x0004
#define NGX_HTTP_CHECK_ALL_DONE         0x0008

typedef struct {
    ngx_pid_t    owner;

    ngx_msec_t   access_time;

    ngx_uint_t   fall_count;
    ngx_uint_t   rise_count;

    ngx_atomic_t lock;
    ngx_atomic_t busyness;
    ngx_atomic_t down;

    ngx_uint_t   access_count;
    ngx_str_t    *upstream_name;

    struct sockaddr  *sockaddr;
    socklen_t         socklen;
} ngx_http_check_peer_shm_t;

typedef struct {
    ngx_uint_t   generation;

    ngx_uint_t   checksum;
    ngx_uint_t   state;
    ngx_atomic_t lock;

    ngx_uint_t   number;

    /* store ngx_http_check_status_peer_t */
    ngx_http_check_peer_shm_t peers[1];
} ngx_http_check_peers_shm_t;

struct ngx_http_check_peer_s {
    ngx_flag_t                       state;
    ngx_pool_t                      *pool;
    ngx_uint_t                       index;
    ngx_uint_t                       max_busy;
    ngx_str_t                       *upstream_name;
    ngx_peer_addr_t                 *peer_addr;
    ngx_event_t                      check_ev;
    ngx_event_t                      check_timeout_ev;
    ngx_peer_connection_t            pc;

    void *                           check_data;
    ngx_event_handler_pt             send_handler;
    ngx_event_handler_pt             recv_handler;

    ngx_http_check_packet_init_pt     init;
    ngx_http_check_packet_parse_pt    parse;
    ngx_http_check_packet_clean_pt    reinit;

    ngx_http_check_peer_shm_t            *shm;
    ngx_http_upstream_check_srv_conf_t   *conf;
};

struct ngx_http_check_peers_s {
    ngx_str_t                        check_shm_name;
    ngx_uint_t                       checksum;
    ngx_array_t                      peers;

    ngx_http_check_peers_shm_t      *peers_shm;
};


ngx_int_t ngx_http_upstream_check_status_handler(ngx_http_request_t *r);

ngx_uint_t ngx_http_check_peer_down(ngx_uint_t index);

void ngx_http_check_get_peer(ngx_uint_t index);
void ngx_http_check_free_peer(ngx_uint_t index);

char * ngx_http_upstream_check_init_shm(ngx_conf_t *cf, void *conf);
ngx_int_t ngx_http_check_add_timers(ngx_cycle_t *cycle);

extern check_conf_t  ngx_check_types[];

#endif //_NGX_HTTP_UPSTREAM_CHECK_HANDLER_H_INCLUDED_

