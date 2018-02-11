
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_drizzle_module.h"
#include "ngx_http_drizzle_output.h"
#include "ngx_http_drizzle_processor.h"
#include "ngx_http_drizzle_util.h"
#include "resty_dbd_stream.h"
#include <nginx.h>


#define ngx_http_drizzle_module_header_key "X-Resty-DBD-Module"

#define ngx_http_drizzle_module_header_key_len  \
    (sizeof(ngx_http_drizzle_module_header_key) - 1)

#define ngx_http_drizzle_module_header_val \
    "ngx_drizzle " \
      ngx_http_drizzle_module_version_string

#define ngx_http_drizzle_module_header_val_len \
    (sizeof(ngx_http_drizzle_module_header_val) - 1)

#define ngx_http_drizzle_module_header_key_len  \
    (sizeof(ngx_http_drizzle_module_header_key) - 1)


static ngx_int_t ngx_http_drizzle_get_buf(ngx_http_request_t *r,
    ngx_http_upstream_drizzle_peer_data_t *dp);
static u_char * ngx_http_drizzle_get_postponed(ngx_http_request_t *r,
    ngx_http_upstream_drizzle_peer_data_t *dp, size_t len);
static rds_col_type_t ngx_http_drizzle_std_col_type(
    drizzle_column_type_t col_type);
static u_char * ngx_http_drizzle_request_mem(ngx_http_request_t *r,
    ngx_http_upstream_drizzle_peer_data_t *dp, size_t len);
static ngx_int_t ngx_http_drizzle_submit_mem(ngx_http_request_t *r,
    ngx_http_upstream_drizzle_peer_data_t *dp, size_t len);


ngx_int_t
ngx_http_drizzle_output_result_header(ngx_http_request_t *r,
    drizzle_result_st *res)
{
    u_char                          *pos, *last;
    ngx_int_t                        rc;
    ngx_http_upstream_t             *u = r->upstream;
    const char                      *errstr;
    size_t                           size;
    uint16_t                         errstr_len;
    uint16_t                         col_count;
    uint16_t                         errcode;

    ngx_http_upstream_drizzle_peer_data_t   *dp = u->peer.data;

    errcode = drizzle_result_error_code(res);

    if (dp->enable_charset && ! dp->has_set_names) {
        if (errcode != 0) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "drizzle: FATAL: failed to set names 'utf8' "
                          "(error %d)", (int) errcode);
            return NGX_ERROR;
        }

        if (dp->drizzle_con && dp->drizzle_res.con) {
            dd("before drizzle result free");
            dd("%p vs. %p", dp->drizzle_res.con, dp->drizzle_con);

            drizzle_result_free(&dp->drizzle_res);

            dd("after drizzle result free");
        }

        /* ngx_http_upstream_drizzle_done(r, u, dp, NGX_OK); */
        dd("returning DONE when set names");
        return NGX_DONE;
    }

    errstr = drizzle_result_error(res);

    errstr_len = (uint16_t) ngx_strlen(errstr);

    col_count = drizzle_result_column_count(res);

    size = sizeof(uint8_t)        /* endian type */
           + sizeof(uint32_t)     /* format version */
           + sizeof(uint8_t)      /* result type */

           + sizeof(uint16_t)     /* standard error code */
           + sizeof(uint16_t)     /* driver-specific error code */

           + sizeof(uint16_t)     /* driver-specific errstr len */
           + errstr_len           /* driver-specific errstr data */
           + sizeof(uint64_t)     /* rows affected */
           + sizeof(uint64_t)     /* insert id */
           + sizeof(uint16_t);    /* column count */

    pos = ngx_http_drizzle_request_mem(r, dp, size);
    if (pos == NULL) {
        return NGX_ERROR;
    }

    last = pos;

#if NGX_HAVE_LITTLE_ENDIAN
    *last++ = 0;
#else /* big endian */
    *last++ = 1;
