
#include <ngx_config.h>
#include <ngx_core.h>
#include <ngx_http.h>
#include "ngx_http_upstream_check_handler.h"


/* ngx_spinlock is defined without a matching unlock primitive */
#define ngx_spinlock_unlock(lock)                          \
    (void) ngx_atomic_cmp_set(lock, ngx_pid, 0)


static void ngx_http_check_begin_handler(ngx_event_t *event);
static void ngx_http_check_connect_handler(ngx_event_t *event);

static void ngx_http_check_peek_handler(ngx_event_t *event);

static void ngx_http_check_send_handler(ngx_event_t *event);
static void ngx_http_check_recv_handler(ngx_event_t *event);

static ngx_int_t ngx_http_check_http_init(ngx_http_check_peer_t *peer);
static ngx_int_t ngx_http_check_http_parse(ngx_http_check_peer_t *peer);
static ngx_int_t ngx_http_check_parse_status_line(ngx_http_check_ctx *ctx,
        ngx_buf_t *b, ngx_http_status_t *status);
static void ngx_http_check_http_reinit(ngx_http_check_peer_t *peer);

static ngx_int_t ngx_http_check_ssl_hello_init(ngx_http_check_peer_t *peer);
static ngx_int_t ngx_http_check_ssl_hello_parse(ngx_http_check_peer_t *peer);
static void ngx_http_check_ssl_hello_reinit(ngx_http_check_peer_t *peer);

static ngx_int_t ngx_http_check_mysql_init(ngx_http_check_peer_t *peer);
static ngx_int_t ngx_http_check_mysql_parse(ngx_http_check_peer_t *peer);
static void ngx_http_check_mysql_reinit(ngx_http_check_peer_t *peer);

static ngx_int_t ngx_http_check_ajp_init(ngx_http_check_peer_t *peer);
static ngx_int_t ngx_http_check_ajp_parse(ngx_http_check_peer_t *peer);
static void ngx_http_check_ajp_reinit(ngx_http_check_peer_t *peer);

static void ngx_http_check_status_update(ngx_http_check_peer_t *peer,
        ngx_int_t result);

static void ngx_http_check_clean_event(ngx_http_check_peer_t *peer);

static void ngx_http_check_timeout_handler(ngx_event_t *event);
static void ngx_http_check_finish_handler(ngx_event_t *event);

static ngx_int_t ngx_http_check_need_exit();
static void ngx_http_check_clear_all_events();


#define SHM_NAME_LEN 256

static ngx_int_t ngx_http_check_get_shm_name(ngx_str_t *shm_name,
        ngx_pool_t *pool, ngx_uint_t generation);
static ngx_shm_zone_t * ngx_shared_memory_find(ngx_cycle_t *cycle,
        ngx_str_t *name, void *tag);
static ngx_http_check_peer_shm_t * ngx_http_check_find_shm_peer(
        ngx_http_check_peers_shm_t *peers_shm, ngx_addr_t *addr,ngx_str_t *upstream_name);
static void ngx_http_check_set_shm_peer(ngx_http_check_peer_shm_t *peer_shm,
        ngx_http_check_peer_shm_t *opeer_shm, ngx_uint_t init_down,ngx_str_t *upstream_name);
static ngx_int_t ngx_http_upstream_check_init_shm_zone(
        ngx_shm_zone_t *shm_zone, void *data);


#define RANDOM "NGX_HTTP_CHECK_SSL_HELLO\n\n\n\n"

/* This is the SSLv3 CLIENT HELLO packet used in conjunction with the
 * check type of ssl_hello to ensure that the remote server speaks SSL.
 *
 * Check RFC 2246 (TLSv1.0) sections A.3 and A.4 for details.
 *
 * Some codes copied from HAProxy 1.4.1
 */
static const char sslv3_client_hello_pkt[] = {
    "\x16"                /* ContentType         : 0x16 = Hanshake           */
    "\x03\x00"            /* ProtocolVersion     : 0x0300 = SSLv3            */
    "\x00\x79"            /* ContentLength       : 0x79 bytes after this one */
    "\x01"                /* HanshakeType        : 0x01 = CLIENT HELLO       */
    "\x00\x00\x75"        /* HandshakeLength     : 0x75 bytes after this one */
    "\x03\x00"            /* Hello Version       : 0x0300 = v3               */
    "\x00\x00\x00\x00"    /* Unix GMT Time (s)   : filled with <now> (@0x0B) */
    RANDOM                /* Random   : must be exactly 28 bytes  */
    "\x00"                /* Session ID length   : empty (no session ID)     */
    "\x00\x4E"            /* Cipher Suite Length : 78 bytes after this one   */
    "\x00\x01" "\x00\x02" "\x00\x03" "\x00\x04" /* 39 most common ciphers :  */
    "\x00\x05" "\x00\x06" "\x00\x07" "\x00\x08" /* 0x01...0x1B, 0x2F...0x3A  */
    "\x00\x09" "\x00\x0A" "\x00\x0B" "\x00\x0C" /* This covers RSA/DH,       */
    "\x00\x0D" "\x00\x0E" "\x00\x0F" "\x00\x10" /* various bit lengths,      */
    "\x00\x11" "\x00\x12" "\x00\x13" "\x00\x14" /* SHA1/MD5, DES/3DES/AES... */
    "\x00\x15" "\x00\x16" "\x00\x17" "\x00\x18"
    "\x00\x19" "\x00\x1A" "\x00\x1B" "\x00\x2F"
    "\x00\x30" "\x00\x31" "\x00\x32" "\x00\x33"
    "\x00\x34" "\x00\x35" "\x00\x36" "\x00\x37"
    "\x00\x38" "\x00\x39" "\x00\x3A"
    "\x01"                /* Compression Length  : 0x01 = 1 byte for types   */
    "\x00"                /* Compression Type    : 0x00 = NULL compression   */
};


#define HANDSHAKE    0x16
#define SERVER_HELLO 0x02


#define AJP_CPING  0x0a
#define AJP_CPONG  0x09

static const char ajp_cping_packet[] ={0x12, 0x34, 0x00, 0x01, AJP_CPING, 0x00};
static const char ajp_cpong_packet[] ={0x41, 0x42, 0x00, 0x01, AJP_CPONG};


