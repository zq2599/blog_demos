
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef NGX_HTTP_MEMC_MODULE_H
#define NGX_HTTP_MEMC_MODULE_H


#include <ngx_core.h>
#include <ngx_http.h>
#include <nginx.h>


typedef enum {
    ngx_http_memc_cmd_set,
    ngx_http_memc_cmd_add,
    ngx_http_memc_cmd_replace,
    ngx_http_memc_cmd_append,
    ngx_http_memc_cmd_prepend,
    /* ngx_http_memc_cmd_cas, */

    ngx_http_memc_cmd_get,
    /* ngx_http_memc_cmd_gets, */

    ngx_http_memc_cmd_delete,

    ngx_http_memc_cmd_incr,
    ngx_http_memc_cmd_decr,

    ngx_http_memc_cmd_stats,

    ngx_http_memc_cmd_flush_all,
    ngx_http_memc_cmd_version,
    /* ngx_http_memc_cmd_verbosity, */

    /* we do not want to support the "quit" command here */
    /* ngx_http_memc_cmd_quit, */

    ngx_http_memc_cmd_unknown
} ngx_http_memc_cmd_t;


typedef struct {
    ngx_flag_t                       flags_to_last_modified;
    ngx_http_upstream_conf_t         upstream;
    ngx_array_t                     *cmds_allowed;
    ngx_http_complex_value_t        *complex_target;

} ngx_http_memc_loc_conf_t;


typedef struct {
    ngx_int_t       key_index;
    ngx_int_t       cmd_index;
    ngx_int_t       value_index;
    ngx_int_t       flags_index;
    ngx_int_t       exptime_index;
    ngx_int_t       module_used;
} ngx_http_memc_main_conf_t;


typedef struct {
#if defined(nginx_version) && nginx_version >= 1001004
    off_t                      rest;
#else
    size_t                     rest;
#endif

    ngx_http_request_t        *request;
    ngx_str_t                  key;

    ngx_str_t                  cmd_str;
    ngx_http_memc_cmd_t        cmd;

    ngx_http_variable_value_t  *memc_value_vv;
    ngx_http_variable_value_t  *memc_key_vv;
    ngx_http_variable_value_t  *memc_flags_vv;
    ngx_http_variable_value_t  *memc_exptime_vv;

    ngx_flag_t                 is_storage_cmd;

    int                        parser_state;

    /* just for the subrequests in memory support */
    size_t                     body_length;

} ngx_http_memc_ctx_t;


extern ngx_module_t  ngx_http_memc_module;


#define NGX_HTTP_MEMC_END   (sizeof(CRLF "END" CRLF) - 1)


#endif /* NGX_HTTP_MEMC_MODULE_H */
