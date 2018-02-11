
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "resty_dbd_stream.h"
#include "ngx_http_rds_csv_util.h"


uintptr_t
ngx_http_rds_csv_escape_csv_str(u_char field_sep, u_char *dst, u_char *src,
    size_t size, unsigned *need_quotes)
{
    ngx_uint_t                   n;

    if (dst == NULL) {
        *need_quotes = 0;

        /* find the number of characters to be escaped */

        n = 0;

        while (size) {
            switch (*src) {
                case '"':
                    n++;

                case '\r':
                case '\n':
                    *need_quotes = 1;
                    break;

                default:
                    if (*src == field_sep) {
                        *need_quotes = 1;
                    }
                    break;
            }

            src++;
            size--;
        }

        return (uintptr_t) n;
    }

    while (size) {
        if (*src == '"') {
            *dst++ = '"';
            *dst++ = '"';
            src++;

        } else {
            *dst++ = *src++;
        }

        size--;
    }

    return (uintptr_t) dst;
}


ngx_int_t
ngx_http_rds_csv_test_content_type(ngx_http_request_t *r)
{
    ngx_str_t           *type;

    type = &r->headers_out.content_type;
    if (type->len != rds_content_type_len
        || ngx_strncmp(type->data, rds_content_type, rds_content_type_len)
           != 0)
    {
        return NGX_DECLINED;
    }

    return NGX_OK;
}


void
ngx_http_rds_csv_discard_bufs(ngx_pool_t *pool, ngx_chain_t *in)
{
    ngx_chain_t         *cl;

    for (cl = in; cl; cl = cl->next) {
#if 0
        if (cl->buf->temporary
                && ngx_buf_size(cl->buf) > 0) {
            ngx_pfree(pool, cl->buf->start);
        }
#endif

        cl->buf->pos = cl->buf->last;
    }
}
