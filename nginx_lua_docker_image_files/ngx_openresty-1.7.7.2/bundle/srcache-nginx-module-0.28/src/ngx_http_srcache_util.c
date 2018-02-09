
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_srcache_util.h"
#include "ngx_http_srcache_headers.h"


static ngx_int_t ngx_http_srcache_set_content_length_header(
        ngx_http_request_t *r, off_t len);


static ngx_str_t ngx_http_status_lines[] = {

    ngx_string("200 OK"),
    ngx_string("201 Created"),
    ngx_string("202 Accepted"),
    ngx_null_string,  /* "203 Non-Authoritative Information" */
    ngx_string("204 No Content"),
    ngx_null_string,  /* "205 Reset Content" */
    ngx_string("206 Partial Content"),

    /* ngx_null_string, */  /* "207 Multi-Status" */

#define NGX_HTTP_LAST_LEVEL_200  207
#define NGX_HTTP_LEVEL_200       (NGX_HTTP_LAST_LEVEL_200 - 200)

    /* ngx_null_string, */  /* "300 Multiple Choices" */

    ngx_string("301 Moved Permanently"),
    ngx_string("302 Moved Temporarily"),
    ngx_string("303 See Other"),
    ngx_string("304 Not Modified"),

    /* ngx_null_string, */  /* "305 Use Proxy" */
    /* ngx_null_string, */  /* "306 unused" */
    /* ngx_null_string, */  /* "307 Temporary Redirect" */

#define NGX_HTTP_LAST_LEVEL_300  305
#define NGX_HTTP_LEVEL_300       (NGX_HTTP_LAST_LEVEL_300 - 301)

    ngx_string("400 Bad Request"),
    ngx_string("401 Unauthorized"),
    ngx_string("402 Payment Required"),
    ngx_string("403 Forbidden"),
    ngx_string("404 Not Found"),
    ngx_string("405 Not Allowed"),
    ngx_string("406 Not Acceptable"),
    ngx_null_string,  /* "407 Proxy Authentication Required" */
    ngx_string("408 Request Time-out"),
    ngx_string("409 Conflict"),
    ngx_string("410 Gone"),
    ngx_string("411 Length Required"),
    ngx_string("412 Precondition Failed"),
    ngx_string("413 Request Entity Too Large"),
    ngx_null_string,  /* "414 Request-URI Too Large", but we never send it
                       * because we treat such requests as the HTTP/0.9
                       * requests and send only a body without a header
                       */
    ngx_string("415 Unsupported Media Type"),
    ngx_string("416 Requested Range Not Satisfiable"),

    /* ngx_null_string, */  /* "417 Expectation Failed" */
    /* ngx_null_string, */  /* "418 unused" */
    /* ngx_null_string, */  /* "419 unused" */
    /* ngx_null_string, */  /* "420 unused" */
    /* ngx_null_string, */  /* "421 unused" */
    /* ngx_null_string, */  /* "422 Unprocessable Entity" */
    /* ngx_null_string, */  /* "423 Locked" */
    /* ngx_null_string, */  /* "424 Failed Dependency" */

#define NGX_HTTP_LAST_LEVEL_400  417
#define NGX_HTTP_LEVEL_400       (NGX_HTTP_LAST_LEVEL_400 - 400)

    ngx_string("500 Internal Server Error"),
    ngx_string("501 Method Not Implemented"),
    ngx_string("502 Bad Gateway"),
    ngx_string("503 Service Temporarily Unavailable"),
    ngx_string("504 Gateway Time-out"),

    ngx_null_string,        /* "505 HTTP Version Not Supported" */
    ngx_null_string,        /* "506 Variant Also Negotiates" */
    ngx_string("507 Insufficient Storage"),
    /* ngx_null_string, */  /* "508 unused" */
    /* ngx_null_string, */  /* "509 unused" */
    /* ngx_null_string, */  /* "510 Not Extended" */

#define NGX_HTTP_LAST_LEVEL_500  508

};


ngx_str_t  ngx_http_srcache_content_length_header_key =
        ngx_string("Content-Length");
ngx_str_t  ngx_http_srcache_get_method =
        ngx_http_srcache_method_name("GET");
ngx_str_t  ngx_http_srcache_put_method =
        ngx_http_srcache_method_name("PUT");
ngx_str_t  ngx_http_srcache_post_method =
        ngx_http_srcache_method_name("POST");
ngx_str_t  ngx_http_srcache_head_method =
        ngx_http_srcache_method_name("HEAD");
ngx_str_t  ngx_http_srcache_copy_method =
        ngx_http_srcache_method_name("COPY");
ngx_str_t  ngx_http_srcache_move_method =
        ngx_http_srcache_method_name("MOVE");
ngx_str_t  ngx_http_srcache_lock_method =
        ngx_http_srcache_method_name("LOCK");
ngx_str_t  ngx_http_srcache_mkcol_method =
        ngx_http_srcache_method_name("MKCOL");
