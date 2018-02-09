#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include <ndk.h>
#include <iconv.h>
#include <ngx_config.h>
#include <ngx_core.h>
#include <ngx_http.h>


static ngx_int_t        iconv_buf_size;
static ngx_int_t        max_iconv_bufs;

static ngx_uint_t       ngx_http_iconv_filter_used = 0;

typedef struct {
    size_t               buf_size;
    ngx_flag_t           enabled;
    ngx_flag_t           skip_body_filter;
    u_char              *from;
    u_char              *to;
} ngx_http_iconv_loc_conf_t;


typedef struct {
    ngx_http_request_t      *r;
    ngx_str_t                uc; /* unfinished character */
} ngx_http_iconv_ctx_t;


static ngx_int_t ngx_http_iconv_filter_pre(ngx_conf_t *cf);
static ngx_int_t ngx_http_iconv_filter_init(ngx_conf_t *cf);
static ngx_int_t ngx_http_iconv_header_filter(ngx_http_request_t *r);
static ngx_int_t ngx_http_iconv_body_filter(ngx_http_request_t *r,
        ngx_chain_t *in);
static ngx_int_t ngx_http_iconv_filter_convert(ngx_http_iconv_ctx_t *ctx,
        ngx_chain_t *in, ngx_chain_t **out);
static ngx_int_t ngx_http_do_iconv(ngx_http_request_t *r, ngx_chain_t **c,
        void *data, size_t len, u_char *from, u_char *to, size_t *conv_bytes,
        size_t *rest_bytes);
static ngx_int_t ngx_http_iconv_merge_chain_link(ngx_http_iconv_ctx_t *ctx,
        ngx_chain_t *in, ngx_chain_t **out);
static char *ngx_http_iconv_conf_handler(ngx_conf_t *cf, ngx_command_t *cmd,
        void *conf);
static char *ngx_http_set_iconv_conf_handler(ngx_conf_t *cf,
        ngx_command_t *cmd, void *conf);
static ngx_int_t ngx_http_set_iconv_handler(ngx_http_request_t *r,
        ngx_str_t *res, ngx_http_variable_value_t *v);
static void *ngx_http_iconv_create_loc_conf(ngx_conf_t *cf);
static char *ngx_http_iconv_merge_loc_conf(ngx_conf_t *cf, void *parent,
        void *child);
static void ngx_http_iconv_discard_body(ngx_chain_t *in);


static ngx_command_t ngx_http_iconv_commands[] = {

    { ngx_string("set_iconv"),
      NGX_HTTP_LOC_CONF|NGX_CONF_TAKE4,
      ngx_http_set_iconv_conf_handler,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL
    },

    { ngx_string("iconv_buffer_size"),
      NGX_HTTP_LOC_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_size_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_iconv_loc_conf_t, buf_size),
      NULL
    },

    { ngx_string("iconv_filter"),
      NGX_HTTP_LOC_CONF|NGX_CONF_TAKE2,
      ngx_http_iconv_conf_handler,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    ngx_null_command
};


static ngx_http_module_t ngx_http_iconv_module_ctx = {
    ngx_http_iconv_filter_pre,          /* preconfiguration */
    ngx_http_iconv_filter_init,         /* postconfiguration */
    NULL,                               /* create main configuration */
    NULL,                               /* init main configuration */
    NULL,                               /* create server configuration */
    NULL,                               /* merge server configuration */
    ngx_http_iconv_create_loc_conf,     /* create location configuration */
    ngx_http_iconv_merge_loc_conf       /* merge location configuration */
};


ngx_module_t ngx_http_iconv_module = {
    NGX_MODULE_V1,
    &ngx_http_iconv_module_ctx,     /* module context */
    ngx_http_iconv_commands,        /* module directives */
    NGX_HTTP_MODULE,                /* module type */
    NULL,                           /* init maseter */
    NULL,                           /* init module */
    NULL,                           /* init process */
    NULL,                           /* init thread */
    NULL,                           /* exit thread */
    NULL,                           /* exit process */
    NULL,                           /* exit master */
    NGX_MODULE_V1_PADDING
};


static ngx_http_output_header_filter_pt ngx_http_next_header_filter;
static ngx_http_output_body_filter_pt   ngx_http_next_body_filter;


static ngx_int_t ngx_http_iconv_filter_pre(ngx_conf_t *cf)
{
    ngx_http_iconv_filter_used  = 0;
    return NGX_OK;
}

