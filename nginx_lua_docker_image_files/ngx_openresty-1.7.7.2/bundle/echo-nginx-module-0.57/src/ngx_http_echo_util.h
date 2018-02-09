
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef NGX_HTTP_ECHO_UTIL_H
#define NGX_HTTP_ECHO_UTIL_H


#include "ngx_http_echo_module.h"


#define ngx_http_echo_strcmp_const(a, b) \
    ngx_strncmp(a, b, sizeof(b) - 1)


#define ngx_http_echo_hash_literal(s)                                        \
    ngx_http_echo_hash_str((u_char *) s, sizeof(s) - 1)


static ngx_inline ngx_uint_t
ngx_http_echo_hash_str(u_char *src, size_t n)
{
    ngx_uint_t  key;

    key = 0;

    while (n--) {
        key = ngx_hash(key, *src);
        src++;
    }

    return key;
}


extern ngx_uint_t  ngx_http_echo_content_length_hash;


ngx_http_echo_ctx_t * ngx_http_echo_create_ctx(ngx_http_request_t *r);
ngx_int_t ngx_http_echo_eval_cmd_args(ngx_http_request_t *r,
        ngx_http_echo_cmd_t *cmd, ngx_array_t *computed_args,
        ngx_array_t *opts);
ngx_int_t ngx_http_echo_send_header_if_needed(ngx_http_request_t* r,
        ngx_http_echo_ctx_t *ctx);
ngx_int_t ngx_http_echo_send_chain_link(ngx_http_request_t* r,
        ngx_http_echo_ctx_t *ctx, ngx_chain_t *cl);
ssize_t ngx_http_echo_atosz(u_char *line, size_t n);
u_char * ngx_http_echo_strlstrn(u_char *s1, u_char *last, u_char *s2, size_t n);
ngx_int_t ngx_http_echo_post_request_at_head(ngx_http_request_t *r,
        ngx_http_posted_request_t *pr);
u_char * ngx_http_echo_rebase_path(ngx_pool_t *pool, u_char *src, size_t osize,
        size_t *nsize);
ngx_int_t ngx_http_echo_flush_postponed_outputs(ngx_http_request_t *r);


#endif /* NGX_HTTP_ECHO_UTIL_H */