#endif

    /* RDS format version */

    *(uint32_t *) last = (uint32_t) resty_dbd_stream_version;
    last += sizeof(uint32_t);

    /* result type fixed to 0 */
    *last++ = 0;

    /* standard error code
     * FIXME: define the standard error code set and map
     * libdrizzle's to it. */
    *(uint16_t *) last = errcode;
    last += sizeof(uint16_t);

     /* driver-specific error code */
    *(uint16_t *) last = drizzle_result_error_code(res);
    last += sizeof(uint16_t);

    /* driver-specific errstr len */
    *(uint16_t *) last = errstr_len;
    last += sizeof(uint16_t);

    /* driver-specific errstr data */
    if (errstr_len) {
        last = ngx_copy(last, (u_char *) errstr, errstr_len);
    }

    /* affected rows */
    *(uint64_t *) last = drizzle_result_affected_rows(res);
    last += sizeof(uint64_t);

    /* insert id */
    *(uint64_t *) last = drizzle_result_insert_id(res);
    last += sizeof(uint64_t);

    /* column count */
    *(uint16_t *) last = col_count;
    last += sizeof(uint16_t);

    if ((size_t) (last - pos) != size) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "drizzle: FATAL: output result header buffer error");
        return NGX_ERROR;
    }

    if (col_count == 0) {
        dd("Col count is ZERO");

        /* we suppress row terminator here when there's no columns */
        dp->seen_stream_end = 1;

        rc = ngx_http_drizzle_submit_mem(r, dp, size);

        if (rc != NGX_OK) {
            return NGX_ERROR;
        }

        dd("about to be done...");
        ngx_http_upstream_drizzle_done(r, u, dp, NGX_DONE);
        dd("i am returning DONE");

        return NGX_DONE;
    }

    return ngx_http_drizzle_submit_mem(r, dp, size);
}


ngx_int_t
ngx_http_drizzle_output_bufs(ngx_http_request_t *r,
    ngx_http_upstream_drizzle_peer_data_t *dp)
{
    ngx_http_upstream_t                    *u = r->upstream;
    ngx_int_t                               rc;
    ngx_str_t                               key, value;
    ngx_http_drizzle_loc_conf_t            *dlcf;
    ngx_chain_t                            *cl;

    dd("enter");
    dd_dump_chain_size();

    if (dp->seen_stream_end) {
        dp->seen_stream_end = 0;

        if (dp->avail_out) {
            cl = ngx_alloc_chain_link(r->pool);
            if (cl == NULL) {
                return NGX_ERROR;
            }

            cl->buf = dp->out_buf;
            cl->next = NULL;
            *dp->last_out = cl;
            dp->last_out = &cl->next;

            dp->avail_out = 0;
        }
    }

    if (!u->header_sent && u->out_bufs) {
        ngx_http_clear_content_length(r);

        r->headers_out.status = NGX_HTTP_OK;

        /* set the Content-Type header */
        r->headers_out.content_type.data = (u_char *) rds_content_type;
        r->headers_out.content_type.len = rds_content_type_len;
        r->headers_out.content_type_len = rds_content_type_len;
        r->headers_out.content_type_lowcase = NULL;

        dlcf = ngx_http_get_module_loc_conf(r, ngx_http_drizzle_module);

        if (dlcf->enable_module_header) {
            /* set the X-Resty-DBD-Module header */

            key.data = (u_char *) ngx_http_drizzle_module_header_key;
            key.len  = ngx_http_drizzle_module_header_key_len;

            value.data = (u_char *) ngx_http_drizzle_module_header_val;
            value.len = ngx_http_drizzle_module_header_val_len;

            rc = ngx_http_drizzle_set_header(r, &key, &value);

            if (rc != NGX_OK) {
                return NGX_ERROR;
            }
        }

        rc = ngx_http_send_header(r);

        if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
            return rc;
        }

        u->header_sent = 1;
    }


    for ( ;; ) {
        if (u->out_bufs == NULL) {
            return NGX_OK;
        }

#if 0
        {
            int              n;
            ngx_chain_t     *cl;

            for (n = 0, cl = u->out_bufs; cl; cl = cl->next, n++) {
            }

            fprintf(stderr, "XXX chain size: %d\n", n);
        }
#endif

        rc = ngx_http_output_filter(r, u->out_bufs);

        if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
            return rc;
        }

#if defined(nginx_version) && nginx_version >= 1001004
        ngx_chain_update_chains(r->pool, &u->free_bufs, &u->busy_bufs,
                                &u->out_bufs, u->output.tag);
#else
        ngx_chain_update_chains(&u->free_bufs, &u->busy_bufs, &u->out_bufs,
                                u->output.tag);
#endif

        dp->last_out = &u->out_bufs;
    }

    /* impossible to reach here */
}


