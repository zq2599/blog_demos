
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef _NGX_HTTP_SRCACHE_UTIL_H_INCLUDED_
#define _NGX_HTTP_SRCACHE_UTIL_H_INCLUDED_

#include "ngx_http_srcache_filter_module.h"


#define ngx_http_srcache_method_name(m) { sizeof(m) - 1, (u_char *) m " " }

#define ngx_http_srcache_strcmp_const(a, b) \
        ngx_strncmp(a, b, sizeof(b) - 1)

extern ngx_str_t  ngx_http_srcache_content_length_header_key;

extern ngx_str_t  ngx_http_srcache_get_method;
extern ngx_str_t  ngx_http_srcache_put_method;
extern ngx_str_t  ngx_http_srcache_post_method;
extern ngx_str_t  ngx_http_srcache_head_method;
extern ngx_str_t  ngx_http_srcache_copy_method;
extern ngx_str_t  ngx_http_srcache_move_method;
extern ngx_str_t  ngx_http_srcache_lock_method;
extern ngx_str_t  ngx_http_srcache_mkcol_method;
extern ngx_str_t  ngx_http_srcache_trace_method;
extern ngx_str_t  ngx_http_srcache_delete_method;
extern ngx_str_t  ngx_http_srcache_unlock_method;
extern ngx_str_t  ngx_http_srcache_options_method;
extern ngx_str_t  ngx_http_srcache_propfind_method;
extern ngx_str_t  ngx_http_srcache_proppatch_method;

ngx_int_t ngx_http_srcache_parse_method_name(ngx_str_t **method_name_ptr);
void ngx_http_srcache_discard_bufs(ngx_pool_t *pool, ngx_chain_t *in);
ngx_int_t ngx_http_srcache_adjust_subrequest(ngx_http_request_t *sr,
        ngx_http_srcache_parsed_request_t *parsed_sr);
ngx_int_t ngx_http_srcache_add_copy_chain(ngx_pool_t *pool,
        ngx_chain_t **chain, ngx_chain_t *in, unsigned *plast);
ngx_int_t ngx_http_srcache_post_request_at_head(ngx_http_request_t *r,
        ngx_http_posted_request_t *pr);
ngx_int_t ngx_http_srcache_request_no_cache(ngx_http_request_t *r,
        unsigned *no_store);
ngx_int_t ngx_http_srcache_response_no_cache(ngx_http_request_t *r,
        ngx_http_srcache_loc_conf_t *conf, ngx_http_srcache_ctx_t *ctx);
ngx_int_t ngx_http_srcache_process_status_line(ngx_http_request_t *r,
        ngx_buf_t *b);
ngx_int_t ngx_http_srcache_process_header(ngx_http_request_t *r,
        ngx_buf_t *b);
ngx_int_t ngx_http_srcache_store_response_header(ngx_http_request_t *r,
        ngx_http_srcache_ctx_t *ctx);
ngx_int_t ngx_http_srcache_hide_headers_hash(ngx_conf_t *cf,
        ngx_http_srcache_loc_conf_t *conf, ngx_http_srcache_loc_conf_t *prev,
        ngx_str_t *default_hide_headers, ngx_hash_init_t *hash);
ngx_int_t ngx_http_srcache_cmp_int(const void *one, const void *two);


#endif /* _NGX_HTTP_SRCACHE_UTIL_H_INCLUDED_ */

/* vi:set ft=c ts=4 sw=4 et fdm=marker: */
