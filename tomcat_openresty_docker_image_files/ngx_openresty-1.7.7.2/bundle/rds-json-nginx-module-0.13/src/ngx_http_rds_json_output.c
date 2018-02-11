
/*
 * Copyright (C) agentzh
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_rds_json_filter_module.h"
#include "ngx_http_rds_json_output.h"
#include "ngx_http_rds_json_util.h"
#include "resty_dbd_stream.h"
#include <nginx.h>


static u_char * ngx_http_rds_json_request_mem(ngx_http_request_t *r,
    ngx_http_rds_json_ctx_t *ctx, size_t len);
static ngx_int_t ngx_http_rds_json_get_buf(ngx_http_request_t *r,
    ngx_http_rds_json_ctx_t *ctx);
static u_char * ngx_http_rds_json_get_postponed(ngx_http_request_t *r,
    ngx_http_rds_json_ctx_t *ctx, size_t len);
static ngx_int_t ngx_http_rds_json_submit_mem(ngx_http_request_t *r,
    ngx_http_rds_json_ctx_t *ctx, size_t len, unsigned last_buf);


static size_t ngx_get_num_size(uint64_t i);


ngx_int_t
ngx_http_rds_json_output_literal(ngx_http_request_t *r,
    ngx_http_rds_json_ctx_t *ctx, u_char *data, size_t len, int last_buf)
{
    u_char                      *pos;

    pos = ngx_http_rds_json_request_mem(r, ctx, len);
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

    return ngx_http_rds_json_submit_mem(r, ctx, len, (unsigned) last_buf);
}


ngx_int_t
ngx_http_rds_json_output_bufs(ngx_http_request_t *r,
    ngx_http_rds_json_ctx_t *ctx)
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

        rc = ngx_http_rds_json_next_body_filter(r, ctx->out);

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
}


ngx_int_t
ngx_http_rds_json_output_header(ngx_http_request_t *r,
    ngx_http_rds_json_ctx_t *ctx, ngx_http_rds_header_t *header)
{
    u_char                  *pos, *last;
    size_t                   size;
    uintptr_t                escape;
    unsigned                 last_buf = 0;
    ngx_uint_t               i;
    ngx_str_t               *values = NULL;
    uintptr_t               *escapes = NULL;

    ngx_http_rds_json_property_t        *prop = NULL;
    ngx_http_rds_json_loc_conf_t        *conf;

    conf = ngx_http_get_module_loc_conf(r, ngx_http_rds_json_filter_module);

    /* calculate the buffer size */

    size = sizeof("{") - 1
           + conf->errcode_key.len
           + sizeof(":") - 1
           + ngx_get_num_size(header->std_errcode)
           + sizeof("}") - 1;

    if (conf->success.len) {
        size += conf->success.len + sizeof(":,") - 1;
        if (header->std_errcode == 0) {
            size += sizeof("true") - 1;

        } else {
            size += sizeof("false") - 1;
        }
    }

    if (conf->user_props) {
        values = ngx_pnalloc(r->pool,
                             conf->user_props->nelts * (sizeof(ngx_str_t)
                             + sizeof(uintptr_t)));

        if (values == NULL) {
            return NGX_ERROR;
        }

        escapes = (uintptr_t *) ((u_char *) values
                  + conf->user_props->nelts * sizeof(ngx_str_t));

        prop = conf->user_props->elts;
        for (i = 0; i < conf->user_props->nelts; i++) {
            if (ngx_http_complex_value(r, &prop[i].value, &values[i])
                != NGX_OK)
            {
                return NGX_ERROR;
            }

            escapes[i] = ngx_http_rds_json_escape_json_str(NULL,
                                                           values[i].data,
                                                           values[i].len);

            size += sizeof(":\"\",") - 1 + prop[i].key.len + values[i].len
                    + escapes[i];
        }
    }


    if (header->errstr.len) {
        escape = ngx_http_rds_json_escape_json_str(NULL, header->errstr.data,
                                                   header->errstr.len);

        size += sizeof(",") - 1
                + conf->errstr_key.len
                + sizeof(":") - 1
                + sizeof("\"") - 1
                + header->errstr.len
                + escape
                + sizeof("\"") - 1;

    } else {
        escape = (uintptr_t) 0;
    }

    if (header->insert_id) {
        size += sizeof(",\"insert_id\":") - 1
                + ngx_get_num_size(header->insert_id);
    }

    if (header->affected_rows) {
        size += sizeof(",\"affected_rows\":") - 1
                + ngx_get_num_size(header->affected_rows);
    }

    /* create the buffer */

    pos = ngx_http_rds_json_request_mem(r, ctx, size);
    if (pos == NULL) {
        return NGX_ERROR;
    }

    last = pos;

    /* fill up the buffer */

    *last++ = '{';

    if (conf->success.len) {
        last = ngx_copy(last, conf->success.data, conf->success.len);

        if (header->std_errcode == 0) {
            last = ngx_copy_literal(last, ":true,");

        } else {
            last = ngx_copy_literal(last, ":false,");
        }
    }

    if (conf->user_props) {
        for (i = 0; i < conf->user_props->nelts; i++) {
            last = ngx_copy(last, prop[i].key.data, prop[i].key.len);
            *last++ = ':';
            *last++ = '"';

            if (escapes[i] == 0) {
                last = ngx_copy(last, values[i].data, values[i].len);

            } else {
                last = (u_char *)
                        ngx_http_rds_json_escape_json_str(last,
                                                          values[i].data,
                                                          values[i].len);
            }

            *last++ = '"';
            *last++ = ',';
        }
    }

    last = ngx_copy(last, conf->errcode_key.data, conf->errcode_key.len);
    *last++ = ':';

    last = ngx_snprintf(last, NGX_UINT16_LEN, "%uD",
                        (uint32_t) header->std_errcode);

    if (header->errstr.len) {
        *last++ = ',';
        last = ngx_copy(last, conf->errstr_key.data, conf->errstr_key.len);
        *last++ = ':';
        *last++ = '"';

        if (escape == 0) {
            last = ngx_copy(last, header->errstr.data, header->errstr.len);

        } else {
            last = (u_char *)
                    ngx_http_rds_json_escape_json_str(last,
                                                      header->errstr.data,
                                                      header->errstr.len);
        }

        *last++ = '"';
    }

    if (header->insert_id) {
        last = ngx_copy_literal(last, ",\"insert_id\":");
        last = ngx_snprintf(last, NGX_UINT64_LEN, "%uL", header->insert_id);
    }

    if (header->affected_rows) {
        last = ngx_copy_literal(last, ",\"affected_rows\":");
        last = ngx_snprintf(last, NGX_UINT64_LEN, "%uL",
                            header->affected_rows);
    }

    *last++ = '}';

    if ((size_t) (last - pos) != size) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds_json: output header buffer error: %O != %uz",
                      (off_t) (last - pos), size);

        return NGX_ERROR;
    }

    if (r == r->main) {
        last_buf = 1;
    }

    ctx->seen_stream_end = 1;

    return ngx_http_rds_json_submit_mem(r, ctx, size, (unsigned) last_buf);
}


