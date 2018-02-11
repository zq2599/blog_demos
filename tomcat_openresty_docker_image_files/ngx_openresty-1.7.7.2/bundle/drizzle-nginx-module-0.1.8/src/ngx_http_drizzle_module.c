
/*
 * Copyright (C) Xiaozhe Wang (chaoslawful)
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_drizzle_module.h"
#include "ngx_http_drizzle_handler.h"
#include "ngx_http_drizzle_upstream.h"
#include "ngx_http_drizzle_keepalive.h"


static ngx_str_t  ngx_http_drizzle_tid_var_name =
        ngx_string("drizzle_thread_id");


/* Forward declaration */

static char * ngx_http_drizzle_set_complex_value_slot(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static char * ngx_http_drizzle_query(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf);
static char * ngx_http_drizzle_pass(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf);
static void * ngx_http_drizzle_create_loc_conf(ngx_conf_t *cf);
static char * ngx_http_drizzle_merge_loc_conf(ngx_conf_t *cf, void *parent,
    void *child);
static ngx_int_t ngx_http_drizzle_add_variables(ngx_conf_t *cf);
static ngx_int_t ngx_http_drizzle_tid_variable(ngx_http_request_t *r,
    ngx_http_variable_value_t *v, uintptr_t data);
static char * ngx_http_drizzle_enable_status(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);


static ngx_http_variable_t ngx_http_drizzle_variables[] = {

    { ngx_string("drizzle_thread_id"), NULL,
      ngx_http_drizzle_tid_variable, 0,
      NGX_HTTP_VAR_CHANGEABLE, 0 },

    { ngx_null_string, NULL, NULL, 0, 0, 0 }
};


/* config directives for module drizzle */
static ngx_command_t ngx_http_drizzle_cmds[] = {
    { ngx_string("drizzle_server"),
      NGX_HTTP_UPS_CONF|NGX_CONF_1MORE,
      ngx_http_upstream_drizzle_server,
      NGX_HTTP_SRV_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("drizzle_keepalive"),
      NGX_HTTP_UPS_CONF|NGX_CONF_1MORE,
      ngx_http_upstream_drizzle_keepalive,
      NGX_HTTP_SRV_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("drizzle_query"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF
          |NGX_HTTP_LIF_CONF|NGX_CONF_1MORE,
      ngx_http_drizzle_query,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("drizzle_dbname"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF
          |NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
      ngx_http_drizzle_set_complex_value_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_drizzle_loc_conf_t, dbname),
      NULL },

    { ngx_string("drizzle_pass"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
      ngx_http_drizzle_pass,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("drizzle_connect_timeout"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF
          |NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_msec_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_drizzle_loc_conf_t, upstream.connect_timeout),
      NULL },

    { ngx_string("drizzle_send_query_timeout"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF
          |NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_msec_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_drizzle_loc_conf_t, upstream.send_timeout),
      NULL },

    { ngx_string("drizzle_recv_cols_timeout"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF
          |NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_msec_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_drizzle_loc_conf_t, recv_cols_timeout),
      NULL },

    { ngx_string("drizzle_recv_rows_timeout"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF
          |NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_msec_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_drizzle_loc_conf_t, recv_rows_timeout),
      NULL },

    { ngx_string("drizzle_module_header"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF
          |NGX_HTTP_LIF_CONF|NGX_CONF_FLAG,
      ngx_conf_set_flag_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_drizzle_loc_conf_t, enable_module_header),
      NULL },

    { ngx_string("drizzle_buffer_size"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE1,
      ngx_conf_set_size_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_drizzle_loc_conf_t, buf_size),
      NULL },

    { ngx_string("drizzle_status"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_NOARGS,
      ngx_http_drizzle_enable_status,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    ngx_null_command
};


/* Nginx HTTP subsystem module hooks */
static ngx_http_module_t ngx_http_drizzle_module_ctx = {
    NULL,    /* preconfiguration */
    NULL,    /* postconfiguration */

    NULL,    /* create_main_conf */
    NULL,    /* merge_main_conf */

    ngx_http_upstream_drizzle_create_srv_conf,
             /* create_srv_conf */
    NULL,    /* merge_srv_conf */

    ngx_http_drizzle_create_loc_conf,    /* create_loc_conf */
    ngx_http_drizzle_merge_loc_conf      /* merge_loc_conf */
};


ngx_module_t ngx_http_drizzle_module = {
    NGX_MODULE_V1,
    &ngx_http_drizzle_module_ctx,       /* module context */
    ngx_http_drizzle_cmds,              /* module directives */
    NGX_HTTP_MODULE,                    /* module type */
    NULL,    /* init master */
    NULL,    /* init module */
    NULL,    /* init process */
    NULL,    /* init thread */
    NULL,    /* exit thread */
    NULL,    /* exit process */
    NULL,    /* exit master */
    NGX_MODULE_V1_PADDING
};


ngx_drizzle_http_method_t ngx_drizzle_http_methods[] = {
   { (u_char *) "GET",       (uint32_t) NGX_HTTP_GET },
   { (u_char *) "HEAD",      (uint32_t) NGX_HTTP_HEAD },
   { (u_char *) "POST",      (uint32_t) NGX_HTTP_POST },
   { (u_char *) "PUT",       (uint32_t) NGX_HTTP_PUT },
   { (u_char *) "DELETE",    (uint32_t) NGX_HTTP_DELETE },
   { (u_char *) "MKCOL",     (uint32_t) NGX_HTTP_MKCOL },
   { (u_char *) "COPY",      (uint32_t) NGX_HTTP_COPY },
   { (u_char *) "MOVE",      (uint32_t) NGX_HTTP_MOVE },
   { (u_char *) "OPTIONS",   (uint32_t) NGX_HTTP_OPTIONS },
   { (u_char *) "PROPFIND" , (uint32_t) NGX_HTTP_PROPFIND },
   { (u_char *) "PROPPATCH", (uint32_t) NGX_HTTP_PROPPATCH },
   { (u_char *) "LOCK",      (uint32_t) NGX_HTTP_LOCK },
   { (u_char *) "UNLOCK",    (uint32_t) NGX_HTTP_UNLOCK },
#if defined(nginx_version) && (nginx_version >= 8041)
   { (u_char *) "PATCH",     (uint32_t) NGX_HTTP_PATCH },
#endif
   { NULL, 0 }
};


static void *
ngx_http_drizzle_create_loc_conf(ngx_conf_t *cf)
{
    ngx_http_drizzle_loc_conf_t             *conf;

    conf = ngx_pcalloc(cf->pool, sizeof(ngx_http_drizzle_loc_conf_t));
    if (conf == NULL) {
        return NULL;
    }

    conf->upstream.connect_timeout = NGX_CONF_UNSET_MSEC;
    conf->upstream.send_timeout = NGX_CONF_UNSET_MSEC;

    conf->recv_cols_timeout = NGX_CONF_UNSET_MSEC;
    conf->recv_rows_timeout = NGX_CONF_UNSET_MSEC;

    conf->enable_module_header = NGX_CONF_UNSET;

    /* the hardcoded values */
    conf->upstream.cyclic_temp_file = 0;
    conf->upstream.buffering = 0;
    conf->upstream.ignore_client_abort = 0;
    conf->upstream.send_lowat = 0;
    conf->upstream.bufs.num = 0;
    conf->upstream.busy_buffers_size = 0;
    conf->upstream.max_temp_file_size = 0;
    conf->upstream.temp_file_write_size = 0;
    conf->upstream.intercept_errors = 1;
    conf->upstream.intercept_404 = 1;
    conf->upstream.pass_request_headers = 0;
    conf->upstream.pass_request_body = 0;

    /* set by ngx_pcalloc:
     *      conf->dbname = NULL
     *      conf->query  = NULL
     */

    conf->complex_target = NGX_CONF_UNSET_PTR;

    conf->buf_size = NGX_CONF_UNSET_SIZE;
    conf->tid_var_index = NGX_CONF_UNSET;

    return conf;
}


static char *
ngx_http_drizzle_merge_loc_conf(ngx_conf_t *cf, void *parent, void *child)
{
    ngx_http_drizzle_loc_conf_t *prev = parent;
    ngx_http_drizzle_loc_conf_t *conf = child;

    ngx_conf_merge_value(conf->enable_module_header,
                         prev->enable_module_header, 1);

    ngx_conf_merge_msec_value(conf->upstream.connect_timeout,
                              prev->upstream.connect_timeout, 60000);

    ngx_conf_merge_msec_value(conf->upstream.send_timeout,
                              prev->upstream.send_timeout, 60000);

    ngx_conf_merge_msec_value(conf->recv_cols_timeout,
                              prev->recv_cols_timeout, 60000);

    ngx_conf_merge_msec_value(conf->recv_rows_timeout,
                              prev->recv_rows_timeout, 60000);

    if (conf->dbname == NULL) {
        conf->dbname = prev->dbname;
    }

    if ((conf->default_query == NULL) && (conf->queries == NULL)) {
        conf->default_query = prev->default_query;
        conf->methods_set = prev->methods_set;
        conf->queries = prev->queries;
    }

    ngx_conf_merge_size_value(conf->buf_size, prev->buf_size,
                              (size_t) ngx_pagesize);

    if (conf->tid_var_index == NGX_CONF_UNSET) {
        conf->tid_var_index = prev->tid_var_index;
    }

    return NGX_CONF_OK;
}


static char *
ngx_http_drizzle_set_complex_value_slot(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf)
{
    char                             *p = conf;
    ngx_http_complex_value_t        **field;
    ngx_str_t                        *value;
    ngx_http_compile_complex_value_t  ccv;

    field = (ngx_http_complex_value_t **) (p + cmd->offset);

    if (*field) {
        return "is duplicate";
    }

    *field = ngx_palloc(cf->pool, sizeof(ngx_http_complex_value_t));
    if (*field == NULL) {
        return NGX_CONF_ERROR;
    }

    value = cf->args->elts;

    if (value[1].len == 0) {
        ngx_memzero(*field, sizeof(ngx_http_complex_value_t));
        return NGX_OK;
    }

    ngx_memzero(&ccv, sizeof(ngx_http_compile_complex_value_t));

    ccv.cf = cf;
    ccv.value = &value[1];
    ccv.complex_value = *field;

    if (ngx_http_compile_complex_value(&ccv) != NGX_OK) {
        return NGX_CONF_ERROR;
    }

    return NGX_CONF_OK;
}


char *
ngx_http_drizzle_query(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_str_t                         *value = cf->args->elts;
    ngx_str_t                          sql = value[cf->args->nelts - 1];
    ngx_http_drizzle_loc_conf_t       *dlcf = conf;
    ngx_http_compile_complex_value_t   ccv;
    ngx_drizzle_mixed_t               *query;
    ngx_drizzle_http_method_t         *method;
    ngx_uint_t                         methods, i;

    if (sql.len == 0) {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "drizzle: empty value in \"%V\" directive",
                           &cmd->name);

        return NGX_CONF_ERROR;
    }

    if (cf->args->nelts == 2) {
        /* default query */
        dd("default query");

        if (dlcf->default_query != NULL) {
            return "is duplicate";
        }

        dlcf->default_query = ngx_pcalloc(cf->pool,
                                          sizeof(ngx_drizzle_mixed_t));
        if (dlcf->default_query == NULL) {
            return NGX_CONF_ERROR;
        }

        methods = 0xFFFF;
        query = dlcf->default_query;

    } else {
        /* method-specific query */
        dd("method-specific query");

        methods = 0;

        for (i = 1; i < cf->args->nelts - 1; i++) {
            for (method = ngx_drizzle_http_methods; method->name; method++) {
                if (ngx_strcasecmp(value[i].data, method->name) == 0) {
                    /* correct method name */
                    if (dlcf->methods_set & method->key) {
                        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                                           "drizzle: \"%V\" directive"
                                           " for method \"%V\" is duplicate",
                                           &cmd->name, &value[i]);

                        return NGX_CONF_ERROR;
                    }

                    methods |= method->key;
                    goto next;
                }
            }

            ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                               "drizzle: invalid method \"%V\"", &value[i]);

            return NGX_CONF_ERROR;

next:

            continue;
        }

        if (dlcf->queries == NULL) {
            dlcf->queries = ngx_array_create(cf->pool, 4,
                                             sizeof(ngx_drizzle_mixed_t));
            if (dlcf->queries == NULL) {
                return NGX_CONF_ERROR;
            }
        }

        query = ngx_array_push(dlcf->queries);
        if (query == NULL) {
            return NGX_CONF_ERROR;
        }

        ngx_memzero(query, sizeof(ngx_drizzle_mixed_t));

        dlcf->methods_set |= methods;
    }

    if (ngx_http_script_variables_count(&sql)) {
        /* complex value */
        dd("complex value");

        query->key = methods;

        query->cv = ngx_palloc(cf->pool, sizeof(ngx_http_complex_value_t));
        if (query->cv == NULL) {
            return NGX_CONF_ERROR;
        }

        ngx_memzero(&ccv, sizeof(ngx_http_compile_complex_value_t));

        ccv.cf = cf;
        ccv.value = &sql;
        ccv.complex_value = query->cv;

        if (ngx_http_compile_complex_value(&ccv) != NGX_OK) {
            return NGX_CONF_ERROR;
        }
    } else {
        /* simple value */
        dd("simple value");

        query->key = methods;
        query->sv = sql;
    }

    return NGX_CONF_OK;
}


static char *
ngx_http_drizzle_pass(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_drizzle_loc_conf_t             *dlcf = conf;

    ngx_http_core_loc_conf_t                *clcf;
    ngx_str_t                               *value;
    ngx_http_compile_complex_value_t         ccv;
    ngx_url_t                                url;
    ngx_uint_t                               n;

    if (dlcf->upstream.upstream) {
        return "is duplicate";
    }

    if (ngx_http_drizzle_add_variables(cf) != NGX_OK) {
        return NGX_CONF_ERROR;
    }

    dlcf->tid_var_index = ngx_http_get_variable_index(cf,
                                              &ngx_http_drizzle_tid_var_name);

    if (dlcf->tid_var_index == NGX_ERROR) {
        return NGX_CONF_ERROR;
    }

    clcf = ngx_http_conf_get_module_loc_conf(cf, ngx_http_core_module);

    clcf->handler = ngx_http_drizzle_handler;

    if (clcf->name.data[clcf->name.len - 1] == '/') {
        clcf->auto_redirect = 1;
    }

    value = cf->args->elts;

    n = ngx_http_script_variables_count(&value[1]);
    if (n) {
        dlcf->complex_target = ngx_palloc(cf->pool,
                                          sizeof(ngx_http_complex_value_t));
        if (dlcf->complex_target == NULL) {
            return NGX_CONF_ERROR;
        }

        ngx_memzero(&ccv, sizeof(ngx_http_compile_complex_value_t));
        ccv.cf = cf;
        ccv.value = &value[1];
        ccv.complex_value = dlcf->complex_target;

        if (ngx_http_compile_complex_value(&ccv) != NGX_OK) {
            return NGX_CONF_ERROR;
        }

        return NGX_CONF_OK;
    }

    dlcf->complex_target = NULL;

    ngx_memzero(&url, sizeof(ngx_url_t));

    url.url = value[1];
    url.no_resolve = 1;

    dlcf->upstream.upstream = ngx_http_upstream_add(cf, &url, 0);

    if (dlcf->upstream.upstream == NULL) {
        return NGX_CONF_ERROR;
    }

    return NGX_CONF_OK;
}


static ngx_int_t
ngx_http_drizzle_add_variables(ngx_conf_t *cf)
{
    ngx_http_variable_t *var, *v;

    for (v = ngx_http_drizzle_variables; v->name.len; v++) {
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
ngx_http_drizzle_tid_variable(ngx_http_request_t *r,
    ngx_http_variable_value_t *v, uintptr_t data)
{
    v->valid = 1;
    v->no_cacheable = 0;
    v->not_found = 0;

    v->len = 0;
    v->data = (u_char *) "";

    return NGX_OK;
}


static char *
ngx_http_drizzle_enable_status(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_core_loc_conf_t                *clcf;

    clcf = ngx_http_conf_get_module_loc_conf(cf, ngx_http_core_module);
    clcf->handler = ngx_http_drizzle_status_handler;

    return NGX_CONF_OK;
}
