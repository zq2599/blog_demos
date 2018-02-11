/* Copyright (C) agentzh */

#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_drizzle_module.h"
#include "ngx_http_drizzle_upstream.h"
#include "ngx_http_drizzle_keepalive.h"
#include "ngx_http_drizzle_processor.h"
#include "ngx_http_drizzle_util.h"

enum {
    ngx_http_drizzle_default_port = 3306
};

static void ngx_http_upstream_drizzle_cleanup(void *data);

static ngx_int_t ngx_http_upstream_drizzle_init(ngx_conf_t *cf,
        ngx_http_upstream_srv_conf_t *uscf);

static ngx_int_t ngx_http_upstream_drizzle_init_peer(ngx_http_request_t *r,
    ngx_http_upstream_srv_conf_t *uscf);

static ngx_int_t ngx_http_upstream_drizzle_get_peer(ngx_peer_connection_t *pc,
        void *data);

static void ngx_http_upstream_drizzle_free_peer(ngx_peer_connection_t *pc,
        void *data, ngx_uint_t state);

/* just a work-around to override the default u->output_filter */
static ngx_int_t ngx_http_drizzle_output_filter(void *data, ngx_chain_t *in);


void *
ngx_http_upstream_drizzle_create_srv_conf(ngx_conf_t *cf)
{
    ngx_pool_cleanup_t  *cln;
    ngx_http_upstream_drizzle_srv_conf_t  *conf;

    dd("drizzle create srv conf");

    conf = ngx_pcalloc(cf->pool,
                       sizeof(ngx_http_upstream_drizzle_srv_conf_t));
    if (conf == NULL) {
        return NULL;
    }

    /* set by ngx_pcalloc:
     *      conf->peers   = NULL
     *      conf->current = 0
     *      conf->servers = NULL
     *      conf->single = 0
     *      conf->max_cached = 0
     *      conf->overflow = 0 (drizzle_keepalive_overflow_ignore)
     */

    conf->pool = cf->pool;

    cln = ngx_pool_cleanup_add(cf->pool, 0);

    (void) drizzle_create(&conf->drizzle);

    cln->handler = ngx_http_upstream_drizzle_cleanup;
    cln->data = &conf->drizzle;

    drizzle_add_options(&conf->drizzle, DRIZZLE_NON_BLOCKING);

    /* we use 0 timeout for the underlying poll event model
     * used by libdrizzle itself. */
    drizzle_set_timeout(&conf->drizzle, 0);

    return conf;
}


/* mostly based on ngx_http_upstream_server in
 * ngx_http_upstream.c of nginx 0.8.30.
 * Copyright (C) Igor Sysoev */
