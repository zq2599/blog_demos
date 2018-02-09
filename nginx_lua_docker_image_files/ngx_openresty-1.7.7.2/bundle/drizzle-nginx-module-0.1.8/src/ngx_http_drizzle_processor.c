
/*
 * Copyright (C) Xiaozhe Wang (chaoslawful)
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_drizzle_processor.h"
#include "ngx_http_drizzle_module.h"
#include "ngx_http_drizzle_util.h"
#include "ngx_http_drizzle_output.h"
#include "ngx_http_drizzle_upstream.h"


#define MYSQL_ER_NO_SUCH_TABLE 1146

static ngx_int_t ngx_http_upstream_drizzle_connect(ngx_http_request_t *r,
    ngx_connection_t *c, ngx_http_upstream_drizzle_peer_data_t *dp,
    drizzle_con_st *dc);
static ngx_int_t ngx_http_upstream_drizzle_send_query(ngx_http_request_t *r,
    ngx_connection_t *c, ngx_http_upstream_drizzle_peer_data_t *dp,
    drizzle_con_st *dc);
static ngx_int_t ngx_http_upstream_drizzle_recv_cols(ngx_http_request_t *r,
    ngx_connection_t *c, ngx_http_upstream_drizzle_peer_data_t *dp,
    drizzle_con_st *dc);
static ngx_int_t ngx_http_upstream_drizzle_recv_rows(ngx_http_request_t *r,
    ngx_connection_t *c, ngx_http_upstream_drizzle_peer_data_t *dp,
    drizzle_con_st *dc);


ngx_int_t
ngx_http_drizzle_process_events(ngx_http_request_t *r)
{
    ngx_http_upstream_t                         *u;
    ngx_connection_t                            *c;
    ngx_http_upstream_drizzle_peer_data_t       *dp;
    drizzle_con_st                              *dc;
    ngx_int_t                                    rc;
#if 0
    drizzle_return_t                             ret;
#endif

    u = r->upstream;
    c = u->peer.connection;

    dp = u->peer.data;

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "drizzle process events, state: %d", dp->state);

    if (!ngx_http_upstream_drizzle_is_my_peer(&u->peer)) {
        ngx_log_error(NGX_LOG_ERR, c->log, 0,
                      "process events: it seems you "
                      "are using a non-drizzle upstream backend"
        );

        return NGX_ERROR;
    }

    dc = dp->drizzle_con;

    switch (dp->state) {
    case state_db_connect:
        rc = ngx_http_upstream_drizzle_connect(r, c, dp, dc);
        break;

    case state_db_idle: /* from connection pool */
        c->log->action = "sending query to drizzle upstream";

    case state_db_send_query:
        rc = ngx_http_upstream_drizzle_send_query(r, c, dp, dc);
        break;

    case state_db_recv_cols:
        rc = ngx_http_upstream_drizzle_recv_cols(r, c, dp, dc);
        break;

    case state_db_recv_rows:
        rc = ngx_http_upstream_drizzle_recv_rows(r, c, dp, dc);
        break;

    default:
        ngx_log_error(NGX_LOG_ERR, c->log, 0,
                      "unknown state: %d", (int) dp->state);
        return NGX_ERROR;
    }

    dd("rc == %d", (int) rc);

    if (rc == NGX_ERROR) {
        ngx_http_upstream_drizzle_next(r, u, NGX_HTTP_UPSTREAM_FT_ERROR);

        return NGX_ERROR;
    }

    if (rc >= NGX_HTTP_SPECIAL_RESPONSE) {
        ngx_http_upstream_drizzle_finalize_request(r, u, rc);

        return NGX_ERROR;
    }

    if (rc == NGX_AGAIN) {
#if 0
        if (ngx_handle_write_event(c->write, 0) != NGX_OK) {
            return NGX_ERROR;
        }
#endif

        rc = ngx_http_drizzle_output_bufs(r, dp);

        if (rc == NGX_ERROR || rc > NGX_OK) {
            ngx_http_upstream_drizzle_finalize_request(r, u,
                                           NGX_HTTP_INTERNAL_SERVER_ERROR);

            return NGX_ERROR;
        }
    }

    return rc;
}