ngx_str_t  ngx_http_srcache_trace_method =
        ngx_http_srcache_method_name("TRACE");
ngx_str_t  ngx_http_srcache_delete_method =
        ngx_http_srcache_method_name("DELETE");
ngx_str_t  ngx_http_srcache_unlock_method =
        ngx_http_srcache_method_name("UNLOCK");
ngx_str_t  ngx_http_srcache_options_method =
        ngx_http_srcache_method_name("OPTIONS");
ngx_str_t  ngx_http_srcache_propfind_method =
        ngx_http_srcache_method_name("PROPFIND");
ngx_str_t  ngx_http_srcache_proppatch_method =
        ngx_http_srcache_method_name("PROPPATCH");


void
ngx_http_srcache_discard_bufs(ngx_pool_t *pool, ngx_chain_t *in)
{
    ngx_chain_t         *cl;

    for (cl = in; cl; cl = cl->next) {
        cl->buf->pos = cl->buf->last;
    }
}


ngx_int_t
ngx_http_srcache_parse_method_name(ngx_str_t **method_name_ptr)
{
    const ngx_str_t* method_name = *method_name_ptr;

    switch (method_name->len) {
    case 3:
        if (ngx_http_srcache_strcmp_const(method_name->data, "GET") == 0) {
            *method_name_ptr = &ngx_http_srcache_get_method;
            return NGX_HTTP_GET;
        }

        if (ngx_http_srcache_strcmp_const(method_name->data, "PUT") == 0) {
            *method_name_ptr = &ngx_http_srcache_put_method;
            return NGX_HTTP_PUT;
        }

        return NGX_HTTP_UNKNOWN;

    case 4:
        if (ngx_http_srcache_strcmp_const(method_name->data, "POST") == 0) {
            *method_name_ptr = &ngx_http_srcache_post_method;
            return NGX_HTTP_POST;
        }

        if (ngx_http_srcache_strcmp_const(method_name->data, "HEAD") == 0) {
            *method_name_ptr = &ngx_http_srcache_head_method;
            return NGX_HTTP_HEAD;
        }

        if (ngx_http_srcache_strcmp_const(method_name->data, "COPY") == 0) {
            *method_name_ptr = &ngx_http_srcache_copy_method;
            return NGX_HTTP_COPY;
        }

        if (ngx_http_srcache_strcmp_const(method_name->data, "MOVE") == 0) {
            *method_name_ptr = &ngx_http_srcache_move_method;
            return NGX_HTTP_MOVE;
        }

        if (ngx_http_srcache_strcmp_const(method_name->data, "LOCK") == 0) {
            *method_name_ptr = &ngx_http_srcache_lock_method;
            return NGX_HTTP_LOCK;
        }

        return NGX_HTTP_UNKNOWN;

    case 5:
        if (ngx_http_srcache_strcmp_const(method_name->data, "MKCOL") == 0) {
            *method_name_ptr = &ngx_http_srcache_mkcol_method;
            return NGX_HTTP_MKCOL;
        }

        if (ngx_http_srcache_strcmp_const(method_name->data, "TRACE") == 0) {
            *method_name_ptr = &ngx_http_srcache_trace_method;
            return NGX_HTTP_TRACE;
        }

        return NGX_HTTP_UNKNOWN;

    case 6:
        if (ngx_http_srcache_strcmp_const(method_name->data, "DELETE") == 0) {
            *method_name_ptr = &ngx_http_srcache_delete_method;
            return NGX_HTTP_DELETE;
        }

        if (ngx_http_srcache_strcmp_const(method_name->data, "UNLOCK") == 0) {
            *method_name_ptr = &ngx_http_srcache_unlock_method;
            return NGX_HTTP_UNLOCK;
        }

        return NGX_HTTP_UNKNOWN;

    case 7:
        if (ngx_http_srcache_strcmp_const(method_name->data, "OPTIONS") == 0) {
            *method_name_ptr = &ngx_http_srcache_options_method;
            return NGX_HTTP_OPTIONS;
        }

        return NGX_HTTP_UNKNOWN;

    case 8:
        if (ngx_http_srcache_strcmp_const(method_name->data, "PROPFIND") == 0) {
            *method_name_ptr = &ngx_http_srcache_propfind_method;
            return NGX_HTTP_PROPFIND;
        }

        return NGX_HTTP_UNKNOWN;

    case 9:
        if (ngx_http_srcache_strcmp_const(method_name->data, "PROPPATCH")
            == 0)
        {
            *method_name_ptr = &ngx_http_srcache_proppatch_method;
            return NGX_HTTP_PROPPATCH;
        }

        return NGX_HTTP_UNKNOWN;

    default:
        return NGX_HTTP_UNKNOWN;
    }

    return NGX_HTTP_UNKNOWN;
}


