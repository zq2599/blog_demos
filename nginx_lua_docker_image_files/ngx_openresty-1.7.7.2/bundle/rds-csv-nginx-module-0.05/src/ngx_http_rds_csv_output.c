
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_rds_csv_filter_module.h"
#include "ngx_http_rds_csv_output.h"
#include "ngx_http_rds_csv_util.h"
#include "resty_dbd_stream.h"


static u_char * ngx_http_rds_csv_request_mem(ngx_http_request_t *r,
    ngx_http_rds_csv_ctx_t *ctx, size_t len);
static ngx_int_t ngx_http_rds_csv_get_buf(ngx_http_request_t *r,
    ngx_http_rds_csv_ctx_t *ctx);
static u_char * ngx_http_rds_csv_get_postponed(ngx_http_request_t *r,
    ngx_http_rds_csv_ctx_t *ctx, size_t len);
static ngx_int_t ngx_http_rds_csv_submit_mem(ngx_http_request_t *r,
    ngx_http_rds_csv_ctx_t *ctx, size_t len, unsigned last_buf);
static size_t ngx_get_num_size(uint64_t i);


ngx_int_t
ngx_http_rds_csv_output_literal(ngx_http_request_t *r,
    ngx_http_rds_csv_ctx_t *ctx, u_char *data, size_t len,
    int last_buf)
{
    u_char                      *pos;

    pos = ngx_http_rds_csv_request_mem(r, ctx, len);
    if (pos == NULL) {
        return NGX_ERROR;
    }

    ngx_memcpy(pos, data, len);

    dd("before output chain");

    if (last_buf) {
        ctx->seen_stream_end = 1;

        if (r != r->main) {
            last_buf = 0;
        }
    }

    return ngx_http_rds_csv_submit_mem(r, ctx, len, (unsigned) last_buf);
}


ngx_int_t
ngx_http_rds_csv_output_bufs(ngx_http_request_t *r,
    ngx_http_rds_csv_ctx_t *ctx)
{
    ngx_int_t                rc;
    ngx_chain_t             *cl;

    dd("entered output chain");

    if (ctx->seen_stream_end) {
        ctx->seen_stream_end = 0;

        if (ctx->avail_out) {
            cl = ngx_alloc_chain_link(r->pool);
            if (cl == NULL) {
                return NGX_ERROR;
            }

            cl->buf = ctx->out_buf;
            cl->next = NULL;
            *ctx->last_out = cl;
            ctx->last_out = &cl->next;

            ctx->avail_out = 0;
        }
    }

    dd_dump_chain_size();

    for ( ;; ) {
        if (ctx->out == NULL) {
            /* fprintf(stderr, "\n"); */
            return NGX_OK;
        }

        /* fprintf(stderr, "XXX Relooping..."); */

        rc = ngx_http_rds_csv_next_body_filter(r, ctx->out);

        if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
            return rc;
        }

#if defined(nginx_version) && nginx_version >= 1001004
        ngx_chain_update_chains(r->pool, &ctx->free_bufs, &ctx->busy_bufs,
                                &ctx->out, ctx->tag);
#else
        ngx_chain_update_chains(&ctx->free_bufs, &ctx->busy_bufs, &ctx->out,
                                ctx->tag);
#endif

        ctx->last_out = &ctx->out;
    }

    /* impossible to reach here */
    return NGX_ERROR;
}