check_conf_t  ngx_check_types[] = {
    { NGX_HTTP_CHECK_TCP,
      "tcp",
      ngx_null_string,
      0,
      ngx_http_check_peek_handler,
      ngx_http_check_peek_handler,
      NULL,
      NULL,
      NULL,
      0 },

    { NGX_HTTP_CHECK_HTTP,
      "http",
      ngx_string("GET / HTTP/1.0\r\n\r\n"),
      NGX_CONF_BITMASK_SET | NGX_CHECK_HTTP_2XX | NGX_CHECK_HTTP_3XX,
      ngx_http_check_send_handler,
      ngx_http_check_recv_handler,
      /*TODO: unite the init function*/
      ngx_http_check_http_init,
      ngx_http_check_http_parse,
      /*TODO: remove the reinit function*/
      ngx_http_check_http_reinit,
      1 },

    { NGX_HTTP_CHECK_SSL_HELLO,
      "ssl_hello",
      ngx_string(sslv3_client_hello_pkt),
      0,
      ngx_http_check_send_handler,
      ngx_http_check_recv_handler,
      ngx_http_check_ssl_hello_init,
      ngx_http_check_ssl_hello_parse,
      ngx_http_check_ssl_hello_reinit,
      1 },

    { NGX_HTTP_CHECK_MYSQL,
      "mysql",
      ngx_null_string,
      0,
      ngx_http_check_send_handler,
      ngx_http_check_recv_handler,
      ngx_http_check_mysql_init,
      ngx_http_check_mysql_parse,
      ngx_http_check_mysql_reinit,
      1 },

    { NGX_HTTP_CHECK_AJP,
      "ajp",
      ngx_string(ajp_cping_packet),
      0,
      ngx_http_check_send_handler,
      ngx_http_check_recv_handler,
      ngx_http_check_ajp_init,
      ngx_http_check_ajp_parse,
      ngx_http_check_ajp_reinit,
      1 },

    { 0, "", ngx_null_string, 0, NULL, NULL, NULL, NULL, NULL, 0 }
};


static ngx_uint_t ngx_http_check_shm_generation = 0;
static ngx_http_check_peers_t *check_peers_ctx = NULL;


ngx_uint_t
ngx_http_check_peer_down(ngx_uint_t index)
{
    ngx_http_check_peer_t     *peer;

    if (check_peers_ctx == NULL || index >= check_peers_ctx->peers.nelts) {
        return 0;
    }

    peer = check_peers_ctx->peers.elts;

    return (peer[index].shm->down);
}


void
ngx_http_check_get_peer(ngx_uint_t index)
{
    ngx_http_check_peer_t     *peer;

    if (check_peers_ctx == NULL || index >= check_peers_ctx->peers.nelts) {
        return;
    }

    peer = check_peers_ctx->peers.elts;

    ngx_spinlock(&peer[index].shm->lock, ngx_pid, 1024);

    peer[index].shm->busyness++;
    peer[index].shm->access_count++;

    ngx_spinlock_unlock(&peer[index].shm->lock);
}


void
ngx_http_check_free_peer(ngx_uint_t index)
{
    ngx_http_check_peer_t     *peer;

    if (check_peers_ctx == NULL || index >= check_peers_ctx->peers.nelts) {
        return;
    }

    peer = check_peers_ctx->peers.elts;

    ngx_spinlock(&peer[index].shm->lock, ngx_pid, 1024);

    if (peer[index].shm->busyness > 0) {
        peer[index].shm->busyness--;
    }

    ngx_spinlock_unlock(&peer[index].shm->lock);
}


ngx_int_t
ngx_http_check_add_timers(ngx_cycle_t *cycle)
{
    ngx_uint_t                          i;
    ngx_msec_t                          t, delay;
    check_conf_t                       *cf;
    ngx_http_check_peer_t              *peer;
    ngx_http_check_peers_t             *peers;
    ngx_http_check_peer_shm_t          *peer_shm;
    ngx_http_check_peers_shm_t         *peers_shm;
    ngx_http_upstream_check_srv_conf_t *ucscf;

    peers = check_peers_ctx;
    if (peers == NULL) {
        return NGX_OK;
    }

    peers_shm = peers->peers_shm;
    if (peers_shm == NULL) {
        return NGX_OK;
    }

    ngx_log_debug2(NGX_LOG_DEBUG_HTTP, cycle->log, 0,
                   "http check upstream init_process, shm_name: %V, "
                   "peer number: %ud",
                   &peers->check_shm_name,
                   peers->peers.nelts);

    srandom(ngx_pid);

    peer = peers->peers.elts;
    peer_shm = peers_shm->peers;

    for (i = 0; i < peers->peers.nelts; i++) {
        peer[i].shm = &peer_shm[i];

        peer[i].check_ev.handler = ngx_http_check_begin_handler;
        peer[i].check_ev.log = cycle->log;
        peer[i].check_ev.data = &peer[i];
        peer[i].check_ev.timer_set = 0;

        peer[i].check_timeout_ev.handler = ngx_http_check_timeout_handler;
        peer[i].check_timeout_ev.log = cycle->log;
        peer[i].check_timeout_ev.data = &peer[i];
        peer[i].check_timeout_ev.timer_set = 0;

        ucscf = peer[i].conf;
        cf = ucscf->check_type_conf;

        if (cf->need_pool) {
            peer[i].pool = ngx_create_pool(ngx_pagesize, cycle->log);
            if (peer[i].pool == NULL) {
                return NGX_ERROR;
            }
        }

        peer[i].send_handler = cf->send_handler;
        peer[i].recv_handler = cf->recv_handler;

        peer[i].init = cf->init;
        peer[i].parse = cf->parse;
        peer[i].reinit = cf->reinit;

        /*
         * I added a random start time. I don't want to trigger the check
         * event too close at the beginning.
         * */
        delay = ucscf->check_interval > 1000 ? ucscf->check_interval : 1000;
        t = ngx_random() % delay;

        ngx_add_timer(&peer[i].check_ev, t);
    }

    return NGX_OK;
}


