/* Copyright (C) by agentzh */

#ifndef NGX_HTTP_ECHO_MODULE_H
#define NGX_HTTP_ECHO_MODULE_H


#include <ngx_core.h>
#include <ngx_http.h>
#include <nginx.h>


extern ngx_module_t  ngx_http_echo_module;


/* config directive's opcode */
typedef enum {
    echo_opcode_echo_sync,
    echo_opcode_echo,
    echo_opcode_echo_request_body,
    echo_opcode_echo_sleep,
    echo_opcode_echo_flush,
    echo_opcode_echo_blocking_sleep,
    echo_opcode_echo_reset_timer,
    echo_opcode_echo_before_body,
    echo_opcode_echo_after_body,
    echo_opcode_echo_location_async,
    echo_opcode_echo_location,
    echo_opcode_echo_subrequest_async,
    echo_opcode_echo_subrequest,
    echo_opcode_echo_duplicate,
    echo_opcode_echo_read_request_body,
    echo_opcode_echo_foreach_split,
    echo_opcode_echo_end,
    echo_opcode_echo_abort_parent,
    echo_opcode_echo_exec
} ngx_http_echo_opcode_t;


/* all the various config directives (or commands) are
 * divided into two categories: "handler commands",
 * and "filter commands". For instance, the "echo"
 * directive is a handler command while
 * "echo_before_body" is a filter one. */
typedef enum {
    echo_handler_cmd,
    echo_filter_cmd

} ngx_http_echo_cmd_category_t;


/* compiled form of a config directive argument's value */
typedef struct {
    /* holds the raw string of the argument value */
    ngx_str_t       raw_value;

    /* fields "lengths" and "values" are set by
     * the function ngx_http_script_compile,
     * iff the argument value indeed contains
     * nginx variables like "$foo" */
    ngx_array_t     *lengths;
    ngx_array_t     *values;

} ngx_http_echo_arg_template_t;


/* represent a config directive (or command) like "echo". */
typedef struct {
    ngx_http_echo_opcode_t      opcode;

    /* each argument is of type echo_arg_template_t: */
    ngx_array_t                 *args;
} ngx_http_echo_cmd_t;


/* location config struct */
typedef struct {
    /* elements of the following arrays are of type
     * ngx_http_echo_cmd_t */
    ngx_array_t     *handler_cmds;
    ngx_array_t     *before_body_cmds;
    ngx_array_t     *after_body_cmds;

    unsigned         seen_leading_output;

    ngx_int_t        status;
} ngx_http_echo_loc_conf_t;


typedef struct {
    ngx_int_t       requires_filter;
} ngx_http_echo_main_conf_t;


typedef struct {
    ngx_array_t     *choices; /* items after splitting */
    ngx_uint_t      next_choice;  /* current item index */
    ngx_uint_t      cmd_index; /* cmd index for the echo_foreach direcitve */
} ngx_http_echo_foreach_ctx_t;


/* context struct in the request handling cycle, holding
 * the current states of the command evaluator */
typedef struct {
    /* index of the next handler command in
     * ngx_http_echo_loc_conf_t's "handler_cmds" array. */
    ngx_uint_t       next_handler_cmd;

    /* index of the next before-body filter command in
     * ngx_http_echo_loc_conf_t's "before_body_cmds" array. */
    ngx_uint_t       next_before_body_cmd;

    /* index of the next after-body filter command in
     * ngx_http_echo_loc_conf_t's "after_body_cmds" array. */
    ngx_uint_t       next_after_body_cmd;

    ngx_http_echo_foreach_ctx_t   *foreach;

    ngx_time_t       timer_begin;

    ngx_event_t      sleep;

    ngx_uint_t       counter;

    unsigned         before_body_sent:1;
    unsigned         skip_filter:1;

    unsigned         wait_read_request_body:1;

    unsigned         waiting:1;
    unsigned         done:1;

    unsigned         run_post_subrequest:1;

} ngx_http_echo_ctx_t;


#endif /* NGX_HTTP_ECHO_MODULE_H */