ngx_int_t
ngx_http_rds_json_output_props(ngx_http_request_t *r,
    ngx_http_rds_json_ctx_t *ctx, ngx_http_rds_json_loc_conf_t *conf)
{
    size_t                   size;
    u_char                  *pos, *last;
    ngx_uint_t               i;
    ngx_str_t               *values = NULL;
    uintptr_t               *escapes = NULL;

    ngx_http_rds_json_property_t        *prop = NULL;

    size = sizeof("{:") - 1 + conf->root.len;

    if (conf->success.len) {
        size += sizeof(",:true") - 1 + conf->success.len;
    }

    if (conf->user_props) {
        values = ngx_pnalloc(r->pool,
                             conf->user_props->nelts * (sizeof(ngx_str_t)
                             + sizeof(uintptr_t)));

        if (values == NULL) {
            return NGX_ERROR;
        }

        escapes = (uintptr_t *) ((u_char *) values
                  + conf->user_props->nelts * sizeof(ngx_str_t));

        prop = conf->user_props->elts;
        for (i = 0; i < conf->user_props->nelts; i++) {
            if (ngx_http_complex_value(r, &prop[i].value, &values[i])
                != NGX_OK)
            {
                return NGX_ERROR;
            }

            escapes[i] = ngx_http_rds_json_escape_json_str(NULL,
                                                           values[i].data,
                                                           values[i].len);

            size += sizeof(":\"\",") - 1 + prop[i].key.len + values[i].len
                    + escapes[i];
        }
    }

    pos = ngx_http_rds_json_request_mem(r, ctx, size);
    if (pos == NULL) {
        return NGX_ERROR;
    }

    last = pos;

    *last++ = '{';

    if (conf->success.len) {
        last = ngx_copy(last, conf->success.data, conf->success.len);
        last = ngx_copy_literal(last, ":true,");
    }

    if (conf->user_props) {
        for (i = 0; i < conf->user_props->nelts; i++) {
            last = ngx_copy(last, prop[i].key.data, prop[i].key.len);
            *last++ = ':';
            *last++ = '"';

            if (escapes[i] == 0) {
                last = ngx_copy(last, values[i].data, values[i].len);

            } else {
                last = (u_char *)
                        ngx_http_rds_json_escape_json_str(last,
                                                          values[i].data,
                                                          values[i].len);
            }

            *last++ = '"';
            *last++ = ',';
        }
    }

    last = ngx_copy(last, conf->root.data, conf->root.len);
    *last++ = ':';

    if (last - pos != (ssize_t) size) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds_json: output props begin: buffer error: %O != %uz",
                      (off_t) (last - pos), size);

        return NGX_ERROR;
    }

    return ngx_http_rds_json_submit_mem(r, ctx, size, 0);
}


