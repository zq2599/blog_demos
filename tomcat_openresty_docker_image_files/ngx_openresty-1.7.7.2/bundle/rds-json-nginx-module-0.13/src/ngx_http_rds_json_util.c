
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "resty_dbd_stream.h"
#include "ngx_http_rds_json_util.h"


uintptr_t
ngx_http_rds_json_escape_json_str(u_char *dst, u_char *src, size_t size)
{
    ngx_uint_t                   n;

    static u_char hex[] = "0123456789abcdef";

    if (dst == NULL) {
        /* find the number of characters to be escaped */

        n = 0;

        while (size) {
            /* UTF-8 char has high bit of 1 */
            if ((*src & 0x80) == 0) {
                switch (*src) {
                case '\r':
                case '\n':
                case '\\':
                case '"':
                case '\f':
                case '\b':
                case '\t':
                    n++;
                    break;
                default:
                    if (*src < 32) {
                        n += sizeof("\\u00xx") - 2;
                    }

                    break;
                }
            }

            src++;
            size--;
        }

        return (uintptr_t) n;
    }

    while (size) {
        if ((*src & 0x80) == 0) {
            switch (*src) {
            case '\r':
                *dst++ = '\\';
                *dst++ = 'r';
                break;

            case '\n':
                *dst++ = '\\';
                *dst++ = 'n';
                break;

            case '\\':
                *dst++ = '\\';
                *dst++ = '\\';
                break;

            case '"':
                *dst++ = '\\';
                *dst++ = '"';
                break;

            case '\f':
                *dst++ = '\\';
                *dst++ = 'f';
                break;

            case '\b':
                *dst++ = '\\';
                *dst++ = 'b';
                break;

            case '\t':
                *dst++ = '\\';
                *dst++ = 't';
                break;

            default:
                if (*src < 32) { /* control chars */
                    *dst++ = '\\';
                    *dst++ = 'u';
                    *dst++ = '0';
                    *dst++ = '0';
                    *dst++ = hex[*src >> 4];
                    *dst++ = hex[*src & 0x0f];
                } else {
                    *dst++ = *src;
                }
                break;
            } /* switch */

            src++;

        } else {
            *dst++ = *src++;
        }

        size--;
    }

    return (uintptr_t) dst;
}


ngx_int_t
ngx_http_rds_json_test_content_type(ngx_http_request_t *r)
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
ngx_http_rds_json_discard_bufs(ngx_pool_t *pool, ngx_chain_t *in)
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
