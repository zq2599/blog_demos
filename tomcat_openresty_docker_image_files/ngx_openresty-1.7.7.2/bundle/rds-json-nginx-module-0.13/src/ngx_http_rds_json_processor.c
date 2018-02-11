
/*
 * Copyright (C) agentzh
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_rds_json_util.h"
#include "ngx_http_rds_json_processor.h"
#include "ngx_http_rds_json_output.h"
#include "ngx_http_rds.h"
#include "ngx_http_rds_utils.h"

#include <ngx_core.h>
#include <ngx_http.h>


ngx_int_t
ngx_http_rds_json_process_header(ngx_http_request_t *r,
    ngx_chain_t *in, ngx_http_rds_json_ctx_t *ctx)
{
    ngx_buf_t                       *b;
    ngx_http_rds_header_t            header;
    ngx_int_t                        rc;

    if (in == NULL) {
        return NGX_OK;
    }

    b = in->buf;

    if (!ngx_buf_in_memory(b)) {
        if (!ngx_buf_special(b)) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "rds_json: process header: buf from "
                          "upstream not in memory");
            goto invalid;
        }

        in = in->next;

        if (in == NULL) {
            return NGX_OK;
        }

        b = in->buf;
    }

    rc = ngx_http_rds_parse_header(r, b, &header);

    if (rc != NGX_OK) {
        goto invalid;
    }

    dd("col count: %d", (int) header.col_count);

    if (header.col_count == 0) {
        /* for empty result set, just return the JSON
         * representation of the RDS header */

        dd("col count == 0");

        if (b->pos != b->last) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "rds_json: header: there's unexpected remaining data "
                          "in the buf");
            goto invalid;
        }

        ctx->state = state_done;

        /* now we send the postponed response header */
        if (!ctx->header_sent) {
            ctx->header_sent = 1;

            rc = ngx_http_rds_json_next_header_filter(r);

            if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
                return rc;
            }
        }

        rc = ngx_http_rds_json_output_header(r, ctx, &header);

        if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
            return rc;
        }

        ngx_http_rds_json_discard_bufs(r->pool, in);

        return rc;
    }

    ctx->cols = ngx_palloc(r->pool,
                           header.col_count * sizeof(ngx_http_rds_column_t));

    if (ctx->cols == NULL) {
        goto invalid;
    }

    ctx->state = state_expect_col;
    ctx->cur_col = 0;
    ctx->col_count = header.col_count;

    /* now we send the postponed response header */
    if (!ctx->header_sent) {
        ctx->header_sent = 1;

        rc = ngx_http_rds_json_next_header_filter(r);
        if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
            return rc;
        }
    }

    return ngx_http_rds_json_process_col(r, b->pos == b->last ? in->next : in,
                                         ctx);

invalid:

    dd("return 500");
    if (!ctx->header_sent) {
        ctx->header_sent = 1;

        r->headers_out.status = NGX_HTTP_INTERNAL_SERVER_ERROR;
        ngx_http_send_header(r);
        ngx_http_send_special(r, NGX_HTTP_LAST);

        return NGX_ERROR;
    }

    return NGX_ERROR;
}


ngx_int_t
ngx_http_rds_json_process_col(ngx_http_request_t *r, ngx_chain_t *in,
    ngx_http_rds_json_ctx_t *ctx)
{
    ngx_buf_t                       *b;
    ngx_int_t                        rc;
    ngx_http_rds_json_loc_conf_t    *conf;

    if (in == NULL) {
        return NGX_OK;
    }

    b = in->buf;

    if (!ngx_buf_in_memory(b)) {
        if (!ngx_buf_special(b)) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "rds_json: process col: buf from "
                          "upstream not in memory");
            return NGX_ERROR;
        }

        in = in->next;

        if (in == NULL) {
            return NGX_OK;
        }

        b = in->buf;
    }

    dd("parsing rds column");

    rc = ngx_http_rds_parse_col(r, b, &ctx->cols[ctx->cur_col]);

    dd("parse col returns %d (%d)", (int) rc, (int) NGX_OK);

    if (rc != NGX_OK) {
        return NGX_ERROR;
    }

    if (b->pos == b->last) {
        dd("parse col buf consumed");
        in = in->next;
    }

    ctx->cur_col++;

    if (ctx->cur_col >= ctx->col_count) {
        dd("end of column list");

        ctx->state = state_expect_row;
        ctx->row = 0;

        dd("output \"[\"");
        dd("before output literal");

        conf = ngx_http_get_module_loc_conf(r, ngx_http_rds_json_filter_module);

        if (conf->root.len) {
            rc = ngx_http_rds_json_output_props(r, ctx, conf);

            dd("after output literal");

            if (rc == NGX_ERROR || rc > NGX_OK) {
                return rc;
            }
        }

        rc = ngx_http_rds_json_output_literal(r, ctx,
                                              (u_char *)"[", sizeof("[") - 1,
                                              0 /* last buf */);

        dd("after output literal");

        if (rc == NGX_ERROR || rc > NGX_OK) {
            return rc;
        }

        if (conf->format == json_format_compact) {
            rc = ngx_http_rds_json_output_cols(r, ctx);

            if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
                return rc;
            }
        }

        dd("after output literal");

        if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
            return rc;
        }

        dd("process col is entering process row...");
        return ngx_http_rds_json_process_row(r, in, ctx);
    }

    return ngx_http_rds_json_process_col(r, in, ctx);
}


