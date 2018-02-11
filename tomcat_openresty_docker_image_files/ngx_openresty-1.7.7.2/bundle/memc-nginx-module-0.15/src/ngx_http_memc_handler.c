
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_memc_handler.h"
#include "ngx_http_memc_module.h"
#include "ngx_http_memc_request.h"
#include "ngx_http_memc_response.h"
#include "ngx_http_memc_util.h"


static ngx_int_t ngx_http_memc_flags_as_http_time_variable(
    ngx_http_request_t *r, ngx_http_variable_value_t *v, uintptr_t data);


static ngx_http_variable_t ngx_http_memc_variables[] = {

    { ngx_string("memc_flags_as_http_time"), NULL,
      ngx_http_memc_flags_as_http_time_variable, 0,
      0, 0 },

    { ngx_null_string, NULL, NULL, 0, 0, 0 }
};


static ngx_str_t  ngx_http_memc_key = ngx_string("memc_key");
static ngx_str_t  ngx_http_memc_cmd = ngx_string("memc_cmd");
static ngx_str_t  ngx_http_memc_value = ngx_string("memc_value");
static ngx_str_t  ngx_http_memc_flags = ngx_string("memc_flags");
static ngx_str_t  ngx_http_memc_exptime = ngx_string("memc_exptime");


static ngx_int_t ngx_http_memc_add_more_variables(ngx_conf_t *cf);
static ngx_int_t ngx_http_memc_variable_not_found(ngx_http_request_t *r,
        ngx_http_variable_value_t *v, uintptr_t data);
static ngx_int_t ngx_http_memc_add_variable(ngx_conf_t *cf, ngx_str_t *name);
static ngx_flag_t ngx_http_memc_in_cmds_allowed(ngx_http_memc_loc_conf_t *mlcf,
        ngx_http_memc_cmd_t memc_cmd);
static ngx_int_t ngx_http_memc_reinit_request(ngx_http_request_t *r);
static void ngx_http_memc_abort_request(ngx_http_request_t *r);
static void ngx_http_memc_finalize_request(ngx_http_request_t *r,
    ngx_int_t rc);
static ngx_flag_t ngx_http_memc_valid_uint32_str(u_char *data, size_t len);
static ngx_flag_t ngx_http_memc_valid_uint64_str(u_char *data, size_t len);


ngx_int_t
ngx_http_memc_handler(ngx_http_request_t *r)
{
    ngx_int_t                       rc;
    ngx_http_upstream_t            *u;
    ngx_http_memc_ctx_t            *ctx;
    ngx_http_memc_loc_conf_t       *mlcf;
    ngx_http_memc_main_conf_t      *mmcf;
    ngx_str_t                       target;
    ngx_url_t                       url;

    ngx_http_variable_value_t      *cmd_vv;
    ngx_http_variable_value_t      *key_vv;
    ngx_http_variable_value_t      *value_vv;
    ngx_http_variable_value_t      *flags_vv;
    ngx_http_variable_value_t      *exptime_vv;

    ngx_http_memc_cmd_t             memc_cmd;
    ngx_flag_t                      is_storage_cmd = 0;

    dd("memc handler");

    mmcf = ngx_http_get_module_main_conf(r, ngx_http_memc_module);

    key_vv = ngx_http_get_indexed_variable(r, mmcf->key_index);
    if (key_vv == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    cmd_vv = ngx_http_get_indexed_variable(r, mmcf->cmd_index);
    if (cmd_vv == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    if (cmd_vv->not_found || cmd_vv->len == 0) {
        dd("variable $memc_cmd not found");
        cmd_vv->not_found = 0;
        cmd_vv->valid = 1;
        cmd_vv->no_cacheable = 0;

        if (r->method & (NGX_HTTP_GET|NGX_HTTP_HEAD)) {
            cmd_vv->len = sizeof("get") - 1;
            cmd_vv->data = (u_char*) "get";
            memc_cmd = ngx_http_memc_cmd_get;

        } else if (r->method == NGX_HTTP_POST) {
            cmd_vv->len = sizeof("add") - 1;
            cmd_vv->data = (u_char*) "add";
            memc_cmd = ngx_http_memc_cmd_add;
            is_storage_cmd = 1;

        } else if (r->method == NGX_HTTP_PUT) {
            cmd_vv->len = sizeof("set") - 1;
            cmd_vv->data = (u_char*) "set";
            memc_cmd = ngx_http_memc_cmd_set;
            is_storage_cmd = 1;

        } else if (r->method == NGX_HTTP_DELETE) {
            cmd_vv->len = sizeof("delete") - 1;
            cmd_vv->data = (u_char*) "delete";
            memc_cmd = ngx_http_memc_cmd_delete;

        } else {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "ngx_memc: $memc_cmd variable not found for HTTP "
                          "%V requests", &r->method_name);

            return NGX_HTTP_BAD_REQUEST;
        }

    } else {

        memc_cmd = ngx_http_memc_parse_cmd(cmd_vv->data, cmd_vv->len,
                                           &is_storage_cmd);

        if (memc_cmd == ngx_http_memc_cmd_unknown) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "ngx_memc: unknown $memc_cmd \"%v\"", cmd_vv);
            return NGX_HTTP_BAD_REQUEST;
        }
    }

    mlcf = ngx_http_get_module_loc_conf(r, ngx_http_memc_module);

    dd("XXX connect timeout %d", (int) mlcf->upstream.connect_timeout);

    if (!ngx_http_memc_in_cmds_allowed(mlcf, memc_cmd)) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "ngx_memc: memcached command \"%v\" not allowed",
                      cmd_vv);

        return NGX_HTTP_FORBIDDEN;
    }

    if (ngx_http_set_content_type(r) != NGX_OK) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