char *
ngx_http_upstream_drizzle_server(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf)
{
    ngx_http_upstream_drizzle_srv_conf_t        *dscf = conf;
    ngx_http_upstream_drizzle_server_t          *ds;
    ngx_str_t                                   *value;
    ngx_url_t                                    u;
    ngx_uint_t                                   i, j;
    ngx_http_upstream_srv_conf_t                *uscf;
    ngx_str_t                                    protocol;
    ngx_str_t                                    charset;
    u_char                                      *p;
    size_t                                       len;

    dd("entered drizzle_server directive handler...");

    uscf = ngx_http_conf_get_module_srv_conf(cf, ngx_http_upstream_module);

    if (dscf->servers == NULL) {
        dscf->servers = ngx_array_create(cf->pool, 4,
                                 sizeof(ngx_http_upstream_drizzle_server_t));

        if (dscf->servers == NULL) {
            return NGX_CONF_ERROR;
        }

        uscf->servers = dscf->servers;
    }

    ds = ngx_array_push(dscf->servers);
    if (ds == NULL) {
        return NGX_CONF_ERROR;
    }

    ngx_memzero(ds, sizeof(ngx_http_upstream_drizzle_server_t));

    value = cf->args->elts;

    /* parse the first name:port argument */

    ngx_memzero(&u, sizeof(ngx_url_t));

    u.url = value[1];
    u.default_port = ngx_http_drizzle_default_port;

    if (ngx_parse_url(cf->pool, &u) != NGX_OK) {
        if (u.err) {
            ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                               "drizzle: %s in upstream \"%V\"", u.err, &u.url);
        }

        return NGX_CONF_ERROR;
    }

    ds->addrs  = u.addrs;
    ds->naddrs = u.naddrs;
    ds->port   = u.port;
    ds->protocol = ngx_http_drizzle_protocol;

    /* parse various options */

    for (i = 2; i < cf->args->nelts; i++) {

        if (ngx_strncmp(value[i].data, "dbname=", sizeof("dbname=") - 1)
                == 0)
        {
            ds->dbname.len = value[i].len - (sizeof("dbname=") - 1);

            if (ds->dbname.len >= DRIZZLE_MAX_DB_SIZE) {
                ngx_log_error(NGX_LOG_EMERG, cf->log, 0,
                       "drizzle: \"dbname\" value too large in upstream \"%V\""
                       " (at most %d bytes)",
                       dscf->peers->name,
                       (int) DRIZZLE_MAX_DB_SIZE);

                return NGX_CONF_ERROR;
            }

            ds->dbname.data = &value[i].data[sizeof("dbname=") - 1];

            continue;
        }

        if (ngx_strncmp(value[i].data, "user=", sizeof("user=") - 1)
                == 0)
        {
            ds->user.len = value[i].len - (sizeof("user=") - 1);

            if (ds->user.len >= DRIZZLE_MAX_USER_SIZE) {
                ngx_log_error(NGX_LOG_EMERG, cf->log, 0,
                       "drizzle: \"user\" value too large in upstream \"%V\""
                       " (at most %d bytes)",
                       dscf->peers->name,
                       (int) DRIZZLE_MAX_USER_SIZE);

                return NGX_CONF_ERROR;
            }

            ds->user.data = &value[i].data[sizeof("user=") - 1];

            continue;
        }

        if (ngx_strncmp(value[i].data, "password=", sizeof("password=") - 1)
                == 0)
        {
            ds->password.len = value[i].len - (sizeof("password=") - 1);

            if (ds->password.len >= DRIZZLE_MAX_PASSWORD_SIZE) {
                ngx_log_error(NGX_LOG_EMERG, cf->log, 0,
                       "drizzle: \"password\" value too large in upstream "
                       "\"%V\" (at most %d bytes)",
                       dscf->peers->name,
                       (int) DRIZZLE_MAX_PASSWORD_SIZE);

                return NGX_CONF_ERROR;
            }

            ds->password.data = &value[i].data[sizeof("password=") - 1];

            continue;
        }

        if (ngx_strncmp(value[i].data, "protocol=", sizeof("protocol=") - 1)
                == 0)
        {
            protocol.len = value[i].len - (sizeof("protocol=") - 1);
            protocol.data = &value[i].data[sizeof("protocol=") - 1];

            switch (protocol.len) {
            case 5:
                if (ngx_http_drizzle_strcmp_const(protocol.data, "mysql") == 0)
                {
                    ds->protocol = ngx_http_mysql_protocol;
                } else {
                    continue;
                }

                break;

            case 7:
                if (ngx_http_drizzle_strcmp_const(protocol.data,
                            "drizzle") != 0)
                {
                    continue;
                }

                break;
            default:
                ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                               "drizzle: invalid protocol \"%V\""
                               " in drizzle_server", &protocol);

                return NGX_CONF_ERROR;
            }

            continue;
        }

        if (ngx_strncmp(value[i].data, "charset=", sizeof("charset=") - 1)
                == 0)
        {
            charset.len = value[i].len - (sizeof("charset=") - 1);
            charset.data = &value[i].data[sizeof("charset=") - 1];

            dd("charset: %.*s", (int) charset.len, charset.data);

            if (charset.len == 0) {
                continue;
            }

            for (j = 0; j < charset.len; j++) {
                if (charset.data[j] == '\'') {
                    ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                                       "bad charste value \"%V\" in"
                                       " drizzle_server", &charset);

                    return NGX_CONF_ERROR;
                }
            }

            len = sizeof("set names ''") - 1 + charset.len;

            p = ngx_palloc(cf->pool, len);
            if (p == NULL) {
                return NGX_CONF_ERROR;
            }

            ds->set_names_query.data = p;
            ds->set_names_query.len = len;

            dd("charset query len: %d", (int) len);

            p = ngx_copy(p, "set names '", sizeof("set names '") - 1);
            p = ngx_copy(p, charset.data, charset.len);
            *p = '\'';

            continue;
        }

        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "invalid parameter \"%V\" in"
                           " drizzle_server", &value[i]);

        return NGX_CONF_ERROR;
    }

    dd("reset init_upstream...");

    uscf->peer.init_upstream = ngx_http_upstream_drizzle_init;

    return NGX_CONF_OK;
}


