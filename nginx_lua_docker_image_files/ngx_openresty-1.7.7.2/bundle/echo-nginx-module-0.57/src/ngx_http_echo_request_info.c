
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_echo_request_info.h"
#include "ngx_http_echo_util.h"
#include "ngx_http_echo_handler.h"

#include <nginx.h>


static void ngx_http_echo_post_read_request_body(ngx_http_request_t *r);


ngx_int_t
ngx_http_echo_exec_echo_read_request_body(ngx_http_request_t* r,
    ngx_http_echo_ctx_t *ctx)
{
    return ngx_http_read_client_request_body(r,
                                        ngx_http_echo_post_read_request_body);
}


static void
ngx_http_echo_post_read_request_body(ngx_http_request_t *r)
{
    ngx_http_echo_ctx_t         *ctx;

    ctx = ngx_http_get_module_ctx(r, ngx_http_echo_module);

    dd("wait read request body %d", (int) ctx->wait_read_request_body);

    if (ctx->wait_read_request_body) {
        ctx->waiting = 0;
        ctx->done = 1;

        r->write_event_handler = ngx_http_echo_wev_handler;

        ngx_http_echo_wev_handler(r);
    }
}


/* this function's implementation is borrowed from nginx 0.8.20
 * and modified a bit to work with subrequests.
 * Copyrighted (C) by Igor Sysoev */
ngx_int_t
ngx_http_echo_request_method_variable(ngx_http_request_t *r,
    ngx_http_variable_value_t *v, uintptr_t data)
{
    if (r->method_name.data) {
        v->len = r->method_name.len;
        v->valid = 1;
        v->no_cacheable = 0;
        v->not_found = 0;
        v->data = r->method_name.data;

    } else {
        v->not_found = 1;
    }

    return NGX_OK;
}


/* this function's implementation is borrowed from nginx 0.8.20
 * and modified a bit to work with subrequests.
 * Copyrighted (C) by Igor Sysoev */
ngx_int_t
ngx_http_echo_client_request_method_variable(ngx_http_request_t *r,
    ngx_http_variable_value_t *v, uintptr_t data)
{
    if (r->main->method_name.data) {
        v->len = r->main->method_name.len;
        v->valid = 1;
        v->no_cacheable = 0;
        v->not_found = 0;
        v->data = r->main->method_name.data;

    } else {
        v->not_found = 1;
    }

    return NGX_OK;
}


/* this function's implementation is borrowed from nginx 0.8.20
 * and modified a bit to work with subrequests.
 * Copyrighted (C) by Igor Sysoev */
ngx_int_t
ngx_http_echo_request_body_variable(ngx_http_request_t *r,
    ngx_http_variable_value_t *v, uintptr_t data)
{
    u_char       *p;
    size_t        len;
    ngx_buf_t    *b;
    ngx_chain_t  *cl;
    ngx_chain_t  *in;

    if (r->request_body == NULL
        || r->request_body->bufs == NULL
        || r->request_body->temp_file)
    {
        v->not_found = 1;

        return NGX_OK;
    }

    in = r->request_body->bufs;

    len = 0;
    for (cl = in; cl; cl = cl->next) {
        b = cl->buf;

        if (!ngx_buf_in_memory(b)) {
            if (b->in_file) {
                ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                               "variable echo_request_body sees in-file only "
                               "buffers and discard the whole body data");

                v->not_found = 1;

                return NGX_OK;
            }

        } else {
            len += b->last - b->pos;
        }
    }

    p = ngx_pnalloc(r->pool, len);
    if (p == NULL) {
        return NGX_ERROR;
    }

    v->data = p;

    for (cl = in; cl; cl = cl->next) {
        b = cl->buf;

        if (ngx_buf_in_memory(b)) {
            p = ngx_copy(p, b->pos, b->last - b->pos);
        }
    }

    if (p - v->data != (ssize_t) len) {
        ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                       "variable echo_request_body: buffer error");

        v->not_found = 1;

        return NGX_OK;
    }

    v->len = len;
    v->valid = 1;
    v->no_cacheable = 0;
    v->not_found = 0;

    return NGX_OK;
}