#if defined(nginx_version) && \
    ((nginx_version >= 7063 && nginx_version < 8000) \
     || nginx_version >= 8007)

    if (ngx_http_upstream_create(r) != NGX_OK) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    u = r->upstream;

#else /* 0.7.x < 0.7.63, 0.8.x < 0.8.7 */

    u = ngx_pcalloc(r->pool, sizeof(ngx_http_upstream_t));
    if (u == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    u->peer.log = r->connection->log;
    u->peer.log_error = NGX_ERROR_ERR;
#  if (NGX_THREADS)
    u->peer.lock = &r->connection->lock;
#  endif

    r->upstream = u;

#endif

    if (mlcf->complex_target) {
        /* variables used in the memc_pass directive */
        if (ngx_http_complex_value(r, mlcf->complex_target, &target)
            != NGX_OK)
        {
            dd("failed to compile");
            return NGX_ERROR;
        }

        if (target.len == 0) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "memc: handler: empty \"memc_pass\" target");
            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }

        url.host = target;
        url.port = 0;
        url.no_resolve = 1;

        mlcf->upstream.upstream = ngx_http_memc_upstream_add(r, &url);

        if (mlcf->upstream.upstream == NULL) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "memc: upstream \"%V\" not found", &target);
            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }
    }

    u->schema.len = sizeof("memcached://") - 1;
    u->schema.data = (u_char *) "memcached://";

    u->output.tag = (ngx_buf_tag_t) &ngx_http_memc_module;

    u->conf = &mlcf->upstream;

    ctx = ngx_palloc(r->pool, sizeof(ngx_http_memc_ctx_t));
    if (ctx == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    ctx->body_length = 0;
    ctx->memc_key_vv = key_vv;
    ctx->memc_value_vv = NULL;
    ctx->memc_flags_vv = NULL;

    ctx->parser_state = NGX_ERROR;

    ctx->rest = NGX_HTTP_MEMC_END;
    ctx->request = r;

    ctx->cmd_str.data = cmd_vv->data;
    ctx->cmd_str.len  = cmd_vv->len;

    ctx->cmd = memc_cmd;

    ctx->is_storage_cmd = is_storage_cmd;

    ngx_http_set_ctx(r, ctx, ngx_http_memc_module);

    if (is_storage_cmd) {
        u->create_request = ngx_http_memc_create_storage_cmd_request;
        u->process_header = ngx_http_memc_process_simple_header;

        u->input_filter_init = ngx_http_memc_empty_filter_init;
        u->input_filter = ngx_http_memc_empty_filter;

    } else if (memc_cmd == ngx_http_memc_cmd_get) {
        u->create_request = ngx_http_memc_create_get_cmd_request;
        u->process_header = ngx_http_memc_process_get_cmd_header;

        u->input_filter_init = ngx_http_memc_get_cmd_filter_init;
        u->input_filter = ngx_http_memc_get_cmd_filter;

    } else if (memc_cmd == ngx_http_memc_cmd_flush_all) {
        u->create_request = ngx_http_memc_create_flush_all_cmd_request;
        u->process_header = ngx_http_memc_process_simple_header;

        u->input_filter_init = ngx_http_memc_empty_filter_init;
        u->input_filter = ngx_http_memc_empty_filter;

    } else if (memc_cmd == ngx_http_memc_cmd_version
            || memc_cmd == ngx_http_memc_cmd_stats)
    {
        u->create_request = ngx_http_memc_create_noarg_cmd_request;
        u->process_header = ngx_http_memc_process_simple_header;

        u->input_filter_init = ngx_http_memc_empty_filter_init;
        u->input_filter = ngx_http_memc_empty_filter;

    } else if (memc_cmd == ngx_http_memc_cmd_delete) {
        u->create_request = ngx_http_memc_create_delete_cmd_request;
        u->process_header = ngx_http_memc_process_simple_header;

        u->input_filter_init = ngx_http_memc_empty_filter_init;
        u->input_filter = ngx_http_memc_empty_filter;

    } else if (memc_cmd == ngx_http_memc_cmd_incr
            || memc_cmd == ngx_http_memc_cmd_decr) {
        u->create_request = ngx_http_memc_create_incr_decr_cmd_request;
        u->process_header = ngx_http_memc_process_simple_header;

        u->input_filter_init = ngx_http_memc_empty_filter_init;
        u->input_filter = ngx_http_memc_empty_filter;

    } else {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
            "assertion failed: command \"%v\" does not have proper "
            "handlers.", cmd_vv);

        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    u->reinit_request = ngx_http_memc_reinit_request;
    u->abort_request = ngx_http_memc_abort_request;
    u->finalize_request = ngx_http_memc_finalize_request;

    u->input_filter_ctx = ctx;

    if (is_storage_cmd
            || memc_cmd == ngx_http_memc_cmd_flush_all
            || memc_cmd == ngx_http_memc_cmd_delete)
    {
        exptime_vv = ngx_http_get_indexed_variable(r, mmcf->exptime_index);

        if (exptime_vv == NULL) {
            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }

        ctx->memc_exptime_vv = exptime_vv;

        if (!exptime_vv->not_found
            && exptime_vv->len
            && !ngx_http_memc_valid_uint32_str(exptime_vv->data,
                                               exptime_vv->len))
        {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "variable \"$memc_exptime\" takes invalid value: %v",
                          exptime_vv);

            return NGX_HTTP_BAD_REQUEST;
        }
    }

    if (is_storage_cmd || memc_cmd == ngx_http_memc_cmd_get) {
        flags_vv = ngx_http_get_indexed_variable(r, mmcf->flags_index);
        if (flags_vv == NULL) {
            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }

        ctx->memc_flags_vv = flags_vv;

        if (is_storage_cmd
            && !flags_vv->not_found
            && flags_vv->len
            && !ngx_http_memc_valid_uint32_str(flags_vv->data, flags_vv->len))
        {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "variable \"$memc_flags\" takes invalid value: %v",
                          flags_vv);

            return NGX_HTTP_BAD_REQUEST;
        }
    }

    if (is_storage_cmd
        || memc_cmd == ngx_http_memc_cmd_incr
        || memc_cmd == ngx_http_memc_cmd_decr)
    {
        value_vv = ngx_http_get_indexed_variable(r, mmcf->value_index);
        if (value_vv == NULL) {
            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }

        if (memc_cmd == ngx_http_memc_cmd_incr
            || memc_cmd == ngx_http_memc_cmd_decr)
        {
            if (value_vv->not_found || value_vv->len == 0) {
                ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                              "the \"$memc_value\" variable is required for "
                              "command \"%V\"", &ctx->cmd_str);

                return NGX_HTTP_BAD_REQUEST;
            }

            if (!ngx_http_memc_valid_uint64_str(value_vv->data,
                                                value_vv->len))
            {
                ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                              "variable \"$memc_value\" is invalid for "
                              "incr/decr: %v", value_vv);

                return NGX_HTTP_BAD_REQUEST;
            }
        }

        ctx->memc_value_vv = value_vv;
    }

    rc = ngx_http_read_client_request_body(r, ngx_http_upstream_init);

    if (rc == NGX_ERROR || rc > NGX_OK) {
        return rc;
    }

    return NGX_DONE;
}


