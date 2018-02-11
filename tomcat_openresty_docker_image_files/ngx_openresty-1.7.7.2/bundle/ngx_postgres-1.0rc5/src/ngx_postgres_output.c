/*
 * Copyright (c) 2010, FRiCKLE Piotr Sikora <info@frickle.com>
 * Copyright (c) 2009-2010, Xiaozhe Wang <chaoslawful@gmail.com>
 * Copyright (c) 2009-2010, Yichun Zhang <agentzh@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#ifndef DDEBUG
#define DDEBUG 0
#endif

#include "ngx_postgres_ddebug.h"
#include "ngx_postgres_module.h"
#include "ngx_postgres_output.h"


ngx_int_t
ngx_postgres_output_value(ngx_http_request_t *r, PGresult *res)
{
    ngx_postgres_ctx_t        *pgctx;
    ngx_http_core_loc_conf_t  *clcf;
    ngx_chain_t               *cl;
    ngx_buf_t                 *b;
    size_t                     size;

    dd("entering");

    pgctx = ngx_http_get_module_ctx(r, ngx_postgres_module);

    if ((pgctx->var_rows != 1) || (pgctx->var_cols != 1)) {
        clcf = ngx_http_get_module_loc_conf(r, ngx_http_core_module);

        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "postgres: \"postgres_output value\" received %d value(s)"
                      " instead of expected single value in location \"%V\"",
                      pgctx->var_rows * pgctx->var_cols, &clcf->name);

        dd("returning NGX_DONE, status NGX_HTTP_INTERNAL_SERVER_ERROR");
        pgctx->status = NGX_HTTP_INTERNAL_SERVER_ERROR;
        return NGX_DONE;
    }

    if (PQgetisnull(res, 0, 0)) {
        clcf = ngx_http_get_module_loc_conf(r, ngx_http_core_module);

        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "postgres: \"postgres_output value\" received NULL value"
                      " in location \"%V\"", &clcf->name);

        dd("returning NGX_DONE, status NGX_HTTP_INTERNAL_SERVER_ERROR");
        pgctx->status = NGX_HTTP_INTERNAL_SERVER_ERROR;
        return NGX_DONE;
    }

    size = PQgetlength(res, 0, 0);
    if (size == 0) {
        clcf = ngx_http_get_module_loc_conf(r, ngx_http_core_module);

        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "postgres: \"postgres_output value\" received empty value"
                      " in location \"%V\"", &clcf->name);

        dd("returning NGX_DONE, status NGX_HTTP_INTERNAL_SERVER_ERROR");
        pgctx->status = NGX_HTTP_INTERNAL_SERVER_ERROR;
        return NGX_DONE;
    }

    b = ngx_create_temp_buf(r->pool, size);
    if (b == NULL) {
        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }

    cl = ngx_alloc_chain_link(r->pool);
    if (cl == NULL) {
        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }

    cl->buf = b;
    b->memory = 1;
    b->tag = r->upstream->output.tag;

    b->last = ngx_copy(b->last, PQgetvalue(res, 0, 0), size);

    if (b->last != b->end) {
        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }

    cl->next = NULL;

    /* set output response */
    pgctx->response = cl;

    dd("returning NGX_DONE");
    return NGX_DONE;
}

ngx_int_t
ngx_postgres_output_text(ngx_http_request_t *r, PGresult *res)
{
    ngx_postgres_ctx_t        *pgctx;
    ngx_chain_t               *cl;
    ngx_buf_t                 *b;
    size_t                     size;
    ngx_int_t                  col_count, row_count, col, row;

    dd("entering");

    pgctx = ngx_http_get_module_ctx(r, ngx_postgres_module);

    col_count = pgctx->var_cols;
    row_count = pgctx->var_rows;

    /* pre-calculate total length up-front for single buffer allocation */
    size = 0;

    for (row = 0; row < row_count; row++) {
        for (col = 0; col < col_count; col++) {
            if (PQgetisnull(res, row, col)) {
                size += sizeof("(null)") - 1;
            } else {
                size += PQgetlength(res, row, col);  /* field string data */
            }
        }
    }

    size += row_count * col_count - 1;               /* delimiters */

    if ((row_count == 0) || (size == 0)) {
        dd("returning NGX_DONE (empty result)");
        return NGX_DONE;
    }

    b = ngx_create_temp_buf(r->pool, size);
    if (b == NULL) {
        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }

    cl = ngx_alloc_chain_link(r->pool);
    if (cl == NULL) {
        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }

    cl->buf = b;
    b->memory = 1;
    b->tag = r->upstream->output.tag;

    /* fill data */
    for (row = 0; row < row_count; row++) {
        for (col = 0; col < col_count; col++) {
            if (PQgetisnull(res, row, col)) {
                b->last = ngx_copy(b->last, "(null)", sizeof("(null)") - 1);
            } else {
                size = PQgetlength(res, row, col);
                if (size) {
                    b->last = ngx_copy(b->last, PQgetvalue(res, row, col),
                                       size);
                }
            }

            if ((row != row_count - 1) || (col != col_count - 1)) {
                b->last = ngx_copy(b->last, "\n", 1);
            }
        }
    }

    if (b->last != b->end) {
        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }

    cl->next = NULL;

    /* set output response */
    pgctx->response = cl;

    dd("returning NGX_DONE");
    return NGX_DONE;
}