ngx_int_t
ngx_http_rds_json_output_cols(ngx_http_request_t *r,
    ngx_http_rds_json_ctx_t *ctx)
{
    ngx_uint_t                           i;
    ngx_http_rds_column_t               *col;
    size_t                               size;
    u_char                              *pos, *last;
    uintptr_t                            key_escape = 0;

    size = sizeof("[]") - 1;

    for (i = 0; i < ctx->col_count; i++) {
        col = &ctx->cols[i];
        key_escape = ngx_http_rds_json_escape_json_str(NULL, col->name.data,
                                                       col->name.len);

        dd("key_escape: %d", (int) key_escape);

        size += col->name.len + key_escape + sizeof("\"\"") - 1;

        if (i != ctx->col_count - 1) {
            /* not the last column */
            size += sizeof(",") - 1;
        }
    }

    ctx->generated_col_names = 1;

    pos = ngx_http_rds_json_request_mem(r, ctx, size);
    if (pos == NULL) {
        return NGX_ERROR;
    }

    last = pos;

    *last++ = '[';

    for (i = 0; i < ctx->col_count; i++) {
        col = &ctx->cols[i];

        *last++ = '"';

        last = (u_char *) ngx_http_rds_json_escape_json_str(last,
                                                            col->name.data,
                                                            col->name.len);

        *last++ = '"';

        if (i != ctx->col_count - 1) {
            *last++ = ',';
        }
    }

    *last++ = ']';

    return ngx_http_rds_json_submit_mem(r, ctx, size, 0);
}