static ngx_int_t ngx_http_iconv_filter_init(ngx_conf_t *cf)
{
    if (ngx_http_iconv_filter_used) {
        ngx_http_next_header_filter = ngx_http_top_header_filter;
        ngx_http_top_header_filter = ngx_http_iconv_header_filter;
        ngx_http_next_body_filter = ngx_http_top_body_filter;
        ngx_http_top_body_filter = ngx_http_iconv_body_filter;
    }

    return NGX_OK;
}


static ngx_int_t ngx_http_iconv_header_filter(ngx_http_request_t *r)
{
    ngx_http_iconv_loc_conf_t       *ilcf;
    ngx_http_iconv_ctx_t            *ctx;

    ilcf = ngx_http_get_module_loc_conf(r, ngx_http_iconv_module);
    iconv_buf_size = ilcf->buf_size;
    r->filter_need_in_memory = 1;

    if (!ilcf->enabled) {
        return ngx_http_next_header_filter(r);
    }

    ngx_http_clear_content_length(r);

    ctx = ngx_http_get_module_ctx(r, ngx_http_iconv_module);
    if (ctx == NULL) {
        dd("create new context");
        /*
         * set by ngx_pcalloc()
         *   ctx->uc->len = 0
         *   ctx->uc->data = NULL
         */
        ctx = ngx_pcalloc(r->pool, sizeof(ngx_http_iconv_module));
        if (ctx == NULL) {
            return NGX_ERROR;
        }
        ctx->r = r;
        ngx_http_set_ctx(r, ctx, ngx_http_iconv_module);
    }

    if (r->http_version == NGX_HTTP_VERSION_10) {
        r->keepalive = 0;
    }

    if (r->http_version < NGX_HTTP_VERSION_10) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
            "iconv does not support HTTP < 1.0 yet");
        ilcf->skip_body_filter = 1;
    }

    return ngx_http_next_header_filter(r);
}


static ngx_int_t
ngx_http_iconv_body_filter(ngx_http_request_t *r, ngx_chain_t *in)
{
    ngx_http_iconv_loc_conf_t           *ilcf;
    ngx_chain_t                         *ncl, *nncl;
    ngx_http_iconv_ctx_t                *ctx;
    ngx_int_t                            rc;

    ilcf = ngx_http_get_module_loc_conf(r, ngx_http_iconv_module);

    if (!ilcf->enabled) {
        return ngx_http_next_body_filter(r, in);
    }

    if (ilcf->skip_body_filter) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
            "iconv body filter skiped");
        return ngx_http_next_body_filter(r, in);
    }

    if (in == NULL) {
        dd("XXX in is NULL");
        return ngx_http_next_body_filter(r, in);
    }

    if (in->buf->last == in->buf->pos) {
        dd("pass 0 size buf to next body filter");
        return ngx_http_next_body_filter(r, in);
    }

    dd("create new chain link");

    ctx = ngx_http_get_module_ctx(r, ngx_http_iconv_module);

    if (ngx_http_iconv_merge_chain_link(ctx, in, &ncl) == NGX_ERROR) {
        return NGX_ERROR;
    }

    dd("discard previous chain link");

    ngx_http_iconv_discard_body(in);

    dd("start to convert");

    rc = ngx_http_iconv_filter_convert(ctx, ncl, &nncl);

    if (rc == NGX_ERROR) {
        dd("iconv convertion error");
        return NGX_ERROR;
    }

    if (nncl == NULL) {
        dd ("nncl is NULL");
        return NGX_OK;
    }

    dd("pass to next body filter->\n%.*s",
            (int) (ncl->buf->last - ncl->buf->pos), ncl->buf->pos);

    dd("nncl: len: %d, sync: %d, last_buf: %d, flush: %d, next: %p",
            (int) (nncl->buf->last - nncl->buf->pos),
            nncl->buf->sync, nncl->buf->last_buf,
            nncl->buf->flush, nncl->next);

    return ngx_http_next_body_filter(r, nncl);
}