static ngx_int_t
ngx_http_memc_reinit_request(ngx_http_request_t *r)
{
    return NGX_OK;
}


static void
ngx_http_memc_abort_request(ngx_http_request_t *r)
{
    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "abort http memcached request");
    return;
}


static void
ngx_http_memc_finalize_request(ngx_http_request_t *r, ngx_int_t rc)
{
    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "finalize http memcached request");
    return;
}


static ngx_flag_t
ngx_http_memc_in_cmds_allowed(ngx_http_memc_loc_conf_t *mlcf,
    ngx_http_memc_cmd_t memc_cmd)
{
    ngx_uint_t                   i;
    ngx_http_memc_cmd_t         *value;

    if (mlcf->cmds_allowed == NULL || mlcf->cmds_allowed->nelts == 0) {
        /* by default, all the memcached commands supported are allowed. */
        return 1;
    }

    value = mlcf->cmds_allowed->elts;

    for (i = 0; i < mlcf->cmds_allowed->nelts; i++) {
        if (memc_cmd == value[i]) {
            return 1;
        }
    }

    return 0;
}


static ngx_flag_t
ngx_http_memc_valid_uint32_str(u_char *data, size_t len)
{
    u_char              *p, *last;

    if (len > NGX_UINT32_LEN) {
        return 0;
    }

    last = data + len;
    for (p = data; p != last; p++) {
        if (*p < '0' || *p > '9') {
            return 0;
        }
    }

    return 1;
}