static ngx_int_t
ngx_http_upstream_drizzle_init(ngx_conf_t *cf,
    ngx_http_upstream_srv_conf_t *uscf)
{
    ngx_uint_t                               i, j, n;
    ngx_http_upstream_drizzle_srv_conf_t    *dscf;
    ngx_http_upstream_drizzle_server_t      *server;
    ngx_http_upstream_drizzle_peers_t       *peers;
    size_t                                   len;

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, cf->log, 0,
            "drizzle upstream init");

    uscf->peer.init = ngx_http_upstream_drizzle_init_peer;

    dscf = ngx_http_conf_upstream_srv_conf(uscf, ngx_http_drizzle_module);

    if (dscf->servers == NULL || dscf->servers->nelts == 0) {
        /* XXX an upstream implicitly defined by drizzle_pass, etc.,
         * is not allowed for now */

        ngx_log_error(NGX_LOG_EMERG, cf->log, 0,
                      "drizzle: no drizzle_server defined in upstream \"%V\""
                      " in %s:%ui",
                      &uscf->host, uscf->file_name, uscf->line);

        return NGX_ERROR;
    }

    /* dscf->servers != NULL */

    server = uscf->servers->elts;

    n = 0;

    for (i = 0; i < uscf->servers->nelts; i++) {
        n += server[i].naddrs;
    }

    peers = ngx_pcalloc(cf->pool, sizeof(ngx_http_upstream_drizzle_peers_t)
            + sizeof(ngx_http_upstream_drizzle_peer_t) * (n - 1));

    if (peers == NULL) {
        return NGX_ERROR;
    }

    peers->single = (n == 1);
    peers->number = n;
    peers->name = &uscf->host;

    n = 0;

    for (i = 0; i < uscf->servers->nelts; i++) {
        for (j = 0; j < server[i].naddrs; j++) {
            peers->peer[n].sockaddr = server[i].addrs[j].sockaddr;
            peers->peer[n].socklen = server[i].addrs[j].socklen;
            peers->peer[n].name = server[i].addrs[j].name;
            peers->peer[n].port = server[i].port;
            peers->peer[n].user = server[i].user;
            peers->peer[n].password = server[i].password;
            peers->peer[n].dbname = server[i].dbname;
            peers->peer[n].protocol = server[i].protocol;
            peers->peer[n].set_names_query = &server[i].set_names_query;

            len = NGX_SOCKADDR_STRLEN + 1 /* for '\0' */;

            peers->peer[n].host = ngx_palloc(cf->pool, len);

            if (peers->peer[n].host == NULL) {
                return NGX_ERROR;
            }

            len = ngx_sock_ntop(peers->peer[n].sockaddr,
#if defined(nginx_version) && (nginx_version >= 1005003)
                                peers->peer[n].socklen,
#endif
                                peers->peer[n].host, len - 1, 0 /* no port */);

            peers->peer[n].host[len] = '\0';

            n++;
        }
    }

    dscf->peers = peers;

    dscf->active_conns = 0;

    if (dscf->max_cached) {
        return ngx_http_drizzle_keepalive_init(cf->pool, dscf);
    }

    return NGX_OK;
}