static void
ngx_http_check_begin_handler(ngx_event_t *event)
{
    ngx_msec_t                          interval;
    ngx_http_check_peer_t              *peer;
    ngx_http_check_peers_t             *peers;
    ngx_http_check_peers_shm_t         *peers_shm;
    ngx_http_upstream_check_srv_conf_t *ucscf;

    if (ngx_http_check_need_exit()) {
        return;
    }

    peers = check_peers_ctx;
    if (peers == NULL) {
        return;
    }

    peers_shm = peers->peers_shm;
    if (peers_shm == NULL) {
        return;
    }

    peer = event->data;
    ucscf = peer->conf;

    ngx_add_timer(event, ucscf->check_interval/2);

    /* This process is processing this peer now. */
    if ((peer->shm->owner == ngx_pid) ||
        (peer->pc.connection != NULL) ||
        (peer->check_timeout_ev.timer_set)) {

        return;
    }

    interval = ngx_current_msec - peer->shm->access_time;
    ngx_log_debug5(NGX_LOG_DEBUG_HTTP, event->log, 0,
                   "http check begin handler index: %ud, owner: %P, "
                   "ngx_pid: %P, interval: %M, check_interval: %M",
                   peer->index, peer->shm->owner,
                   ngx_pid, interval,
                   ucscf->check_interval);

    ngx_spinlock(&peer->shm->lock, ngx_pid, 1024);

    if (peers_shm->generation != ngx_http_check_shm_generation) {
        ngx_spinlock_unlock(&peer->shm->lock);
        return;
    }

    if ((interval >= ucscf->check_interval)
            && peer->shm->owner == NGX_INVALID_PID)
    {
        peer->shm->owner = ngx_pid;
    }
    else if (interval >= (ucscf->check_interval << 4)) {
        /* If the check peer has been untouched for 4 times of
         * the check interval, activates current timer.
         * The checking process may be disappeared
         * in some circumstance, and the clean event will never
         * be triggered. */
        peer->shm->owner = ngx_pid;
        peer->shm->access_time = ngx_current_msec;
    }

    ngx_spinlock_unlock(&peer->shm->lock);

    if (peer->shm->owner == ngx_pid) {
        ngx_http_check_connect_handler(event);
    }
}


static void
ngx_http_check_connect_handler(ngx_event_t *event)
{
    ngx_int_t                            rc;
    ngx_connection_t                    *c;
    ngx_http_check_peer_t               *peer;
    ngx_http_upstream_check_srv_conf_t  *ucscf;

    if (ngx_http_check_need_exit()) {
        return;
    }

    peer = event->data;
    ucscf = peer->conf;

    ngx_memzero(&peer->pc, sizeof(ngx_peer_connection_t));

    peer->pc.sockaddr = peer->peer_addr->sockaddr;
    peer->pc.socklen = peer->peer_addr->socklen;
    peer->pc.name = &peer->peer_addr->name;

    peer->pc.get = ngx_event_get_peer;
    peer->pc.log = event->log;
    peer->pc.log_error = NGX_ERROR_ERR;

    peer->pc.cached = 0;
    peer->pc.connection = NULL;

    rc = ngx_event_connect_peer(&peer->pc);

    if (rc == NGX_ERROR || rc == NGX_DECLINED) {
        ngx_http_check_status_update(peer, 0);
        return;
    }

    /* NGX_OK or NGX_AGAIN */
    c = peer->pc.connection;
    c->data = peer;
    c->log = peer->pc.log;
    c->sendfile = 0;
    c->read->log = c->log;
    c->write->log = c->log;
    c->pool = peer->pool;

    peer->state = NGX_HTTP_CHECK_CONNECT_DONE;

    c->write->handler = peer->send_handler;
    c->read->handler = peer->recv_handler;

    ngx_add_timer(&peer->check_timeout_ev, ucscf->check_timeout);

    if (rc == NGX_OK) {
        c->write->handler(c->write);
    }
}


static void
ngx_http_check_peek_handler(ngx_event_t *event)
{
    char                           buf[1];
    ngx_int_t                      n;
    ngx_err_t                      err;
    ngx_connection_t              *c;
    ngx_http_check_peer_t         *peer;

    if (ngx_http_check_need_exit()) {
        return;
    }

    c = event->data;
    peer = c->data;

    n = recv(c->fd, buf, 1, MSG_PEEK);

    err = ngx_socket_errno;

    ngx_log_debug2(NGX_LOG_DEBUG_HTTP, c->log, err,
                   "http check upstream recv(): %i, fd: %d",
                   n, c->fd);

    if (n >= 0 || err == NGX_EAGAIN) {
        ngx_http_check_status_update(peer, 1);

    } else {
        c->error = 1;
        ngx_http_check_status_update(peer, 0);
    }

    ngx_http_check_clean_event(peer);

    ngx_http_check_finish_handler(event);
}


static void
ngx_http_check_send_handler(ngx_event_t *event)
{
    ssize_t                         size;
    ngx_connection_t               *c;
    ngx_http_check_ctx             *ctx;
    ngx_http_check_peer_t          *peer;

    if (ngx_http_check_need_exit()) {
        return;
    }

    c = event->data;
    peer = c->data;

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, c->log, 0, "http check send.");

    if (c->pool == NULL) {
        ngx_log_error(NGX_LOG_ERR, event->log, 0,
                      "check pool NULL with peer: %V ", &peer->peer_addr->name);

        goto check_send_fail;
    }

    if (peer->state != NGX_HTTP_CHECK_CONNECT_DONE) {
        if (ngx_handle_write_event(c->write, 0) != NGX_OK) {

            ngx_log_error(NGX_LOG_ERR, event->log, 0,
                          "check handle write event error with peer: %V ",
                          &peer->peer_addr->name);

            goto check_send_fail;
        }

        return;
    }

    if (peer->check_data == NULL) {

        peer->check_data = ngx_pcalloc(peer->pool, sizeof(ngx_http_check_ctx));
        if (peer->check_data == NULL) {
            goto check_send_fail;
        }

        if (peer->init == NULL || peer->init(peer) != NGX_OK) {

            ngx_log_error(NGX_LOG_ERR, event->log, 0,
                          "check init error with peer: %V ",
                          &peer->peer_addr->name);

            goto check_send_fail;
        }
    }

    ctx = peer->check_data;

    while (ctx->send.pos < ctx->send.last) {

        size = c->send(c, ctx->send.pos, ctx->send.last - ctx->send.pos);

#if (NGX_DEBUG)
        ngx_err_t                       err;
        err = (size >= 0) ? 0 : ngx_socket_errno;
        ngx_log_debug2(NGX_LOG_DEBUG_HTTP, c->log, err,
                       "http check send size: %z, total: %z",
                       size, ctx->send.last - ctx->send.pos);
#endif

        if (size > 0) {
            ctx->send.pos += size;

        } else if (size == 0 || size == NGX_AGAIN) {
            return;

        } else {
            c->error = 1;
            goto check_send_fail;
        }
    }

    if (ctx->send.pos == ctx->send.last) {
        ngx_log_debug0(NGX_LOG_DEBUG_HTTP, c->log, 0, "http check send done.");
        peer->state = NGX_HTTP_CHECK_SEND_DONE;
    }

    return;