ngx_int_t
ngx_http_rds_csv_output_header(ngx_http_request_t *r,
    ngx_http_rds_csv_ctx_t *ctx, ngx_http_rds_header_t *header)
{
    u_char                  *pos, *last;
    size_t                   size;
    uintptr_t                escape;
    unsigned                 last_buf = 0;
    unsigned                 need_quotes;
    u_char                   sep;

    ngx_http_rds_csv_loc_conf_t       *conf;

    /* calculate the buffer size */

    conf = ngx_http_get_module_loc_conf(r, ngx_http_rds_csv_filter_module);

    if (conf->field_name_header) {
        size = sizeof("errcode,errstr,insert_id,affected_rows") - 1
               + conf->row_term.len;

    } else {
        size = 0;
    }

    sep = (u_char) conf->field_sep;

    size += 3 /* field seperators */ + conf->row_term.len;

    size += ngx_get_num_size(header->std_errcode);

    escape = ngx_http_rds_csv_escape_csv_str(sep, NULL, header->errstr.data,
                                             header->errstr.len,
                                             &need_quotes);

    if (need_quotes) {
        size += sizeof("\"\"") - 1;
    }

    size += header->errstr.len + escape
            + ngx_get_num_size(header->insert_id)
            + ngx_get_num_size(header->affected_rows);

    /* create the buffer */

    pos = ngx_http_rds_csv_request_mem(r, ctx, size);
    if (pos == NULL) {
        return NGX_ERROR;
    }

    last = pos;

    /* fill up the buffer */

    last = ngx_sprintf(last, "errcode%cerrstr%cinsert_id%caffected_rows%V"
                       "%uD%c", sep, sep, sep, &conf->row_term,
                       (uint32_t) header->std_errcode, sep);

    if (need_quotes) {
        *last++ = '"';
    }

    if (escape == 0) {
        last = ngx_copy(last, header->errstr.data, header->errstr.len);

    } else {
        last = (u_char *)
                ngx_http_rds_csv_escape_csv_str(sep, last,
                                                header->errstr.data,
                                                header->errstr.len, NULL);
    }

    if (need_quotes) {
        *last++ = '"';
    }

    last = ngx_sprintf(last, "%c%uL%c%uL%V", sep, header->insert_id, sep,
                       header->affected_rows, &conf->row_term);

    if ((size_t) (last - pos) != size) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds_csv: output header buffer error: %uz != %uz",
                      (size_t) (last - pos), size);

        return NGX_ERROR;
    }

    if (r == r->main) {
        last_buf = 1;
    }

    ctx->seen_stream_end = 1;

    return ngx_http_rds_csv_submit_mem(r, ctx, size, last_buf);
}


ngx_int_t
ngx_http_rds_csv_output_field_names(ngx_http_request_t *r,
    ngx_http_rds_csv_ctx_t *ctx)
{
    ngx_uint_t                           i;
    ngx_http_rds_column_t               *col;
    size_t                               size;
    u_char                              *pos, *last;
    uintptr_t                            escape = 0;
    unsigned                             need_quotes;
    u_char                               sep;
    ngx_http_rds_csv_loc_conf_t         *conf;

    conf = ngx_http_get_module_loc_conf(r, ngx_http_rds_csv_filter_module);

    sep = (u_char) conf->field_sep;

    size = ctx->col_count - 1 /* field sep count */
         + conf->row_term.len;

    for (i = 0; i < ctx->col_count; i++) {
        col = &ctx->cols[i];
        escape = ngx_http_rds_csv_escape_csv_str(sep, NULL, col->name.data,
                                                 col->name.len, &need_quotes);

        dd("field escape: %d", (int) escape);

        if (need_quotes) {
            size += sizeof("\"\"") - 1;
        }

        size += col->name.len + escape;
    }

    ctx->generated_col_names = 1;

    pos = ngx_http_rds_csv_request_mem(r, ctx, size);
    if (pos == NULL) {
        return NGX_ERROR;
    }

    last = pos;

    for (i = 0; i < ctx->col_count; i++) {
        col = &ctx->cols[i];

        escape = ngx_http_rds_csv_escape_csv_str(sep, NULL, col->name.data,
                                                 col->name.len, &need_quotes);

        if (need_quotes) {
            *last++ = '"';
        }

        if (escape == 0) {
            last = ngx_copy(last, col->name.data, col->name.len);

        } else {
            last = (u_char *)
                   ngx_http_rds_csv_escape_csv_str(sep, last,
                                                   col->name.data,
                                                   col->name.len, NULL);
        }

        if (need_quotes) {
            *last++ = '"';
        }

        if (i != ctx->col_count - 1) {
            *last++ = sep;
        }
    }

    last = ngx_copy(last, conf->row_term.data, conf->row_term.len);

    if ((size_t) (last - pos) != size) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds_csv: output field names buffer error: %uz != %uz",
                      (size_t) (last - pos), size);

        return NGX_ERROR;
    }

    return ngx_http_rds_csv_submit_mem(r, ctx, size, 0);
}


