#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_echo_filter.h"
#include "ngx_http_echo_util.h"
#include "ngx_http_echo_echo.h"

#include <ngx_log.h>



ngx_http_output_header_filter_pt ngx_http_echo_next_header_filter;

ngx_http_output_body_filter_pt ngx_http_echo_next_body_filter;

static ngx_int_t ngx_http_echo_header_filter(ngx_http_request_t *r);

static ngx_int_t ngx_http_echo_body_filter(ngx_http_request_t *r,
        ngx_chain_t *in);

/* filter handlers */
static ngx_int_t ngx_http_echo_exec_filter_cmds(ngx_http_request_t *r,
    ngx_http_echo_ctx_t *ctx, ngx_array_t *cmds, ngx_uint_t *iterator);


static volatile ngx_cycle_t  *ngx_http_echo_prev_cycle = NULL;


ngx_int_t
ngx_http_echo_filter_init(ngx_conf_t *cf)
{
    int                              multi_http_blocks;
    ngx_http_echo_main_conf_t       *emcf;

    emcf = ngx_http_conf_get_module_main_conf(cf, ngx_http_echo_module);

    if (ngx_http_echo_prev_cycle != ngx_cycle) {
        ngx_http_echo_prev_cycle = ngx_cycle;
        multi_http_blocks = 0;

    } else {
        multi_http_blocks = 1;
    }

    if (multi_http_blocks || emcf->requires_filter) {
        dd("top header filter: %ld",
           (unsigned long) ngx_http_top_header_filter);

        ngx_http_echo_next_header_filter = ngx_http_top_header_filter;
        ngx_http_top_header_filter = ngx_http_echo_header_filter;

        dd("top body filter: %ld", (unsigned long) ngx_http_top_body_filter);

        ngx_http_echo_next_body_filter = ngx_http_top_body_filter;
        ngx_http_top_body_filter  = ngx_http_echo_body_filter;
    }

    return NGX_OK;
}


static ngx_int_t
ngx_http_echo_header_filter(ngx_http_request_t *r)
{
    ngx_http_echo_loc_conf_t    *conf;
    ngx_http_echo_ctx_t         *ctx;

    ngx_log_debug2(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "echo header filter, uri \"%V?%V\"", &r->uri, &r->args);

    ctx = ngx_http_get_module_ctx(r, ngx_http_echo_module);

    /* XXX we should add option to insert contents for responses
     * of non-200 status code here... */
    /*
    if (r->headers_out.status != NGX_HTTP_OK) {
        if (ctx != NULL) {
            ctx->skip_filter = 1;
        }
        return ngx_http_echo_next_header_filter(r);
    }
    */

    conf = ngx_http_get_module_loc_conf(r, ngx_http_echo_module);
    if (conf->before_body_cmds == NULL && conf->after_body_cmds == NULL) {
        if (ctx != NULL) {
            ctx->skip_filter = 1;
        }
        return ngx_http_echo_next_header_filter(r);
    }

    if (ctx == NULL) {
        ctx = ngx_http_echo_create_ctx(r);
        if (ctx == NULL) {
            return NGX_ERROR;
        }

        ngx_http_set_ctx(r, ctx, ngx_http_echo_module);
    }

    /* enable streaming here (use chunked encoding) */
    ngx_http_clear_content_length(r);
    ngx_http_clear_accept_ranges(r);

    return ngx_http_echo_next_header_filter(r);
}


