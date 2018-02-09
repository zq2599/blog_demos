#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_redis2_reply.h"
#include "ngx_http_redis2_util.h"
#include <nginx.h>

%%{
    machine reply;

    include multi_bulk_reply "multi_bulk_reply.rl";

    main := single_line_reply @finalize
          | chunk @finalize
          | multi_bulk_reply
          ;

}%%

%% write data;

ngx_int_t
ngx_http_redis2_process_reply(ngx_http_redis2_ctx_t *ctx, ssize_t bytes)
{
    ngx_buf_t                *b;
    ngx_http_upstream_t      *u;
    ngx_str_t                 buf;
    ngx_flag_t                done;
    ngx_chain_t              *cl = NULL;
    ngx_chain_t             **ll = NULL;

    int                       cs;
    signed char              *p;
    signed char              *orig_p;
    ssize_t                   orig_len;
    signed char              *pe;

    u = ctx->request->upstream;
    b = &u->buffer;

    orig_p = (signed char *) b->last;
    orig_len = bytes;

    while (ctx->query_count) {
        done = 0;

        if (ctx->state == NGX_ERROR) {
            dd("init the state machine");

            %% write init;

            ctx->state = cs;

        } else {
            cs = ctx->state;
            dd("resumed the old state %d", cs);
        }

        p  = (signed char *) b->last;
        pe = (signed char *) b->last + bytes;

        dd("response body: %.*s", (int) bytes, p);

        %% write exec;

        dd("state after exec: %d, done: %d, %.*s", cs, (int) done,
            (int) (bytes - ((u_char *) p - b->last)), p);

        ctx->state = cs;

        if (!done && cs == reply_error) {
            if (cl) {
                cl->buf->last = cl->buf->pos;
                cl = NULL;
                *ll = NULL;
            }

            buf.data = b->pos;
            buf.len = b->last - b->pos + bytes;

            ngx_log_error(NGX_LOG_ERR, ctx->request->connection->log, 0,
                "Redis server returned invalid response near pos %z in "
                "\"%V\"",
                    (ssize_t) ((u_char *) p - b->pos), &buf);

            u->length = 0;

            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }

        if (cl == NULL) {
            for (cl = u->out_bufs, ll = &u->out_bufs; cl; cl = cl->next) {
                ll = &cl->next;
            }

            cl = ngx_chain_get_free_buf(ctx->request->pool, &u->free_bufs);
            if (cl == NULL) {
                u->length = 0;
                return NGX_ERROR;
            }

            cl->buf->flush = 1;
            cl->buf->memory = 1;

            *ll = cl;

            dd("response body: %.*s", (int) bytes, p);

            cl->buf->pos = b->last;
            cl->buf->last = (u_char *) p;
            cl->buf->tag = u->output.tag;

        } else {
            cl->buf->last = (u_char *) p;
        }

        bytes -= (ssize_t) ((u_char *) p - b->last);
        b->last = (u_char *) p;

        if (done) {
            dd("response parser done");

            ctx->query_count--;

            if (ctx->query_count == 0) {
                if (cs == reply_error) {
                    buf.data = (u_char *) p;
                    buf.len = orig_p - p + orig_len;

                    ngx_log_error(NGX_LOG_WARN, ctx->request->connection->log,
                        0, "Redis server returned extra bytes: \"%V\" (len %z)",
                        &buf, buf.len);

#if 0
                    if (cl) {
                        cl->buf->last = cl->buf->pos;
                        cl = NULL;
                        *ll = NULL;
                    }

                    u->length = 0;

                    return NGX_HTTP_INTERNAL_SERVER_ERROR;
#endif

                } else {
#if defined(nginx_version) && nginx_version >= 1001004
                    u->keepalive = 1;
#endif
                }

                u->length = 0;

                break;

            } else {
                ctx->state = NGX_ERROR;
                /* continue */
            }

        } else {
            /* need more data */
            break;
        }
    }

    return NGX_OK;
}