ngx_int_t
ngx_http_rds_csv_output_field(ngx_http_request_t *r,
    ngx_http_rds_csv_ctx_t *ctx, u_char *data, size_t len, int is_null)
{
    u_char                              *pos, *last;
    ngx_http_rds_column_t               *col;
    size_t                               size;
    uintptr_t                            val_escape = 0;
    unsigned                             need_quotes;
    u_char                               sep;
    ngx_http_rds_csv_loc_conf_t         *conf;
#if DDEBUG
    u_char                              *p;
#endif

    conf = ngx_http_get_module_loc_conf(r, ngx_http_rds_csv_filter_module);

    sep = (u_char) conf->field_sep;

    dd("reading row %llu, col %d, len %d",
       (unsigned long long) ctx->row,
       (int) ctx->cur_col, (int) len);

    /* calculate the buffer size */

    if (ctx->cur_col == 0) {
        size = 0;

    } else {
        size = 1 /* field sep */;
    }

    col = &ctx->cols[ctx->cur_col];

    if (len == 0 && ctx->field_data_rest > 0) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds_csv: at least one octet should go with the field "
                      "size in one buf");

        return NGX_ERROR;
    }

    if (is_null) {
        /* SQL NULL is just empty in the CSV field */

    } else if (len == 0) {
        /* empty string is also empty */

    } else {
        switch (col->std_type & 0xc000) {
        case rds_rough_col_type_float:
        case rds_rough_col_type_int:
        case rds_rough_col_type_bool:
            size += len;
            break;

        default:
            dd("string field found");

            val_escape = ngx_http_rds_csv_escape_csv_str(sep, NULL, data, len,
                                                         &need_quotes);

            if (ctx->field_data_rest > 0 && !need_quotes) {
                need_quotes = 1;
            }

            if (need_quotes) {
                if (ctx->field_data_rest == 0) {
                    size += sizeof("\"\"") - 1;

                } else {
                    size += sizeof("\"") - 1;
                }
            }

            size += len + val_escape;
            break;
        }
    }

    if (ctx->field_data_rest == 0 && ctx->cur_col == ctx->col_count - 1) {
        /* last column in the row */
        size += conf->row_term.len;
    }

    /* allocate the buffer */

    pos = ngx_http_rds_csv_request_mem(r, ctx, size);
    if (pos == NULL) {
        return NGX_ERROR;
    }

    last = pos;

    /* fill up the buffer */

    if (ctx->cur_col != 0) {
        *last++ = sep;
    }

    if (is_null || len == 0) {
        /* do nothing */

    } else {
        switch (col->std_type & 0xc000) {
        case rds_rough_col_type_int:
        case rds_rough_col_type_float:
        case rds_rough_col_type_bool:
            last = ngx_copy(last, data, len);
            break;

        default:
            /* string */
            if (need_quotes) {
                *last++ = '"';
            }

            if (val_escape == 0) {
                last = ngx_copy(last, data, len);

            } else {
                dd("field: string value escape non-zero: %d",
                   (int) val_escape);

#if DDEBUG
                p = last;
#endif

                last = (u_char *)
                       ngx_http_rds_csv_escape_csv_str(sep, last, data, len,
                                                       NULL);

                dd("escaped value \"%.*s\" (len %d, escape %d, escape2 %d)",
                   (int) (len + val_escape),
                   p, (int) (len + val_escape),
                   (int) val_escape,
                   (int) ((last - p) - len));
            }

            if (need_quotes && ctx->field_data_rest == 0) {
                *last++ = '"';
            }

            break;
        }
    }

    if (ctx->field_data_rest == 0 && ctx->cur_col == ctx->col_count - 1) {
        last = ngx_copy(last, conf->row_term.data, conf->row_term.len);
    }

    if ((size_t) (last - pos) != size) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds_csv: output field: buffer error (%d left)",
                      (int) size - (last - pos));

        return NGX_ERROR;
    }

    return ngx_http_rds_csv_submit_mem(r, ctx, size, 0);
}


