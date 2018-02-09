
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef _NGX_HTTP_SRCACHE_FETCH_H_INCLUDED_
#define _NGX_HTTP_SRCACHE_FETCH_H_INCLUDED_


#include "ngx_http_srcache_filter_module.h"


ngx_int_t ngx_http_srcache_access_handler(ngx_http_request_t *r);

ngx_int_t ngx_http_srcache_fetch_post_subrequest(ngx_http_request_t *r,
        void *data, ngx_int_t rc);


#endif /* _NGX_HTTP_SRCACHE_FETCH_H_INCLUDED_ */

/* vi:set ft=c ts=4 sw=4 et fdm=marker: */
