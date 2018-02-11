
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_srcache_headers.h"


static ngx_int_t ngx_http_srcache_process_content_type(ngx_http_request_t *r,
    ngx_table_elt_t *h, ngx_uint_t offset);
static ngx_int_t ngx_http_srcache_process_content_length(ngx_http_request_t *r,
    ngx_table_elt_t *h, ngx_uint_t offset);
static ngx_int_t ngx_http_srcache_process_last_modified(ngx_http_request_t *r,
    ngx_table_elt_t *h, ngx_uint_t offset);
static ngx_int_t
    ngx_http_srcache_process_multi_header_lines(ngx_http_request_t *r,
    ngx_table_elt_t *h, ngx_uint_t offset);
static ngx_int_t ngx_http_srcache_process_allow_ranges(ngx_http_request_t *r,
    ngx_table_elt_t *h, ngx_uint_t offset);
static ngx_int_t ngx_http_srcache_process_accept_ranges(ngx_http_request_t *r,
    ngx_table_elt_t *h, ngx_uint_t offset);
static ngx_int_t ngx_http_srcache_ignore_header_line(ngx_http_request_t *r,
    ngx_table_elt_t *h, ngx_uint_t offset);

#if (NGX_HTTP_GZIP)
static ngx_int_t
    ngx_http_srcache_process_content_encoding(ngx_http_request_t *r,
    ngx_table_elt_t *h, ngx_uint_t offset);
#endif


ngx_http_srcache_header_t  ngx_http_srcache_headers_in[] = {

    { ngx_string("Content-Type"),
                 ngx_http_srcache_process_content_type, 0 },

    { ngx_string("Content-Length"),
                 ngx_http_srcache_process_content_length, 0 },

    { ngx_string("Date"),
                 ngx_http_srcache_process_header_line,
                 offsetof(ngx_http_headers_out_t, date) },

    { ngx_string("Last-Modified"),
                 ngx_http_srcache_process_last_modified, 0 },

    { ngx_string("ETag"),
                 ngx_http_srcache_process_header_line,
                 offsetof(ngx_http_headers_out_t, etag) },

    { ngx_string("Server"),
                 ngx_http_srcache_process_header_line,
                 offsetof(ngx_http_headers_out_t, server) },

    { ngx_string("WWW-Authenticate"),
                 ngx_http_srcache_process_header_line,
                 offsetof(ngx_http_headers_out_t, www_authenticate) },

    { ngx_string("Location"),
                 ngx_http_srcache_process_header_line,
                 offsetof(ngx_http_headers_out_t, location) },

    { ngx_string("Refresh"),
                 ngx_http_srcache_process_header_line,
                 offsetof(ngx_http_headers_out_t, refresh) },

    { ngx_string("Cache-Control"),
                 ngx_http_srcache_process_multi_header_lines,
                 offsetof(ngx_http_headers_out_t, cache_control) },

    { ngx_string("Expires"),
                 ngx_http_srcache_process_header_line,
                 offsetof(ngx_http_headers_out_t, expires) },

    { ngx_string("X-SRCache-Allow-Ranges"),
                 ngx_http_srcache_process_allow_ranges,
                 offsetof(ngx_http_headers_out_t, accept_ranges) },

    { ngx_string("Accept-Ranges"),
                 ngx_http_srcache_process_accept_ranges,
                 offsetof(ngx_http_headers_out_t, accept_ranges) },

    { ngx_string("Connection"),
                 ngx_http_srcache_ignore_header_line, 0 },

    { ngx_string("Keep-Alive"),
                 ngx_http_srcache_ignore_header_line, 0 },

#if (NGX_HTTP_GZIP)
    { ngx_string("Content-Encoding"),
                 ngx_http_srcache_process_content_encoding,
                 offsetof(ngx_http_headers_out_t, content_encoding) },
#endif

    { ngx_null_string, NULL, 0 }
};


ngx_int_t
ngx_http_srcache_process_header_line(ngx_http_request_t *r, ngx_table_elt_t *h,
    ngx_uint_t offset)
{
    ngx_table_elt_t  *ho, **ph;

    ho = ngx_list_push(&r->headers_out.headers);
    if (ho == NULL) {
        return NGX_ERROR;
    }

    *ho = *h;

    if (offset) {
        ph = (ngx_table_elt_t **) ((char *) &r->headers_out + offset);
        *ph = ho;
    }

    return NGX_OK;
}