static ngx_int_t
ngx_http_iconv_merge_chain_link(ngx_http_iconv_ctx_t *ctx, ngx_chain_t *in,
        ngx_chain_t **out)
{
    ngx_chain_t             *cl, *ncl;
    ngx_http_request_t      *r;
    ngx_buf_t               *buf;
    size_t                  len = 0;

    r = ctx->r;

    for (cl = in; cl; cl = cl->next) {
        buf = cl->buf;
        len += buf->last - buf->pos;
        dd("len: %d", (int) len);
        dd("sync: %d, last_buf: %d, flush: %d, memory: %d, in-file: %d,"
                "temp: %d",
                (int) buf->sync, (int) buf->last_buf,
                (int) buf->flush, (int) buf->memory, (int) buf->in_file,
                (int) buf->temporary);
    }

    len += ctx->uc.len;
    /* requires C99 */

    dd("XXX count buffer length: %zu", len);

    ncl = ngx_alloc_chain_link(r->pool);
    if (ncl == NULL) {
        return NGX_ERROR;
    }

    buf = ngx_create_temp_buf(r->pool, len);
    if (buf == NULL) {
        return NGX_ERROR;
    }

    ncl->buf = buf;
    if (ctx->uc.len) {
        dd("copy unfinished character");
        buf->last = ngx_copy(buf->start, ctx->uc.data, ctx->uc.len);
    }

    dd("copy old chain link");

    for (cl = in; cl; cl = cl->next) {
        if (cl->buf->last > cl->buf->pos) {
            buf->last = ngx_copy(buf->last, cl->buf->pos, cl->buf->last -
                    cl->buf->pos);
        }

        if (cl->buf->sync) {
            buf->sync = 1;
        }

        if (cl->buf->flush) {
            buf->flush = 1;
        }

        if (! ngx_buf_in_memory(cl->buf) && ! ngx_buf_special(cl->buf)) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                "ngx_iconv only supports in-memory bufs");

            return NGX_ERROR;
        }

        if (cl->buf->last_buf) {
            dd("find last buf");
            buf->last_buf = 1;
            break;
        }
    }

    ncl->next = NULL;
    *out = ncl;
    return NGX_OK;
}


static ngx_int_t
ngx_http_iconv_filter_convert(ngx_http_iconv_ctx_t *ctx, ngx_chain_t *in,
        ngx_chain_t **out)
{
    ngx_http_iconv_loc_conf_t   *ilcf;
    size_t                       rest;
    ngx_chain_t                 *cl;
    ngx_int_t                    rc;

    rest = 0;
    ilcf = ngx_http_get_module_loc_conf(ctx->r, ngx_http_iconv_module);

    dd("XXX in->buf: %.*s",
            (int) (in->buf->last - in->buf->pos),
            in->buf->pos);

    dd("XXX last buf: %d",
            (int) in->buf->last_buf);

    if (in->buf->last - in->buf->pos) {
        rc = ngx_http_do_iconv(ctx->r, out, in->buf->pos, in->buf->last -
            in->buf->pos, ilcf->from, ilcf->to, NULL, &rest);
        if (rc != NGX_OK) {
            ngx_log_error(NGX_LOG_ERR, ctx->r->connection->log, 0,
                    "convert error from ngx_http_do_iconv");
            return rc;
        }

        if (in->buf->last_buf) {
            for (cl = *out; cl->next; cl = cl->next);
            cl->buf->last_buf = 1;
        }

    } else {
        *out = in;
    }

    dd("ilcf->to:%s", ilcf->to);

    if (rest) {
        ctx->uc.data = ngx_palloc(ctx->r->pool, rest);
        if (ctx->uc.data == NULL) {
            return NGX_ERROR;
        }

        dd("%p, %p, %zu", ctx->uc.data, in->buf->last, rest);

        ngx_memcpy(ctx->uc.data, in->buf->last - rest, rest);

        ctx->uc.len = rest;

    } else {
        ctx->uc.data = NULL;
        ctx->uc.len = 0;
    }

    return NGX_OK;
}