check_send_fail:
    ngx_http_check_status_update(peer, 0);
    ngx_http_check_clean_event(peer);
    return;
}


static void
ngx_http_check_recv_handler(ngx_event_t *event)
{
    u_char                         *new_buf;
    ssize_t                         size, n;
    ngx_int_t                       rc;
    ngx_connection_t               *c;
    ngx_http_check_ctx             *ctx;
    ngx_http_check_peer_t          *peer;

    if (ngx_http_check_need_exit()) {
        return;
    }

    c = event->data;
    peer = c->data;

    if (peer->state != NGX_HTTP_CHECK_SEND_DONE) {

        if (ngx_handle_read_event(c->read, 0) != NGX_OK) {
            goto check_recv_fail;
        }

        return;
    }

    ctx = peer->check_data;

    if (ctx->recv.start == NULL) {
        /* 2048, is it enough? */
        ctx->recv.start = ngx_palloc(c->pool, ngx_pagesize/2);
        if (ctx->recv.start == NULL) {
            goto check_recv_fail;
        }

        ctx->recv.last = ctx->recv.pos = ctx->recv.start;
        ctx->recv.end = ctx->recv.start + ngx_pagesize/2;
    }

    while (1) {
        n = ctx->recv.end - ctx->recv.last;
        /* Not enough buffer? Enlarge twice */
        if (n == 0) {
            size = ctx->recv.end - ctx->recv.start;
            new_buf = ngx_palloc(c->pool, size * 2);
            if (new_buf == NULL) {
                goto check_recv_fail;
            }

            ngx_memcpy(new_buf, ctx->recv.start, size);

            ctx->recv.pos = ctx->recv.start = new_buf;
            ctx->recv.last = new_buf + size;
            ctx->recv.end = new_buf + size * 2;

            n = ctx->recv.end - ctx->recv.last;
        }

        size = c->recv(c, ctx->recv.last, n);

#if (NGX_DEBUG)
        ngx_err_t                       err;
        err = (size >= 0) ? 0 : ngx_socket_errno;
        ngx_log_debug2(NGX_LOG_DEBUG_HTTP, c->log, err,
                       "http check recv size: %z, peer: %V",
                       size, &peer->peer_addr->name);
#endif

        if (size > 0) {
            ctx->recv.last += size;
            continue;
        } else if (size == 0 || size == NGX_AGAIN) {
            break;
        }
        else {
            c->error = 1;
            goto check_recv_fail;
        }
    }

    rc = peer->parse(peer);

    ngx_log_debug2(NGX_LOG_DEBUG_HTTP, c->log, 0,
                   "http check parse rc: %i, peer: %V",
                   rc, &peer->peer_addr->name);

    switch (rc) {

    case NGX_AGAIN:
        /* The peer has closed its half side of the connection. */
        if (size == 0) {
            ngx_http_check_status_update(peer, 0);
            break;
        }

        return;

    case NGX_ERROR:
        ngx_log_error(NGX_LOG_ERR, event->log, 0,
                      "check protocol %s error with peer: %V ",
                      peer->conf->check_type_conf->name,
                      &peer->peer_addr->name);

        ngx_http_check_status_update(peer, 0);
        break;

    case NGX_OK:

    default:
        ngx_http_check_status_update(peer, 1);
    }

    peer->state = NGX_HTTP_CHECK_RECV_DONE;
    ngx_http_check_clean_event(peer);
    return;

check_recv_fail:
    ngx_http_check_status_update(peer, 0);
    ngx_http_check_clean_event(peer);
    return;
}


static ngx_int_t
ngx_http_check_http_init(ngx_http_check_peer_t *peer)
{
    ngx_http_check_ctx                  *ctx;
    ngx_http_upstream_check_srv_conf_t  *ucscf;

    ctx = peer->check_data;
    ucscf = peer->conf;

    ctx->send.start = ctx->send.pos = (u_char *)ucscf->send.data;
    ctx->send.end = ctx->send.last = ctx->send.start + ucscf->send.len;

    ctx->recv.start = ctx->recv.pos = NULL;
    ctx->recv.end = ctx->recv.last = NULL;

    ctx->state = 0;

    ngx_memzero(&ctx->status, sizeof(ngx_http_status_t));

    return NGX_OK;
}


static ngx_int_t
ngx_http_check_http_parse(ngx_http_check_peer_t *peer)
{
    ngx_int_t                            rc, code, code_n;
    ngx_http_check_ctx                  *ctx;
    ngx_http_upstream_check_srv_conf_t  *ucscf;

    ucscf = peer->conf;
    ctx = peer->check_data;

    if ((ctx->recv.last - ctx->recv.pos) > 0) {

        rc = ngx_http_check_parse_status_line(ctx, &ctx->recv, &ctx->status);

        if (rc == NGX_AGAIN) {
            return rc;
        }

        if (rc == NGX_ERROR) {
            ngx_log_error(NGX_LOG_ERR, ngx_cycle->log, 0,
                          "http parse error with peer: %V, recv data: %s",
                          &peer->peer_addr->name, ctx->recv.start);
            return rc;
        }

        code = ctx->status.code;

        if (code >= 200 && code < 300) {
            code_n = NGX_CHECK_HTTP_2XX;

        } else if (code >= 300 && code < 400) {
            code_n = NGX_CHECK_HTTP_3XX;

        } else if (code >= 400 && code < 500) {
            code_n = NGX_CHECK_HTTP_4XX;

        } else if (code >= 500 && code < 600) {
            code_n = NGX_CHECK_HTTP_5XX;

        } else {
            code_n = NGX_CHECK_HTTP_ERR;
        }

        ngx_log_debug2(NGX_LOG_DEBUG_HTTP, ngx_cycle->log, 0,
                       "http_parse: code_n: %i, conf: %ui",
                       code_n, ucscf->code.status_alive);

        if (code_n & ucscf->code.status_alive) {
            return NGX_OK;

        } else {
            return NGX_ERROR;
        }

    } else {
        return NGX_AGAIN;
    }

    return NGX_OK;
}