ngx_int_t
ngx_postgres_output_rds(ngx_http_request_t *r, PGresult *res)
{
    ngx_postgres_ctx_t  *pgctx;
    ngx_chain_t         *first, *last;
    ngx_int_t            col_count, row_count, aff_count, row;

    dd("entering");

    pgctx = ngx_http_get_module_ctx(r, ngx_postgres_module);

    col_count = pgctx->var_cols;
    row_count = pgctx->var_rows;
    aff_count = (pgctx->var_affected == NGX_ERROR) ? 0 : pgctx->var_affected;

    /* render header */
    first = last = ngx_postgres_render_rds_header(r, r->pool, res, col_count,
                                                  aff_count);
    if (last == NULL) {
        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        goto done;
    }

    /* render columns */
    last->next = ngx_postgres_render_rds_columns(r, r->pool, res, col_count);
    if (last->next == NULL) {
        dd("returning NGX_ERROR");
        return NGX_ERROR;
    }
    last = last->next;

    /* render rows */
    for (row = 0; row < row_count; row++) {
        last->next = ngx_postgres_render_rds_row(r, r->pool, res, col_count,
                                                 row, (row == row_count - 1));
        if (last->next == NULL) {
            dd("returning NGX_ERROR");
            return NGX_ERROR;
        }
        last = last->next;
    }

    /* render row terminator (for empty result-set only) */
    if (row == 0) {
        last->next = ngx_postgres_render_rds_row_terminator(r, r->pool);
        if (last->next == NULL) {
            dd("returning NGX_ERROR");
            return NGX_ERROR;
        }
        last = last->next;
    }

done:

    last->next = NULL;

    /* set output response */
    pgctx->response = first;

    dd("returning NGX_DONE");
    return NGX_DONE;
}

ngx_chain_t *
ngx_postgres_render_rds_header(ngx_http_request_t *r, ngx_pool_t *pool,
    PGresult *res, ngx_int_t col_count, ngx_int_t aff_count)
{
    ngx_chain_t  *cl;
    ngx_buf_t    *b;
    size_t        size;
    char         *errstr;
    size_t        errstr_len;

    dd("entering");

    errstr = PQresultErrorMessage(res);
    errstr_len = ngx_strlen(errstr);

    size = sizeof(uint8_t)        /* endian type */
         + sizeof(uint32_t)       /* format version */
         + sizeof(uint8_t)        /* result type */
         + sizeof(uint16_t)       /* standard error code */
         + sizeof(uint16_t)       /* driver-specific error code */
         + sizeof(uint16_t)       /* driver-specific error string length */
         + (uint16_t) errstr_len  /* driver-specific error string data */
         + sizeof(uint64_t)       /* rows affected */
         + sizeof(uint64_t)       /* insert id */
         + sizeof(uint16_t)       /* column count */
         ;

    b = ngx_create_temp_buf(pool, size);
    if (b == NULL) {
        dd("returning NULL");
        return NULL;
    }

    cl = ngx_alloc_chain_link(pool);
    if (cl == NULL) {
        dd("returning NULL");
        return NULL;
    }

    cl->buf = b;
    b->memory = 1;
    b->tag = r->upstream->output.tag;

    /* fill data */
#if NGX_HAVE_LITTLE_ENDIAN
    *b->last++ = 0;
#else
    *b->last++ = 1;
#endif

    *(uint32_t *) b->last = (uint32_t) resty_dbd_stream_version;
    b->last += sizeof(uint32_t);

    *b->last++ = 0;

    *(uint16_t *) b->last = (uint16_t) 0;
    b->last += sizeof(uint16_t);

    *(uint16_t *) b->last = (uint16_t) PQresultStatus(res);
    b->last += sizeof(uint16_t);

    *(uint16_t *) b->last = (uint16_t) errstr_len;
    b->last += sizeof(uint16_t);

    if (errstr_len) {
        b->last = ngx_copy(b->last, (u_char *) errstr, errstr_len);
    }

    *(uint64_t *) b->last = (uint64_t) aff_count;
    b->last += sizeof(uint64_t);

    *(uint64_t *) b->last = (uint64_t) PQoidValue(res);
    b->last += sizeof(uint64_t);

    *(uint16_t *) b->last = (uint16_t) col_count;
    b->last += sizeof(uint16_t);

    if (b->last != b->end) {
        dd("returning NULL");
        return NULL;
    }

    dd("returning");
    return cl;
}

