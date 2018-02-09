
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef NGX_HTTP_DRIZZLE_MODULE_H
#define NGX_HTTP_DRIZZLE_MODULE_H


#include <ngx_config.h>
#include <nginx.h>
#include <ngx_http.h>


/* XXX nginx undefines "bool", which breaks the libdrizzle 1.0 API
 * which makes use of "bool" */
#if defined(__GNUC__)
#   ifndef bool
#       define bool _Bool
#   endif
#endif

#include <libdrizzle/drizzle_client.h>


#ifdef _WIN32
/* remove the bad macros defined in libdrizzle/drizzle.h */
#   undef close
#   undef snprintf
#endif


#ifndef NGX_HTTP_GONE
#define NGX_HTTP_GONE 410
#endif

#define ngx_http_drizzle_module_version 1008
#define ngx_http_drizzle_module_version_string \
    "0.1.7"

extern ngx_module_t ngx_http_drizzle_module;


typedef struct {
    ngx_uint_t                          key;
    ngx_str_t                           sv;
    ngx_http_complex_value_t           *cv;
} ngx_drizzle_mixed_t;


typedef struct {
    u_char                             *name;
    uint32_t                            key;
} ngx_drizzle_http_method_t;


typedef struct {
    ngx_http_upstream_conf_t             upstream;

    /* drizzle database name */
    ngx_http_complex_value_t            *dbname;

    /* SQL query to be executed */
    ngx_drizzle_mixed_t                 *default_query;
    ngx_uint_t                           methods_set;
    ngx_array_t                         *queries;

    ngx_msec_t                           recv_cols_timeout;
    ngx_msec_t                           recv_rows_timeout;

    ngx_flag_t                           enable_module_header;

    /* for quoting */
    ngx_array_t                         *vars_to_quote;
                /* of ngx_http_drizzle_var_to_quote_t */

    ngx_array_t                         *user_types;
                /* of ngx_http_drizzle_var_type_t */

    ngx_http_complex_value_t            *complex_target;

    size_t                               buf_size;

    ngx_int_t                            tid_var_index; /* thread id variable
                                                           index */
} ngx_http_drizzle_loc_conf_t;


#if defined(nginx_version) && (nginx_version < 8017)
typedef struct {
    ngx_int_t                           status;
} ngx_http_drizzle_ctx_t;
#endif


/* states for the drizzle client state machine */
typedef enum {
    state_db_connect,
    state_db_send_query,
    state_db_recv_cols,
    state_db_recv_rows,
    state_db_idle

} ngx_http_drizzle_state_t;


#endif /* NGX_HTTP_DRIZZLE_MODULE_H */