ngx_int_t
ngx_http_rds_json_output_field(ngx_http_request_t *r,
    ngx_http_rds_json_ctx_t *ctx, u_char *data, size_t len, int is_null)
{
    u_char                              *pos, *last;
    ngx_http_rds_column_t               *col;
    ngx_flag_t                           bool_val = 0;
    size_t                               size;
    uintptr_t                            key_escape = 0;
    uintptr_t                            val_escape = 0;
    u_char                              *p;
    ngx_uint_t                           i;
    ngx_http_rds_json_loc_conf_t        *conf;
    ngx_uint_t                           format;

    conf = ngx_http_get_module_loc_conf(r, ngx_http_rds_json_filter_module);

    format = conf->format;

    dd("reading row %llu, col %d, len %d",
       (unsigned long long) ctx->row,
       (int) ctx->cur_col, (int) len);

    /* calculate the buffer size */

    if (format == json_format_normal) {
        if (ctx->cur_col == 0) {
            dd("first column");
            if (ctx->row == 1) {
                dd("first column, first row");
                size = sizeof("{\"") - 1;

            } else {
                size = sizeof(",{\"") - 1;
            }

        } else {
            size = sizeof(",\"") - 1;
        }

    } else if (format == json_format_compact) {
        if (ctx->cur_col == 0) {
            dd("first column");
            size = sizeof(",[") - 1;

        } else {
            size = sizeof(",") - 1;
        }

    } else {
        return NGX_ERROR;
    }

    col = &ctx->cols[ctx->cur_col];

    if (format == json_format_normal) {
        key_escape = ngx_http_rds_json_escape_json_str(NULL, col->name.data,
                                                       col->name.len);

        dd("key_escape: %d", (int) key_escape);

        size += col->name.len + key_escape + sizeof("\":") - 1;

    } else if (format == json_format_compact) {
        /* do nothing */

    } else {
        return NGX_ERROR;
    }

    if (len == 0 && ctx->field_data_rest > 0) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds_json: at least one octet should go with the "
                      "field size in one buf");

        return NGX_ERROR;
    }

    if (is_null) {
        size += sizeof("null") - 1;

    } else if (len == 0) {
        dd("empty string value found");

        size += sizeof("\"\"") - 1;

    } else {
        switch (col->std_type & 0xc000) {
        case rds_rough_col_type_float:
            dd("float field found");
            /* TODO check validity of floating numbers */
            size += len;
            break;

        case rds_rough_col_type_int:
            dd("int field found");

            for (p = data, i = 0; i < len; i++, p++) {
                if (i == 0 && *p == '-') {
                    continue;
                }

                if (*p < '0' || *p > '9') {
                    ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                                  "rds_json: invalid integral field value: "
                                  "\"%*s\"", len, data);
                    return NGX_ERROR;
                }
            }

            size += len;
            break;

        case rds_rough_col_type_bool:
            dd("bool field found");

            if (*data == '0' || *data == 'f' || *data == 'F'
                || *data == '1' || *data == 't' || *data == 'T' )
            {
                if (len != 1 || ctx->field_data_rest) {
                    ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                                  "rds_json: invalid boolean field value "
                                  "leading by \"%*s\"", len, data);
                    return NGX_ERROR;
                }

                if (*data == '0' || *data == 'f' || *data == 'F') {
                    bool_val = 0;
                    size += sizeof("false") - 1;

                } else {
                    bool_val = 1;
                    size += sizeof("true") - 1;
                }

            } else {
                ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                              "rds_json: output field: invalid boolean value "
                              "leading by \"%*s\"", len, data);
                return NGX_ERROR;
            }

            break;

        default:
            dd("string field found");

            /* TODO: further inspect array types and key-value types */

            val_escape = ngx_http_rds_json_escape_json_str(NULL, data, len);

            size += sizeof("\"") - 1 + len + val_escape;

            if (ctx->field_data_rest == 0) {
                size += sizeof("\"") - 1;
            }
        }
    }

    if (ctx->field_data_rest == 0
        && ctx->cur_col == ctx->col_count - 1)
    {
        /* last column in the row */
        if (format == json_format_normal) {
            size += sizeof("}") - 1;

        } else if (format == json_format_compact) {
            size += sizeof("]") - 1;

        } else {
            return NGX_ERROR;
        }
    }

    /* allocate the buffer */

    pos = ngx_http_rds_json_request_mem(r, ctx, size);
    if (pos == NULL) {
        return NGX_ERROR;
    }

    last = pos;

    /* fill up the buffer */

    if (format == json_format_normal) {
        if (ctx->cur_col == 0) {
            dd("first column");
            if (ctx->row == 1) {
                dd("first column, first row");
                *last++ = '{';

            } else {
                *last++ = ','; *last++ = '{';
            }

        } else {
            *last++ = ',';
        }

        *last++ = '"';

        if (key_escape == 0) {
            last = ngx_copy(last, col->name.data, col->name.len);

        } else {
            last = (u_char *)
                    ngx_http_rds_json_escape_json_str(last, col->name.data,
                                                      col->name.len);
        }

        *last++ = '"'; *last++ = ':';

    } else if (format == json_format_compact) {

        if (ctx->cur_col == 0) {
            dd("first column");
            *last++ = ',';
            *last++ = '[';

        } else {
            *last++ = ',';
        }

    } else {
        return NGX_ERROR;
    }

    if (is_null) {
        dd("copy null value over");
        last = ngx_copy_literal(last, "null");

    } else if (len == 0) {
        dd("copy emtpy string over");
        last = ngx_copy_literal(last, "\"\"");

    } else {
        switch (col->std_type & 0xc000) {
        case rds_rough_col_type_int:
        case rds_rough_col_type_float:
            last = ngx_copy(last, data, len);

            dd("copy over int/float value: %.*s", (int) len, data);

            break;

        case rds_rough_col_type_bool:
            if (bool_val) {
                last = ngx_copy_literal(last, "true");

            } else {
                last = ngx_copy_literal(last, "false");
            }

            break;

        default:
            /* string */
            *last++ = '"';

            if (val_escape == 0) {
                last = ngx_copy(last, data, len);

            } else {
                dd("field: string value escape non-zero: %d",
                   (int) val_escape);

#if DDEBUG
                p = last;
#endif

                last = (u_char *)
                        ngx_http_rds_json_escape_json_str(last, data, len);

#if DDEBUG
                dd("escaped value \"%.*s\" (len %d, escape %d, escape2 %d)",
                   (int) (len + val_escape),
                   p, (int) (len + val_escape),
                   (int) val_escape,
                   (int) ((last - p) - len));
#endif
            }

            if (ctx->field_data_rest == 0) {
                *last++ = '"';
            }

            break;
        }
    }

    if (ctx->field_data_rest == 0 && ctx->cur_col == ctx->col_count - 1) {
        dd("last column in the row");
        if (format == json_format_normal) {
            *last++ = '}';

        } else if (format == json_format_compact) {
            *last++ = ']';

        } else {
            return NGX_ERROR;
        }
    }

    if ((size_t) (last - pos) != size) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds_json: output field: buffer error (%d left)",
                      (int) size - (last - pos));

        return NGX_ERROR;
    }

    return ngx_http_rds_json_submit_mem(r, ctx, size, 0);
}