ngx_int_t
ngx_http_rds_json_process_row(ngx_http_request_t *r, ngx_chain_t *in,
    ngx_http_rds_json_ctx_t *ctx)
{
    ngx_buf_t                   *b;
    ngx_int_t                    rc;

    ngx_http_rds_json_loc_conf_t        *conf;

    if (in == NULL) {
        return NGX_OK;
    }

    dd("process row");

    b = in->buf;

    if (!ngx_buf_in_memory(b)) {
        if (!ngx_buf_special(b)) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "rds_json: process row: buf from "
                          "upstream not in memory");
            return NGX_ERROR;
        }

        in = in->next;

        if (in == NULL) {
            return NGX_OK;
        }

        b = in->buf;
    }

    if (b->last - b->pos < (ssize_t) sizeof(uint8_t)) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds_json: row flag is incomplete in the buf");
        return NGX_ERROR;
    }

    dd("row flag: %d (offset %d)",
       (char) *b->pos,
       (int) (b->pos - b->start));

    if (*b->pos++ == 0) {
        /* end of row list */
        ctx->state = state_done;

        if (b->pos != b->last) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "rds_json: row: there's unexpected remaining data "
                          "in the buf");
            return NGX_ERROR;
        }

        conf = ngx_http_get_module_loc_conf(r, ngx_http_rds_json_filter_module);

        if (conf->root.len) {
            rc = ngx_http_rds_json_output_literal(r, ctx,
                                                  (u_char *)"]}",
                                                  sizeof("]}") - 1,
                                                  1 /* last buf*/);

        } else {
            rc = ngx_http_rds_json_output_literal(r, ctx,
                                                  (u_char *)"]",
                                                  sizeof("]") - 1,
                                                  1 /* last buf*/);
        }

        if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
            return rc;
        }

        return rc;
    }

    ctx->row++;
    ctx->cur_col = 0;
    ctx->state = state_expect_field;

    if (b->pos == b->last) {
        in = in->next;

    } else {
        dd("process row: buf not consumed completely");
    }

    return ngx_http_rds_json_process_field(r, in, ctx);
}


ngx_int_t
ngx_http_rds_json_process_field(ngx_http_request_t *r, ngx_chain_t *in,
    ngx_http_rds_json_ctx_t *ctx)
{
    size_t              total, len;
    ngx_buf_t          *b;
    ngx_int_t           rc;

    for (;;) {
        if (in == NULL) {
            return NGX_OK;
        }

        b = in->buf;

        if (!ngx_buf_in_memory(b)) {
            dd("buf not in memory");

            if (!ngx_buf_special(b)) {
                ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                              "rds_json: process field: buf from "
                              "upstream not in memory");
                return NGX_ERROR;
            }

            in = in->next;

            if (in == NULL) {
                return NGX_OK;
            }

            b = in->buf;
        }

        dd("process field: buf size: %d", (int) ngx_buf_size(b));

        if (b->last - b->pos < (ssize_t) sizeof(uint32_t)) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "rds_json: field size is incomplete in the buf: %*s "
                          "(len: %d)", b->last - b->pos, b->pos,
                          (int) (b->last - b->pos));

            return NGX_ERROR;
        }

        total = *(uint32_t *) b->pos;

        dd("total: %d", (int) total);

        b->pos += sizeof(uint32_t);

        if (total == (uint32_t) -1) {
            /* SQL NULL found */
            total = 0;
            len = 0;
            ctx->field_data_rest = 0;

            rc = ngx_http_rds_json_output_field(r, ctx, b->pos, len,
                                                1 /* is null */);

        } else {
            len = (uint32_t) (b->last - b->pos);

            if (len >= total) {
                len = total;
            }

            ctx->field_data_rest = total - len;

            rc = ngx_http_rds_json_output_field(r, ctx, b->pos, len,
                                                0 /* not null */);
        }

        if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
            return rc;
        }

        b->pos += len;

        if (b->pos == b->last) {
            in = in->next;
        }

        if (len < total) {
            dd("process field: need to read more field data");

            ctx->state = state_expect_more_field_data;

            return ngx_http_rds_json_process_more_field_data(r, in, ctx);
        }

        ctx->cur_col++;

        if (ctx->cur_col >= ctx->col_count) {
            dd("reached the end of the current row");

            ctx->state = state_expect_row;

            return ngx_http_rds_json_process_row(r, in, ctx);
        }

        /* continue to process the next field (if any) */
    }

    /* impossible to reach here */
}


ngx_int_t
ngx_http_rds_json_process_more_field_data(ngx_http_request_t *r,
    ngx_chain_t *in, ngx_http_rds_json_ctx_t *ctx)
{
    ngx_int_t                    rc;
    ngx_buf_t                   *b;
    size_t                       len;

    for (;;) {
        if (in == NULL) {
            return NGX_OK;
        }

        b = in->buf;

        if (!ngx_buf_in_memory(b)) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "rds_json: buf from upstream not in memory");
            return NGX_ERROR;
        }

        len = b->last - b->pos;

        if (len >= ctx->field_data_rest) {
            len = ctx->field_data_rest;
            ctx->field_data_rest = 0;

        } else {
            ctx->field_data_rest -= len;
        }

        rc = ngx_http_rds_json_output_more_field_data(r, ctx, b->pos, len);

        if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
            return rc;
        }

        b->pos += len;

        if (b->pos == b->last) {
            in = in->next;
        }

        if (ctx->field_data_rest) {
            dd("process more field data: still some data remaining");
            continue;
        }

        dd("process more field data: reached the end of the current field");

        ctx->cur_col++;

        if (ctx->cur_col >= ctx->col_count) {
            dd("process more field data: reached the end of the current row");

            ctx->state = state_expect_row;

            return ngx_http_rds_json_process_row(r, in, ctx);
        }

        dd("proces more field data: read the next field");

        ctx->state = state_expect_field;

        return ngx_http_rds_json_process_field(r, in, ctx);
    }

    /* impossible to reach here */
}