/* This function copied from ngx_http_parse.c */
static ngx_int_t
ngx_http_check_parse_status_line(ngx_http_check_ctx *ctx, ngx_buf_t *b,
    ngx_http_status_t *status)
{
    u_char   ch;
    u_char  *p;
    enum {
        sw_start = 0,
        sw_H,
        sw_HT,
        sw_HTT,
        sw_HTTP,
        sw_first_major_digit,
        sw_major_digit,
        sw_first_minor_digit,
        sw_minor_digit,
        sw_status,
        sw_space_after_status,
        sw_status_text,
        sw_almost_done
    } state;

    state = ctx->state;

    for (p = b->pos; p < b->last; p++) {
        ch = *p;

        switch (state) {

        /* "HTTP/" */
        case sw_start:
            switch (ch) {
            case 'H':
                state = sw_H;
                break;
            default:
                return NGX_ERROR;
            }
            break;

        case sw_H:
            switch (ch) {
            case 'T':
                state = sw_HT;
                break;
            default:
                return NGX_ERROR;
            }
            break;

        case sw_HT:
            switch (ch) {
            case 'T':
                state = sw_HTT;
                break;
            default:
                return NGX_ERROR;
            }
            break;

        case sw_HTT:
            switch (ch) {
            case 'P':
                state = sw_HTTP;
                break;
            default:
                return NGX_ERROR;
            }
            break;

        case sw_HTTP:
            switch (ch) {
            case '/':
                state = sw_first_major_digit;
                break;
            default:
                return NGX_ERROR;
            }
            break;

        /* the first digit of major HTTP version */
        case sw_first_major_digit:
            if (ch < '1' || ch > '9') {
                return NGX_ERROR;
            }

            state = sw_major_digit;
            break;

        /* the major HTTP version or dot */
        case sw_major_digit:
            if (ch == '.') {
                state = sw_first_minor_digit;
                break;
            }

            if (ch < '0' || ch > '9') {
                return NGX_ERROR;
            }

            break;

        /* the first digit of minor HTTP version */
        case sw_first_minor_digit:
            if (ch < '0' || ch > '9') {
                return NGX_ERROR;
            }

            state = sw_minor_digit;
            break;

        /* the minor HTTP version or the end of the request line */
        case sw_minor_digit:
            if (ch == ' ') {
                state = sw_status;
                break;
            }

            if (ch < '0' || ch > '9') {
                return NGX_ERROR;
            }

            break;

        /* HTTP status code */
        case sw_status:
            if (ch == ' ') {
                break;
            }

            if (ch < '0' || ch > '9') {
                return NGX_ERROR;
            }

            status->code = status->code * 10 + ch - '0';

            if (++status->count == 3) {
                state = sw_space_after_status;
                status->start = p - 2;
            }

            break;

        /* space or end of line */
        case sw_space_after_status:
            switch (ch) {
            case ' ':
                state = sw_status_text;
                break;
            case '.':                    /* IIS may send 403.1, 403.2, etc */
                state = sw_status_text;
                break;
            case CR:
                state = sw_almost_done;
                break;
            case LF:
                goto done;
            default:
                return NGX_ERROR;
            }
            break;

        /* any text until end of line */
        case sw_status_text:
            switch (ch) {
            case CR:
                state = sw_almost_done;

                break;
            case LF:
                goto done;
            }
            break;

        /* end of status line */
        case sw_almost_done:
            status->end = p - 1;
            switch (ch) {
            case LF:
                goto done;
            default:
                return NGX_ERROR;
            }
        }
    }

    b->pos = p;
    ctx->state = state;

    return NGX_AGAIN;

done:

    b->pos = p + 1;

    if (status->end == NULL) {
        status->end = p;
    }

    ctx->state = sw_start;

    return NGX_OK;
}


static void
ngx_http_check_http_reinit(ngx_http_check_peer_t *peer)
{
    ngx_http_check_ctx *ctx;

    ctx = peer->check_data;

    ctx->send.pos = ctx->send.start;
    ctx->send.last = ctx->send.end;

    ctx->recv.pos = ctx->recv.last = ctx->recv.start;

    ctx->state = 0;

    ngx_memzero(&ctx->status, sizeof(ngx_http_status_t));
}


static ngx_int_t
ngx_http_check_ssl_hello_init(ngx_http_check_peer_t *peer)
{
    ngx_http_check_ctx                  *ctx;
    ngx_http_upstream_check_srv_conf_t  *ucscf;

    ctx = peer->check_data;
    ucscf = peer->conf;

    ctx->send.start = ctx->send.pos = (u_char *)ucscf->send.data;
    ctx->send.end = ctx->send.last = ctx->send.start + ucscf->send.len;

    ctx->recv.start = ctx->recv.pos = NULL;
    ctx->recv.end = ctx->recv.last = NULL;

    return NGX_OK;
}


/* a rough check of server ssl_hello responses */
static ngx_int_t
ngx_http_check_ssl_hello_parse(ngx_http_check_peer_t *peer)
{
    size_t                        size;
    server_ssl_hello_t           *resp;
    ngx_http_check_ctx           *ctx;

    ctx = peer->check_data;

    size = ctx->recv.last - ctx->recv.pos;
    if (size < sizeof(server_ssl_hello_t)) {
        return NGX_AGAIN;
    }

    resp = (server_ssl_hello_t *) ctx->recv.pos;

    ngx_log_debug7(NGX_LOG_DEBUG_HTTP, ngx_cycle->log, 0,
                   "http check ssl_parse, type: %ud, version: %ud.%ud, "
                   "length: %ud, handshanke_type: %ud, hello_version: %ud.%ud",
                   resp->msg_type, resp->version.major, resp->version.minor,
                   ntohs(resp->length), resp->handshake_type,
                   resp->hello_version.major, resp->hello_version.minor);

    if (resp->msg_type != HANDSHAKE) {
        return NGX_ERROR;
    }

    if (resp->handshake_type != SERVER_HELLO) {
        return NGX_ERROR;
    }

    return NGX_OK;
}


static void
ngx_http_check_ssl_hello_reinit(ngx_http_check_peer_t *peer)
{
    ngx_http_check_ctx *ctx;

    ctx = peer->check_data;

    ctx->send.pos = ctx->send.start;
    ctx->send.last = ctx->send.end;

    ctx->recv.pos = ctx->recv.last = ctx->recv.start;
}


static ngx_int_t
ngx_http_check_mysql_init(ngx_http_check_peer_t *peer)
{
    ngx_http_check_ctx                  *ctx;
    ngx_http_upstream_check_srv_conf_t  *ucscf;

    ctx = peer->check_data;
    ucscf = peer->conf;

    ctx->send.start = ctx->send.pos = (u_char *)ucscf->send.data;
    ctx->send.end = ctx->send.last = ctx->send.start + ucscf->send.len;

    ctx->recv.start = ctx->recv.pos = NULL;
    ctx->recv.end = ctx->recv.last = NULL;

    return NGX_OK;
}


