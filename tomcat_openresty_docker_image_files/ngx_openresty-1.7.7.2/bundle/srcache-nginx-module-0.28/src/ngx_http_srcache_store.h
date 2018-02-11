
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef _NGX_HTTP_SRCACHE_STORE_H_INCLUDED_
#define _NGX_HTTP_SRCACHE_STORE_H_INCLUDED_


#include "ngx_http_srcache_filter_module.h"


extern ngx_http_output_header_filter_pt  ngx_http_srcache_next_header_filter;
extern ngx_http_output_body_filter_pt    ngx_http_srcache_next_body_filter;


ngx_int_t ngx_http_srcache_filter_init(ngx_conf_t *cf);


#endif /* _NGX_HTTP_SRCACHE_STORE_H_INCLUDED_ */

/* vi:set ft=c ts=4 sw=4 et fdm=marker: */