ngx_int_t
ngx_http_drizzle_output_col(ngx_http_request_t *r, drizzle_column_st *col)
{
    u_char                              *pos, *last;
    ngx_http_upstream_t                 *u = r->upstream;
    drizzle_column_type_t                col_type = 0;
    uint16_t                             std_col_type = 0;
    const char                          *col_name = NULL;
    uint16_t                             col_name_len = 0;
    size_t                               size;

    ngx_http_upstream_drizzle_peer_data_t   *dp = u->peer.data;

    if (col == NULL) {
        return NGX_ERROR;
    }

    col_type = drizzle_column_type(col);
    col_name = drizzle_column_name(col);
    col_name_len = (uint16_t) strlen(col_name);

    size = sizeof(uint16_t)     /* std col type */
           + sizeof(uint16_t)     /* driver-specific col type */
           + sizeof(uint16_t)     /* col name str len */
           + col_name_len;        /* col name str len */

    pos = ngx_http_drizzle_request_mem(r, dp, size);
    if (pos == NULL) {
        return NGX_ERROR;
    }

    last = pos;

    /* std column type */

    std_col_type = (uint16_t) ngx_http_drizzle_std_col_type(col_type);

#if 0
    dd("std col type for %s: %d, %d (%d, %d, %d)",
            col_name, std_col_type, rds_col_type_blob,
            rds_rough_col_type_str,
            rds_rough_col_type_str << 14,
            (uint16_t) (19 | (rds_rough_col_type_str << 14))
            );
#endif

    *(uint16_t *) last = std_col_type;
    last += sizeof(uint16_t);

    /* drizzle column type */
    *(uint16_t *) last = col_type;
    last += sizeof(uint16_t);

    /* column name string length */
    *(uint16_t *) last = col_name_len;
    last += sizeof(uint16_t);

    /* column name string data */
    last = ngx_copy(last, col_name, col_name_len);

    if ((size_t) (last - pos) != size) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "drizzle: FATAL: output column buffer error");
        return NGX_ERROR;
    }

    return ngx_http_drizzle_submit_mem(r, dp, size);
}


ngx_int_t
ngx_http_drizzle_output_row(ngx_http_request_t *r, uint64_t row)
{
    u_char                              *pos, *last;
    ngx_http_upstream_t                 *u = r->upstream;
    size_t                               size;

    ngx_http_upstream_drizzle_peer_data_t   *dp = u->peer.data;

    size = sizeof(uint8_t);

    pos = ngx_http_drizzle_request_mem(r, dp, size);
    if (pos == NULL) {
        return NGX_ERROR;
    }

    last = pos;
    *last++ = (row != 0);

    if (row == 0) {
        dp->seen_stream_end = 1;
    }

    return ngx_http_drizzle_submit_mem(r, dp, size);
}


ngx_int_t
ngx_http_drizzle_output_field(ngx_http_request_t *r, size_t offset,
    size_t len, size_t total, drizzle_field_t field)
{
    u_char                              *pos, *last;
    ngx_http_upstream_t                 *u = r->upstream;
    size_t                               size = 0;

    ngx_http_upstream_drizzle_peer_data_t   *dp = u->peer.data;

    if (offset == 0) {

        if (len == 0 && total != 0) {
            return NGX_DONE;
        }

        size = sizeof(uint32_t);     /* field total length */
    }

    /* (more) field data */
    size += (uint32_t) len;

    /* request memory */

    pos = ngx_http_drizzle_request_mem(r, dp, size);
    if (pos == NULL) {
        return NGX_ERROR;
    }

    last = pos;

    /* fill in the buffer */

    if (offset == 0) {
        /* field total length */
        if (field == NULL) {
            *(uint32_t *) last = (uint32_t) -1;

        } else {
            *(uint32_t *) last = (uint32_t) total;
        }

        last += sizeof(uint32_t);
    }

    /* field data */
    if (len && field) {
        last = ngx_copy(last, field, (uint32_t) len);
    }

    if ((size_t) (last - pos) != size) {
        dd("offset %d, len %d, size %d", (int) offset,
           (int) len, (int) size);

        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "drizzle: FATAL: output field buffer error");
        return NGX_ERROR;
    }

    return ngx_http_drizzle_submit_mem(r, dp, size);
}