ngx_int_t
ngx_http_rds_json_output_more_field_data(ngx_http_request_t *r,
    ngx_http_rds_json_ctx_t *ctx, u_char *data, size_t len)
{
    u_char                          *pos, *last;
    size_t                           size = 0;
    ngx_http_rds_column_t           *col;
    uintptr_t                        escape = 0;
    u_char                          *p;
    ngx_uint_t                       i;
    ngx_http_rds_json_loc_conf_t    *conf;

    conf = ngx_http_get_module_loc_conf(r, ngx_http_rds_json_filter_module);

    /* calculate the buffer size */

    col = &ctx->cols[ctx->cur_col];

    switch (col->std_type & 0xc000) {
    case rds_rough_col_type_int:
        for (p = data, i = 0; i < len; i++, p++) {
            if (*p < '0' || *p > '9') {
                ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                              "rds_json: invalid integral field value: \"%*s\"",
                              len, data);
                return NGX_ERROR;
            }
        }

        size += len;
        break;

    case rds_rough_col_type_float:
        /* TODO: check validity of floating-point field values */
        size += len;
        break;

    case rds_rough_col_type_bool:
        break;

    default:
        /* string */
        escape = ngx_http_rds_json_escape_json_str(NULL, data, len);
        size = len + escape;

        if (ctx->field_data_rest == 0) {
            size += sizeof("\"") - 1;
        }

        break;
    }

    if (ctx->field_data_rest == 0 && ctx->cur_col == ctx->col_count - 1) {
        /* last column in the row */
        if (conf->format == json_format_normal) {
            size += sizeof("}") - 1;

        } else if (conf->format == json_format_compact) {
            size += sizeof("]") - 1;

        } else {
            return NGX_ERROR;
        }
    }

    /* allocate the buffer */

    pos = ngx_http_rds_json_request_mem(r, ctx, size);
    if (pos == NULL) {
        return NGX_ERROR;
    }

    last = pos;

    /* fill up the buffer */

    switch (col->std_type & 0xc000) {
    case rds_rough_col_type_int:
    case rds_rough_col_type_float:
        last = ngx_copy(last, data, len);
        break;

    case rds_rough_col_type_bool:
        /* no op */
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

            last = (u_char *) ngx_http_rds_json_escape_json_str(last,
                    data, len);

#if DDEBUG
            dd("escaped value \"%.*s\" (len %d, escape %d, escape2 %d)",
               (int) (len + escape),
               p, (int) (len + escape),
               (int) escape,
               (int) ((last - p) - len));
#endif
        }

        if (ctx->field_data_rest == 0) {
            *last++ = '"';
        }

        break;
    } /* switch */

    if (ctx->field_data_rest == 0 && ctx->cur_col == ctx->col_count - 1) {
        /* last column in the row */
        if (conf->format == json_format_normal) {
            *last++ = '}';

        } else if (conf->format == json_format_compact) {
            *last++ = ']';

        } else {
            return NGX_ERROR;
        }
    }

    if ((size_t) (last - pos) != size) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds_json: output more field data: buffer error "
                      "(%d left)", (int) (size - (last - pos)));
        return NGX_ERROR;
    }

    return ngx_http_rds_json_submit_mem(r, ctx, size, 0);
}