static ngx_int_t
ngx_http_upstream_drizzle_connect(ngx_http_request_t *r,
    ngx_connection_t *c, ngx_http_upstream_drizzle_peer_data_t *dp,
    drizzle_con_st *dc)
{
    drizzle_return_t             ret;

    ngx_log_debug2(NGX_LOG_DEBUG_HTTP, c->log, 0,
                   "drizzle connect: user %s, password %s", dc->user,
                   dc->password);

    ret = drizzle_con_connect(dc);

    if (ret == DRIZZLE_RETURN_IO_WAIT) {
        ngx_log_debug0(NGX_LOG_DEBUG_HTTP, c->log, 0,
                       "drizzle libdrizzle returned IO_WAIT while "
                       "connecting");

#if 0
        if (ngx_handle_write_event(c->write, 0) != NGX_OK) {
            return NGX_ERROR;
        }
#endif

        return NGX_AGAIN;
    }

    if (c->write->timer_set) {
        ngx_del_timer(c->write);
    }

    if (ret != DRIZZLE_RETURN_OK) {
       ngx_log_error(NGX_LOG_ERR, c->log, 0, "failed to connect: %d: %s",
                     (int) ret, drizzle_error(dc->drizzle));

       return NGX_ERROR;
    }

    /* ret == DRIZZLE_RETURN_OK */

    c->log->action = "sending query to drizzle upstream";

    return ngx_http_upstream_drizzle_send_query(r, c, dp, dc);
}


static ngx_int_t
ngx_http_upstream_drizzle_send_query(ngx_http_request_t *r,
    ngx_connection_t *c, ngx_http_upstream_drizzle_peer_data_t *dp,
    drizzle_con_st *dc)
{
    ngx_http_upstream_t         *u = r->upstream;
    drizzle_return_t             ret;
    ngx_int_t                    rc;
    ngx_str_t                    query;
    ngx_flag_t                   has_set_names = 0;
    ngx_flag_t                   enable_charset = 0;

    dd("enable charset: %d", (int) dp->enable_charset);

    if (dp->enable_charset && ! dp->has_set_names) {
        ngx_log_debug0(NGX_LOG_DEBUG_HTTP, c->log, 0,
                       "drizzle enables connection charset setting");

        query.len = dp->set_names_query->len;
        query.data = dp->set_names_query->data;

    } else {
        query.data = dp->query.data;
        query.len = dp->query.len;
    }

    ngx_log_debug1(NGX_LOG_DEBUG_HTTP, c->log, 0,
                   "drizzle sending query \"%V\"", &query);

    (void) drizzle_query(dc, &dp->drizzle_res, (const char *) query.data,
                         query.len, &ret);

    if (ret == DRIZZLE_RETURN_IO_WAIT) {
        ngx_log_debug0(NGX_LOG_DEBUG_HTTP, c->log, 0,
                       "drizzle libdrizzle returned IO_WAIT while sending "
                       "query");

        if (dp->state != state_db_send_query) {
            dp->state = state_db_send_query;

            if (c->write->timer_set) {
                ngx_del_timer(c->write);
            }

            ngx_add_timer(c->write, u->conf->send_timeout);
        }

        return NGX_AGAIN;
    }

    if (c->write->timer_set) {
        ngx_del_timer(c->write);
    }

    if (ret != DRIZZLE_RETURN_OK) {
#if 1
        if (ret == DRIZZLE_RETURN_ERROR_CODE) {
            if (drizzle_error_code(dc->drizzle) == MYSQL_ER_NO_SUCH_TABLE) {
                ngx_log_error(NGX_LOG_NOTICE, c->log, 0,
                              "failed to send query: %i (%d): %s",
                              ret, drizzle_error_code(dc->drizzle),
                              drizzle_error(dc->drizzle));

                if (dp->enable_charset && ! dp->has_set_names) {
                    c->log->action = "sending query to drizzle upstream";
                    dp->has_set_names = 1;

                    return ngx_http_upstream_drizzle_send_query(r, c, dp, dc);
                }

                ngx_http_upstream_drizzle_done(r, u, dp, NGX_HTTP_GONE);

                return NGX_DONE;
            }
        }
#endif

        ngx_log_error(NGX_LOG_ERR, c->log, 0,
                      "failed to send query: %d (%d): %s",
                      (int) ret, drizzle_error_code(dc->drizzle),
                      drizzle_error(dc->drizzle));

        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    /* ret == DRIZZLE_RETURN_OK */

    dd_drizzle_result(&dp->drizzle_res);

    dd("after drizzle restult");

    if (dp->enable_charset) {
        enable_charset = 1;
    }

    has_set_names = dp->has_set_names;

    rc = ngx_http_drizzle_output_result_header(r, &dp->drizzle_res);

    if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
        return rc;
    }

    if (rc == NGX_DONE) {
        if (enable_charset && ! has_set_names) {
            c->log->action = "sending query to drizzle upstream";
            dp->has_set_names = 1;

            dp->state = state_db_idle;

            return ngx_http_upstream_drizzle_send_query(r, c, dp, dc);
        }

        /* no data set following the header */
        return rc;
    }

    c->log->action = "receiving result set columns from drizzle upstream";

    return ngx_http_upstream_drizzle_recv_cols(r, c, dp, dc);
}