ngx_int_t
ngx_http_srcache_adjust_subrequest(ngx_http_request_t *sr,
    ngx_http_srcache_parsed_request_t *parsed_sr)
{
    ngx_http_core_main_conf_t  *cmcf;
    ngx_http_request_t         *r;
    ngx_http_request_body_t    *body;
    ngx_int_t                   rc;

    sr->method      = parsed_sr->method;
    sr->method_name = parsed_sr->method_name;

    r = sr->parent;

    dd("subrequest method: %d %.*s", (int) sr->method,
       (int) sr->method_name.len, sr->method_name.data);

    sr->header_in = r->header_in;

#if 1
    /* XXX work-around a bug in ngx_http_subrequest */
    if (r->headers_in.headers.last == &r->headers_in.headers.part) {
        sr->headers_in.headers.last = &sr->headers_in.headers.part;
    }
#endif

    /* we do not inherit the parent request's variables */
    cmcf = ngx_http_get_module_main_conf(sr, ngx_http_core_module);
    sr->variables = ngx_pcalloc(sr->pool, cmcf->variables.nelts
                                * sizeof(ngx_http_variable_value_t));

    if (sr->variables == NULL) {
        return NGX_ERROR;
    }

    body = parsed_sr->request_body;
    if (body) {
        sr->request_body = body;

        rc = ngx_http_srcache_set_content_length_header(sr,
                                                 parsed_sr->content_length_n);

        if (rc != NGX_OK) {
            return NGX_ERROR;
        }
    }

    return NGX_OK;
}


ngx_int_t
ngx_http_srcache_add_copy_chain(ngx_pool_t *pool, ngx_chain_t **chain,
    ngx_chain_t *in, unsigned *plast)
{
    ngx_chain_t     *cl, **ll;
    size_t           len;

    ll = chain;

    for (cl = *chain; cl; cl = cl->next) {
        ll = &cl->next;
    }

    *plast = 0;

    while (in) {
        cl = ngx_alloc_chain_link(pool);
        if (cl == NULL) {
            return NGX_ERROR;
        }

        if (in->buf->last_buf || in->buf->last_in_chain) {
            *plast = 1;
        }

        if (ngx_buf_special(in->buf)) {
            cl->buf = in->buf;

        } else {
            if (ngx_buf_in_memory(in->buf)) {
                len = ngx_buf_size(in->buf);
                cl->buf = ngx_create_temp_buf(pool, len);
                if (cl->buf == NULL) {
                    return NGX_ERROR;
                }

                dd("buf: %.*s", (int) len, in->buf->pos);

                cl->buf->last = ngx_copy(cl->buf->pos, in->buf->pos, len);

            } else {
                return NGX_ERROR;
            }
        }

        *ll = cl;
        ll = &cl->next;
        in = in->next;
    }

    *ll = NULL;

    return NGX_OK;
}


ngx_int_t
ngx_http_srcache_post_request_at_head(ngx_http_request_t *r,
    ngx_http_posted_request_t *pr)
{
    dd_enter();

    if (pr == NULL) {
        pr = ngx_palloc(r->pool, sizeof(ngx_http_posted_request_t));
        if (pr == NULL) {
            return NGX_ERROR;
        }
    }

    pr->request = r;
    pr->next = r->main->posted_requests;
    r->main->posted_requests = pr;

    return NGX_OK;
}


static ngx_int_t
ngx_http_srcache_set_content_length_header(ngx_http_request_t *r, off_t len)
{
    ngx_table_elt_t                 *h, *header;
    u_char                          *p;
    ngx_list_part_t                 *part;
    ngx_http_request_t              *pr;
    ngx_uint_t                       i;

    r->headers_in.content_length_n = len;

    if (ngx_list_init(&r->headers_in.headers, r->pool, 20,
                      sizeof(ngx_table_elt_t))
        != NGX_OK)
    {
        return NGX_ERROR;
    }

    h = ngx_list_push(&r->headers_in.headers);
    if (h == NULL) {
        return NGX_ERROR;
    }

    h->key = ngx_http_srcache_content_length_header_key;
    h->lowcase_key = ngx_pnalloc(r->pool, h->key.len);
    if (h->lowcase_key == NULL) {
        return NGX_ERROR;
    }

    ngx_strlow(h->lowcase_key, h->key.data, h->key.len);

    r->headers_in.content_length = h;

    p = ngx_palloc(r->pool, NGX_OFF_T_LEN);
    if (p == NULL) {
        return NGX_ERROR;
    }

    h->value.data = p;

    h->value.len = ngx_sprintf(h->value.data, "%O", len) - h->value.data;

    h->hash = ngx_hash(ngx_hash(ngx_hash(ngx_hash(ngx_hash(ngx_hash(ngx_hash(
            ngx_hash(ngx_hash(ngx_hash(ngx_hash(ngx_hash(
            ngx_hash('c', 'o'), 'n'), 't'), 'e'), 'n'), 't'), '-'), 'l'), 'e'),
            'n'), 'g'), 't'), 'h');

    dd("r content length: %.*s",
       (int)r->headers_in.content_length->value.len,
       r->headers_in.content_length->value.data);

    pr = r->parent;

    if (pr == NULL) {
        return NGX_OK;
    }

    /* forward the parent request's all other request headers */

    part = &pr->headers_in.headers.part;
    header = part->elts;

    for (i = 0; /* void */; i++) {

        if (i >= part->nelts) {
            if (part->next == NULL) {
                break;
            }

            part = part->next;
            header = part->elts;
            i = 0;
        }

        if (header[i].key.len == sizeof("Content-Length") - 1
            && ngx_strncasecmp(header[i].key.data,
                               (u_char *) "Content-Length",
                               sizeof("Content-Length") - 1) == 0)
        {
            continue;
        }

        h = ngx_list_push(&r->headers_in.headers);
        if (h == NULL) {
            return NGX_ERROR;
        }

        *h = header[i];
    }

    /* XXX maybe we should set those built-in header slot in
     * ngx_http_headers_in_t too? */

    return NGX_OK;
}