/* a rough check of mysql greeting responses */
static ngx_int_t
ngx_http_check_mysql_parse(ngx_http_check_peer_t *peer)
{
    size_t                         size;
    ngx_http_check_ctx            *ctx;
    mysql_handshake_init_t        *handshake;

    ctx = peer->check_data;

    size = ctx->recv.last - ctx->recv.pos;
    if (size < sizeof(mysql_handshake_init_t)) {
        return NGX_AGAIN;
    }

    handshake = (mysql_handshake_init_t *) ctx->recv.pos;

    ngx_log_debug2(NGX_LOG_DEBUG_HTTP, ngx_cycle->log, 0,
                   "mysql_parse: packet_number=%ud, protocol=%ud",
                   handshake->packet_number, handshake->protocol_version);

    /* The mysql greeting packet's serial number always begins with 0. */
    if (handshake->packet_number != 0x00) {
        return NGX_ERROR;
    }

    return NGX_OK;
}


static void
ngx_http_check_mysql_reinit(ngx_http_check_peer_t *peer)
{
    ngx_http_check_ctx *ctx;

    ctx = peer->check_data;

    ctx->send.pos = ctx->send.start;
    ctx->send.last = ctx->send.end;

    ctx->recv.pos = ctx->recv.last = ctx->recv.start;
}


static ngx_int_t
ngx_http_check_ajp_init(ngx_http_check_peer_t *peer)
{
    ngx_http_check_ctx                  *ctx;
    ngx_http_upstream_check_srv_conf_t  *ucscf;

    ctx = peer->check_data;
    ucscf = peer->conf;

    ctx->send.start = ctx->send.pos = (u_char *)ucscf->send.data;
    ctx->send.end = ctx->send.last = ctx->send.start + ucscf->send.len;

    ctx->recv.start = ctx->recv.pos = NULL;
    ctx->recv.end = ctx->recv.last = NULL;

    return NGX_OK;
}


static ngx_int_t
ngx_http_check_ajp_parse(ngx_http_check_peer_t *peer)
{
    u_char                        *p;
    ngx_http_check_ctx            *ctx;

    ctx = peer->check_data;

    if ((size_t)(ctx->recv.last - ctx->recv.pos) < sizeof(ajp_cpong_packet)) {
        return NGX_AGAIN;
    }

    p = ctx->recv.pos;

#if (NGX_DEBUG)
    ajp_raw_packet_t              *ajp;

    ajp = (ajp_raw_packet_t *)p;
    ngx_log_debug3(NGX_LOG_DEBUG_HTTP, ngx_cycle->log, 0,
                   "ajp_parse: preamble=0x%xd, length=0x%xd, type=0x%xd",
                   ntohs(ajp->preamble), ntohs(ajp->length), ajp->type);
#endif

    if (ngx_memcmp(ajp_cpong_packet, p, sizeof(ajp_cpong_packet)) == 0) {
        return NGX_OK;

    } else {
        return NGX_ERROR;
    }
}


static void
ngx_http_check_ajp_reinit(ngx_http_check_peer_t *peer)
{
    ngx_http_check_ctx *ctx;

    ctx = peer->check_data;

    ctx->send.pos = ctx->send.start;
    ctx->send.last = ctx->send.end;

    ctx->recv.pos = ctx->recv.last = ctx->recv.start;
}


static void
ngx_http_check_status_update(ngx_http_check_peer_t *peer, ngx_int_t result)
{
    ngx_http_upstream_check_srv_conf_t   *ucscf;

    ucscf = peer->conf;

    if (result) {
        peer->shm->rise_count++;
        peer->shm->fall_count = 0;
        if (peer->shm->down && peer->shm->rise_count >= ucscf->rise_count) {
            peer->shm->down = 0;
        }

    } else {
        peer->shm->rise_count = 0;
        peer->shm->fall_count++;
        if (!peer->shm->down && peer->shm->fall_count >= ucscf->fall_count) {
            peer->shm->down = 1;
        }
    }

    peer->shm->access_time = ngx_current_msec;
}


static void
ngx_http_check_clean_event(ngx_http_check_peer_t *peer)
{
    ngx_connection_t             *c;

    c = peer->pc.connection;

    if (c) {
        ngx_log_debug2(NGX_LOG_DEBUG_HTTP, c->log, 0,
                "http check clean event: index:%ui, fd: %d",
                peer->index, c->fd);

        ngx_close_connection(c);
        peer->pc.connection = NULL;
    }

    if (peer->check_timeout_ev.timer_set) {
        ngx_del_timer(&peer->check_timeout_ev);
    }

    peer->state = NGX_HTTP_CHECK_ALL_DONE;

    if (peer->check_data != NULL && peer->reinit) {
        peer->reinit(peer);
    }

    peer->shm->owner = NGX_INVALID_PID;
}


static void
ngx_http_check_timeout_handler(ngx_event_t *event)
{
    ngx_http_check_peer_t          *peer;

    if (ngx_http_check_need_exit()) {
        return;
    }

    peer = event->data;

    ngx_log_error(NGX_LOG_ERR, event->log, 0,
                  "check time out with peer: %V ", &peer->peer_addr->name);

    ngx_http_check_status_update(peer, 0);
    ngx_http_check_clean_event(peer);
}


static void
ngx_http_check_finish_handler(ngx_event_t *event)
{
    if (ngx_http_check_need_exit()) {
        return;
    }
}


static ngx_int_t
ngx_http_check_need_exit()
{
    if (ngx_terminate || ngx_exiting || ngx_quit) {
        ngx_http_check_clear_all_events();
        return 1;
    }

    return 0;
}


static ngx_flag_t has_cleared = 0;

static void
ngx_http_check_clear_all_events()
{
    ngx_uint_t                      i;
    ngx_connection_t               *c;
    ngx_http_check_peer_t          *peer;
    ngx_http_check_peers_t         *peers;

    if (has_cleared || check_peers_ctx == NULL) {
        return;
    }

    ngx_log_error(NGX_LOG_NOTICE, ngx_cycle->log, 0,
                  "clear all the events on %P ", ngx_pid);

    has_cleared = 1;

    peers = check_peers_ctx;

    peer = peers->peers.elts;
    for (i = 0; i < peers->peers.nelts; i++) {

        if (peer[i].check_ev.timer_set) {
            ngx_del_timer(&peer[i].check_ev);
        }

        /* Be careful, The shared memory may have been freed after reload */
        if (peer[i].check_timeout_ev.timer_set) {
            c = peer[i].pc.connection;
            if (c) {
                ngx_close_connection(c);
                peer[i].pc.connection = NULL;
            }
            ngx_del_timer(&peer[i].check_timeout_ev);
        }

        if (peer[i].pool != NULL) {
            ngx_destroy_pool(peer[i].pool);
            peer[i].pool = NULL;
        }
    }
}