static rds_col_type_t
ngx_http_drizzle_std_col_type(drizzle_column_type_t col_type)
{
    dd("drizzle col type: %d", col_type);

    switch (col_type) {
    case DRIZZLE_COLUMN_TYPE_DECIMAL:
        return rds_col_type_decimal;

    case DRIZZLE_COLUMN_TYPE_TINY:
        return rds_col_type_smallint;

    case DRIZZLE_COLUMN_TYPE_SHORT:
        return rds_col_type_smallint;

    case DRIZZLE_COLUMN_TYPE_LONG:
        return rds_col_type_integer;

    case DRIZZLE_COLUMN_TYPE_FLOAT:
        return rds_col_type_real;

    case DRIZZLE_COLUMN_TYPE_DOUBLE:
        return rds_col_type_double;

    case DRIZZLE_COLUMN_TYPE_NULL:
        return rds_col_type_unknown;

    case DRIZZLE_COLUMN_TYPE_TIMESTAMP:
        return rds_col_type_timestamp;

    case DRIZZLE_COLUMN_TYPE_LONGLONG:
        return rds_col_type_bigint;

    case DRIZZLE_COLUMN_TYPE_INT24:
        return rds_col_type_integer;

    case DRIZZLE_COLUMN_TYPE_DATE:
        return rds_col_type_timestamp;

    case DRIZZLE_COLUMN_TYPE_TIME:
        return rds_col_type_time;

    case DRIZZLE_COLUMN_TYPE_DATETIME:
        return rds_col_type_timestamp;

    case DRIZZLE_COLUMN_TYPE_YEAR:
        return rds_col_type_smallint;

    case DRIZZLE_COLUMN_TYPE_NEWDATE:
        return rds_col_type_timestamp;

    case DRIZZLE_COLUMN_TYPE_VARCHAR:
        return rds_col_type_varchar;

    case DRIZZLE_COLUMN_TYPE_BIT:
        return rds_col_type_bit;

    case DRIZZLE_COLUMN_TYPE_NEWDECIMAL:
        return rds_col_type_decimal;

    case DRIZZLE_COLUMN_TYPE_ENUM:
        return rds_col_type_varchar;

    case DRIZZLE_COLUMN_TYPE_SET:
        return rds_col_type_varchar;

    case DRIZZLE_COLUMN_TYPE_TINY_BLOB:
        return rds_col_type_blob;

    case DRIZZLE_COLUMN_TYPE_MEDIUM_BLOB:
        return rds_col_type_blob;

    case DRIZZLE_COLUMN_TYPE_LONG_BLOB:
        return rds_col_type_blob;

    case DRIZZLE_COLUMN_TYPE_BLOB:
        return rds_col_type_blob;

    case DRIZZLE_COLUMN_TYPE_VAR_STRING:
        return rds_col_type_varchar;

    case DRIZZLE_COLUMN_TYPE_STRING:
        return rds_col_type_varchar;

    case DRIZZLE_COLUMN_TYPE_GEOMETRY:
        return rds_col_type_varchar;

    default:
        return rds_col_type_unknown;
    }

    /* impossible to reach here */
}


static u_char *
ngx_http_drizzle_request_mem(ngx_http_request_t *r,
    ngx_http_upstream_drizzle_peer_data_t *dp, size_t len)
{
    ngx_int_t                rc;
    u_char                  *p;

    rc = ngx_http_drizzle_get_buf(r, dp);

    if (rc != NGX_OK) {
        return NULL;
    }

    if (dp->avail_out < len) {
        p = ngx_http_drizzle_get_postponed(r, dp, len);
        if (p == NULL) {
            return NULL;
        }

        dp->postponed.pos = p;
        dp->postponed.last = p + len;

        return p;
    }

    return dp->out_buf->last;
}


static ngx_int_t
ngx_http_drizzle_get_buf(ngx_http_request_t *r,
    ngx_http_upstream_drizzle_peer_data_t *dp)
{
    ngx_http_drizzle_loc_conf_t         *conf = dp->loc_conf;
    ngx_http_upstream_t                 *u = dp->upstream;

    dd("MEM enter");

    if (dp->avail_out) {
        return NGX_OK;
    }

    if (u->free_bufs) {
        dd("MEM reusing temp buf from free_bufs");

        dp->out_buf = u->free_bufs->buf;
        u->free_bufs = u->free_bufs->next;

    } else {
        dd("MEM creating temp buf with size: %d", (int) conf->buf_size);
        dp->out_buf = ngx_create_temp_buf(r->pool,
                conf->buf_size);

        if (dp->out_buf == NULL) {
            return NGX_ERROR;
        }

        dp->out_buf->tag = (ngx_buf_tag_t) &ngx_http_drizzle_module;
        dp->out_buf->recycled = 1;
    }

    dp->avail_out = conf->buf_size;

    return NGX_OK;
}


