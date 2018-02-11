
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef _NGX_HTTP_SRCACHE_FILTER_MODULE_H_INCLUDED_
#define _NGX_HTTP_SRCACHE_FILTER_MODULE_H_INCLUDED_


#include <ngx_core.h>
#include <ngx_http.h>
#include <nginx.h>


enum {
    NGX_HTTP_SRCACHE_FETCH_BYPASS = 0,
    NGX_HTTP_SRCACHE_FETCH_MISS   = 1,
    NGX_HTTP_SRCACHE_FETCH_HIT    = 2
};


enum {
    NGX_HTTP_SRCACHE_STORE_BYPASS = 0,
    NGX_HTTP_SRCACHE_STORE_STORE  = 1
};


extern ngx_module_t  ngx_http_srcache_filter_module;


typedef struct {
    ngx_uint_t                  method;
    ngx_str_t                   method_name;
    ngx_http_complex_value_t    location;
    ngx_http_complex_value_t    args;

} ngx_http_srcache_request_t;


typedef struct {
    ngx_uint_t                  method;
    ngx_str_t                   method_name;
    ngx_str_t                   location;
    ngx_str_t                   args;
    ngx_http_request_body_t    *request_body;
    ssize_t                     content_length_n;

} ngx_http_srcache_parsed_request_t;


typedef struct {
    ngx_http_srcache_request_t      *fetch;
    ngx_http_srcache_request_t      *store;
    size_t                           buf_size;
    size_t                           store_max_size;
    size_t                           header_buf_size;

    ngx_http_complex_value_t        *fetch_skip;
    ngx_http_complex_value_t        *store_skip;

    ngx_uint_t                       cache_methods;

    ngx_int_t                       *store_statuses;

    ngx_flag_t                       req_cache_control;
    ngx_flag_t                       resp_cache_control;

    ngx_flag_t                       store_private;
    ngx_flag_t                       store_no_store;
    ngx_flag_t                       store_no_cache;
    ngx_flag_t                       store_ranges;

    ngx_flag_t                       ignore_content_encoding;

    ngx_hash_t                       hide_headers_hash;
    ngx_array_t                     *hide_headers;
    ngx_array_t                     *pass_headers;

    time_t                           max_expire;
    time_t                           default_expire;

    unsigned                         postponed_to_access_phase_end:1;
    unsigned                         hide_content_type:1;
    unsigned                         hide_last_modified:1;
} ngx_http_srcache_loc_conf_t;


typedef struct {
    unsigned            postponed_to_access_phase_end;
    unsigned            module_used;
    ngx_hash_t          headers_in_hash;

} ngx_http_srcache_main_conf_t;


typedef struct ngx_http_srcache_ctx_s ngx_http_srcache_ctx_t;

typedef struct ngx_http_srcache_postponed_request_s
    ngx_http_srcache_postponed_request_t;


struct ngx_http_srcache_ctx_s {
    ngx_chain_t                     *body_from_cache;
    ngx_chain_t                     *body_to_cache;
    size_t                           response_length;
    size_t                           response_body_length;
    void                            *store_wev_handler_ctx;

    ngx_int_t                      (*process_header)(ngx_http_request_t *r,
                                        ngx_buf_t *b);

    time_t                           valid_sec;

    ngx_http_status_t                status;
    ngx_buf_t                       *header_buf;

    ngx_http_srcache_postponed_request_t  *postponed_requests;

    ngx_uint_t      http_status; /* the HTTP status code that has been
                                    actually sent */

    unsigned        waiting_subrequest:1;
    unsigned        request_done:1;
    unsigned        from_cache:1;
    /* unsigned      fetch_error:1; */
    unsigned        in_fetch_subrequest:1;
    unsigned        in_store_subrequest:1;
    unsigned        ignore_body:1;
    unsigned        parsing_cached_headers:1;
    unsigned        store_response:1;
    unsigned        store_skip:1;
    unsigned        issued_fetch_subrequest:1;
    unsigned        seen_subreq_eof:1;
    unsigned        waiting_request_body:1;
    unsigned        request_body_done:1;
};


struct ngx_http_srcache_postponed_request_s {
    ngx_http_srcache_postponed_request_t     *next;

    ngx_http_request_t              *request;
    ngx_http_srcache_ctx_t          *ctx;
    unsigned                         ready:1;
    unsigned                         done:1;
};


#endif  /* _NGX_HTTP_SRCACHE_FILTER_MODULE_H_INCLUDED_ */

/* vi:set ft=c ts=4 sw=4 et fdm=marker: */