static ngx_int_t
ngx_http_upstream_drizzle_recv_cols(ngx_http_request_t *r,
    ngx_connection_t *c, ngx_http_upstream_drizzle_peer_data_t *dp,
    drizzle_con_st *dc)
{
    drizzle_column_st               *col;
    ngx_int_t                        rc;
    drizzle_return_t                 ret;

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, c->log, 0,
                   "drizzle receive resultset columns");

    for (;;) {
        col = drizzle_column_read(&dp->drizzle_res, &dp->drizzle_col, &ret);

        if (ret == DRIZZLE_RETURN_IO_WAIT) {

            if (dp->state != state_db_recv_cols) {
                dp->state = state_db_recv_cols;

                if (c->read->timer_set) {
                    ngx_del_timer(c->read);
                }

                ngx_add_timer(c->read, dp->loc_conf->recv_cols_timeout);

            }

#if 0
            if (ngx_handle_read_event(c->read, 0) != NGX_OK) {
                return NGX_ERROR;
            }
#endif

            return NGX_AGAIN;
        }

        if (ret != DRIZZLE_RETURN_OK) {
            ngx_log_error(NGX_LOG_ERR, c->log, 0,
                          "failed to recv cols: %d: %s",
                          (int) ret,
                          drizzle_error(dc->drizzle));

            return NGX_ERROR;
        }

        /* ret == DRIZZLE_RETURN_OK */

        if (col) {
            rc = ngx_http_drizzle_output_col(r, col);

            drizzle_column_free(col);

            if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
                return rc;
            }

        } else { /* after the last column */
            if (c->read->timer_set) {
                ngx_del_timer(c->read);
            }

            c->log->action = "receiving result set rows from drizzle "
                             "upstream";

            return ngx_http_upstream_drizzle_recv_rows(r, c, dp, dc);
        }

        dd_drizzle_column(col);
    }

    /* impossible to reach here */
}


