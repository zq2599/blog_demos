#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include <ndk.h>
#include "ngx_http_set_quote_sql.h"


static ngx_int_t ngx_http_pg_utf_escape(ngx_http_request_t *r, ngx_str_t *res);
static ngx_int_t ngx_http_pg_utf_islegal(const unsigned char *s, ngx_int_t len);
static ngx_int_t ngx_http_pg_utf_mblen(const unsigned char *s);


ngx_int_t
ngx_http_set_misc_quote_pgsql_str(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{
    u_char                   *pstr;
    ngx_int_t               length;

    if (v->not_found || v->len ==0) {
        res->data = (u_char *) "''";
        res->len = sizeof("''") - 1;
        return NGX_OK;
    }

    ngx_http_set_misc_quote_sql_str(r, res, v);
    length  = res->len;

    pstr    = ngx_palloc(r->pool, length + 1);
    if (pstr == NULL) {
        return NGX_ERROR;
    }

    *pstr   = 'E';
    memcpy(pstr + 1, res->data, length);
    res->data   = pstr;
    res->len    = length + 1;

    if (ngx_http_pg_utf_islegal(res->data, res->len)) {
       return NGX_OK;
    }

    if (ngx_http_pg_utf_escape(r, res) != NGX_OK) {
        return NGX_ERROR;
    }

    return NGX_OK;
}


static ngx_int_t
ngx_http_pg_utf_mblen(const unsigned char *s)
{
    int len;

    if ((*s & 0x80) == 0)
        len = 1;
    else if ((*s & 0xe0) == 0xc0)
        len = 2;
    else if ((*s & 0xf0) == 0xe0)
        len = 3;
    else if ((*s & 0xf8) == 0xf0)
        len = 4;
#ifdef NOT_USED
    else if ((*s & 0xfc) == 0xf8)
        len = 5;
    else if ((*s & 0xfe) == 0xfc)
        len = 6;
#endif
    else
        len = 1;
    return len;
}


static ngx_int_t
ngx_http_pg_utf_islegal(const unsigned char *s, ngx_int_t len)
{
    ngx_int_t               mblen;
    ngx_int_t               slen;
    u_char                  a;

    slen = len;

    while (slen > 0) {
        mblen = ngx_http_pg_utf_mblen(s);
        if (slen < mblen)
            return 0;

        switch(mblen)
        {
            default:
                return 0;
            case 4:
                a = *(s + 3);
                if (a < 0x80 || a > 0xBF)
                    return 0;
            case 3:
                a = *(s + 2);
                if (a < 0x80 || a > 0xBF)
                    return 0;
            case 2:
                a = *(s + 1);
                switch (*s)
                {
                    case 0xE0:
                        if (a < 0xA0 || a > 0xBF)
                            return 0;
                        break;
                    case 0xED:
                        if (a < 0x80 || a > 0x9F)
                            return 0;
                        break;
                    case 0xF0:
                        if (a < 0x90 || a > 0xBF)
                            return 0;
                        break;
                    case 0xF4:
                        if (a < 0x80 || a > 0x8F)
                            return 0;
                        break;
                    default:
                        if (a < 0x80 || a > 0xBF)
                            return 0;
                        break;
                }
            case 1:
                a = *s;
                if (a >= 0x80 && a < 0xC2)
                    return 0;
                if (a > 0xF4)
                    return 0;
                break;
        }

        s       += mblen;
        slen    -= mblen;
    }

    return 1;
}


static ngx_int_t
ngx_http_pg_utf_escape(ngx_http_request_t *r, ngx_str_t *res)
{
    ngx_str_t               *result;
    ngx_int_t                l, count;
    u_char                  *d, *p, *p1;

    l      = res->len;
    d      = res->data;
    result = res;
    count  = 0;

    while (l-- > 0) {
        if (*d & 0x80) {
            count += 4;
        }
        d++;
        count++;
    }

    d = res->data;
    l = res->len;

    p = ngx_palloc(r->pool, count);
    if (p == NULL) {
        return NGX_ERROR;
    }

    p1  = p;
    while (l-- > 0) {
        if ((*d & 0x80)) {
            *p++ = '\\';
            *p++ = '\\';
            *p++ = (*d >> 6) + '0';
            *p++ = ((*d >> 3) & 07) + '0';
            *p++ = (*d & 07) + '0';
        } else {
            *p++ = *d;
        }
        d++;
    }

    result->len  = count;
    result->data = p1;

    return NGX_OK;
}


ngx_int_t
ngx_http_set_misc_quote_sql_str(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{
    size_t                   len;
    u_char                  *p;
    size_t                   escape;

    if (v->not_found || v->len == 0) {
        res->data   = (u_char *) "''";
        res->len    = sizeof("''") - 1;
        return NGX_OK;
    }

    escape = ngx_http_set_misc_escape_sql_str(NULL, v->data, v->len);

    len = sizeof("''") - 1 + v->len + escape;

    p = ngx_palloc(r->pool, len);
    if (p == NULL) {
        return NGX_ERROR;
    }

    res->data = p;
    res->len = len;

    *p++ = '\'';

    if (escape == 0) {
        p = ngx_copy(p, v->data, v->len);
    } else {
        p = (u_char *) ngx_http_set_misc_escape_sql_str(p, v->data, v->len);
    }

    *p++ = '\'';

    if (p != res->data + res->len) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                "set_quote_sql_str: buffer error");
        return NGX_ERROR;
    }

    return NGX_OK;
}


uintptr_t
ngx_http_set_misc_escape_sql_str(u_char *dst, u_char *src, size_t size)
{
    ngx_uint_t               n;

    if (dst == NULL) {
        /* find the number of chars to be escaped */
        n = 0;
        while (size) {
            /* the highest bit of all the UTF-8 chars
             * is always 1 */
            if ((*src & 0x80) == 0) {
                switch (*src) {
                    case '\0':
                    case '\b':
                    case '\n':
                    case '\r':
                    case '\t':
                    case '\\':
                    case '\'':
                    case '"':
                    case 26: /* \Z */
                        n++;
                        break;
                    default:
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
                case '\0':
                    *dst++ = '\\';
                    *dst++ = '0';
                    break;

                case '\b':
                    *dst++ = '\\';
                    *dst++ = 'b';
                    break;

                case '\n':
                    *dst++ = '\\';
                    *dst++ = 'n';
                    break;

                case '\r':
                    *dst++ = '\\';
                    *dst++ = 'r';
                    break;

                case '\t':
                    *dst++ = '\\';
                    *dst++ = 't';
                    break;

                case '\\':
                    *dst++ = '\\';
                    *dst++ = '\\';
                    break;

                case '\'':
                    *dst++ = '\\';
                    *dst++ = '\'';
                    break;

                case '"':
                    *dst++ = '\\';
                    *dst++ = '"';
                    break;

                case 26:
                    *dst++ = '\\';
                    *dst++ = 'Z';
                    break;

                default:
                    *dst++ = *src;
                    break;
            }
        } else {
            *dst++ = *src;
        }
        src++;
        size--;
    } /* while (size) */

    return (uintptr_t) dst;
}

