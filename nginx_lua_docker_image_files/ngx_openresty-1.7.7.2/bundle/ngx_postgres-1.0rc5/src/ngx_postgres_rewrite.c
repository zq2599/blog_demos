/*
 * Copyright (c) 2010, FRiCKLE Piotr Sikora <info@frickle.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#ifndef DDEBUG
#define DDEBUG 0
#endif

#include "ngx_postgres_ddebug.h"
#include "ngx_postgres_module.h"
#include "ngx_postgres_rewrite.h"


ngx_int_t
ngx_postgres_rewrite(ngx_http_request_t *r,
    ngx_postgres_rewrite_conf_t *pgrcf)
{
    ngx_postgres_rewrite_t  *rewrite;
    ngx_uint_t               i;

    dd("entering");

    if (pgrcf->methods_set & r->method) {
        /* method-specific */
        rewrite = pgrcf->methods->elts;
        for (i = 0; i < pgrcf->methods->nelts; i++) {
            if (rewrite[i].key & r->method) {
                dd("returning status:%d", (int) rewrite[i].status);
                return rewrite[i].status;
            }
        }
    } else if (pgrcf->def) {
        /* default */
        dd("returning status:%d", (int) pgrcf->def->status);
        return pgrcf->def->status;
    }

    dd("returning NGX_DECLINED");
    return NGX_DECLINED;
}

ngx_int_t
ngx_postgres_rewrite_changes(ngx_http_request_t *r,
    ngx_postgres_rewrite_conf_t *pgrcf)
{
    ngx_postgres_ctx_t  *pgctx;

    dd("entering");

    pgctx = ngx_http_get_module_ctx(r, ngx_postgres_module);

    if ((pgrcf->key % 2 == 0) && (pgctx->var_affected == 0)) {
        /* no_changes */
        dd("returning");
        return ngx_postgres_rewrite(r, pgrcf);
    }

    if ((pgrcf->key % 2 == 1) && (pgctx->var_affected > 0)) {
        /* changes */
        dd("returning");
        return ngx_postgres_rewrite(r, pgrcf);
    }

    dd("returning NGX_DECLINED");
    return NGX_DECLINED;
}

ngx_int_t
ngx_postgres_rewrite_rows(ngx_http_request_t *r,
    ngx_postgres_rewrite_conf_t *pgrcf)
{
    ngx_postgres_ctx_t  *pgctx;

    dd("entering");

    pgctx = ngx_http_get_module_ctx(r, ngx_postgres_module);

    if ((pgrcf->key % 2 == 0) && (pgctx->var_rows == 0)) {
        /* no_rows */
        dd("returning");
        return ngx_postgres_rewrite(r, pgrcf);
    }

    if ((pgrcf->key % 2 == 1) && (pgctx->var_rows > 0)) {
        /* rows */
        dd("returning");
        return ngx_postgres_rewrite(r, pgrcf);
    }

    dd("returning NGX_DECLINED");
    return NGX_DECLINED;
}