static ngx_int_t
ngx_http_upstream_drizzle_init_peer(ngx_http_request_t *r,
    ngx_http_upstream_srv_conf_t *uscf)
{
    ngx_http_upstream_drizzle_peer_data_t   *dp;
    ngx_http_upstream_drizzle_srv_conf_t    *dscf;
    ngx_http_upstream_t                     *u;
    ngx_http_core_loc_conf_t                *clcf;
    ngx_http_drizzle_loc_conf_t             *dlcf;
    ngx_drizzle_mixed_t                     *query;
    ngx_str_t                                dbname, sql;
    ngx_uint_t                               i;

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
            "drizzle init peer");

    dp = ngx_pcalloc(r->pool, sizeof(ngx_http_upstream_drizzle_peer_data_t));
    if (dp == NULL) {
        goto failed;
    }

    u = r->upstream;

    dp->upstream = u;
    dp->request  = r;

    dp->last_out = &u->out_bufs;

    dscf = ngx_http_conf_upstream_srv_conf(uscf, ngx_http_drizzle_module);

    dp->srv_conf = dscf;

    dlcf = ngx_http_get_module_loc_conf(r, ngx_http_drizzle_module);

    dp->loc_conf = dlcf;

    dp->query.len  = 0;
    dp->dbname.len = 0;

    /* to force ngx_output_chain not to use ngx_chain_writer */

    u->output.output_filter = ngx_http_drizzle_output_filter;
    u->output.filter_ctx = r;
    u->output.in   = NULL;
    u->output.busy = NULL;

    u->peer.data = dp;
    u->peer.get = ngx_http_upstream_drizzle_get_peer;
    u->peer.free = ngx_http_upstream_drizzle_free_peer;

    /* prepare dbname */

    dp->dbname.len = 0;

    if (dlcf->dbname) {
        /* check if dbname requires overriding at request time */
        if (ngx_http_complex_value(r, dlcf->dbname, &dbname) != NGX_OK) {
            goto failed;
        }

        if (dbname.len) {
            if (dbname.len >= DRIZZLE_MAX_DB_SIZE) {
                ngx_log_error(NGX_LOG_EMERG, r->connection->log, 0,
                       "drizzle: \"dbname\" value too large in upstream \"%V\"",
                       dscf->peers->name);

                goto failed;
            }

            dp->dbname = dbname;
        }
    }

    /* prepare SQL query */

    if (dlcf->methods_set & r->method) {
        /* method-specific query */
        dd("using method-specific query");

        query = dlcf->queries->elts;
        for (i = 0; i < dlcf->queries->nelts; i++) {
            if (query[i].key & r->method) {
                query = &query[i];
                break;
            }
        }

        if (i == dlcf->queries->nelts) {
            goto failed;
        }

    } else {
        /* default query */
        dd("using default query");

        query = dlcf->default_query;
    }

    if (query->cv) {
        /* complex value */
        dd("using complex value");

        if (ngx_http_complex_value(r, query->cv, &sql) != NGX_OK) {
            goto failed;
        }

        if (sql.len == 0) {
            clcf = ngx_http_get_module_loc_conf(r, ngx_http_core_module);

            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "drizzle: empty \"drizzle_query\" (was: \"%V\")"
                          " in location \"%V\"", &query->cv->value,
                          &clcf->name);

            goto failed;
        }

        dp->query = sql;

        return NGX_OK;
    }

    /* simple value */
    dd("using simple value");

    dp->query = query->sv;

    return NGX_OK;

failed:

#if defined(nginx_version) && (nginx_version >= 8017)
    return NGX_ERROR;
#else
    r->upstream->peer.data = NULL;

    return NGX_OK;
#endif
}


static ngx_int_t
ngx_http_upstream_drizzle_get_peer(ngx_peer_connection_t *pc, void *data)
{
    ngx_http_upstream_drizzle_peer_data_t   *dp = data;
    ngx_http_upstream_drizzle_srv_conf_t    *dscf;
    ngx_http_upstream_drizzle_peers_t       *peers;
    ngx_http_upstream_drizzle_peer_t        *peer;
#if defined(nginx_version) && (nginx_version < 8017)
    ngx_http_drizzle_ctx_t                  *dctx;
#endif
    ngx_connection_t                        *c = NULL;
    drizzle_con_st                          *dc = NULL;
    ngx_str_t                                dbname;
    drizzle_return_t                         ret;
    ngx_socket_t                             fd;
    ngx_event_t                             *rev, *wev;
    ngx_int_t                                rc;
    ngx_int_t                                event;

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, dp->request->connection->log, 0,
            "drizzle get peer");

#if defined(nginx_version) && (nginx_version < 8017)
    if (data == NULL) {
        goto failed;
    }

    dctx = ngx_http_get_module_ctx(dp->request, ngx_http_drizzle_module);