static ngx_int_t
ngx_http_echo_body_filter(ngx_http_request_t *r, ngx_chain_t *in)
{
    ngx_http_echo_ctx_t         *ctx;
    ngx_int_t                    rc;
    ngx_http_echo_loc_conf_t    *conf;
    unsigned                     last;
    ngx_chain_t                 *cl;
    ngx_buf_t                   *b;

    ngx_log_debug2(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "echo body filter, uri \"%V?%V\"", &r->uri, &r->args);

    if (in == NULL || r->header_only) {
        return ngx_http_echo_next_body_filter(r, in);
    }

    ctx = ngx_http_get_module_ctx(r, ngx_http_echo_module);

    if (ctx == NULL || ctx->skip_filter) {
        return ngx_http_echo_next_body_filter(r, in);
    }

    conf = ngx_http_get_module_loc_conf(r, ngx_http_echo_module);

    if (!ctx->before_body_sent) {
        ctx->before_body_sent = 1;

        if (conf->before_body_cmds != NULL) {
            rc = ngx_http_echo_exec_filter_cmds(r, ctx, conf->before_body_cmds,
                                                &ctx->next_before_body_cmd);
            if (rc != NGX_OK) {
                return NGX_ERROR;
            }
        }
    }

    if (conf->after_body_cmds == NULL) {
        ctx->skip_filter = 1;
        return ngx_http_echo_next_body_filter(r, in);
    }

    last = 0;

    for (cl = in; cl; cl = cl->next) {
        dd("cl %p, special %d", cl, ngx_buf_special(cl->buf));

        if (cl->buf->last_buf || cl->buf->last_in_chain) {
            cl->buf->last_buf = 0;
            cl->buf->last_in_chain = 0;
            cl->buf->sync = 1;
            last = 1;
        }
    }

    dd("in %p, last %d", in, (int) last);

    if (in) {
        rc = ngx_http_echo_next_body_filter(r, in);

#if 0
        if (rc == NGX_AGAIN) {
            return NGX_ERROR;
        }
#endif

        dd("next filter returns %d, last %d", (int) rc, (int) last);

        if (rc == NGX_ERROR || rc > NGX_OK || !last) {
            return rc;
        }
    }

    dd("exec filter cmds for after body cmds");

    rc = ngx_http_echo_exec_filter_cmds(r, ctx, conf->after_body_cmds,
                                        &ctx->next_after_body_cmd);
    if (rc == NGX_ERROR || rc > NGX_OK) {
        dd("FAILED: exec filter cmds for after body cmds");
        return NGX_ERROR;
    }

    ctx->skip_filter = 1;

    dd("after body cmds executed...terminating...");

    /* XXX we can NOT use
     * ngx_http_send_special(r, NGX_HTTP_LAST) here
     * because we should bypass the upstream filters. */

    b = ngx_calloc_buf(r->pool);
    if (b == NULL) {
        return NGX_ERROR;
    }

    if (r == r->main && !r->post_action) {
        b->last_buf = 1;

    } else {
        b->sync = 1;
        b->last_in_chain = 1;
    }

    cl = ngx_alloc_chain_link(r->pool);
    if (cl == NULL) {
        return NGX_ERROR;
    }

    cl->next = NULL;
    cl->buf = b;

    return ngx_http_echo_next_body_filter(r, cl);
}


static ngx_int_t
ngx_http_echo_exec_filter_cmds(ngx_http_request_t *r,
    ngx_http_echo_ctx_t *ctx, ngx_array_t *cmds, ngx_uint_t *iterator)
{
    ngx_int_t                    rc;
    ngx_array_t                 *opts = NULL;
    ngx_array_t                 *computed_args = NULL;
    ngx_http_echo_cmd_t         *cmd;
    ngx_http_echo_cmd_t         *cmd_elts;

    for (cmd_elts = cmds->elts; *iterator < cmds->nelts; (*iterator)++) {
        cmd = &cmd_elts[*iterator];

        /* evaluate arguments for the current cmd (if any) */
        if (cmd->args) {
            computed_args = ngx_array_create(r->pool, cmd->args->nelts,
                                             sizeof(ngx_str_t));
            if (computed_args == NULL) {
                return NGX_ERROR;
            }

            opts = ngx_array_create(r->pool, 1, sizeof(ngx_str_t));
            if (opts == NULL) {
                return NGX_ERROR;
            }

            rc = ngx_http_echo_eval_cmd_args(r, cmd, computed_args, opts);

            if (rc != NGX_OK) {
                ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                              "Failed to evaluate arguments for "
                              "the directive.");
                return rc;
            }
        }

        /* do command dispatch based on the opcode */
        switch (cmd->opcode) {
        case echo_opcode_echo_before_body:
        case echo_opcode_echo_after_body:
            dd("exec echo_before_body or echo_after_body...");

            rc = ngx_http_echo_exec_echo(r, ctx, computed_args,
                                         1 /* in filter */, opts);

            if (rc == NGX_ERROR || rc > NGX_OK) {
                return rc;
            }

            break;
        default:
            break;
        }
    }

    return NGX_OK;
}