static ngx_flag_t
ngx_http_memc_valid_uint64_str(u_char *data, size_t len)
{
    u_char              *p, *last;

    if (len > NGX_UINT64_LEN) {
        return 0;
    }

    last = data + len;
    for (p = data; p != last; p++) {
        if (*p < '0' || *p > '9') {
            return 0;
        }
    }

    return 1;
}


ngx_int_t
ngx_http_memc_init(ngx_conf_t *cf)
{
    ngx_http_memc_main_conf_t       *mmcf;

    mmcf = ngx_http_conf_get_module_main_conf(cf, ngx_http_memc_module);

    if (!mmcf->module_used) {
        return NGX_OK;
    }

    mmcf->key_index = ngx_http_memc_add_variable(cf, &ngx_http_memc_key);
    if (mmcf->key_index == NGX_ERROR) {
        return NGX_ERROR;
    }

    mmcf->cmd_index = ngx_http_memc_add_variable(cf, &ngx_http_memc_cmd);
    if (mmcf->cmd_index == NGX_ERROR) {
        return NGX_ERROR;
    }

    mmcf->flags_index = ngx_http_memc_add_variable(cf, &ngx_http_memc_flags);
    if (mmcf->flags_index == NGX_ERROR) {
        return NGX_ERROR;
    }

    mmcf->exptime_index = ngx_http_memc_add_variable(cf,
                                                     &ngx_http_memc_exptime);
    if (mmcf->exptime_index == NGX_ERROR) {
        return NGX_ERROR;
    }

    mmcf->value_index = ngx_http_memc_add_variable(cf, &ngx_http_memc_value);
    if (mmcf->value_index == NGX_ERROR) {
        return NGX_ERROR;
    }

    return ngx_http_memc_add_more_variables(cf);
}


static ngx_int_t
ngx_http_memc_add_variable(ngx_conf_t *cf, ngx_str_t *name)
{
    ngx_http_variable_t         *v;

    v = ngx_http_add_variable(cf, name, NGX_HTTP_VAR_CHANGEABLE);
    if (v == NULL) {
        return NGX_ERROR;
    }

    v->get_handler = ngx_http_memc_variable_not_found;
    return ngx_http_get_variable_index(cf, name);
}


static ngx_int_t
ngx_http_memc_variable_not_found(ngx_http_request_t *r,
    ngx_http_variable_value_t *v, uintptr_t data)
{
    v->not_found = 1;
    return NGX_OK;
}


static ngx_int_t
ngx_http_memc_add_more_variables(ngx_conf_t *cf)
{
    ngx_http_variable_t *var, *v;
    for (v = ngx_http_memc_variables; v->name.len; v++) {
        var = ngx_http_add_variable(cf, &v->name, v->flags);
        if (var == NULL) {
            return NGX_ERROR;
        }
        var->get_handler = v->get_handler;
        var->data = v->data;
    }
    return NGX_OK;
}


static ngx_int_t
ngx_http_memc_flags_as_http_time_variable(
    ngx_http_request_t *r, ngx_http_variable_value_t *v, uintptr_t data)
{
    u_char                      *p;
    size_t                       len;
    time_t                       flags_time = 0;
    ngx_http_memc_ctx_t         *ctx;

    ngx_http_variable_value_t   *flags_vv;

    ctx = ngx_http_get_module_ctx(r, ngx_http_memc_module);

    flags_vv = ctx->memc_flags_vv;
    if (flags_vv == NULL) {
        goto not_found;
    }

    if (flags_vv->not_found || flags_vv->len == 0) {
        goto not_found;
    }

    flags_time = ngx_atotm(flags_vv->data, flags_vv->len);
    if (flags_time == NGX_ERROR) {
        return NGX_ERROR;
    }

    len = sizeof("Mon, 28 Sep 1970 06:00:00 GMT") - 1;

    p = ngx_pnalloc(r->pool, len);
    if (p == NULL) {
        return NGX_ERROR;
    }

    ngx_http_time(p, flags_time);

    v->len = len;
    v->data = p;

    v->valid = 1;
    v->not_found = 0;
    v->no_cacheable = 0;

    return NGX_OK;

not_found:

    v->len = 0;
    v->data = NULL;
    v->valid = 1;
    v->not_found = 1;

    return NGX_OK;
}