static ngx_int_t
ngx_http_do_iconv(ngx_http_request_t *r, ngx_chain_t **c, void *data,
        size_t len, u_char *from, u_char *to, size_t *conved_bytes,
        size_t *rest_bytes)
{
    iconv_t           cd;
    ngx_chain_t      *cl, *chain, **ll;
    ngx_buf_t        *b;
    size_t            cv, rest, rv;

    cv = 0;
    dd("iconv from=%s, to=%s", from, to);
    cd = iconv_open((const char *) to, (const char *) from);

    if (cd == (iconv_t) -1) {
        dd("iconv open error");
        return NGX_ERROR;
    }

    dd("len=%zu, iconv_buf_size=%zu", len, iconv_buf_size);
    ll = &chain;

conv_begin:
    while (len) {
        cl = ngx_alloc_chain_link(r->pool);
        if (cl == NULL) {
            iconv_close(cd);
            return NGX_ERROR;
        }
        /* --- b->temporary--- */
        b = ngx_create_temp_buf(r->pool, iconv_buf_size);
        if (b == NULL) {
            iconv_close(cd);
            return NGX_ERROR;
        }

        cl->buf = b;
        rest = iconv_buf_size;

        do {
            rv = iconv(cd, (void *) &data, &len, (void *) &b->last, &rest);

            if (rv == (size_t) -1) {
                if (errno == EINVAL) {
                    cv += iconv_buf_size - rest;
                    dd("iconv error:EINVAL,len=%d cv=%d rest=%d", (int) len,
                        (int) cv, (int) rest);
                    goto conv_done;
                }

                if (errno == E2BIG) {
                    dd("E2BIG");
                    /* E2BIG error is not considered*/
                    break;
                }

                if (errno == EILSEQ) {
                    ngx_log_error(NGX_LOG_NOTICE, r->connection->log, 0,
                        "iconv sees invalid character sequence (EILSEQ)");

                    if (len >= 1) {
                        if (rest == 0) {
                            dd("EILSEQ:rest=0");
                            cv += iconv_buf_size - rest;
                            *ll = cl;
                            ll = &cl->next;
                            goto conv_begin;
                        } else {
                            dd("EILSEQ:rest=%d", (int)rest);
                            len--;
                            data = (u_char *)data + 1;
                            /* replace illegal character to '?' */
                            *b->last = '?';
                            b->last = (u_char *)b->last + 1;
                            rest--;
                        }
                    } else {
                        goto conv_done;
                    }

                }
            }
        } while (rv == (size_t) -1 && errno == EILSEQ);

        /* E2BIG error is not considered*/
        /* this code can work but stops when meet illegal encoding */
        /*
        if (rv == (size_t) -1) {
            if (errno == EILSEQ) {
                ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                        "iconv error:EILSEQ");
                iconv_close(cd);
                return NGX_ERROR;
            }

            if (errno == EINVAL) {
                cv += iconv_buf_size - rest;
                dd("iconv error:EINVAL,len=%d cv=%d rest=%d", (int) len,
                        (int) cv, (int) rest);
                break;
            } else if (errno == E2BIG) {
                dd("iconv error:E2BIG");
            }
        }
        */
        cv += iconv_buf_size - rest;
        *ll = cl;
        ll = &cl->next;
    }

conv_done:
    *ll = NULL;
    if (conved_bytes) {
        *conved_bytes = cv;
    }
    if (rest_bytes) {
        dd("rest bytes:%zu", len);
        *rest_bytes = len;
    }
    if (c) {
        dd("chain: %p", chain);

        /* chain may be null */
        /*
        dd("conv done: chain buf: %.*s",
                (int) (chain->buf->last - chain->buf->pos),
                chain->buf->last);
        */
        *c = chain;
    }
    dd("out");
    iconv_close(cd);
    return NGX_OK;
}


static void
ngx_http_iconv_discard_body(ngx_chain_t *in)
{
    ngx_chain_t         *cl;

    for (cl = in; cl; cl = cl->next) {
        cl->buf->pos = cl->buf->last;
    }
}


static char *
ngx_http_iconv_conf_handler(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_iconv_loc_conf_t   *ilcf;
    ngx_str_t                   *value;
    u_char                      *p;
    size_t                       tl;
    ilcf = conf;

    ngx_http_iconv_filter_used = 1;

    ilcf->enabled = 1;
    value = cf->args->elts;

    tl = sizeof("from=") - 1;
    if (ngx_strncasecmp((u_char *) "from=", value[1].data, tl)
        != 0)
    {
        dd("invalid 'from='");
        return NGX_CONF_ERROR;
    }
    ilcf->from = ngx_palloc(cf->pool, value[1].len - tl + 1);
    if (ilcf->from == NULL) {
        return NGX_CONF_ERROR;
    }
    p = ngx_copy(ilcf->from, value[1].data + tl, value[1].len - tl);
    *p = '\0';

    tl = sizeof("to=") - 1;
    if (ngx_strncasecmp((u_char *) "to=", value[2].data, tl)
        != 0)
    {
        dd("invalid 'to='");
        return NGX_CONF_ERROR;
    }
    ilcf->to = ngx_palloc(cf->pool, value[2].len - tl + 1);
    if (ilcf->to == NULL) {
        return NGX_CONF_ERROR;
    }
    p = ngx_copy(ilcf->to, value[2].data + tl, value[2].len - tl);
    *p = '\0';

    dd("iconv_filter:from=%s to=%s", ilcf->from, ilcf->to);
    return NGX_CONF_OK;
}