ngx_int_t
ngx_http_srcache_request_no_cache(ngx_http_request_t *r, unsigned *no_store)
{
    ngx_table_elt_t                 *h;
    ngx_list_part_t                 *part;
    u_char                          *p;
    u_char                          *last;
    ngx_uint_t                       i;
    unsigned                         no_cache;

    part = &r->headers_in.headers.part;
    h = part->elts;

    *no_store = 0;
    no_cache = 0;

    for (i = 0; /* void */; i++) {

        if (i >= part->nelts) {
            if (part->next == NULL) {
                break;
            }

            part = part->next;
            h = part->elts;
            i = 0;
        }

        if (h[i].key.len == sizeof("Cache-Control") - 1
            && ngx_strncasecmp(h[i].key.data, (u_char *) "Cache-Control",
                               sizeof("Cache-Control") - 1) == 0)
        {
            p = h[i].value.data;
            last = p + h[i].value.len;

            if (!*no_store
                && ngx_strlcasestrn(p, last, (u_char *) "no-store", 8 - 1)
                   != NULL)
            {
                *no_store = 1;
            }

            if (ngx_strlcasestrn(p, last, (u_char *) "no-cache", 8 - 1) != NULL)
            {
                no_cache = 1;
            }

            continue;
        }

        if (h[i].key.len == sizeof("Pragma") - 1
            && ngx_strncasecmp(h[i].key.data, (u_char *) "Pragma",
                               sizeof("Pragma") - 1) == 0)
        {
            p = h[i].value.data;
            last = p + h[i].value.len;

            if (ngx_strlcasestrn(p, last, (u_char *) "no-cache", 8 - 1) != NULL)
            {
                no_cache = 1;
            }
        }
    }

    return no_cache ? NGX_OK : NGX_DECLINED;
}


ngx_int_t
ngx_http_srcache_response_no_cache(ngx_http_request_t *r,
    ngx_http_srcache_loc_conf_t *conf, ngx_http_srcache_ctx_t *ctx)
{
    ngx_table_elt_t   **ccp;
    ngx_table_elt_t    *h;
    ngx_uint_t          i;
    u_char             *p, *last;
    ngx_int_t           n;
    time_t              expires;

    dd("checking response cache control settings");

    ccp = r->headers_out.cache_control.elts;

    if (ccp == NULL) {
        goto check_expires;
    }

    for (i = 0; i < r->headers_out.cache_control.nelts; i++) {
        if (!ccp[i]->hash) {
            continue;
        }

        p = ccp[i]->value.data;
        last = p + ccp[i]->value.len;

        if (!conf->store_private
            && ngx_strlcasestrn(p, last, (u_char *) "private", 7 - 1) != NULL)
        {
            return NGX_OK;
        }

        if (!conf->store_no_store
            && ngx_strlcasestrn(p, last, (u_char *) "no-store", 8 - 1) != NULL)
        {
            return NGX_OK;
        }

        if (!conf->store_no_cache
            && ngx_strlcasestrn(p, last, (u_char *) "no-cache", 8 - 1) != NULL)
        {
            return NGX_OK;
        }

        if (ctx->valid_sec != 0) {
            continue;
        }

        p = ngx_strlcasestrn(p, last, (u_char *) "max-age=", 8 - 1);

        if (p == NULL) {
            continue;
        }

        n = 0;

        for (p += 8; p < last; p++) {
            if (*p == ',' || *p == ';' || *p == ' ') {
                break;
            }

            if (*p >= '0' && *p <= '9') {
                n = n * 10 + *p - '0';
                continue;
            }

            return NGX_OK;
        }

        if (n == 0) {
            return NGX_OK;
        }

        ctx->valid_sec = ngx_time() + n;
    }

check_expires:
    dd("valid_sec after processing cache-control: %d", (int) ctx->valid_sec);

    if (ctx->valid_sec == 0) {
        h = r->headers_out.expires;

        dd("expires header: %p", h);

        if (h != NULL && h->hash != 0) {
            expires = ngx_http_parse_time(h->value.data, h->value.len);

            if (expires == NGX_ERROR || expires <= ngx_time()) {
                return NGX_OK;
            }

            ctx->valid_sec = expires;
        }
    }

    return NGX_DECLINED;
}


