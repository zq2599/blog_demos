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

#ifndef _NGX_COOLKIT_MODULE_H_
#define _NGX_COOLKIT_MODULE_H_

#include <nginx.h>
#include <ngx_core.h>
#include <ngx_http.h>


extern ngx_module_t        ngx_coolkit_module;
extern ngx_conf_bitmask_t  ngx_coolkit_http_methods[];


typedef struct {
    /* override_method */
    ngx_uint_t                 override_methods;
    ngx_http_complex_value_t  *override_source;
} ngx_coolkit_loc_conf_t;

typedef struct {
    /* override_method */
    ngx_uint_t                 overridden_method;
    ngx_str_t                  overridden_method_name;
} ngx_coolkit_ctx_t;


ngx_int_t   ngx_coolkit_add_variables(ngx_conf_t *);
ngx_int_t   ngx_coolkit_init(ngx_conf_t *);
void       *ngx_coolkit_create_loc_conf(ngx_conf_t *);
char       *ngx_coolkit_merge_loc_conf(ngx_conf_t *, void *, void *);
char       *ngx_coolkit_conf_override_method(ngx_conf_t *, ngx_command_t *,
                void *);

#endif /* _NGX_COOLKIT_MODULE_H_ */