static u_char *
ngx_http_drizzle_get_postponed(ngx_http_request_t *r,
    ngx_http_upstream_drizzle_peer_data_t *dp, size_t len)
{
    u_char          *p;

    dd("MEM enter");

    if (dp->cached.start == NULL) {
        goto alloc;
    }

    if ((size_t) (dp->cached.end - dp->cached.start) < len) {
        ngx_pfree(r->pool, dp->cached.start);
        goto alloc;
    }

    return dp->cached.start;

alloc:

    p = ngx_palloc(r->pool, len);
    if (p == NULL) {
        return NULL;
    }

    dp->cached.start = p;
    dp->cached.end = p + len;

    return p;
}


static ngx_int_t
ngx_http_drizzle_submit_mem(ngx_http_request_t *r,
    ngx_http_upstream_drizzle_peer_data_t *dp, size_t len)
{
    ngx_chain_t             *cl;
    ngx_int_t                rc;
    size_t                   postponed_len;

    ngx_http_drizzle_loc_conf_t         *conf = dp->loc_conf;

    if (dp->postponed.pos != NULL) {
        dd("MEM copy postponed data over to u->out_bufs for len %d", (int) len);

        postponed_len = dp->postponed.last - dp->postponed.pos;

        if (postponed_len > dp->avail_out) {
            /* we should ensure that rds atoms do not get
             * splitted into multiple bufs. */

            if (dp->out_buf && dp->out_buf->pos != dp->out_buf->last) {
                /* save the current dp->out_buf */
                cl = ngx_alloc_chain_link(r->pool);
                if (cl == NULL) {
                    return NGX_ERROR;
                }

                cl->buf = dp->out_buf;
                cl->next = NULL;
                *dp->last_out = cl;
                dp->last_out = &cl->next;
            }

            /* create a buf for the postponed buf */

            len = postponed_len > conf->buf_size ?
                postponed_len : conf->buf_size;

            dp->out_buf = ngx_create_temp_buf(r->pool, len);

            if (dp->out_buf == NULL) {
                return NGX_ERROR;
            }

            dp->out_buf->tag = (ngx_buf_tag_t) &ngx_http_drizzle_module;
            dp->out_buf->recycled = 1;

            dp->out_buf->last = ngx_copy(dp->out_buf->last, dp->postponed.pos,
                                         postponed_len);

            dp->avail_out = len - postponed_len;

            dp->postponed.pos = NULL;

            if (dp->avail_out == 0) {
                /* save the new big buf */

                cl = ngx_alloc_chain_link(r->pool);
                if (cl == NULL) {
                    return NGX_ERROR;
                }

                cl->buf = dp->out_buf;
                cl->next = NULL;
                *dp->last_out = cl;
                dp->last_out = &cl->next;
            }

            return NGX_OK;
        }

        for ( ;; ) {
            len = dp->postponed.last - dp->postponed.pos;
            if (len > dp->avail_out) {
                len = dp->avail_out;
            }

            dp->out_buf->last = ngx_copy(dp->out_buf->last,
                    dp->postponed.pos, len);

            dp->avail_out -= len;

            dp->postponed.pos += len;

            if (dp->postponed.pos == dp->postponed.last) {
                dp->postponed.pos = NULL;
            }

            if (dp->avail_out > 0) {
                break;
            }

            dd("MEM save dp->out_buf");

            cl = ngx_alloc_chain_link(r->pool);
            if (cl == NULL) {
                return NGX_ERROR;
            }

            cl->buf = dp->out_buf;
            cl->next = NULL;
            *dp->last_out = cl;
            dp->last_out = &cl->next;

            if (dp->postponed.pos == NULL) {
                break;
            }

            rc = ngx_http_drizzle_get_buf(r, dp);
            if (rc != NGX_OK) {
                return NGX_ERROR;
            }
        }

        return NGX_OK;
    }

    dd("MEM consuming out_buf for %d", (int) len);

    dp->out_buf->last += len;
    dp->avail_out -= len;

    if (dp->avail_out == 0) {
        dd("MEM save dp->out_buf");

        cl = ngx_alloc_chain_link(r->pool);
        if (cl == NULL) {
            return NGX_ERROR;
        }

        cl->buf = dp->out_buf;
        cl->next = NULL;
        *dp->last_out = cl;
        dp->last_out = &cl->next;
    }

    return NGX_OK;
}