static ngx_int_t
ngx_http_upstream_drizzle_recv_rows(ngx_http_request_t *r,
    ngx_connection_t *c, ngx_http_upstream_drizzle_peer_data_t *dp,
    drizzle_con_st *dc)
{
    ngx_http_upstream_t             *u = r->upstream;
    ngx_int_t                        rc;
    drizzle_return_t                 ret;
    size_t                           offset;
    size_t                           len;
    size_t                           total;
    drizzle_field_t                  field;

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, c->log, 0,
                   "drizzle receive resultset rows");

    for (;;) {
        ngx_log_debug1(NGX_LOG_DEBUG_HTTP, c->log, 0,
                       "drizzle receive resultset row %uL", dp->drizzle_row);

        if (dp->drizzle_row == 0) {
            dp->drizzle_row = drizzle_row_read(&dp->drizzle_res, &ret);

            if (ret == DRIZZLE_RETURN_IO_WAIT) {
                dp->drizzle_row = 0;

                goto io_wait;
            }

            if (ret != DRIZZLE_RETURN_OK) {
                ngx_log_error(NGX_LOG_ERR, c->log, 0,
                              "failed to read row: %d: %s",
                              (int) ret,
                              drizzle_error(dc->drizzle));

                return NGX_ERROR;
            }

            /* ret == DRIZZLE_RETURN_OK */

            rc = ngx_http_drizzle_output_row(r, dp->drizzle_row);

            if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
                drizzle_result_free(&dp->drizzle_res);

                return rc;
            }

            if (dp->drizzle_row == 0) {
                /* after last row */

                drizzle_result_free(&dp->drizzle_res);

                if (c->read->timer_set) {
                    ngx_del_timer(c->read);
                }

                if (dp->enable_charset && ! dp->has_set_names) {
                    c->log->action = "sending query to drizzle upstream";
                    dp->has_set_names = 1;

                    return ngx_http_upstream_drizzle_send_query(r, c, dp, dc);
                }

                ngx_http_upstream_drizzle_done(r, u, dp, NGX_DONE);
                return NGX_DONE;
            }
        }

        /* dp->drizzle_row != 0 */

        for (;;) {
            field = drizzle_field_read(&dp->drizzle_res, &offset, &len,
                                       &total, &ret);

            ngx_log_debug3(NGX_LOG_DEBUG_HTTP, c->log, 0,
                           "drizzle field read: %p (offset %z, len %z)",
                           field, offset, len);

            if (ret == DRIZZLE_RETURN_IO_WAIT) {
                goto io_wait;
            }

            if (ret == DRIZZLE_RETURN_ROW_END) {
                /* reached the end of the current row */
                break;
            }

            if (ret != DRIZZLE_RETURN_OK) {
                drizzle_result_free(&dp->drizzle_res);

                ngx_log_error(NGX_LOG_ERR, c->log, 0,
                              "failed to read row field: %d: %s",
                              (int) ret,
                              drizzle_error(dc->drizzle));

                return NGX_ERROR;
            }

            /* ret == DRIZZLE_RETURN_OK */

            rc = ngx_http_drizzle_output_field(r, offset, len, total, field);

            if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
                drizzle_result_free(&dp->drizzle_res);

                return rc;
            }

            if (field) {
                ngx_log_debug2(NGX_LOG_DEBUG_HTTP, c->log, 0,
                               "drizzle field value read: %*s", len, field);
            }
        }

        dp->drizzle_row = 0;
    }

    /* impossible to reach here */

io_wait:

    if (dp->state != state_db_recv_rows) {
        dp->state = state_db_recv_rows;

        if (c->read->timer_set) {
            ngx_del_timer(c->read);
        }

        ngx_add_timer(c->read, dp->loc_conf->recv_rows_timeout);
    }

#if 0
    if (ngx_handle_read_event(c->read, 0) != NGX_OK) {
        return NGX_ERROR;
    }
#endif

    return NGX_AGAIN;
}


void
ngx_http_upstream_drizzle_done(ngx_http_request_t *r,
    ngx_http_upstream_t *u, ngx_http_upstream_drizzle_peer_data_t *dp,
    ngx_int_t rc)
{
    ngx_connection_t            *c;

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "drizzle upstream done");

    (void) ngx_http_drizzle_output_bufs(r, dp);

    /* to persuade Maxim Dounin's ngx_http_upstream_keepalive
     * module to cache the current connection */

    u->length = 0;

    if (rc == NGX_DONE) {
        u->header_sent = 1;
        u->headers_in.status_n = NGX_HTTP_OK;
        rc = NGX_OK;

    } else {
        r->headers_out.status = rc;
        u->headers_in.status_n = rc;
    }

    c = u->peer.connection;

    c->log->action = "being idle";

    /* reset the state machine */
    dp->state = state_db_idle;

    dd("about to finalize request...");
    ngx_http_upstream_drizzle_finalize_request(r, u, rc);
    dd("after finalize request...");
}