ngx_int_t
ngx_http_rds_csv_output_more_field_data(ngx_http_request_t *r,
    ngx_http_rds_csv_ctx_t *ctx, u_char *data, size_t len)
{
    u_char                          *pos, *last;
    size_t                           size = 0;
    ngx_http_rds_column_t           *col;
    uintptr_t                        escape = 0;
#if DDEBUG
    u_char                          *p;
#endif
    unsigned                         need_quotes;
    u_char                           sep;
    ngx_http_rds_csv_loc_conf_t     *conf;

    conf = ngx_http_get_module_loc_conf(r, ngx_http_rds_csv_filter_module);

    sep = (u_char) conf->field_sep;

    /* calculate the buffer size */

    col = &ctx->cols[ctx->cur_col];

    switch (col->std_type & 0xc000) {
    case rds_rough_col_type_int:
    case rds_rough_col_type_float:
    case rds_rough_col_type_bool:
        size += len;
        break;

    default:
        /* string */

        escape = ngx_http_rds_csv_escape_csv_str(sep, NULL, data, len,
                                                 &need_quotes);

        size = len + escape;

        if (ctx->field_data_rest == 0) {
            size += sizeof("\"") - 1;
        }

        break;
    }

    if (ctx->field_data_rest == 0 && ctx->cur_col == ctx->col_count - 1) {
        /* last column in the row */
        size += conf->row_term.len;
    }

    /* allocate the buffer */

    pos = ngx_http_rds_csv_request_mem(r, ctx, size);
    if (pos == NULL) {
        return NGX_ERROR;
    }

    last = pos;

    /* fill up the buffer */

    switch (col->std_type & 0xc000) {
    case rds_rough_col_type_int:
    case rds_rough_col_type_float:
    case rds_rough_col_type_bool:
        last = ngx_copy(last, data, len);
        break;

    default:
        /* string */
        if (escape == 0) {
            last = ngx_copy(last, data, len);

        } else {
            dd("more field data: string value escape non-zero: %d",
               (int) escape);

#if DDEBUG
            p = last;
#endif

            last = (u_char *) ngx_http_rds_csv_escape_csv_str(sep, last, data,
                                                              len, NULL);

            dd("escaped value \"%.*s\" (len %d, escape %d, escape2 %d)",
               (int) (len + escape),
               p, (int) (len + escape),
               (int) escape,
               (int) ((last - p) - len));
        }

        if (ctx->field_data_rest == 0) {
            *last++ = '"';
        }

        break;
    } /* switch */

    if (ctx->field_data_rest == 0 && ctx->cur_col == ctx->col_count - 1) {
        /* last column in the row */
        last = ngx_copy(last, conf->row_term.data, conf->row_term.len);
    }

    if ((size_t) (last - pos) != size) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds_csv: output more field data: buffer error "
                      "(%d left)", (int) (size - (last - pos)));
        return NGX_ERROR;
    }

    return ngx_http_rds_csv_submit_mem(r, ctx, size, 0);
}


static u_char *
ngx_http_rds_csv_request_mem(ngx_http_request_t *r,
    ngx_http_rds_csv_ctx_t *ctx, size_t len)
{
    ngx_int_t                rc;
    u_char                  *p;

    rc = ngx_http_rds_csv_get_buf(r, ctx);
    if (rc != NGX_OK) {
        return NULL;
    }

    if (ctx->avail_out < len) {
        p = ngx_http_rds_csv_get_postponed(r, ctx, len);
        if (p == NULL) {
            return NULL;
        }

        ctx->postponed.pos = p;
        ctx->postponed.last = p + len;

        return p;
    }

    return ctx->out_buf->last;
}