ngx_chain_t *
ngx_postgres_render_rds_columns(ngx_http_request_t *r, ngx_pool_t *pool,
    PGresult *res, ngx_int_t col_count)
{
    ngx_chain_t  *cl;
    ngx_buf_t    *b;
    size_t        size;
    ngx_int_t     col;
    Oid           col_type;
    char         *col_name;
    size_t        col_name_len;

    dd("entering");

    /* pre-calculate total length up-front for single buffer allocation */
    size = col_count
         * (sizeof(uint16_t)    /* standard column type */
            + sizeof(uint16_t)  /* driver-specific column type */
            + sizeof(uint16_t)  /* column name string length */
           )
         ;

    for (col = 0; col < col_count; col++) {
        size += ngx_strlen(PQfname(res, col));  /* column name string data */
    }

    b = ngx_create_temp_buf(pool, size);
    if (b == NULL) {
        dd("returning NULL");
        return NULL;
    }

    cl = ngx_alloc_chain_link(pool);
    if (cl == NULL) {
        dd("returning NULL");
        return NULL;
    }

    cl->buf = b;
    b->memory = 1;
    b->tag = r->upstream->output.tag;

    /* fill data */
    for (col = 0; col < col_count; col++) {
        col_type = PQftype(res, col);
        col_name = PQfname(res, col);
        col_name_len = (uint16_t) ngx_strlen(col_name);

        *(uint16_t *) b->last = (uint16_t) ngx_postgres_rds_col_type(col_type);
        b->last += sizeof(uint16_t);

        *(uint16_t *) b->last = col_type;
        b->last += sizeof(uint16_t);

        *(uint16_t *) b->last = col_name_len;
        b->last += sizeof(uint16_t);

        b->last = ngx_copy(b->last, col_name, col_name_len);
    }

    if (b->last != b->end) {
        dd("returning NULL");
        return NULL;
    }

    dd("returning");
    return cl;
}

ngx_chain_t *
ngx_postgres_render_rds_row(ngx_http_request_t *r, ngx_pool_t *pool,
    PGresult *res, ngx_int_t col_count, ngx_int_t row, ngx_int_t last_row)
{
    ngx_chain_t  *cl;
    ngx_buf_t    *b;
    size_t        size;
    ngx_int_t     col;

    dd("entering, row:%d", (int) row);

    /* pre-calculate total length up-front for single buffer allocation */
    size = sizeof(uint8_t)                 /* row number */
         + (col_count * sizeof(uint32_t))  /* field string length */
         ;

    if (last_row) {
        size += sizeof(uint8_t);
    }

    for (col = 0; col < col_count; col++) {
        size += PQgetlength(res, row, col);  /* field string data */
    }

    b = ngx_create_temp_buf(pool, size);
    if (b == NULL) {
        dd("returning NULL");
        return NULL;
    }

    cl = ngx_alloc_chain_link(pool);
    if (cl == NULL) {
        dd("returning NULL");
        return NULL;
    }

    cl->buf = b;
    b->memory = 1;
    b->tag = r->upstream->output.tag;

    /* fill data */
    *b->last++ = (uint8_t) 1; /* valid row */

    for (col = 0; col < col_count; col++) {
        if (PQgetisnull(res, row, col)) {
            *(uint32_t *) b->last = (uint32_t) -1;
             b->last += sizeof(uint32_t);
        } else {
            size = PQgetlength(res, row, col);
            *(uint32_t *) b->last = (uint32_t) size;
            b->last += sizeof(uint32_t);

            if (size) {
                b->last = ngx_copy(b->last, PQgetvalue(res, row, col), size);
            }
        }
    }

    if (last_row) {
        *b->last++ = (uint8_t) 0; /* row terminator */
    }

    if (b->last != b->end) {
        dd("returning NULL");
        return NULL;
    }

    dd("returning");
    return cl;
}