ngx_int_t
ngx_http_srcache_process_status_line(ngx_http_request_t *r, ngx_buf_t *b)
{
    ngx_int_t                    rc;
    ngx_http_srcache_ctx_t      *ctx;
    ngx_http_request_t          *pr;
    ngx_http_srcache_loc_conf_t *conf;

    ctx = ngx_http_get_module_ctx(r, ngx_http_srcache_filter_module);

    rc = ngx_http_parse_status_line(r, b, &ctx->status);

    if (rc == NGX_AGAIN) {
        return NGX_AGAIN;
    }

    if (rc == NGX_ERROR) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "srcache_fetch: cache sent invalid status line");
        return NGX_ERROR;
    }

    /* rc == NGX_OK */

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "srcache_fetch status line done");

    pr = r->parent;
    pr->headers_out.status = ctx->status.code;

    ctx->process_header = ngx_http_srcache_process_header;

    conf = ngx_http_get_module_loc_conf(pr, ngx_http_srcache_filter_module);

    dd("header buffer size: %d", (int) conf->header_buf_size);

    ctx->header_buf = ngx_create_temp_buf(r->pool, conf->header_buf_size);
    if (ctx->header_buf == NULL) {
        return NGX_ERROR;
    }

    if (b->pos == b->last) {
        return NGX_AGAIN;
    }

    return ngx_http_srcache_process_header(r, b);
}


ngx_int_t
ngx_http_srcache_process_header(ngx_http_request_t *r, ngx_buf_t *b)
{
    ngx_int_t                       rc;
    ngx_table_elt_t                 header;
    ngx_http_srcache_ctx_t         *ctx;
    off_t                           len, rest;
    unsigned                        truncate;
    u_char                         *p;
    ngx_http_srcache_header_t      *hh;
    ngx_http_srcache_main_conf_t   *smcf;

    smcf = ngx_http_get_module_main_conf(r, ngx_http_srcache_filter_module);

    ctx = ngx_http_get_module_ctx(r, ngx_http_srcache_filter_module);

    for ( ;; ) {

        len = b->last - b->pos;
        rest = ctx->header_buf->end - ctx->header_buf->last;

        dd("len: %d, rest: %d", (int) len, (int) rest);

        if (len > rest) {
            len = rest;
            truncate = 1;

        } else {
            truncate = 0;
        }

        ctx->header_buf->last = ngx_copy(ctx->header_buf->last, b->pos,
                                         (size_t) len);

        p = ctx->header_buf->pos;

        rc = ngx_http_parse_header_line(r, ctx->header_buf, 1);

        b->pos += ctx->header_buf->pos - p;

        if (rc == NGX_OK) {

            /* a header line has been parsed successfully */

            ngx_memzero(&header, sizeof(ngx_table_elt_t));

            header.hash = r->header_hash;

            header.key.len = r->header_name_end - r->header_name_start;
            header.value.len = r->header_end - r->header_start;

            header.key.data = ngx_pnalloc(r->pool,
                                          header.key.len + 1
                                          + header.value.len + 1
                                          + header.key.len);

            if (header.key.data == NULL) {
                return NGX_ERROR;
            }

            header.value.data = header.key.data + header.key.len + 1;
            header.lowcase_key = header.key.data + header.key.len + 1
                                 + header.value.len + 1;

            ngx_cpystrn(header.key.data, r->header_name_start,
                        header.key.len + 1);

            ngx_cpystrn(header.value.data, r->header_start,
                        header.value.len + 1);

            if (header.key.len == r->lowcase_index) {
                ngx_memcpy(header.lowcase_key, r->lowcase_header,
                           header.key.len);

            } else {
                ngx_strlow(header.lowcase_key, header.key.data, header.key.len);
            }

            hh = ngx_hash_find(&smcf->headers_in_hash, header.hash,
                               header.lowcase_key, header.key.len);

            if (hh) {
                if (hh->handler(r->parent, &header, hh->offset) != NGX_OK) {
                    return NGX_ERROR;
                }

            } else {

                if (ngx_http_srcache_process_header_line(r->parent, &header, 0)
                    != NGX_OK)
                {
                    return NGX_ERROR;
                }
            }

            ngx_log_debug2(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                           "srcache_fetch header: \"%V: %V\"",
                           &header.key, &header.value);

            ctx->header_buf->pos = ctx->header_buf->start;
            ctx->header_buf->last = ctx->header_buf->start;

            continue;
        }

        if (rc == NGX_HTTP_PARSE_HEADER_DONE) {

            /* a whole header has been parsed successfully */

            ctx->header_buf->pos = ctx->header_buf->start;
            ctx->header_buf->last = ctx->header_buf->start;
            ngx_pfree(r->pool, ctx->header_buf->start);

            ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                           "srcache_fetch header done");

            return NGX_OK;
        }

        if (rc == NGX_AGAIN) {
            if (truncate) {
                ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                              "srcache_fetch: header buffer overflown "
                              "(maybe you should consider increasing "
                              "srcache_header_buffer_size?)");

                ctx->header_buf->pos = ctx->header_buf->start;
                ctx->header_buf->last = ctx->header_buf->start;
                ngx_pfree(r->pool, ctx->header_buf->start);

                return NGX_ERROR;
            }

            return NGX_AGAIN;
        }

        /* there was error while a header line parsing */

        ctx->header_buf->pos = ctx->header_buf->start;
        ctx->header_buf->last = ctx->header_buf->start;
        ngx_pfree(r->pool, ctx->header_buf->start);

        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "srcache_fetch: cache sent invalid header");

        return NGX_ERROR;
    }
}


