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

#ifndef _NGX_POSTGRES_PROCESSOR_H_
#define _NGX_POSTGRES_PROCESSOR_H_

#include <ngx_core.h>
#include <ngx_http.h>
#include <libpq-fe.h>

#include "ngx_postgres_upstream.h"


void       ngx_postgres_process_events(ngx_http_request_t *);
ngx_int_t  ngx_postgres_upstream_connect(ngx_http_request_t *,
               ngx_connection_t *, ngx_postgres_upstream_peer_data_t *);
ngx_int_t  ngx_postgres_upstream_send_query(ngx_http_request_t *,
               ngx_connection_t *, ngx_postgres_upstream_peer_data_t *);
ngx_int_t  ngx_postgres_upstream_get_result(ngx_http_request_t *,
               ngx_connection_t *, ngx_postgres_upstream_peer_data_t *);
ngx_int_t  ngx_postgres_process_response(ngx_http_request_t *, PGresult *);
ngx_int_t  ngx_postgres_upstream_get_ack(ngx_http_request_t *,
               ngx_connection_t *, ngx_postgres_upstream_peer_data_t *);
ngx_int_t  ngx_postgres_upstream_done(ngx_http_request_t *,
               ngx_http_upstream_t *, ngx_postgres_upstream_peer_data_t *);

#endif /* _NGX_POSTGRES_PROCESSOR_H_ */