static ngx_int_t
ngx_http_upstream_check_init_shm_zone(ngx_shm_zone_t *shm_zone, void *data)
{
    size_t                               size;
    ngx_str_t                            oshm_name = ngx_null_string;
    ngx_uint_t                           i, same, number;
    ngx_shm_zone_t                      *oshm_zone;
    ngx_slab_pool_t                     *shpool;
    ngx_http_check_peer_t               *peer;
    ngx_http_check_peers_t              *peers;
    ngx_http_check_peer_shm_t           *peer_shm, *opeer_shm;
    ngx_http_check_peers_shm_t          *peers_shm, *opeers_shm;
    ngx_http_upstream_check_srv_conf_t  *ucscf;

    opeers_shm = NULL;
    peers_shm = NULL;

    same = 0;
    peers = check_peers_ctx;

    number =  peers->peers.nelts;
    shpool = (ngx_slab_pool_t *) shm_zone->shm.addr;

    if (peers == NULL || number == 0) {
        return NGX_OK;
    }

    if (data) {
        opeers_shm = data;

        if (opeers_shm->number == number
            && opeers_shm->checksum == peers->checksum) {

            peers_shm = data;
            same = 1;
        }
    }

    if (!same) {

        if (ngx_http_check_shm_generation > 1) {

            ngx_http_check_get_shm_name(&oshm_name, ngx_cycle->pool,
                                        ngx_http_check_shm_generation - 1);

            /* The global variable ngx_cycle still points to the old version */
            oshm_zone = ngx_shared_memory_find((ngx_cycle_t *)ngx_cycle,
                                               &oshm_name,
                                               &ngx_http_upstream_check_module);

            if (oshm_zone) {
                opeers_shm = oshm_zone->data;

                ngx_log_debug2(NGX_LOG_DEBUG_HTTP, shm_zone->shm.log, 0,
                               "http upstream check, find oshm_zone:%p, "
                               "opeers_shm: %p",
                               oshm_zone, opeers_shm);
            }
        }

        size = sizeof(*peers_shm) +
               (number - 1) * sizeof(ngx_http_check_peer_shm_t);

        peers_shm = ngx_slab_alloc(shpool, size);

        if (peers_shm == NULL) {
            goto failure;
        }

        ngx_memzero(peers_shm, size);
    }

    peers_shm->generation = ngx_http_check_shm_generation;
    peers_shm->checksum = peers->checksum;
    peers_shm->number = number;

    peer = peers->peers.elts;

    for (i = 0; i < number; i++) {

        peer_shm = &peers_shm->peers[i];

        /* This function may be triggered before the old stale
         * work process exits. The owner may stick to the old
         * pid. */
        peer_shm->owner = NGX_INVALID_PID;

        if (same) {
            continue;
        }

        peer_shm->socklen = peer[i].peer_addr->socklen;
        peer_shm->sockaddr = ngx_slab_alloc(shpool, peer_shm->socklen);
        if (peer_shm->sockaddr == NULL) {
            goto failure;
        }

        ngx_memcpy(peer_shm->sockaddr, peer[i].peer_addr->sockaddr,
                   peer_shm->socklen);

        if (opeers_shm) {

            opeer_shm = ngx_http_check_find_shm_peer(opeers_shm,peer[i].peer_addr,peer[i].upstream_name);
            if (opeer_shm) {
                ngx_log_debug1(NGX_LOG_DEBUG_HTTP, shm_zone->shm.log, 0,
                               "http upstream check: inherit opeer:%V",
                               &peer[i].peer_addr->name);

                ngx_http_check_set_shm_peer(peer_shm, opeer_shm, 0,peer[i].upstream_name);
                continue;
            }
        }

        ucscf = peer[i].conf;
        ngx_http_check_set_shm_peer(peer_shm, NULL, ucscf->default_down,peer[i].upstream_name);
    }

    peers->peers_shm = peers_shm;
    shm_zone->data = peers_shm;

    return NGX_OK;

failure:
    ngx_log_error(NGX_LOG_EMERG, shm_zone->shm.log, 0,
                  "http upstream check_shm_size is too small, "
                  "you should specify a larger size.");
    return NGX_ERROR;
}


static ngx_http_check_peer_shm_t *
ngx_http_check_find_shm_peer(ngx_http_check_peers_shm_t *peers_shm,
                             ngx_addr_t *addr,
			     ngx_str_t *upstream_name)
{
    ngx_uint_t                    i;
    ngx_http_check_peer_shm_t    *peer_shm;

    for (i = 0; i < peers_shm->number; i++) {

        peer_shm = &peers_shm->peers[i];

	if (addr->socklen != peer_shm->socklen) {
		continue;
        }
	
        if (ngx_memcmp(addr->sockaddr, peer_shm->sockaddr, addr->socklen) == 0 && ngx_strcmp(upstream_name->data,peer_shm->upstream_name->data) == 0) 
	{
            return peer_shm;
        }
    }

    return NULL;
}


static void
ngx_http_check_set_shm_peer(ngx_http_check_peer_shm_t *peer_shm,
                            ngx_http_check_peer_shm_t *opeer_shm,
                            ngx_uint_t init_down,
			    ngx_str_t *upstream_name)
{

    if (opeer_shm) {
        peer_shm->access_time  = opeer_shm->access_time;
        peer_shm->access_count = opeer_shm->access_count;

        peer_shm->fall_count   = opeer_shm->fall_count;
        peer_shm->rise_count   = opeer_shm->rise_count;
        peer_shm->busyness     = opeer_shm->busyness;

        peer_shm->down         = opeer_shm->down;

    } else{
        peer_shm->access_time  = 0;
        peer_shm->access_count = 0;

        peer_shm->fall_count   = 0;
        peer_shm->rise_count   = 0;
        peer_shm->busyness     = 0;

        peer_shm->down         = init_down;
    }
    peer_shm->upstream_name = upstream_name;
}


static ngx_int_t
ngx_http_check_get_shm_name(ngx_str_t *shm_name, ngx_pool_t *pool,
                            ngx_uint_t generation)
{
    u_char    *last;

    shm_name->data = ngx_palloc(pool, SHM_NAME_LEN);
    if (shm_name->data == NULL) {
        return NGX_ERROR;
    }

    last = ngx_snprintf(shm_name->data, SHM_NAME_LEN, "%s#%ui",
                        "ngx_http_upstream_check", generation);

    shm_name->len = last - shm_name->data;

    return NGX_OK;
}


