/*
 * Copyright (c) 2010, FRiCKLE Piotr Sikora <info@frickle.com>
 * Copyright (c) 2009-2010, Xiaozhe Wang <chaoslawful@gmail.com>
 * Copyright (c) 2009-2010, Yichun Zhang <agentzh@gmail.com>
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

#ifndef _NGX_POSTGRES_OUTPUT_H_
#define _NGX_POSTGRES_OUTPUT_H_

#include <ngx_core.h>
#include <ngx_http.h>
#include <libpq-fe.h>

#include "ngx_postgres_module.h"
#include "resty_dbd_stream.h"


ngx_int_t        ngx_postgres_output_value(ngx_http_request_t *, PGresult *);
ngx_int_t        ngx_postgres_output_text(ngx_http_request_t *, PGresult *);
ngx_int_t        ngx_postgres_output_rds(ngx_http_request_t *, PGresult *);
ngx_chain_t     *ngx_postgres_render_rds_header(ngx_http_request_t *,
                     ngx_pool_t *, PGresult *, ngx_int_t, ngx_int_t);
ngx_chain_t     *ngx_postgres_render_rds_columns(ngx_http_request_t *,
                     ngx_pool_t *, PGresult *, ngx_int_t);
ngx_chain_t     *ngx_postgres_render_rds_row(ngx_http_request_t *, ngx_pool_t *,
                     PGresult *, ngx_int_t, ngx_int_t, ngx_int_t);
ngx_chain_t     *ngx_postgres_render_rds_row_terminator(ngx_http_request_t *,
                     ngx_pool_t *);
ngx_int_t        ngx_postgres_output_chain(ngx_http_request_t *, ngx_chain_t *);
rds_col_type_t   ngx_postgres_rds_col_type(Oid);

#endif /* _NGX_POSTGRES_OUTPUT_H_ */