ngx_chain_t *
ngx_postgres_render_rds_row_terminator(ngx_http_request_t *r, ngx_pool_t *pool)
{
    ngx_chain_t  *cl;
    ngx_buf_t    *b;

    dd("entering");

    b = ngx_create_temp_buf(pool, sizeof(uint8_t));
    if (b == NULL) {
        dd("returning NULL");
        return NULL;
    }

    cl = ngx_alloc_chain_link(pool);
    if (cl == NULL) {
        dd("returning NULL");
        return NULL;
    }

    cl->buf = b;
    b->memory = 1;
    b->tag = r->upstream->output.tag;

    /* fill data */
    *b->last++ = (uint8_t) 0; /* row terminator */

    if (b->last != b->end) {
        dd("returning NULL");
        return NULL;
    }

    dd("returning");
    return cl;
}

ngx_int_t
ngx_postgres_output_chain(ngx_http_request_t *r, ngx_chain_t *cl)
{
    ngx_http_upstream_t       *u = r->upstream;
    ngx_http_core_loc_conf_t  *clcf;
    ngx_postgres_loc_conf_t   *pglcf;
    ngx_postgres_ctx_t        *pgctx;
    ngx_int_t                  rc;

    dd("entering");

    if (!r->header_sent) {
        ngx_http_clear_content_length(r);

        pglcf = ngx_http_get_module_loc_conf(r, ngx_postgres_module);
        pgctx = ngx_http_get_module_ctx(r, ngx_postgres_module);

        r->headers_out.status = pgctx->status ? abs(pgctx->status)
                                              : NGX_HTTP_OK;

        if (pglcf->output_handler == &ngx_postgres_output_rds) {
            /* RDS for output rds */
            r->headers_out.content_type.data = (u_char *) rds_content_type;
            r->headers_out.content_type.len = rds_content_type_len;
            r->headers_out.content_type_len = rds_content_type_len;
        } else if (pglcf->output_handler != NULL) {
            /* default type for output value|row */
            clcf = ngx_http_get_module_loc_conf(r, ngx_http_core_module);

            r->headers_out.content_type = clcf->default_type;
            r->headers_out.content_type_len = clcf->default_type.len;
        }

        r->headers_out.content_type_lowcase = NULL;

        rc = ngx_http_send_header(r);
        if (rc == NGX_ERROR || rc > NGX_OK || r->header_only) {
            dd("returning rc:%d", (int) rc);
            return rc;
        }
    }

    if (cl == NULL) {
        dd("returning NGX_DONE");
        return NGX_DONE;
    }

    rc = ngx_http_output_filter(r, cl);
    if (rc == NGX_ERROR || rc > NGX_OK) {
        dd("returning rc:%d", (int) rc);
        return rc;
    }

#if defined(nginx_version) && (nginx_version >= 1001004)
    ngx_chain_update_chains(r->pool, &u->free_bufs, &u->busy_bufs, &cl,
                            u->output.tag);
#else
    ngx_chain_update_chains(&u->free_bufs, &u->busy_bufs, &cl, u->output.tag);
#endif

    dd("returning rc:%d", (int) rc);
    return rc;
}

rds_col_type_t
ngx_postgres_rds_col_type(Oid col_type)
{
    switch (col_type) {
    case 20: /* int8 */
        return rds_col_type_bigint;
    case 1560: /* bit */
        return rds_col_type_bit;
    case 1562: /* varbit */
        return rds_col_type_bit_varying;
    case 16: /* bool */
        return rds_col_type_bool;
    case 18: /* char */
        return rds_col_type_char;
    case 19: /* name */
        /* FALLTROUGH */
    case 25: /* text */
        /* FALLTROUGH */
    case 1043: /* varchar */
        return rds_col_type_varchar;
    case 1082: /* date */
        return rds_col_type_date;
    case 701: /* float8 */
        return rds_col_type_double;
    case 23: /* int4 */
        return rds_col_type_integer;
    case 1186: /* interval */
        return rds_col_type_interval;
    case 1700: /* numeric */
        return rds_col_type_decimal;
    case 700: /* float4 */
        return rds_col_type_real;
    case 21: /* int2 */
        return rds_col_type_smallint;
    case 1266: /* timetz */
        return rds_col_type_time_with_time_zone;
    case 1083: /* time */
        return rds_col_type_time;
    case 1184: /* timestamptz */
        return rds_col_type_timestamp_with_time_zone;
    case 1114: /* timestamp */
        return rds_col_type_timestamp;
    case 142: /* xml */
        return rds_col_type_xml;
    case 17: /* bytea */
        return rds_col_type_blob;
    default:
        return rds_col_type_unknown;
    }
}