ngx_int_t
ngx_http_srcache_store_response_header(ngx_http_request_t *r,
    ngx_http_srcache_ctx_t *ctx)
{
    ngx_chain_t             *cl;
    size_t                   len;
    ngx_buf_t               *b;
    ngx_uint_t               status;
    ngx_uint_t               i;
    ngx_str_t               *status_line;
    ngx_list_part_t         *part;
    ngx_table_elt_t         *header;

    u_char                   buf[sizeof("Mon, 28 Sep 1970 06:00:00 GMT") - 1];

    ngx_http_srcache_loc_conf_t    *conf;

    conf = ngx_http_get_module_loc_conf(r, ngx_http_srcache_filter_module);

    dd("request: %p, uri: %.*s", r, (int) r->uri.len, r->uri.data);

    len = sizeof("HTTP/1.x ") - 1 + sizeof(CRLF) - 1
          /* the end of the header */
          + sizeof(CRLF) - 1;

    if (r->headers_out.status_line.len) {
        dd("status line defined");
        len += r->headers_out.status_line.len;
        status_line = &r->headers_out.status_line;
        status = 0;

    } else {
        dd("status line not defined");

        status = r->headers_out.status;

        if (status >= NGX_HTTP_OK
            && status < NGX_HTTP_LAST_LEVEL_200)
        {
            /* 2XX */

            status -= NGX_HTTP_OK;
            dd("status: %d", (int) status);
            status_line = &ngx_http_status_lines[status];
            len += ngx_http_status_lines[status].len;

        } else if (status >= NGX_HTTP_MOVED_PERMANENTLY
                   && status < NGX_HTTP_LAST_LEVEL_300)
        {
            /* 3XX */

            if (status == NGX_HTTP_NOT_MODIFIED) {
                r->header_only = 1;
            }

            status = status - NGX_HTTP_MOVED_PERMANENTLY + NGX_HTTP_LEVEL_200;
            status_line = &ngx_http_status_lines[status];
            len += ngx_http_status_lines[status].len;

        } else if (status >= NGX_HTTP_BAD_REQUEST
                   && status < NGX_HTTP_LAST_LEVEL_400)
        {
            /* 4XX */
            status = status - NGX_HTTP_BAD_REQUEST
                            + NGX_HTTP_LEVEL_200
                            + NGX_HTTP_LEVEL_300;

            status_line = &ngx_http_status_lines[status];
            len += ngx_http_status_lines[status].len;

        } else if (status >= NGX_HTTP_INTERNAL_SERVER_ERROR
                   && status < NGX_HTTP_LAST_LEVEL_500)
        {
            /* 5XX */
            status = status - NGX_HTTP_INTERNAL_SERVER_ERROR
                            + NGX_HTTP_LEVEL_200
                            + NGX_HTTP_LEVEL_300
                            + NGX_HTTP_LEVEL_400;

            status_line = &ngx_http_status_lines[status];
            len += ngx_http_status_lines[status].len;

        } else {
            len += NGX_INT_T_LEN;
            status_line = NULL;
        }
    }

    if (!conf->hide_content_type && r->headers_out.content_type.len) {
        len += sizeof("Content-Type: ") - 1
               + r->headers_out.content_type.len + 2;

        if (r->headers_out.content_type_len == r->headers_out.content_type.len
            && r->headers_out.charset.len)
        {
            len += sizeof("; charset=") - 1 + r->headers_out.charset.len;
        }
    }

    if (!conf->hide_last_modified) {
        if (r->headers_out.last_modified_time != -1) {
            if (r->headers_out.status != NGX_HTTP_OK
                && r->headers_out.status != NGX_HTTP_PARTIAL_CONTENT
                && r->headers_out.status != NGX_HTTP_NOT_MODIFIED
                && r->headers_out.status != NGX_HTTP_NO_CONTENT)
            {
                r->headers_out.last_modified_time = -1;
                r->headers_out.last_modified = NULL;
            }
        }

        dd("last modified time: %d", (int) r->headers_out.last_modified_time);

        if (r->headers_out.last_modified == NULL
            && r->headers_out.last_modified_time != -1)
        {
            (void) ngx_http_time(buf, r->headers_out.last_modified_time);

            len += sizeof("Last-Modified: ") - 1 + sizeof(buf) + 2;
        }
    }

    if (r->allow_ranges) {
        len += sizeof("X-SRCache-Allow-Ranges: 1") - 1 + 2;
    }

    part = &r->headers_out.headers.part;
    header = part->elts;

    for (i = 0; /* void */; i++) {

        if (i >= part->nelts) {
            if (part->next == NULL) {
                break;
            }

            part = part->next;
            header = part->elts;
            i = 0;
        }

        if (header[i].hash == 0) {
            continue;
        }

        if (ngx_hash_find(&conf->hide_headers_hash, header[i].hash,
                          header[i].lowcase_key, header[i].key.len))
        {
            continue;
        }

        len += header[i].key.len + sizeof(": ") - 1 + header[i].value.len
               + sizeof(CRLF) - 1;
    }

    b = ngx_create_temp_buf(r->pool, len);
    if (b == NULL) {
        return NGX_ERROR;
    }

    /* "HTTP/1.x " */
    b->last = ngx_cpymem(b->last, "HTTP/1.1 ", sizeof("HTTP/1.x ") - 1);

    /* status line */
    if (status_line) {
        b->last = ngx_copy(b->last, status_line->data, status_line->len);

    } else {
        b->last = ngx_sprintf(b->last, "%ui", status);
    }
    *b->last++ = CR; *b->last++ = LF;

    if (!conf->hide_content_type && r->headers_out.content_type.len) {
        b->last = ngx_cpymem(b->last, "Content-Type: ",
                             sizeof("Content-Type: ") - 1);
        b->last = ngx_copy(b->last, r->headers_out.content_type.data,
                           r->headers_out.content_type.len);

        if (r->headers_out.content_type_len == r->headers_out.content_type.len
            && r->headers_out.charset.len)
        {
            b->last = ngx_cpymem(b->last, "; charset=",
                                 sizeof("; charset=") - 1);
            b->last = ngx_copy(b->last, r->headers_out.charset.data,
                               r->headers_out.charset.len);
        }

        *b->last++ = CR; *b->last++ = LF;
    }

    if (!conf->hide_last_modified
        && r->headers_out.last_modified == NULL
        && r->headers_out.last_modified_time != -1)
    {
        b->last = ngx_cpymem(b->last, "Last-Modified: ",
                             sizeof("Last-Modified: ") - 1);

        b->last = ngx_cpymem(b->last, buf, sizeof(buf));

        *b->last++ = CR; *b->last++ = LF;
    }

    if (r->allow_ranges) {
        b->last = ngx_cpymem(b->last, "X-SRCache-Allow-Ranges: 1\r\n",
                             sizeof("X-SRCache-Allow-Ranges: 1\r\n") - 1);
    }

    part = &r->headers_out.headers.part;
    header = part->elts;

    for (i = 0; /* void */; i++) {

        if (i >= part->nelts) {
            if (part->next == NULL) {
                break;
            }

            part = part->next;
            header = part->elts;
            i = 0;
        }

        if (header[i].hash == 0) {
            continue;
        }

        dd("header hash: %lu, hash lc: %lu", (unsigned long) header[i].hash,
           (unsigned long) ngx_hash_key_lc(header[i].key.data,
           header[i].key.len));

        if (ngx_hash_find(&conf->hide_headers_hash, header[i].hash,
                          header[i].lowcase_key, header[i].key.len))
        {
            dd("skipped header key: %.*s", (int) header[i].key.len,
               header[i].key.data);
            continue;
        }

        dd("header not skipped");

        b->last = ngx_copy(b->last, header[i].key.data, header[i].key.len);
        *b->last++ = ':'; *b->last++ = ' ';

        b->last = ngx_copy(b->last, header[i].value.data, header[i].value.len);
        *b->last++ = CR; *b->last++ = LF;
    }

    ngx_log_debug2(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "srcache store header %*s", (size_t) (b->last - b->pos),
                   b->pos);

    /* the end of HTTP header */
    *b->last++ = CR; *b->last++ = LF;

    if (b->last != b->end) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "srcache_fetch: buffer error when serializing the "
                      "response header: %O left", (off_t) (b->last - b->end));

        return NGX_ERROR;
    }

    cl = ngx_alloc_chain_link(r->pool);
    if (cl == NULL) {
        return NGX_ERROR;
    }

    cl->buf = b;
    cl->next = NULL;

    ctx->body_to_cache = cl;

    ctx->response_length += len;

    return NGX_OK;
}