static u_char *
ngx_http_rds_json_request_mem(ngx_http_request_t *r,
    ngx_http_rds_json_ctx_t *ctx, size_t len)
{
    ngx_int_t                rc;
    u_char                  *p;

    rc = ngx_http_rds_json_get_buf(r, ctx);

    if (rc != NGX_OK) {
        return NULL;
    }

    if (ctx->avail_out < len) {
        p = ngx_http_rds_json_get_postponed(r, ctx, len);
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
ngx_http_rds_json_get_buf(ngx_http_request_t *r,
    ngx_http_rds_json_ctx_t *ctx)
{
    ngx_http_rds_json_loc_conf_t         *conf;

    dd("MEM enter");

    if (ctx->avail_out) {
        return NGX_OK;
    }

    conf = ngx_http_get_module_loc_conf(r, ngx_http_rds_json_filter_module);

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

        ctx->out_buf->tag = (ngx_buf_tag_t) &ngx_http_rds_json_filter_module;
        ctx->out_buf->recycled = 1;
    }

    ctx->avail_out = conf->buf_size;

    return NGX_OK;
}


static u_char *
ngx_http_rds_json_get_postponed(ngx_http_request_t *r,
    ngx_http_rds_json_ctx_t *ctx, size_t len)
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
ngx_http_rds_json_submit_mem(ngx_http_request_t *r,
    ngx_http_rds_json_ctx_t *ctx, size_t len, unsigned last_buf)
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

            rc = ngx_http_rds_json_get_buf(r, ctx);
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
