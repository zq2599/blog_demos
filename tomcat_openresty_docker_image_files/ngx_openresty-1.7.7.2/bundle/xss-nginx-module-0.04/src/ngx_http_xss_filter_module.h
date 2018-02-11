#ifndef NGX_HTTP_XSS_FILTER_MODULE_H
#define NGX_HTTP_XSS_FILTER_MODULE_H


#include <ngx_core.h>
#include <ngx_http.h>


typedef struct {
    ngx_str_t       callback_arg;

    ngx_hash_t      input_types;
    ngx_array_t    *input_types_keys;

    ngx_str_t       output_type;

    ngx_flag_t      get_enabled;
    ngx_flag_t      check_status;
    ngx_flag_t      override_status;

} ngx_http_xss_loc_conf_t;


typedef struct {
    ngx_int_t       requires_filter;

} ngx_http_xss_main_conf_t;


typedef struct {
    ngx_str_t       callback;
    ngx_flag_t      before_body_sent;

} ngx_http_xss_ctx_t;


#endif /* NGX_HTTP_XSS_FILTER_MODULE_H */