#endif

    dscf = dp->srv_conf;

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, dp->request->connection->log, 0,
            "active drizzle connections %ui", dscf->active_conns);

    dp->failed = 0;

    /* try to get an idle connection from our single-mode
     * keep-alive pool */

    if (dscf->max_cached && dscf->single) {
        rc = ngx_http_drizzle_keepalive_get_peer_single(pc, dp, dscf);
        if (rc != NGX_DECLINED) {
            return rc;
        }
    }

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, dp->request->connection->log, 0,
        "drizzle get peer using simple round robin");

    peers = dscf->peers;

    if (dscf->current > peers->number - 1) {
        dscf->current = 0;
    }

    peer = &peers->peer[dscf->current++];

    dp->name.data = peer->name.data;
    dp->name.len = peer->name.len;

    dp->sockaddr = *peer->sockaddr;

    dp->enable_charset = (peer->set_names_query->len > 0);
    dp->set_names_query = peer->set_names_query;

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, pc->log, 0,
            "drizzle set connection charset query \"%V\"", dp->set_names_query);

    pc->name = &dp->name;
    pc->sockaddr = &dp->sockaddr;
    pc->socklen = peer->socklen;
    pc->cached = 0;

    if (dscf->max_cached && ! dscf->single) {
        rc = ngx_http_drizzle_keepalive_get_peer_multi(pc, dp, dscf);
        if (rc != NGX_DECLINED) {
            return rc;
        }
    }

    if (dscf->overflow == drizzle_keepalive_overflow_reject &&
            dscf->active_conns >= dscf->max_cached)
    {
        ngx_log_error(NGX_LOG_INFO, pc->log, 0,
                       "drizzle: connection pool full, rejecting request "
                       "to upstream \"%V\"",
                       &peer->name);

        /* a bit hack-ish way to return error response (setup part) */
        pc->connection = ngx_get_connection(0, pc->log);

#if defined(nginx_version) && (nginx_version < 8017)
        dctx->status = NGX_HTTP_SERVICE_UNAVAILABLE;
#endif

        return NGX_AGAIN;
    }

    /* set up the peer's drizzle connection */

    dc = ngx_pcalloc(dscf->pool, sizeof(drizzle_con_st));

    if (dc == NULL) {
#if defined(nginx_version) && (nginx_version >= 8017)
        return NGX_ERROR;
#else
        goto failed;
#endif
    }

    dp->drizzle_con = dc;

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, pc->log, 0,
        "drizzle creating connection");

    (void) drizzle_con_create(&dscf->drizzle, dc);

    /* set protocol for the drizzle connection */

    if (peer->protocol == ngx_http_mysql_protocol) {
        ngx_log_debug0(NGX_LOG_DEBUG_HTTP, pc->log, 0,
            "drizzle using mysql protocol");

        drizzle_con_add_options(dc, DRIZZLE_CON_MYSQL);

    }

    /* set dbname for the drizzle connection */

    if (dp->dbname.len) {
        dbname = dp->dbname;

    } else {
        dbname = peer->dbname;
    }

    ngx_memcpy(dc->db, dbname.data, dbname.len);
    dc->db[dbname.len] = '\0';

    /* set user for the drizzle connection */

    ngx_memcpy(dc->user, peer->user.data, peer->user.len);
    dc->user[peer->user.len] = '\0';

    /* set password for the drizzle connection */

    ngx_memcpy(dc->password, peer->password.data, peer->password.len);
    dc->password[peer->password.len] = '\0';

    dd("user %s, password %s", dc->user, dc->password);

    /* TODO add support for uds (unix domain socket) */

    /* set host and port for the drizzle connection */

    drizzle_con_set_tcp(dc, (char *) peer->host, peer->port);

    /* ask drizzle to connect to the remote */

    ngx_log_debug7(NGX_LOG_DEBUG_HTTP, pc->log, 0,
            "drizzle connecting: host %s, port %d, dbname \"%V\", "
            "user \"%V\", pass \"%V\", dc pass \"%s\", "
            "protocol %d", peer->host, (int) peer->port, &dbname,
            &peer->user, &peer->password, dc->password, (int) peer->protocol);

    ret = drizzle_con_connect(dc);

    if (ret != DRIZZLE_RETURN_OK && ret != DRIZZLE_RETURN_IO_WAIT) {
        ngx_log_error(NGX_LOG_EMERG, pc->log, 0,
                       "drizzle: failed to connect: %d: %s in upstream \"%V\"",
                       (int) ret,
                       drizzle_error(&dscf->drizzle),
                       &peer->name);

        drizzle_con_free(dc);
        ngx_pfree(dscf->pool, dc);

#if defined(nginx_version) && (nginx_version >= 8017)
        return NGX_DECLINED;
#else
        dctx->status = NGX_HTTP_BAD_GATEWAY;
        goto failed;
#endif
    }

    dscf->active_conns++;

    /* add the file descriptor (fd) into an nginx connection structure */

    fd = drizzle_con_fd(dc);

    if (fd == -1) {
        ngx_log_error(NGX_LOG_ERR, pc->log, 0,
                "drizzle: failed to get the drizzle connection fd");

        goto invalid;
    }

    c = ngx_get_connection(fd, pc->log);
    if (c == NULL) {
        ngx_log_error(NGX_LOG_ERR, pc->log, 0,
                "drizzle: failed to get a free nginx connection");

        goto invalid;
    }

    c->log = pc->log;
    c->log_error = pc->log_error;
    c->number = ngx_atomic_fetch_add(ngx_connection_counter, 1);

    rev = c->read;
    wev = c->write;

    rev->log = pc->log;
    wev->log = pc->log;

    pc->connection = c;

    /* register the connection with the drizzle fd into the
     * nginx event model */