static char *
ngx_http_set_iconv_conf_handler(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ndk_set_var_t                filter;
    ngx_str_t                   *value, s[3];

    max_iconv_bufs = 256;

    filter.type = NDK_SET_VAR_MULTI_VALUE;
    filter.func = ngx_http_set_iconv_handler;
    filter.size = 3;

    /* set_iconv $dst $src from= to= */
    value = cf->args->elts;
    value++;
    s[0] = value[1];

    s[1] = value[2];
    if (ngx_strncasecmp((u_char *) "from=", s[1].data, sizeof("from=") - 1)
        != 0)
    {
        return NGX_CONF_ERROR;
    }
    s[1].data += sizeof("from=") - 1;
    s[1].len -= (sizeof("from=") - 1);

    s[2] = value[3];
    if (ngx_strncasecmp((u_char *) "to=", s[2].data, sizeof("to=") - 1) != 0) {
        return NGX_CONF_ERROR;
    }
    s[2].data += sizeof("to=") - 1;
    s[2].len -= (sizeof("to=") - 1);

    return ndk_set_var_multi_value_core(cf, value, s, &filter);
}


static ngx_int_t
ngx_http_set_iconv_handler(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{
    ngx_chain_t                 *cl, *chain;
    ngx_buf_t                   *buf;
    u_char                      *src, *dst, *p;
    size_t                       converted;
    u_char *                     end;
    ngx_http_iconv_loc_conf_t   *ilcf;
    ngx_int_t                    rc;

    if (v->len == 0) {
        res->data = NULL;
        res->len = 0;
        return NGX_OK;
    }

    ilcf = ngx_http_get_module_loc_conf(r, ngx_http_iconv_module);
    iconv_buf_size = ilcf->buf_size;
    dd("iconv_buf_size=%d", (int) iconv_buf_size);

    src = ngx_palloc(r->pool, v[1].len + 1);
    if (src == NULL) {
        return NGX_ERROR;
    }

    dst = ngx_palloc(r->pool, v[2].len + 1);
    if (dst == NULL) {
        return NGX_ERROR;
    }

    end = ngx_copy(src, v[1].data, v[1].len);
    *end = '\0';
    end = ngx_copy(dst, v[2].data, v[2].len);
    *end = '\0';

    dd("dst:%s\n, src:%s\n", dst, src);
    rc = ngx_http_do_iconv(r, &chain, v[0].data, v[0].len, src, dst,
            &converted, NULL);
    if (rc == NGX_ERROR) {
        dd("convert error");
        return NGX_ERROR;
    }
    res->data = ngx_palloc(r->pool, converted);
    if (res->data == NULL) {
        return NGX_ERROR;
    }

    p = res->data;
    res->len = converted;

    for (cl = chain; cl; cl = cl->next) {
        buf = cl->buf;
        dd("after convert, buf:%.*s", (int) (buf->last - buf->pos), buf->pos);
        p = ngx_copy(p, buf->pos, buf->last - buf->pos);
    }

    dd("%.*s\n%.*s\n%.*s\n",v[0].len, v[0].data,v[1].len, v[1].data, v[2].len,
            v[2].data);

    return NGX_OK;
}


static void *ngx_http_iconv_create_loc_conf(ngx_conf_t *cf)
{
    ngx_http_iconv_loc_conf_t       *ilcf;

    ilcf = ngx_palloc(cf->pool, sizeof(ngx_http_iconv_loc_conf_t));
    if (ilcf == NULL) {
        return NULL;
    }

    ilcf->buf_size = NGX_CONF_UNSET_SIZE;
    ilcf->enabled = NGX_CONF_UNSET;
    ilcf->skip_body_filter = NGX_CONF_UNSET;
    ilcf->from = NGX_CONF_UNSET_PTR;
    ilcf->to = NGX_CONF_UNSET_PTR;
    return ilcf;
}


static char
*ngx_http_iconv_merge_loc_conf(ngx_conf_t *cf, void *parent, void *child)
{
    ngx_http_iconv_loc_conf_t       *conf, *prev;

    conf = child;
    prev = parent;

    dd("before merge:conf->size=%d,prev->size=%d", (int) conf->buf_size,
            (int) prev->buf_size);
    if (conf->buf_size <= 1 || prev->buf_size <= 1) {
        ngx_log_error(NGX_LOG_ERR, cf->log, 0,
                "iconv_buffer_size must not less than 2 bytes");
        return NGX_CONF_ERROR;
    }
    ngx_conf_merge_size_value(conf->buf_size, prev->buf_size,
            (size_t) ngx_pagesize);
    dd("after merge:conf->size=%d,prev->size=%d", (int) conf->buf_size,
            (int) prev->buf_size);
    ngx_conf_merge_value(conf->enabled, prev->enabled, 0);
    ngx_conf_merge_value(conf->skip_body_filter, prev->skip_body_filter, 0);
    ngx_conf_merge_ptr_value(conf->from, (void *)prev->from, (char *) "utf-8");
    ngx_conf_merge_ptr_value(conf->to, (void *)prev->to, (char *) "gbk");
    return NGX_CONF_OK;
}