ngx_int_t
ngx_http_echo_client_request_headers_variable(ngx_http_request_t *r,
    ngx_http_variable_value_t *v, uintptr_t data)
{
    int                          line_break_len;
    size_t                       size;
    u_char                      *p, *last, *pos;
    ngx_int_t                    i, j;
    ngx_buf_t                   *b, *first = NULL;
    unsigned                     found;
    ngx_connection_t            *c;
    ngx_http_request_t          *mr;
    ngx_http_connection_t       *hc;

    mr = r->main;
    hc = r->main->http_connection;
    c = mr->connection;

    size = 0;
    b = c->buffer;

    if (mr->request_line.data[mr->request_line.len] == CR) {
        line_break_len = 2;

    } else {
        line_break_len = 1;
    }

    if (mr->request_line.data >= b->start
        && mr->request_line.data + mr->request_line.len + line_break_len
           <= b->pos)
    {
        first = b;
        size += b->pos - mr->request_line.data;
    }

    if (hc->nbusy) {
        b = NULL;
        for (i = 0; i < hc->nbusy; i++) {
            b = hc->busy[i];

            if (first == NULL) {
                if (mr->request_line.data >= b->pos
                    || mr->request_line.data + mr->request_line.len
                       + line_break_len <= b->start)
                {
                    continue;
                }

                dd("found first at %d", (int) i);
                first = b;
            }

            size += b->pos - b->start;
        }
    }


    size++;  /* plus the null terminator, as required by the later
                ngx_strstr() call */

    v->data = ngx_palloc(r->pool, size);
    if (v->data == NULL) {
        return NGX_ERROR;
    }

    last = v->data;

    b = c->buffer;
    found = 0;

    if (first == b) {
        found = 1;
        pos = b->pos;

        last = ngx_copy(v->data, mr->request_line.data,
                        pos - mr->request_line.data);

        if (b != mr->header_in) {
            /* skip truncated header entries (if any) */
            while (last > v->data && last[-1] != LF) {
                last--;
            }
        }

        i = 0;
        for (p = v->data; p != last; p++) {
            if (*p == '\0') {
                i++;
                if (p + 1 != last && *(p + 1) == LF) {
                    *p = CR;

                } else if (i % 2 == 1) {
                    *p = ':';

                } else {
                    *p = LF;
                }
            }
        }
    }

    if (hc->nbusy) {
        for (i = 0; i < hc->nbusy; i++) {
            b = hc->busy[i];

            if (!found) {
                if (b != first) {
                    continue;
                }

                dd("found first");
                found = 1;
            }

            p = last;

            pos = b->pos;

            if (b == first) {
                dd("request line: %.*s", (int) mr->request_line.len,
                   mr->request_line.data);

                last = ngx_copy(last,
                                mr->request_line.data,
                                pos - mr->request_line.data);

            } else {
                last = ngx_copy(last, b->start, pos - b->start);
            }

#if 1
            /* skip truncated header entries (if any) */
            while (last > p && last[-1] != LF) {
                last--;
            }
#endif

            j = 0;
            for (; p != last; p++) {
                if (*p == '\0') {
                    j++;
                    if (p + 1 == last) {
                        /* XXX this should not happen */
                        dd("found string end!!");

                    } else if (*(p + 1) == LF) {
                        *p = CR;

                    } else if (j % 2 == 1) {
                        *p = ':';

                    } else {
                        *p = LF;
                    }
                }
            }

            if (b == mr->header_in) {
                break;
            }
        }
    }

    *last++ = '\0';

    if (last - v->data > (ssize_t) size) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "buffer error when evaluating "
                      "$echo_client__request_headers: \"%V\"",
                      (ngx_int_t) (last - v->data - size));

        return NGX_ERROR;
    }

    /* strip the leading part (if any) of the request body in our header.
     * the first part of the request body could slip in because nginx core's
     * ngx_http_request_body_length_filter and etc can move r->header_in->pos
     * in case that some of the body data has been preread into r->header_in.
     */

    if ((p = (u_char *) ngx_strstr(v->data, CRLF CRLF)) != NULL) {
        last = p + sizeof(CRLF CRLF) - 1;

    } else if ((p = (u_char *) ngx_strstr(v->data, CRLF "\n")) != NULL) {
        last = p + sizeof(CRLF "\n") - 1;

    } else if ((p = (u_char *) ngx_strstr(v->data, "\n" CRLF)) != NULL) {
        last = p + sizeof("\n" CRLF) - 1;

    } else {
        for (p = last - 1; p - v->data >= 2; p--) {
            if (p[0] == LF && p[-1] == CR) {
                p[-1] = LF;
                last = p + 1;
                break;
            }

            if (p[0] == LF && p[-1] == LF) {
                last = p + 1;
                break;
            }
        }
    }

    v->len = last - v->data;
    v->valid = 1;
    v->no_cacheable = 0;
    v->not_found = 0;

    return NGX_OK;
}


ngx_int_t
ngx_http_echo_cacheable_request_uri_variable(ngx_http_request_t *r,
    ngx_http_variable_value_t *v, uintptr_t data)
{
    if (r->uri.len) {
        v->len = r->uri.len;
        v->valid = 1;
        v->no_cacheable = 0;
        v->not_found = 0;
        v->data = r->uri.data;

    } else {
        v->not_found = 1;
    }

    return NGX_OK;
}


ngx_int_t
ngx_http_echo_request_uri_variable(ngx_http_request_t *r,
    ngx_http_variable_value_t *v, uintptr_t data)
{
    if (r->uri.len) {
        v->len = r->uri.len;
        v->valid = 1;
        v->no_cacheable = 1;
        v->not_found = 0;
        v->data = r->uri.data;

    } else {
        v->not_found = 1;
    }

    return NGX_OK;
}


ngx_int_t
ngx_http_echo_response_status_variable(ngx_http_request_t *r,
    ngx_http_variable_value_t *v, uintptr_t data)
{
    u_char                      *p;

    if (r->headers_out.status) {
        dd("headers out status: %d", (int) r->headers_out.status);

        p = ngx_palloc(r->pool, NGX_INT_T_LEN);
        if (p == NULL) {
            return NGX_ERROR;
        }

        v->len = ngx_sprintf(p, "%ui", r->headers_out.status) - p;
        v->data = p;

        v->valid = 1;
        v->no_cacheable = 1;
        v->not_found = 0;

    } else {
        v->not_found = 1;
    }

    return NGX_OK;
}

/* vi:set ft=c ts=4 sw=4 et fdm=marker: */