#if 0
    if (ngx_nonblocking(fd) == -1) {
        ngx_log_error(NGX_LOG_ALERT, pc->log, ngx_socket_errno,
                      ngx_nonblocking_n " failed");

        goto invalid;
    }
#endif

    if (ret == DRIZZLE_RETURN_IO_WAIT) {
        ngx_log_debug0(NGX_LOG_DEBUG_HTTP, pc->log, 0,
                "drizzle get peer: still connecting to remote");

        dp->state = state_db_connect;

        c->log->action = "connecting to drizzle upstream";

    } else {
        ngx_log_debug0(NGX_LOG_DEBUG_HTTP, pc->log, 0,
                "drizzle get peer: already connected to remote");

        /* to ensure send_query sets corresponding timers */
        dp->state = state_db_idle;
    }

    if (ngx_add_conn) {
        dd("Found ngx_add_conn");

        if (ngx_add_conn(c) == NGX_ERROR) {
            goto invalid;
        }

        if (ret == DRIZZLE_RETURN_IO_WAIT) {
            dd("returned NGX_AGAIN!!!");

            return NGX_AGAIN;
        }

        /* ret == DRIZZLE_RETURN_OK */

        wev->ready = 1;

        return NGX_DONE;
    }

    if (ngx_event_flags & NGX_USE_CLEAR_EVENT) {

        /* kqueue */

        event = NGX_CLEAR_EVENT;

    } else {

        /* select, poll, /dev/poll */

        event = NGX_LEVEL_EVENT;
    }

    if (ngx_add_event(rev, NGX_READ_EVENT, event) != NGX_OK) {
        ngx_log_error(NGX_LOG_ERR, pc->log, 0,
                "drizzle: failed to add connection into nginx event model");

        goto invalid;
    }

    if (ret == DRIZZLE_RETURN_IO_WAIT) {
        if (ngx_add_event(wev, NGX_WRITE_EVENT, event) != NGX_OK) {
            goto invalid;
        }

        return NGX_AGAIN;
    }

    ngx_log_debug0(NGX_LOG_DEBUG_EVENT, pc->log, 0, "drizzle connected");

    wev->ready = 1;

    /* ret == DRIZZLE_RETURN_OK */

    return NGX_DONE;

invalid:

    ngx_http_upstream_drizzle_free_connection(pc->log, pc->connection,
            dc, dscf);

#if defined(nginx_version) && (nginx_version >= 8017)
    return NGX_ERROR;
#else

failed:

    /* a bit hack-ish way to return error response (setup part) */
    pc->connection = ngx_get_connection(0, pc->log);

    return NGX_AGAIN;
#endif
}


static void
ngx_http_upstream_drizzle_free_peer(ngx_peer_connection_t *pc,
    void *data, ngx_uint_t state)
{
    ngx_http_upstream_drizzle_peer_data_t   *dp = data;
    ngx_http_upstream_drizzle_srv_conf_t    *dscf;

#if defined(nginx_version) && (nginx_version < 8017)
    if (data == NULL) {
        return;
    }
#endif

    if (pc && pc->log) {
        ngx_log_debug0(NGX_LOG_DEBUG_HTTP, pc->log, 0,
            "drizzle free peer");
    }

    dscf = dp->srv_conf;

    if (dp->drizzle_con && dp->drizzle_res.con) {
        dd("before drizzle result free");

        dd("%p vs. %p", dp->drizzle_res.con, dp->drizzle_con);

        drizzle_result_free(&dp->drizzle_res);

        dd("after drizzle result free");
    }

    if (dscf->max_cached) {
        ngx_http_drizzle_keepalive_free_peer(pc, dp, dscf, state);
    }

    if (pc && pc->connection) {
        dd("actually free the drizzle connection");

        ngx_http_upstream_drizzle_free_connection(pc->log, pc->connection,
                dp->drizzle_con, dscf);

        dp->drizzle_con = NULL;
        pc->connection = NULL;
    }
}