ngx_int_t
ngx_http_srcache_hide_headers_hash(ngx_conf_t *cf,
    ngx_http_srcache_loc_conf_t *conf, ngx_http_srcache_loc_conf_t *prev,
    ngx_str_t *default_hide_headers, ngx_hash_init_t *hash)
{
    ngx_str_t       *h;
    ngx_uint_t       i, j;
    ngx_array_t      hide_headers;
    ngx_hash_key_t  *hk;

    if (conf->hide_headers == NGX_CONF_UNSET_PTR
        && conf->pass_headers == NGX_CONF_UNSET_PTR)
    {
        conf->hide_headers_hash = prev->hide_headers_hash;

        if (conf->hide_headers_hash.buckets) {
            return NGX_OK;
        }

        conf->hide_headers = prev->hide_headers;
        conf->pass_headers = prev->pass_headers;
        conf->hide_content_type = prev->hide_content_type;
        conf->hide_last_modified = prev->hide_last_modified;

    } else {
        if (conf->hide_headers == NGX_CONF_UNSET_PTR) {
            conf->hide_headers = prev->hide_headers;
        }

        if (conf->pass_headers == NGX_CONF_UNSET_PTR) {
            conf->pass_headers = prev->pass_headers;
        }
    }

    dd("init hide headers");

    if (ngx_array_init(&hide_headers, cf->temp_pool, 4, sizeof(ngx_hash_key_t))
        != NGX_OK)
    {
        return NGX_ERROR;
    }

    for (h = default_hide_headers; h->len; h++) {
        hk = ngx_array_push(&hide_headers);
        if (hk == NULL) {
            return NGX_ERROR;
        }

        hk->key = *h;
        hk->key_hash = ngx_hash_key_lc(h->data, h->len);
        hk->value = (void *) 1;
    }

    if (conf->hide_headers != NGX_CONF_UNSET_PTR) {
        dd("hide headers not empty");

        h = conf->hide_headers->elts;

        for (i = 0; i < conf->hide_headers->nelts; i++) {

            hk = hide_headers.elts;

            for (j = 0; j < hide_headers.nelts; j++) {
                if (ngx_strcasecmp(h[i].data, hk[j].key.data) == 0) {
                    goto exist;
                }
            }

            hk = ngx_array_push(&hide_headers);
            if (hk == NULL) {
                return NGX_ERROR;
            }

            hk->key = h[i];
            hk->key_hash = ngx_hash_key_lc(h[i].data, h[i].len);
            hk->value = (void *) 1;

            if (h[i].len == sizeof("Last-Modified") - 1
                && ngx_strncasecmp(h[i].data, (u_char *) "Last-Modified",
                                   sizeof("Last-Modified") - 1)
                == 0)
            {
                conf->hide_last_modified = 1;
            }

            if (h[i].len == sizeof("Content-Type") - 1
                && ngx_strncasecmp(h[i].data, (u_char *) "Content-Type",
                                   sizeof("Content-Type") - 1) == 0)
            {
                conf->hide_content_type = 1;
            }

            dd("adding header to hide headers: %.*s", (int) h[i].len,
                    h[i].data);

        exist:

            continue;
        }
    }

    if (conf->pass_headers != NGX_CONF_UNSET_PTR) {

        h = conf->pass_headers->elts;
        hk = hide_headers.elts;

        for (i = 0; i < conf->pass_headers->nelts; i++) {
            for (j = 0; j < hide_headers.nelts; j++) {

                if (hk[j].key.data == NULL) {
                    continue;
                }

                if (h[i].len == sizeof("Content-Type") - 1
                    && ngx_strncasecmp(h[i].data, (u_char *) "Content-Type",
                                       sizeof("Content-Type") - 1) == 0)
                {
                    conf->hide_content_type = 0;
                }

                if (h[i].len == sizeof("Last-Modified") - 1
                    && ngx_strncasecmp(h[i].data, (u_char *) "Last-Modified",
                                       sizeof("Last-Modified") - 1) == 0)
                {
                    conf->hide_last_modified = 0;
                }

                if (ngx_strcasecmp(h[i].data, hk[j].key.data) == 0) {
                    hk[j].key.data = NULL;
                    break;
                }
            }
        }
    }

    hash->hash = &conf->hide_headers_hash;
    hash->key = ngx_hash_key_lc;
    hash->pool = cf->pool;
    hash->temp_pool = NULL;

    return ngx_hash_init(hash, hide_headers.elts, hide_headers.nelts);
}


ngx_int_t
ngx_http_srcache_cmp_int(const void *one, const void *two)
{
    const ngx_int_t           *a = one;
    const ngx_int_t           *b = two;

    return (*a < *b);
}

/* vi:set ft=c ts=4 sw=4 et fdm=marker: */
