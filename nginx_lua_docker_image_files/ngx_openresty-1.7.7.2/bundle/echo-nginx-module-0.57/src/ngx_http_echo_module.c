
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_echo_handler.h"
#include "ngx_http_echo_filter.h"
#include "ngx_http_echo_echo.h"
#include "ngx_http_echo_request_info.h"
#include "ngx_http_echo_var.h"
#include "ngx_http_echo_util.h"

#include <nginx.h>
#include <ngx_config.h>
#include <ngx_log.h>

/* config init handler */
static void * ngx_http_echo_create_loc_conf(ngx_conf_t *cf);
static char * ngx_http_echo_merge_loc_conf(ngx_conf_t *cf, void *parent,
    void *child);
static void *ngx_http_echo_create_main_conf(ngx_conf_t *cf);
static ngx_int_t ngx_http_echo_post_config(ngx_conf_t *cf);

/* config directive handlers */
static char * ngx_http_echo_echo(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf);
static char * ngx_http_echo_echo_request_body(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_echo_echo_sleep(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf);
static char * ngx_http_echo_echo_flush(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf);
static char * ngx_http_echo_echo_blocking_sleep(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_echo_echo_reset_timer(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_echo_echo_before_body(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_echo_echo_after_body(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_echo_echo_location_async(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_echo_echo_location(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_echo_echo_subrequest_async(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_echo_echo_subrequest(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_echo_echo_duplicate(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_echo_echo_read_request_body(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_echo_echo_foreach_split(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_echo_echo_end(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_echo_echo_abort_parent(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_echo_echo_exec(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_echo_helper(ngx_http_echo_opcode_t opcode,
    ngx_http_echo_cmd_category_t cat,
    ngx_conf_t *cf, ngx_command_t *cmd, void *conf);


static ngx_http_module_t ngx_http_echo_module_ctx = {
    NULL,                           /* preconfiguration */
    ngx_http_echo_post_config,      /* postconfiguration */

    ngx_http_echo_create_main_conf, /* create main configuration */
    NULL,                           /* init main configuration */

    NULL,                           /* create server configuration */
    NULL,                           /* merge server configuration */

    ngx_http_echo_create_loc_conf,  /* create location configuration */
    ngx_http_echo_merge_loc_conf    /* merge location configuration */
};


static ngx_command_t  ngx_http_echo_commands[] = {

    { ngx_string("echo"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_ANY,
      ngx_http_echo_echo,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, handler_cmds),
      NULL },

    { ngx_string("echo_request_body"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_NOARGS,
      ngx_http_echo_echo_request_body,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, handler_cmds),
      NULL },

    { ngx_string("echo_sleep"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
      ngx_http_echo_echo_sleep,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, handler_cmds),
      NULL },

    { ngx_string("echo_flush"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_NOARGS,
      ngx_http_echo_echo_flush,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, handler_cmds),
      NULL },

    { ngx_string("echo_blocking_sleep"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
      ngx_http_echo_echo_blocking_sleep,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, handler_cmds),
      NULL },

    { ngx_string("echo_reset_timer"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_NOARGS,
      ngx_http_echo_echo_reset_timer,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, handler_cmds),
      NULL },

    { ngx_string("echo_before_body"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_ANY,
      ngx_http_echo_echo_before_body,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, before_body_cmds),
      NULL },

    { ngx_string("echo_after_body"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_ANY,
      ngx_http_echo_echo_after_body,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, after_body_cmds),
      NULL },

    { ngx_string("echo_location_async"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
      ngx_http_echo_echo_location_async,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, handler_cmds),
      NULL },

    { ngx_string("echo_location"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
      ngx_http_echo_echo_location,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, handler_cmds),
      NULL },

    { ngx_string("echo_subrequest_async"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_2MORE,
      ngx_http_echo_echo_subrequest_async,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, handler_cmds),
      NULL },

    { ngx_string("echo_subrequest"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_2MORE,
      ngx_http_echo_echo_subrequest,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, handler_cmds),
      NULL },

    { ngx_string("echo_duplicate"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_2MORE,
      ngx_http_echo_echo_duplicate,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, handler_cmds),
      NULL },

    { ngx_string("echo_read_request_body"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_NOARGS,
      ngx_http_echo_echo_read_request_body,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, handler_cmds),
      NULL },

    { ngx_string("echo_foreach_split"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_2MORE,
      ngx_http_echo_echo_foreach_split,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, handler_cmds),
      NULL },

    { ngx_string("echo_end"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_NOARGS,
      ngx_http_echo_echo_end,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, handler_cmds),
      NULL },

    { ngx_string("echo_abort_parent"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_NOARGS,
      ngx_http_echo_echo_abort_parent,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, handler_cmds),
      NULL },

    { ngx_string("echo_exec"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
      ngx_http_echo_echo_exec,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, handler_cmds),
      NULL },

    { ngx_string("echo_status"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_num_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_echo_loc_conf_t, status),
      NULL },

      ngx_null_command
};


ngx_module_t ngx_http_echo_module = {
    NGX_MODULE_V1,
    &ngx_http_echo_module_ctx,     /* module context */
    ngx_http_echo_commands,        /* module directives */
    NGX_HTTP_MODULE,               /* module type */
    NULL,                          /* init master */
    NULL,                          /* init module */
    NULL,                          /* init process */
    NULL,                          /* init thread */
    NULL,                          /* exit thread */
    NULL,                          /* exit process */
    NULL,                          /* exit master */
    NGX_MODULE_V1_PADDING
};


static void *
ngx_http_echo_create_loc_conf(ngx_conf_t *cf)
{
    ngx_http_echo_loc_conf_t        *conf;

    conf = ngx_pcalloc(cf->pool, sizeof(ngx_http_echo_loc_conf_t));
    if (conf == NULL) {
        return NULL;
    }

    /* set by ngx_pcalloc
     *  conf->handler_cmds = NULL
     *  conf->before_body_cmds = NULL
     *  conf->after_body_cmds = NULL
     *  conf->seen_leading_output = 0
     *  conf->seen_trailing_output = 0
     */

    conf->status = NGX_CONF_UNSET;

    return conf;
}


static char *
ngx_http_echo_merge_loc_conf(ngx_conf_t *cf, void *parent, void *child)
{
    ngx_http_echo_loc_conf_t    *prev = parent;
    ngx_http_echo_loc_conf_t    *conf = child;

    if (conf->handler_cmds == NULL) {
        conf->handler_cmds = prev->handler_cmds;
        conf->seen_leading_output = prev->seen_leading_output;
    }

    if (conf->before_body_cmds == NULL) {
        conf->before_body_cmds = prev->before_body_cmds;
    }

    if (conf->after_body_cmds == NULL) {
        conf->after_body_cmds = prev->after_body_cmds;
    }

    ngx_conf_merge_value(conf->status, prev->status, 200);

    return NGX_CONF_OK;
}


static char *
ngx_http_echo_helper(ngx_http_echo_opcode_t opcode,
    ngx_http_echo_cmd_category_t cat,
    ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_str_t                       *raw_args;
    ngx_uint_t                       i, n;
    ngx_array_t                    **args_ptr;
    ngx_array_t                    **cmds_ptr;
    ngx_http_echo_cmd_t             *echo_cmd;
    ngx_http_core_loc_conf_t        *clcf;
    ngx_http_script_compile_t        sc;
    ngx_http_echo_main_conf_t       *emcf;
    ngx_http_echo_arg_template_t    *arg;

    emcf = ngx_http_conf_get_module_main_conf(cf, ngx_http_echo_module);

    /* cmds_ptr points to ngx_http_echo_loc_conf_t's
     * handler_cmds, before_body_cmds, or after_body_cmds
     * array, depending on the actual offset */
    cmds_ptr = (ngx_array_t**)(((u_char*)conf) + cmd->offset);

    if (*cmds_ptr == NULL) {
        *cmds_ptr = ngx_array_create(cf->pool, 1,
                                     sizeof(ngx_http_echo_cmd_t));

        if (*cmds_ptr == NULL) {
            return NGX_CONF_ERROR;
        }

        if (cat == echo_handler_cmd) {
            dd("registering the content handler");
            /* register the content handler */
            clcf = ngx_http_conf_get_module_loc_conf(cf,
                                                     ngx_http_core_module);

            dd("registering the content handler (2)");
            clcf->handler = ngx_http_echo_handler;

        } else {
            dd("filter used = 1");
            emcf->requires_filter = 1;
        }
    }

    echo_cmd = ngx_array_push(*cmds_ptr);

    if (echo_cmd == NULL) {
        return NGX_CONF_ERROR;
    }

    echo_cmd->opcode = opcode;

    args_ptr = &echo_cmd->args;
    *args_ptr = ngx_array_create(cf->pool, 1,
            sizeof(ngx_http_echo_arg_template_t));

    if (*args_ptr == NULL) {
        return NGX_CONF_ERROR;
    }

    raw_args = cf->args->elts;

    /* we skip the first arg and start from the second */

    for (i = 1 ; i < cf->args->nelts; i++) {
        arg = ngx_array_push(*args_ptr);

        if (arg == NULL) {
            return NGX_CONF_ERROR;
        }

        arg->raw_value = raw_args[i];

        dd("found raw arg %s", raw_args[i].data);

        arg->lengths = NULL;
        arg->values  = NULL;

        n = ngx_http_script_variables_count(&arg->raw_value);

        if (n > 0) {
            ngx_memzero(&sc, sizeof(ngx_http_script_compile_t));

            sc.cf = cf;
            sc.source = &arg->raw_value;
            sc.lengths = &arg->lengths;
            sc.values = &arg->values;
            sc.variables = n;
            sc.complete_lengths = 1;
            sc.complete_values = 1;

            if (ngx_http_script_compile(&sc) != NGX_OK) {
                return NGX_CONF_ERROR;
            }
        }
    } /* end for */

    return NGX_CONF_OK;
}


static char *
ngx_http_echo_echo(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_echo_loc_conf_t        *elcf = conf;

    if (!elcf->seen_leading_output) {
        elcf->seen_leading_output = 1;
    }

    dd("in echo_echo...");
    return ngx_http_echo_helper(echo_opcode_echo, echo_handler_cmd,
                                cf, cmd, conf);
}


static char *
ngx_http_echo_echo_request_body(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf)
{
    ngx_http_echo_loc_conf_t        *elcf = conf;

    if (!elcf->seen_leading_output) {
        elcf->seen_leading_output = 1;
    }

    dd("in echo_echo_request_body...");
    return ngx_http_echo_helper(echo_opcode_echo_request_body, echo_handler_cmd,
                                cf, cmd, conf);
}


static char *
ngx_http_echo_echo_sleep(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    dd("in echo_sleep...");
    return ngx_http_echo_helper(echo_opcode_echo_sleep, echo_handler_cmd,
                                cf, cmd, conf);
}


static char *
ngx_http_echo_echo_flush(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_echo_loc_conf_t        *elcf = conf;

    if (!elcf->seen_leading_output) {
        elcf->seen_leading_output = 1;
    }

    dd("in echo_flush...");
    return ngx_http_echo_helper(echo_opcode_echo_flush, echo_handler_cmd,
                                cf, cmd, conf);
}


static char *
ngx_http_echo_echo_blocking_sleep(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf)
{
    dd("in echo_blocking_sleep...");
    return ngx_http_echo_helper(echo_opcode_echo_blocking_sleep,
                                echo_handler_cmd, cf, cmd, conf);
}


static char *
ngx_http_echo_echo_reset_timer(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    return ngx_http_echo_helper(echo_opcode_echo_reset_timer, echo_handler_cmd,
                                cf, cmd, conf);
}


static char *
ngx_http_echo_echo_before_body(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    dd("processing echo_before_body directive...");
    return ngx_http_echo_helper(echo_opcode_echo_before_body, echo_filter_cmd,
                                cf, cmd, conf);
}


static char *
ngx_http_echo_echo_after_body(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    return ngx_http_echo_helper(echo_opcode_echo_after_body, echo_filter_cmd,
                                cf, cmd, conf);
}


static char *
ngx_http_echo_echo_location_async(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf)
{
    ngx_http_echo_loc_conf_t        *elcf = conf;
    char                            *ret;

    if (!elcf->seen_leading_output) {
        elcf->seen_leading_output = 1;

        ret = ngx_http_echo_helper(echo_opcode_echo_sync, echo_handler_cmd,
                                   cf, cmd, conf);

        if (ret != NGX_CONF_OK) {
            return ret;
        }
    }

    return ngx_http_echo_helper(echo_opcode_echo_location_async,
                                echo_handler_cmd, cf, cmd, conf);
}


static char *
ngx_http_echo_echo_location(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_echo_loc_conf_t        *elcf = conf;
    char                            *ret;

    if (!elcf->seen_leading_output) {
        elcf->seen_leading_output = 1;

        ret = ngx_http_echo_helper(echo_opcode_echo_sync, echo_handler_cmd,
                                   cf, cmd, conf);

        if (ret != NGX_CONF_OK) {
            return ret;
        }
    }

    return ngx_http_echo_helper(echo_opcode_echo_location, echo_handler_cmd,
                                cf, cmd, conf);
}


static char *
ngx_http_echo_echo_subrequest_async(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf)
{
    char                            *ret;
    ngx_http_echo_loc_conf_t        *elcf = conf;

    if (!elcf->seen_leading_output) {
        elcf->seen_leading_output = 1;

        ret = ngx_http_echo_helper(echo_opcode_echo_sync, echo_handler_cmd,
                                   cf, cmd, conf);

        if (ret != NGX_CONF_OK) {
            return ret;
        }
    }

    return ngx_http_echo_helper(echo_opcode_echo_subrequest_async,
                                echo_handler_cmd, cf, cmd, conf);
}


static char *
ngx_http_echo_echo_subrequest(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf)
{
    ngx_http_echo_loc_conf_t        *elcf = conf;
    char                            *ret;

    if (!elcf->seen_leading_output) {
        elcf->seen_leading_output = 1;

        ret = ngx_http_echo_helper(echo_opcode_echo_sync, echo_handler_cmd,
                                   cf, cmd, conf);

        if (ret != NGX_CONF_OK) {
            return ret;
        }
    }

    return ngx_http_echo_helper(echo_opcode_echo_subrequest, echo_handler_cmd,
                                cf, cmd, conf);
}


static char *
ngx_http_echo_echo_duplicate(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_echo_loc_conf_t        *elcf = conf;

    if (!elcf->seen_leading_output) {
        elcf->seen_leading_output = 1;
    }

    return ngx_http_echo_helper(echo_opcode_echo_duplicate, echo_handler_cmd,
                                cf, cmd, conf);
}


static char *
ngx_http_echo_echo_read_request_body(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf)
{
    return ngx_http_echo_helper(echo_opcode_echo_read_request_body,
                                echo_handler_cmd, cf, cmd, conf);
}


static char *
ngx_http_echo_echo_foreach_split(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    return ngx_http_echo_helper(echo_opcode_echo_foreach_split,
                                echo_handler_cmd, cf, cmd, conf);
}


static char *
ngx_http_echo_echo_end(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    return ngx_http_echo_helper(echo_opcode_echo_end, echo_handler_cmd, cf,
                                cmd, conf);
}


static char *
ngx_http_echo_echo_abort_parent(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    return ngx_http_echo_helper(echo_opcode_echo_abort_parent, echo_handler_cmd,
                                cf, cmd, conf);
}


static char *
ngx_http_echo_echo_exec(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    return ngx_http_echo_helper(echo_opcode_echo_exec, echo_handler_cmd,
                                cf, cmd, conf);
}


static void *
ngx_http_echo_create_main_conf(ngx_conf_t *cf)
{
    ngx_http_echo_main_conf_t    *emcf;

    emcf = ngx_pcalloc(cf->pool, sizeof(ngx_http_echo_main_conf_t));
    if (emcf == NULL) {
        return NULL;
    }

    /* set by ngx_pcalloc:
     *      hmcf->requires_filter = 0;
     */

    return emcf;
}


static ngx_int_t
ngx_http_echo_post_config(ngx_conf_t *cf)
{
    ngx_int_t         rc;

    rc = ngx_http_echo_filter_init(cf);
    if (rc != NGX_OK) {
        return rc;
    }

    rc = ngx_http_echo_echo_init(cf);
    if (rc != NGX_OK) {
        return rc;
    }

    ngx_http_echo_content_length_hash =
                                  ngx_http_echo_hash_literal("content-length");

    return ngx_http_echo_add_variables(cf);
}