static ngx_int_t
ngx_http_drizzle_output_filter(void *data, ngx_chain_t *in)
{
    ngx_http_request_t              *r = data;

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
        "drizzle output filter");

    /* just to ensure u->reinit_request always gets called for
     * upstream_next */
    r->upstream->request_sent = 1;

    (void) ngx_http_drizzle_process_events(r);

    /* discard the ret val from process events because
     * we can only return NGX_AGAIN here to prevent
     * ngx_http_upstream_process_header from being called
     * and avoid u->write_event_handler to be set to
     * ngx_http_upstream_dummy. */

    return NGX_AGAIN;
}


ngx_flag_t
ngx_http_upstream_drizzle_is_my_peer(const ngx_peer_connection_t    *peer)
{
    return (peer->get == ngx_http_upstream_drizzle_get_peer);
}


void
ngx_http_upstream_drizzle_free_connection(ngx_log_t *log,
    ngx_connection_t *c, drizzle_con_st *dc,
    ngx_http_upstream_drizzle_srv_conf_t *dscf)
{
    ngx_event_t  *rev, *wev;

    dd("drizzle free peer connection");

    dscf->active_conns--;

    if (dc) {
        dd("before con free");
        drizzle_con_free(dc);
        dd("after con free");
        ngx_pfree(dscf->pool, dc);
    }

    if (c) {
        /* dd("c pool: %p", c->pool); */
        rev = c->read;
        wev = c->write;

        if (rev->timer_set) {
            ngx_del_timer(rev);
        }

        if (wev->timer_set) {
            ngx_del_timer(wev);
        }

        if (ngx_del_conn) {
           ngx_del_conn(c, NGX_CLOSE_EVENT);

        } else {
            if (rev->active || rev->disabled) {
                ngx_del_event(rev, NGX_READ_EVENT, NGX_CLOSE_EVENT);
            }

            if (wev->active || wev->disabled) {
                ngx_del_event(wev, NGX_WRITE_EVENT, NGX_CLOSE_EVENT);
            }
        }

#if defined(nginx_version) && nginx_version >= 1007005
        if (rev->posted) {
#else
        if (rev->prev) {
#endif
            ngx_delete_posted_event(rev);
        }

#if defined(nginx_version) && nginx_version >= 1007005
        if (wev->posted) {
#else
        if (wev->prev) {
#endif
            ngx_delete_posted_event(wev);
        }

        rev->closed = 1;
        wev->closed = 1;

        ngx_free_connection(c);

        c->fd = (ngx_socket_t) -1;
    }
}


ngx_http_upstream_srv_conf_t *
ngx_http_upstream_drizzle_add(ngx_http_request_t *r, ngx_url_t *url)
{
    ngx_http_upstream_main_conf_t  *umcf;
    ngx_http_upstream_srv_conf_t  **uscfp;
    ngx_uint_t                      i;

    umcf = ngx_http_get_module_main_conf(r, ngx_http_upstream_module);

    uscfp = umcf->upstreams.elts;

    for (i = 0; i < umcf->upstreams.nelts; i++) {

        if (uscfp[i]->host.len != url->host.len
                || ngx_strncasecmp(uscfp[i]->host.data, url->host.data,
                    url->host.len) != 0)
        {
            dd("upstream_add: host not match");
            continue;
        }

        if (uscfp[i]->port != url->port) {
            dd("upstream_add: port not match: %d != %d",
                    (int) uscfp[i]->port, (int) url->port);
            continue;
        }

        if (uscfp[i]->default_port && url->default_port
            && uscfp[i]->default_port != url->default_port)
        {
            dd("upstream_add: default_port not match");
            continue;
        }

        return uscfp[i];
    }

    dd("No upstream found: %.*s", (int) url->host.len, url->host.data);

    return NULL;
}


static void
ngx_http_upstream_drizzle_cleanup(void *data)
{
    drizzle_st  *drizzle = data;

    drizzle_free(drizzle);
}

