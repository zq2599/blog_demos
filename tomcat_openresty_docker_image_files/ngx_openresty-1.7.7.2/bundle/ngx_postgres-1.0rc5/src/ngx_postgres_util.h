/*
 * Copyright (c) 2010, FRiCKLE Piotr Sikora <info@frickle.com>
 * Copyright (c) 2009-2010, Yichun Zhang <agentzh@gmail.com>
 * Copyright (C) 2002-2010, Igor Sysoev
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
 * THIS SOFTWARE IS PROVIDED BY AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

#ifndef _NGX_POSTGRES_UTIL_H_
#define _NGX_POSTGRES_UTIL_H_

#include <ngx_core.h>
#include <ngx_http.h>


extern ngx_module_t  ngx_http_rewrite_module;


typedef struct {
    ngx_array_t  *codes;        /* uintptr_t */

    ngx_uint_t    stack_size;

    ngx_flag_t    log;
    ngx_flag_t    uninitialized_variable_warn;
} ngx_postgres_rewrite_loc_conf_t;


void       ngx_postgres_upstream_finalize_request(ngx_http_request_t *,
               ngx_http_upstream_t *, ngx_int_t);
void       ngx_postgres_upstream_next(ngx_http_request_t *,
               ngx_http_upstream_t *, ngx_int_t);
ngx_int_t  ngx_postgres_upstream_test_connect(ngx_connection_t *);

ngx_int_t  ngx_postgres_rewrite_var(ngx_http_request_t *,
               ngx_http_variable_value_t *, uintptr_t);
char      *ngx_postgres_rewrite_value(ngx_conf_t *,
               ngx_postgres_rewrite_loc_conf_t *, ngx_str_t *);

#endif /* _NGX_POSTGRES_UTIL_H_ */