static ngx_int_t
ngx_http_rds_csv_get_buf(ngx_http_request_t *r, ngx_http_rds_csv_ctx_t *ctx)
{
    ngx_http_rds_csv_loc_conf_t         *conf;

    dd("MEM enter");

    if (ctx->avail_out) {
        return NGX_OK;
    }

    conf = ngx_http_get_module_loc_conf(r, ngx_http_rds_csv_filter_module);

    if (ctx->free_bufs) {
        dd("MEM reusing temp buf from free_bufs");

        ctx->out_buf = ctx->free_bufs->buf;
        ctx->free_bufs = ctx->free_bufs->next;

    } else {
        dd("MEM creating temp buf with size: %d", (int) conf->buf_size);
        ctx->out_buf = ngx_create_temp_buf(r->pool, conf->buf_size);
        if (ctx->out_buf == NULL) {
            return NGX_ERROR;
        }

        ctx->out_buf->tag = (ngx_buf_tag_t) &ngx_http_rds_csv_filter_module;
        ctx->out_buf->recycled = 1;
    }

    ctx->avail_out = conf->buf_size;

    return NGX_OK;
}


static u_char *
ngx_http_rds_csv_get_postponed(ngx_http_request_t *r,
    ngx_http_rds_csv_ctx_t *ctx, size_t len)
{
    u_char          *p;

    dd("MEM enter");

    if (ctx->cached.start == NULL) {
        goto alloc;
    }

    if ((size_t) (ctx->cached.end - ctx->cached.start) < len) {
        ngx_pfree(r->pool, ctx->cached.start);
        goto alloc;
    }

    return ctx->cached.start;

alloc:
    p = ngx_palloc(r->pool, len);
    if (p == NULL) {
        return NULL;
    }

    ctx->cached.start = p;
    ctx->cached.end = p + len;

    return p;
}


static ngx_int_t
ngx_http_rds_csv_submit_mem(ngx_http_request_t *r,
    ngx_http_rds_csv_ctx_t *ctx, size_t len, unsigned last_buf)
{
    ngx_chain_t             *cl;
    ngx_int_t                rc;

    if (ctx->postponed.pos != NULL) {
        dd("MEM copy postponed data over to ctx->out for len %d", (int) len);

        for ( ;; ) {
            len = ctx->postponed.last - ctx->postponed.pos;
            if (len > ctx->avail_out) {
                len = ctx->avail_out;
            }

            ctx->out_buf->last = ngx_copy(ctx->out_buf->last,
                                          ctx->postponed.pos, len);

            ctx->avail_out -= len;

            ctx->postponed.pos += len;

            if (ctx->postponed.pos == ctx->postponed.last) {
                ctx->postponed.pos = NULL;
            }

            if (ctx->avail_out > 0) {
                break;
            }

            dd("MEM save ctx->out_buf");

            cl = ngx_alloc_chain_link(r->pool);
            if (cl == NULL) {
                return NGX_ERROR;
            }

            cl->buf = ctx->out_buf;
            cl->next = NULL;
            *ctx->last_out = cl;
            ctx->last_out = &cl->next;

            if (ctx->postponed.pos == NULL) {
                ctx->out_buf->last_buf = last_buf;
                break;
            }

            rc = ngx_http_rds_csv_get_buf(r, ctx);
            if (rc != NGX_OK) {
                return NGX_ERROR;
            }
        }

        return NGX_OK;
    }

    dd("MEM consuming out_buf for %d", (int) len);

    ctx->out_buf->last += len;
    ctx->avail_out -= len;
    ctx->out_buf->last_buf = last_buf;

    if (ctx->avail_out == 0) {
        dd("MEM save ctx->out_buf");

        cl = ngx_alloc_chain_link(r->pool);
        if (cl == NULL) {
            return NGX_ERROR;
        }

        cl->buf = ctx->out_buf;
        cl->next = NULL;
        *ctx->last_out = cl;
        ctx->last_out = &cl->next;
    }

    return NGX_OK;
}


static size_t
ngx_get_num_size(uint64_t i)
{
    size_t          n = 0;

    do {
        i = i / 10;
        n++;
    } while (i > 0);

    return n;
}