char *
ngx_http_upstream_check_init_shm(ngx_conf_t *cf, void *conf)
{
    ngx_str_t                            *shm_name;
    ngx_uint_t                            shm_size;
    ngx_shm_zone_t                       *shm_zone;
    ngx_http_upstream_check_main_conf_t  *ucmcf = conf;

    if (ucmcf->peers->peers.nelts > 0) {

        ngx_http_check_shm_generation++;

        shm_name = &ucmcf->peers->check_shm_name;

        ngx_http_check_get_shm_name(shm_name, cf->pool,
                                    ngx_http_check_shm_generation);

        /* the default check share memory size, 1M */
        shm_size = 1 * 1024 * 1024;

        shm_size = shm_size < ucmcf->check_shm_size ?
                   ucmcf->check_shm_size : shm_size;

        shm_zone = ngx_shared_memory_add(cf, shm_name, shm_size,
                                         &ngx_http_upstream_check_module);

        ngx_log_debug2(NGX_LOG_DEBUG_HTTP, cf->log, 0,
                       "[http_upstream] upsteam:%V, shm_zone size:%ui",
                       shm_name, shm_size);

        shm_zone->data = NULL;
        check_peers_ctx = ucmcf->peers;

        shm_zone->init = ngx_http_upstream_check_init_shm_zone;
    }
    else {
         check_peers_ctx = NULL;
    }

    return NGX_CONF_OK;
}


static ngx_shm_zone_t *
ngx_shared_memory_find(ngx_cycle_t *cycle, ngx_str_t *name, void *tag)
{
    ngx_uint_t        i;
    ngx_shm_zone_t   *shm_zone;
    ngx_list_part_t  *part;

    part = (ngx_list_part_t *) & (cycle->shared_memory.part);
    shm_zone = part->elts;

    for (i = 0; /* void */ ; i++) {

        if (i >= part->nelts) {
            if (part->next == NULL) {
                break;
            }
            part = part->next;
            shm_zone = part->elts;
            i = 0;
        }

        if (name->len != shm_zone[i].shm.name.len) {
            continue;
        }

        if (ngx_strncmp(name->data, shm_zone[i].shm.name.data, name->len)
                != 0)
        {
            continue;
        }

        if (tag != shm_zone[i].tag) {
            continue;
        }

        return &shm_zone[i];
    }

    return NULL;
}


ngx_int_t
ngx_http_upstream_check_status_handler(ngx_http_request_t *r)
{
    size_t                          buffer_size;
    ngx_int_t                       rc;
    ngx_buf_t                      *b;
    ngx_uint_t                      i;
    ngx_chain_t                     out;
    ngx_http_check_peer_t          *peer;
    ngx_http_check_peers_t         *peers;
    ngx_http_check_peer_shm_t      *peer_shm;
    ngx_http_check_peers_shm_t     *peers_shm;

    if (r->method != NGX_HTTP_GET && r->method != NGX_HTTP_HEAD) {
        return NGX_HTTP_NOT_ALLOWED;
    }

    rc = ngx_http_discard_request_body(r);

    if (rc != NGX_OK) {
        return rc;
    }

    r->headers_out.content_type.len = sizeof("text/html; charset=utf-8") - 1;
    r->headers_out.content_type.data = (u_char *) "text/html; charset=utf-8";

    if (r->method == NGX_HTTP_HEAD) {
        r->headers_out.status = NGX_HTTP_OK;

        rc = ngx_http_send_header(r);

        if (rc == NGX_ERROR || rc > NGX_OK || r->header_only) {
            return rc;
        }
    }

    peers = check_peers_ctx;
    if (peers == NULL) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "[http upstream check] can not find the check servers, "
                      "have you added the check servers? ");

        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    peers_shm = peers->peers_shm;

    peer = peers->peers.elts;
    peer_shm = peers_shm->peers;

    buffer_size = peers->peers.nelts * ngx_pagesize / 4;
    buffer_size = ngx_align(buffer_size, ngx_pagesize) + ngx_pagesize;

    b = ngx_create_temp_buf(r->pool, buffer_size);
    if (b == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    out.buf = b;
    out.next = NULL;

    b->last = ngx_snprintf(b->last, b->end - b->last,
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\n"
            "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
            "<head>\n"
            "  <title>Nginx http upstream check status</title>\n"
            "</head>\n"
            "<body>\n"
            "<h1>Nginx http upstream check status</h1>\n"
            "<h2>Check upstream server number: %ui, generation: %ui</h2>\n"
            "<table style=\"background-color:white\" cellspacing=\"0\" "
            "       cellpadding=\"3\" border=\"1\">\n"
            "  <tr bgcolor=\"#C0C0C0\">\n"
            "    <th>Index</th>\n"
            "    <th>Upstream</th>\n"
            "    <th>Name</th>\n"
            "    <th>Status</th>\n"
            "    <th>Rise counts</th>\n"
            "    <th>Fall counts</th>\n"
            "    <th>Check type</th>\n"
            "  </tr>\n",
            peers->peers.nelts, ngx_http_check_shm_generation);

    for (i = 0; i < peers->peers.nelts; i++) {
        b->last = ngx_snprintf(b->last, b->end - b->last,
                "  <tr%s>\n"
                "    <td>%ui</td>\n"
                "    <td>%V</td>\n"
                "    <td>%V</td>\n"
                "    <td>%s</td>\n"
                "    <td>%ui</td>\n"
                "    <td>%ui</td>\n"
                "    <td>%s</td>\n"
                "  </tr>\n",
                peer_shm[i].down ? " bgcolor=\"#FF0000\"" : "",
                i,
                peer[i].upstream_name,
                &peer[i].peer_addr->name,
                peer_shm[i].down ? "down" : "up",
                peer_shm[i].rise_count,
                peer_shm[i].fall_count,
                peer[i].conf->check_type_conf->name);
    }

    b->last = ngx_snprintf(b->last, b->end - b->last,
            "</table>\n"
            "</body>\n"
            "</html>\n");

    r->headers_out.status = NGX_HTTP_OK;
    r->headers_out.content_length_n = b->last - b->pos;

    b->last_buf = 1;

    rc = ngx_http_send_header(r);

    if (rc == NGX_ERROR || rc > NGX_OK || r->header_only) {
        return rc;
    }

    return ngx_http_output_filter(r, &out);
}