static ngx_int_t
ngx_http_srcache_process_content_type(ngx_http_request_t *r, ngx_table_elt_t *h,
    ngx_uint_t offset)
{
    u_char  *p, *last;

    r->headers_out.content_type_len = h->value.len;
    r->headers_out.content_type = h->value;
    r->headers_out.content_type_lowcase = NULL;

    for (p = h->value.data; *p; p++) {

        if (*p != ';') {
            continue;
        }

        last = p;

        while (*++p == ' ') { /* void */ }

        if (*p == '\0') {
            return NGX_OK;
        }

        if (ngx_strncasecmp(p, (u_char *) "charset=", 8) != 0) {
            continue;
        }

        p += 8;

        r->headers_out.content_type_len = last - h->value.data;

        if (*p == '"') {
            p++;
        }

        last = h->value.data + h->value.len;

        if (*(last - 1) == '"') {
            last--;
        }

        r->headers_out.charset.len = last - p;
        r->headers_out.charset.data = p;

        return NGX_OK;
    }

    return NGX_OK;
}


static ngx_int_t
ngx_http_srcache_process_content_length(ngx_http_request_t *r,
    ngx_table_elt_t *h, ngx_uint_t offset)
{
    ngx_table_elt_t  *ho;

    ho = ngx_list_push(&r->headers_out.headers);
    if (ho == NULL) {
        return NGX_ERROR;
    }

    *ho = *h;

    r->headers_out.content_length = ho;
    r->headers_out.content_length_n = ngx_atoof(h->value.data, h->value.len);

    return NGX_OK;
}


static ngx_int_t
ngx_http_srcache_process_last_modified(ngx_http_request_t *r,
    ngx_table_elt_t *h, ngx_uint_t offset)
{
#if 1
    ngx_table_elt_t  *ho;

    ho = ngx_list_push(&r->headers_out.headers);
    if (ho == NULL) {
        return NGX_ERROR;
    }

    *ho = *h;

    r->headers_out.last_modified = ho;
#endif

    r->headers_out.last_modified_time = ngx_http_parse_time(h->value.data,
                                                            h->value.len);

    dd("setting last-modified-time: %d",
         (int) r->headers_out.last_modified_time);

    return NGX_OK;
}


static ngx_int_t
ngx_http_srcache_process_multi_header_lines(ngx_http_request_t *r,
    ngx_table_elt_t *h, ngx_uint_t offset)
{
    ngx_array_t      *pa;
    ngx_table_elt_t  *ho, **ph;

    pa = (ngx_array_t *) ((char *) &r->headers_out + offset);

    if (pa->elts == NULL) {
        if (ngx_array_init(pa, r->pool, 2, sizeof(ngx_table_elt_t *)) != NGX_OK)
        {
            return NGX_ERROR;
        }
    }

    ph = ngx_array_push(pa);
    if (ph == NULL) {
        return NGX_ERROR;
    }

    ho = ngx_list_push(&r->headers_out.headers);
    if (ho == NULL) {
        return NGX_ERROR;
    }

    *ho = *h;
    *ph = ho;

    return NGX_OK;
}


static ngx_int_t
ngx_http_srcache_process_accept_ranges(ngx_http_request_t *r,
    ngx_table_elt_t *h, ngx_uint_t offset)
{
    ngx_table_elt_t  *ho;

    ho = ngx_list_push(&r->headers_out.headers);
    if (ho == NULL) {
        return NGX_ERROR;
    }

    *ho = *h;

    r->headers_out.accept_ranges = ho;

    return NGX_OK;
}


static ngx_int_t
ngx_http_srcache_process_allow_ranges(ngx_http_request_t *r,
    ngx_table_elt_t *h, ngx_uint_t offset)
{
    r->allow_ranges = 1;

    return NGX_OK;
}


static ngx_int_t
ngx_http_srcache_ignore_header_line(ngx_http_request_t *r, ngx_table_elt_t *h,
    ngx_uint_t offset)
{
    return NGX_OK;
}


#if (NGX_HTTP_GZIP)

static ngx_int_t
ngx_http_srcache_process_content_encoding(ngx_http_request_t *r,
    ngx_table_elt_t *h, ngx_uint_t offset)
{
    ngx_table_elt_t  *ho;

    ho = ngx_list_push(&r->headers_out.headers);
    if (ho == NULL) {
        return NGX_ERROR;
    }

    *ho = *h;

    r->headers_out.content_encoding = ho;

    return NGX_OK;
}

#endif

/* vi:set ft=c ts=4 sw=4 et fdm=marker: */
