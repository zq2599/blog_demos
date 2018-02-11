/*
 * Copyright (c) 2010, FRiCKLE Piotr Sikora <info@frickle.com>
 * Copyright (c) 2009-2010, Xiaozhe Wang <chaoslawful@gmail.com>
 * Copyright (c) 2009-2010, Yichun Zhang <agentzh@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#ifndef DDEBUG
#define DDEBUG 0
#endif

#include "ngx_postgres_ddebug.h"
#include "ngx_postgres_escape.h"
#include "ngx_postgres_handler.h"
#include "ngx_postgres_keepalive.h"
#include "ngx_postgres_module.h"
#include "ngx_postgres_output.h"
#include "ngx_postgres_upstream.h"
#include "ngx_postgres_util.h"
#include "ngx_postgres_variable.h"
#include "ngx_postgres_rewrite.h"


#define NGX_CONF_TAKE34  (NGX_CONF_TAKE3|NGX_CONF_TAKE4)


static ngx_command_t ngx_postgres_module_commands[] = {

    { ngx_string("postgres_server"),
      NGX_HTTP_UPS_CONF|NGX_CONF_1MORE,
      ngx_postgres_conf_server,
      NGX_HTTP_SRV_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("postgres_keepalive"),
      NGX_HTTP_UPS_CONF|NGX_CONF_1MORE,
      ngx_postgres_conf_keepalive,
      NGX_HTTP_SRV_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("postgres_pass"),
      NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
      ngx_postgres_conf_pass,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("postgres_query"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|
          NGX_HTTP_LIF_CONF|NGX_CONF_1MORE,
      ngx_postgres_conf_query,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("postgres_rewrite"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|
          NGX_HTTP_LIF_CONF|NGX_CONF_2MORE,
      ngx_postgres_conf_rewrite,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("postgres_output"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|
          NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
      ngx_postgres_conf_output,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("postgres_set"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_TAKE34,
      ngx_postgres_conf_set,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("postgres_escape"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_TAKE12,
      ngx_postgres_conf_escape,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("postgres_connect_timeout"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_msec_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_postgres_loc_conf_t, upstream.connect_timeout),
      NULL },

    { ngx_string("postgres_result_timeout"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_msec_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_postgres_loc_conf_t, upstream.read_timeout),
      NULL },

      ngx_null_command
};

static ngx_http_variable_t ngx_postgres_module_variables[] = {

    { ngx_string("postgres_columns"), NULL,
      ngx_postgres_variable_columns, 0,
      NGX_HTTP_VAR_NOCACHEABLE|NGX_HTTP_VAR_NOHASH, 0 },

    { ngx_string("postgres_rows"), NULL,
      ngx_postgres_variable_rows, 0,
      NGX_HTTP_VAR_NOCACHEABLE|NGX_HTTP_VAR_NOHASH, 0 },

    { ngx_string("postgres_affected"), NULL,
      ngx_postgres_variable_affected, 0,
      NGX_HTTP_VAR_NOCACHEABLE|NGX_HTTP_VAR_NOHASH, 0 },

    { ngx_string("postgres_query"), NULL,
      ngx_postgres_variable_query, 0,
      NGX_HTTP_VAR_NOCACHEABLE|NGX_HTTP_VAR_NOHASH, 0 },

    { ngx_null_string, NULL, NULL, 0, 0, 0 }
};

static ngx_http_module_t ngx_postgres_module_ctx = {
    ngx_postgres_add_variables,             /* preconfiguration */
    NULL,                                   /* postconfiguration */

    NULL,                                   /* create main configuration */
    NULL,                                   /* init main configuration */

    ngx_postgres_create_upstream_srv_conf,  /* create server configuration */
    NULL,                                   /* merge server configuration */

    ngx_postgres_create_loc_conf,           /* create location configuration */
    ngx_postgres_merge_loc_conf             /* merge location configuration */
};

ngx_module_t ngx_postgres_module = {
    NGX_MODULE_V1,
    &ngx_postgres_module_ctx,      /* module context */
    ngx_postgres_module_commands,  /* module directives */
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

ngx_conf_bitmask_t ngx_postgres_http_methods[] = {
   { ngx_string("GET"),       NGX_HTTP_GET },
   { ngx_string("HEAD"),      NGX_HTTP_HEAD },
   { ngx_string("POST"),      NGX_HTTP_POST },
   { ngx_string("PUT"),       NGX_HTTP_PUT },
   { ngx_string("DELETE"),    NGX_HTTP_DELETE },
   { ngx_string("MKCOL"),     NGX_HTTP_MKCOL },
   { ngx_string("COPY"),      NGX_HTTP_COPY },
   { ngx_string("MOVE"),      NGX_HTTP_MOVE },
   { ngx_string("OPTIONS"),   NGX_HTTP_OPTIONS },
   { ngx_string("PROPFIND"),  NGX_HTTP_PROPFIND },
   { ngx_string("PROPPATCH"), NGX_HTTP_PROPPATCH },
   { ngx_string("LOCK"),      NGX_HTTP_LOCK },
   { ngx_string("UNLOCK"),    NGX_HTTP_UNLOCK },
#if defined(nginx_version) && (nginx_version >= 8041)
   { ngx_string("PATCH"),     NGX_HTTP_PATCH },
#endif
    { ngx_null_string, 0 }
};

ngx_conf_enum_t ngx_postgres_upstream_mode_options[] = {
    { ngx_string("multi"),  0 },
    { ngx_string("single"), 1 },
    { ngx_null_string, 0 }
};

ngx_conf_enum_t ngx_postgres_upstream_overflow_options[] = {
    { ngx_string("ignore"), 0 },
    { ngx_string("reject"), 1 },
    { ngx_null_string, 0 }
};

ngx_conf_enum_t ngx_postgres_requirement_options[] = {
    { ngx_string("optional"), 0 },
    { ngx_string("required"), 1 },
    { ngx_null_string, 0 }
};

ngx_postgres_rewrite_enum_t ngx_postgres_rewrite_handlers[] = {
    { ngx_string("no_changes"), 0, ngx_postgres_rewrite_changes },
    { ngx_string("changes"),    1, ngx_postgres_rewrite_changes },
    { ngx_string("no_rows"),    2, ngx_postgres_rewrite_rows },
    { ngx_string("rows"),       3, ngx_postgres_rewrite_rows },
    { ngx_null_string, 0, NULL }
};

ngx_postgres_output_enum_t ngx_postgres_output_handlers[] = {
    { ngx_string("none"),         0, NULL },
    { ngx_string("rds"),          0, ngx_postgres_output_rds },
    { ngx_string("text") ,        0, ngx_postgres_output_text },
    { ngx_string("value"),        0, ngx_postgres_output_value },
    { ngx_string("binary_value"), 1, ngx_postgres_output_value },
    { ngx_null_string, 0, NULL }
};


ngx_int_t
ngx_postgres_add_variables(ngx_conf_t *cf)
{
    ngx_http_variable_t  *var, *v;

    dd("entering");

    for (v = ngx_postgres_module_variables; v->name.len; v++) {
        var = ngx_http_add_variable(cf, &v->name, v->flags);
        if (var == NULL) {
            dd("returning NGX_ERROR");
            return NGX_ERROR;
        }

        var->get_handler = v->get_handler;
        var->data = v->data;
    }

    dd("returning NGX_OK");
    return NGX_OK;
}

void *
ngx_postgres_create_upstream_srv_conf(ngx_conf_t *cf)
{
    ngx_postgres_upstream_srv_conf_t  *conf;
    ngx_pool_cleanup_t                *cln;

    dd("entering");

    conf = ngx_pcalloc(cf->pool, sizeof(ngx_postgres_upstream_srv_conf_t));
    if (conf == NULL) {
        dd("returning NULL");
        return NULL;
    }

    /*
     * set by ngx_pcalloc():
     *
     *     conf->peers = NULL
     *     conf->current = 0
     *     conf->servers = NULL
     *     conf->free = { NULL, NULL }
     *     conf->cache = { NULL, NULL }
     *     conf->active_conns = 0
     *     conf->reject = 0
     */

    conf->pool = cf->pool;

    /* enable keepalive (single) by default */
    conf->max_cached = 10;
    conf->single = 1;

    cln = ngx_pool_cleanup_add(cf->pool, 0);
    cln->handler = ngx_postgres_keepalive_cleanup;
    cln->data = conf;

    dd("returning");
    return conf;
}

void *
ngx_postgres_create_loc_conf(ngx_conf_t *cf)
{
    ngx_postgres_loc_conf_t  *conf;

    dd("entering");

    conf = ngx_pcalloc(cf->pool, sizeof(ngx_postgres_loc_conf_t));
    if (conf == NULL) {
        dd("returning NULL");
        return NULL;
    }

    /*
     * set by ngx_pcalloc():
     *
     *     conf->upstream.* = 0 / NULL
     *     conf->upstream_cv = NULL
     *     conf->query.methods_set = 0
     *     conf->query.methods = NULL
     *     conf->query.def = NULL
     *     conf->output_binary = 0
     */

    conf->upstream.connect_timeout = NGX_CONF_UNSET_MSEC;
    conf->upstream.read_timeout = NGX_CONF_UNSET_MSEC;

    conf->rewrites = NGX_CONF_UNSET_PTR;
    conf->output_handler = NGX_CONF_UNSET_PTR;
    conf->variables = NGX_CONF_UNSET_PTR;

    /* the hardcoded values */
    conf->upstream.cyclic_temp_file = 0;
    conf->upstream.buffering = 1;
    conf->upstream.ignore_client_abort = 1;
    conf->upstream.send_lowat = 0;
    conf->upstream.bufs.num = 0;
    conf->upstream.busy_buffers_size = 0;
    conf->upstream.max_temp_file_size = 0;
    conf->upstream.temp_file_write_size = 0;
    conf->upstream.intercept_errors = 1;
    conf->upstream.intercept_404 = 1;
    conf->upstream.pass_request_headers = 0;
    conf->upstream.pass_request_body = 0;

    dd("returning");
    return conf;
}

char *
ngx_postgres_merge_loc_conf(ngx_conf_t *cf, void *parent, void *child)
{
    ngx_postgres_loc_conf_t  *prev = parent;
    ngx_postgres_loc_conf_t  *conf = child;

    dd("entering");

    ngx_conf_merge_msec_value(conf->upstream.connect_timeout,
                              prev->upstream.connect_timeout, 10000);

    ngx_conf_merge_msec_value(conf->upstream.read_timeout,
                              prev->upstream.read_timeout, 30000);

    if ((conf->upstream.upstream == NULL) && (conf->upstream_cv == NULL)) {
        conf->upstream.upstream = prev->upstream.upstream;
        conf->upstream_cv = prev->upstream_cv;
    }

    if ((conf->query.def == NULL) && (conf->query.methods == NULL)) {
        conf->query.methods_set = prev->query.methods_set;
        conf->query.methods = prev->query.methods;
        conf->query.def = prev->query.def;
    }

    ngx_conf_merge_ptr_value(conf->rewrites, prev->rewrites, NULL);

    if (conf->output_handler == NGX_CONF_UNSET_PTR) {
        if (prev->output_handler == NGX_CONF_UNSET_PTR) {
            /* default */
            conf->output_handler = ngx_postgres_output_rds;
            conf->output_binary = 0;
        } else {
            /* merge */
            conf->output_handler = prev->output_handler;
            conf->output_binary = prev->output_binary;
        }
    }

    ngx_conf_merge_ptr_value(conf->variables, prev->variables, NULL);

    dd("returning NGX_CONF_OK");
    return NGX_CONF_OK;
}

/*
 * Based on: ngx_http_upstream.c/ngx_http_upstream_server
 * Copyright (C) Igor Sysoev
 */
char *
ngx_postgres_conf_server(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_str_t                         *value = cf->args->elts;
    ngx_postgres_upstream_srv_conf_t  *pgscf = conf;
    ngx_postgres_upstream_server_t    *pgs;
    ngx_http_upstream_srv_conf_t      *uscf;
    ngx_url_t                          u;
    ngx_uint_t                         i;

    dd("entering");

    uscf = ngx_http_conf_get_module_srv_conf(cf, ngx_http_upstream_module);

    if (pgscf->servers == NULL) {
        pgscf->servers = ngx_array_create(cf->pool, 4,
                             sizeof(ngx_postgres_upstream_server_t));
        if (pgscf->servers == NULL) {
            dd("returning NGX_CONF_ERROR");
            return NGX_CONF_ERROR;
        }

        uscf->servers = pgscf->servers;
    }

    pgs = ngx_array_push(pgscf->servers);
    if (pgs == NULL) {
        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    ngx_memzero(pgs, sizeof(ngx_postgres_upstream_server_t));

    /* parse the first name:port argument */

    ngx_memzero(&u, sizeof(ngx_url_t));

    u.url = value[1];
    u.default_port = 5432; /* PostgreSQL default */

    if (ngx_parse_url(cf->pool, &u) != NGX_OK) {
        if (u.err) {
            ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                               "postgres: %s in upstream \"%V\"",
                               u.err, &u.url);
        }

        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    pgs->addrs = u.addrs;
    pgs->naddrs = u.naddrs;
    pgs->port = u.port;

    /* parse various options */
    for (i = 2; i < cf->args->nelts; i++) {

        if (ngx_strncmp(value[i].data, "dbname=", sizeof("dbname=") - 1)
                == 0)
        {
            pgs->dbname.len = value[i].len - (sizeof("dbname=") - 1);
            pgs->dbname.data = &value[i].data[sizeof("dbname=") - 1];
            continue;
        }

        if (ngx_strncmp(value[i].data, "user=", sizeof("user=") - 1)
                == 0)
        {
            pgs->user.len = value[i].len - (sizeof("user=") - 1);
            pgs->user.data = &value[i].data[sizeof("user=") - 1];
            continue;
        }

        if (ngx_strncmp(value[i].data, "password=", sizeof("password=") - 1)
                == 0)
        {
            pgs->password.len = value[i].len - (sizeof("password=") - 1);
            pgs->password.data = &value[i].data[sizeof("password=") - 1];
            continue;
        }

        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "postgres: invalid parameter \"%V\" in"
                           " \"postgres_server\"", &value[i]);

        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    uscf->peer.init_upstream = ngx_postgres_upstream_init;

    dd("returning NGX_CONF_OK");
    return NGX_CONF_OK;
}

char *
ngx_postgres_conf_keepalive(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_str_t                         *value = cf->args->elts;
    ngx_postgres_upstream_srv_conf_t  *pgscf = conf;
    ngx_conf_enum_t                   *e;
    ngx_uint_t                         i, j;
    ngx_int_t                          n;

    dd("entering");

    if (pgscf->max_cached != 10 /* default */) {
        dd("returning");
        return "is duplicate";
    }

    if ((cf->args->nelts == 2) && (ngx_strcmp(value[1].data, "off") == 0)) {
        pgscf->max_cached = 0;

        dd("returning NGX_CONF_OK");
        return NGX_CONF_OK;
    }

    for (i = 1; i < cf->args->nelts; i++) {

        if (ngx_strncmp(value[i].data, "max=", sizeof("max=") - 1)
                == 0)
        {
            value[i].len = value[i].len - (sizeof("max=") - 1);
            value[i].data = &value[i].data[sizeof("max=") - 1];

            n = ngx_atoi(value[i].data, value[i].len);
            if (n == NGX_ERROR) {
                ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                                   "postgres: invalid \"max\" value \"%V\""
                                   " in \"%V\" directive",
                                   &value[i], &cmd->name);

                dd("returning NGX_CONF_ERROR");
                return NGX_CONF_ERROR;
            }

            pgscf->max_cached = (ngx_uint_t) n;

            continue;
        }

        if (ngx_strncmp(value[i].data, "mode=", sizeof("mode=") - 1)
                == 0)
        {
            value[i].len = value[i].len - (sizeof("mode=") - 1);
            value[i].data = &value[i].data[sizeof("mode=") - 1];

            e = ngx_postgres_upstream_mode_options;
            for (j = 0; e[j].name.len; j++) {
                if ((e[j].name.len == value[i].len)
                    && (ngx_strcasecmp(e[j].name.data, value[i].data) == 0))
                {
                    pgscf->single = e[j].value;
                    break;
                }
            }

            if (e[j].name.len == 0) {
                ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                                   "postgres: invalid \"mode\" value \"%V\""
                                   " in \"%V\" directive",
                                   &value[i], &cmd->name);

                dd("returning NGX_CONF_ERROR");
                return NGX_CONF_ERROR;
            }

            continue;
        }

        if (ngx_strncmp(value[i].data, "overflow=", sizeof("overflow=") - 1)
                == 0)
        {
            value[i].len = value[i].len - (sizeof("overflow=") - 1);
            value[i].data = &value[i].data[sizeof("overflow=") - 1];

            e = ngx_postgres_upstream_overflow_options;
            for (j = 0; e[j].name.len; j++) {
                if ((e[j].name.len == value[i].len)
                    && (ngx_strcasecmp(e[j].name.data, value[i].data) == 0))
                {
                    pgscf->reject = e[j].value;
                    break;
                }
            }

            if (e[j].name.len == 0) {
                ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                                   "postgres: invalid \"overflow\" value \"%V\""
                                   " in \"%V\" directive",
                                   &value[i], &cmd->name);

                dd("returning NGX_CONF_ERROR");
                return NGX_CONF_ERROR;
            }

            continue;
        }

        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "postgres: invalid parameter \"%V\" in"
                           " \"%V\" directive",
                           &value[i], &cmd->name);

        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    dd("returning NGX_CONF_OK");
    return NGX_CONF_OK;
}

char *
ngx_postgres_conf_pass(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_str_t                         *value = cf->args->elts;
    ngx_postgres_loc_conf_t           *pglcf = conf;
    ngx_http_core_loc_conf_t          *clcf;
    ngx_http_compile_complex_value_t   ccv;
    ngx_url_t                          url;

    dd("entering");

    if ((pglcf->upstream.upstream != NULL) || (pglcf->upstream_cv != NULL)) {
        dd("returning");
        return "is duplicate";
    }

    if (value[1].len == 0) {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "postgres: empty upstream in \"%V\" directive",
                           &cmd->name);

        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    clcf = ngx_http_conf_get_module_loc_conf(cf, ngx_http_core_module);

    clcf->handler = ngx_postgres_handler;

    if (clcf->name.data[clcf->name.len - 1] == '/') {
        clcf->auto_redirect = 1;
    }

    if (ngx_http_script_variables_count(&value[1])) {
        /* complex value */
        dd("complex value");

        pglcf->upstream_cv = ngx_palloc(cf->pool,
                                        sizeof(ngx_http_complex_value_t));
        if (pglcf->upstream_cv == NULL) {
            dd("returning NGX_CONF_ERROR");
            return NGX_CONF_ERROR;
        }

        ngx_memzero(&ccv, sizeof(ngx_http_compile_complex_value_t));

        ccv.cf = cf;
        ccv.value = &value[1];
        ccv.complex_value = pglcf->upstream_cv;

        if (ngx_http_compile_complex_value(&ccv) != NGX_OK) {
            dd("returning NGX_CONF_ERROR");
            return NGX_CONF_ERROR;
        }

        dd("returning NGX_CONF_OK");
        return NGX_CONF_OK;
    } else {
        /* simple value */
        dd("simple value");

        ngx_memzero(&url, sizeof(ngx_url_t));

        url.url = value[1];
        url.no_resolve = 1;

        pglcf->upstream.upstream = ngx_http_upstream_add(cf, &url, 0);
        if (pglcf->upstream.upstream == NULL) {
            dd("returning NGX_CONF_ERROR");
            return NGX_CONF_ERROR;
        }

        dd("returning NGX_CONF_OK");
        return NGX_CONF_OK;
    }
}

char *
ngx_postgres_conf_query(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_str_t                         *value = cf->args->elts;
    ngx_str_t                          sql = value[cf->args->nelts - 1];
    ngx_postgres_loc_conf_t           *pglcf = conf;
    ngx_http_compile_complex_value_t   ccv;
    ngx_postgres_mixed_t              *query;
    ngx_conf_bitmask_t                *b;
    ngx_uint_t                         methods, i, j;

    dd("entering");

    if (sql.len == 0) {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "postgres: empty query in \"%V\" directive",
                           &cmd->name);

        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    if (cf->args->nelts == 2) {
        /* default query */
        dd("default query");

        if (pglcf->query.def != NULL) {
            dd("returning");
            return "is duplicate";
        }

        pglcf->query.def = ngx_palloc(cf->pool, sizeof(ngx_postgres_mixed_t));
        if (pglcf->query.def == NULL) {
            dd("returning NGX_CONF_ERROR");
            return NGX_CONF_ERROR;
        }

        methods = 0xFFFF;
        query = pglcf->query.def;
    } else {
        /* method-specific query */
        dd("method-specific query");

        methods = 0;

        for (i = 1; i < cf->args->nelts - 1; i++) {
            b = ngx_postgres_http_methods;
            for (j = 0; b[j].name.len; j++) {
                if ((b[j].name.len == value[i].len)
                    && (ngx_strcasecmp(b[j].name.data, value[i].data) == 0))
                {
                    if (pglcf->query.methods_set & b[j].mask) {
                        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                                           "postgres: method \"%V\" is"
                                           " duplicate in \"%V\" directive",
                                           &value[i], &cmd->name);

                        dd("returning NGX_CONF_ERROR");
                        return NGX_CONF_ERROR;
                    }

                    methods |= b[j].mask;
                    break;
                }
            }

            if (b[j].name.len == 0) {
                ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                                   "postgres: invalid method \"%V\""
                                   " in \"%V\" directive",
                                   &value[i], &cmd->name);

                dd("returning NGX_CONF_ERROR");
                return NGX_CONF_ERROR;
            }
        }

        if (pglcf->query.methods == NULL) {
            pglcf->query.methods = ngx_array_create(cf->pool, 4,
                                       sizeof(ngx_postgres_mixed_t));
            if (pglcf->query.methods == NULL) {
                dd("returning NGX_CONF_ERROR");
                return NGX_CONF_ERROR;
            }
        }

        query = ngx_array_push(pglcf->query.methods);
        if (query == NULL) {
            dd("returning NGX_CONF_ERROR");
            return NGX_CONF_ERROR;
        }

        pglcf->query.methods_set |= methods;
    }

    if (ngx_http_script_variables_count(&sql)) {
        /* complex value */
        dd("complex value");

        query->key = methods;

        query->cv = ngx_palloc(cf->pool, sizeof(ngx_http_complex_value_t));
        if (query->cv == NULL) {
            dd("returning NGX_CONF_ERROR");
            return NGX_CONF_ERROR;
        }

        ngx_memzero(&ccv, sizeof(ngx_http_compile_complex_value_t));

        ccv.cf = cf;
        ccv.value = &sql;
        ccv.complex_value = query->cv;

        if (ngx_http_compile_complex_value(&ccv) != NGX_OK) {
            dd("returning NGX_CONF_ERROR");
            return NGX_CONF_ERROR;
        }
    } else {
        /* simple value */
        dd("simple value");

        query->key = methods;
        query->sv = sql;
        query->cv = NULL;
    }

    dd("returning NGX_CONF_OK");
    return NGX_CONF_OK;
}

char *
ngx_postgres_conf_rewrite(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_str_t                    *value = cf->args->elts;
    ngx_str_t                     what = value[cf->args->nelts - 2];
    ngx_str_t                     to = value[cf->args->nelts - 1];
    ngx_postgres_loc_conf_t      *pglcf = conf;
    ngx_postgres_rewrite_conf_t  *pgrcf;
    ngx_postgres_rewrite_t       *rewrite;
    ngx_postgres_rewrite_enum_t  *e;
    ngx_conf_bitmask_t           *b;
    ngx_uint_t                    methods, keep_body, i, j;

    dd("entering");

    e = ngx_postgres_rewrite_handlers;
    for (i = 0; e[i].name.len; i++) {
        if ((e[i].name.len == what.len)
            && (ngx_strcasecmp(e[i].name.data, what.data) == 0))
        {
            break;
        }
    }

    if (e[i].name.len == 0) {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "postgres: invalid condition \"%V\""
                           " in \"%V\" directive", &what, &cmd->name);

        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    if (pglcf->rewrites == NGX_CONF_UNSET_PTR) {
        pglcf->rewrites = ngx_array_create(cf->pool, 2,
                                           sizeof(ngx_postgres_rewrite_conf_t));
        if (pglcf->rewrites == NULL) {
            dd("returning NGX_CONF_ERROR");
            return NGX_CONF_ERROR;
        }
    } else {
        pgrcf = pglcf->rewrites->elts;
        for (j = 0; j < pglcf->rewrites->nelts; j++) {
            if (pgrcf[j].key == e[i].key) {
                pgrcf = &pgrcf[j];
                goto found;
            }
        }
    }

    pgrcf = ngx_array_push(pglcf->rewrites);
    if (pgrcf == NULL) {
        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    ngx_memzero(pgrcf, sizeof(ngx_postgres_rewrite_conf_t));

    pgrcf->key = e[i].key;
    pgrcf->handler = e[i].handler;

found:

    if (cf->args->nelts == 3) {
        /* default rewrite */
        dd("default rewrite");

        if (pgrcf->def != NULL) {
            dd("returning");
            return "is duplicate";
        }

        pgrcf->def = ngx_palloc(cf->pool, sizeof(ngx_postgres_rewrite_t));
        if (pgrcf->def == NULL) {
            dd("returning NGX_CONF_ERROR");
            return NGX_CONF_ERROR;
        }

        methods = 0xFFFF;
        rewrite = pgrcf->def;
    } else {
        /* method-specific rewrite */
        dd("method-specific rewrite");

        methods = 0;

        for (i = 1; i < cf->args->nelts - 2; i++) {
            b = ngx_postgres_http_methods;
            for (j = 0; b[j].name.len; j++) {
                if ((b[j].name.len == value[i].len)
                    && (ngx_strcasecmp(b[j].name.data, value[i].data) == 0))
                {
                    if (pgrcf->methods_set & b[j].mask) {
                        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                                           "postgres: method \"%V\" for"
                                           " condition \"%V\" is duplicate"
                                           " in \"%V\" directive",
                                           &value[i], &what, &cmd->name);

                        dd("returning NGX_CONF_ERROR");
                        return NGX_CONF_ERROR;
                    }

                    methods |= b[j].mask;
                    break;
                }
            }

            if (b[j].name.len == 0) {
                ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                                   "postgres: invalid method \"%V\" for"
                                   " condition \"%V\" in \"%V\" directive",
                                   &value[i], &what, &cmd->name);

                dd("returning NGX_CONF_ERROR");
                return NGX_CONF_ERROR;
            }
        }

        if (pgrcf->methods == NULL) {
            pgrcf->methods = ngx_array_create(cf->pool, 4,
                                              sizeof(ngx_postgres_rewrite_t));
            if (pgrcf->methods == NULL) {
                dd("returning NGX_CONF_ERROR");
                return NGX_CONF_ERROR;
            }
        }

        rewrite = ngx_array_push(pgrcf->methods);
        if (rewrite == NULL) {
            dd("returning NGX_CONF_ERROR");
            return NGX_CONF_ERROR;
        }

        pgrcf->methods_set |= methods;
    }

    if (to.data[0] == '=') {
        keep_body = 1;
        to.len--;
        to.data++;
    } else {
        keep_body = 0;
    }

    rewrite->key = methods;
    rewrite->status = ngx_atoi(to.data, to.len);
    if ((rewrite->status == NGX_ERROR)
        || (rewrite->status < NGX_HTTP_OK)
        || (rewrite->status > NGX_HTTP_INSUFFICIENT_STORAGE)
        || ((rewrite->status >= NGX_HTTP_SPECIAL_RESPONSE)
            && (rewrite->status < NGX_HTTP_BAD_REQUEST)))
    {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "postgres: invalid status value \"%V\" for"
                           " condition \"%V\" in \"%V\" directive",
                           &to, &what, &cmd->name);

        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    if (keep_body) {
        rewrite->status = -rewrite->status;
    }

    dd("returning NGX_CONF_OK");
    return NGX_CONF_OK;
}

char *
ngx_postgres_conf_output(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_str_t                   *value = cf->args->elts;
    ngx_postgres_loc_conf_t     *pglcf = conf;
    ngx_postgres_output_enum_t  *e;
    ngx_uint_t                   i;

    dd("entering");

    if (pglcf->output_handler != NGX_CONF_UNSET_PTR) {
        dd("returning");
        return "is duplicate";
    }

    e = ngx_postgres_output_handlers;
    for (i = 0; e[i].name.len; i++) {
        if ((e[i].name.len == value[1].len)
            && (ngx_strcasecmp(e[i].name.data, value[1].data) == 0))
        {
            pglcf->output_handler = e[i].handler;
            break;
        }
    }

    if (e[i].name.len == 0) {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "postgres: invalid output format \"%V\""
                           " in \"%V\" directive", &value[1], &cmd->name);

        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    pglcf->output_binary = e[i].binary;

    dd("returning NGX_CONF_OK");
    return NGX_CONF_OK;
}

char *
ngx_postgres_conf_set(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_str_t                *value = cf->args->elts;
    ngx_postgres_loc_conf_t  *pglcf = conf;
    ngx_postgres_variable_t  *pgvar;
    ngx_conf_enum_t          *e;
    ngx_int_t                 idx;
    ngx_uint_t                i;

    dd("entering");

    if (value[1].len < 2) {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "postgres: empty variable name in \"%V\" directive",
                           &cmd->name);

        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    if (value[1].data[0] != '$') {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "postgres: invalid variable name \"%V\""
                           " in \"%V\" directive", &value[1], &cmd->name);

        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    value[1].len--;
    value[1].data++;

    if (value[3].len == 0) {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "postgres: empty column in \"%V\" directive",
                           &cmd->name);

        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    if (pglcf->variables == NGX_CONF_UNSET_PTR) {
        pglcf->variables = ngx_array_create(cf->pool, 4,
                                            sizeof(ngx_postgres_variable_t));
        if (pglcf->variables == NULL) {
            dd("returning NGX_CONF_ERROR");
            return NGX_CONF_ERROR;
        }
    }

    pgvar = ngx_array_push(pglcf->variables);
    if (pgvar == NULL) {
        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    pgvar->idx = pglcf->variables->nelts - 1;

    pgvar->var = ngx_http_add_variable(cf, &value[1], 0);
    if (pgvar->var == NULL) {
        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    idx = ngx_http_get_variable_index(cf, &value[1]);
    if (idx == NGX_ERROR) {
        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    /*
     * Check if "$variable" was previously defined,
     * back-off even if it was marked as "CHANGEABLE".
     */
    if (pgvar->var->get_handler != NULL) {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "postgres: variable \"$%V\" is duplicate"
                           " in \"%V\" directive", &value[1], &cmd->name);

        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    pgvar->var->get_handler = ngx_postgres_variable_get_custom;
    pgvar->var->data = (uintptr_t) pgvar;

    pgvar->value.row = ngx_atoi(value[2].data, value[2].len);
    if (pgvar->value.row == NGX_ERROR) {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "postgres: invalid row number \"%V\""
                           " in \"%V\" directive", &value[2], &cmd->name);

        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    pgvar->value.column = ngx_atoi(value[3].data, value[3].len);
    if (pgvar->value.column == NGX_ERROR) {
        /* get column by name */
        pgvar->value.col_name = ngx_pnalloc(cf->pool, value[3].len + 1);
        if (pgvar->value.col_name == NULL) {
            dd("returning NGX_CONF_ERROR");
            return NGX_CONF_ERROR;
        }

        (void) ngx_cpystrn(pgvar->value.col_name,
                           value[3].data, value[3].len + 1);
    }

    if (cf->args->nelts == 4) {
        /* default value */
        pgvar->value.required = 0;
    } else {
        /* user-specified value */
        e = ngx_postgres_requirement_options;
        for (i = 0; e[i].name.len; i++) {
            if ((e[i].name.len == value[4].len)
                && (ngx_strcasecmp(e[i].name.data, value[4].data) == 0))
            {
                pgvar->value.required = e[i].value;
                break;
            }
        }

        if (e[i].name.len == 0) {
            ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                               "postgres: invalid requirement option \"%V\""
                               " in \"%V\" directive", &value[4], &cmd->name);

            dd("returning NGX_CONF_ERROR");
            return NGX_CONF_ERROR;
        }
    }

    dd("returning NGX_CONF_OK");
    return NGX_CONF_OK;
}

/*
 * Based on: ngx_http_rewrite_module.c/ngx_http_rewrite_set
 * Copyright (C) Igor Sysoev
 */
char *
ngx_postgres_conf_escape(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_str_t                           *value = cf->args->elts;
    ngx_str_t                            src = value[cf->args->nelts - 1];
    ngx_int_t                            index;
    ngx_http_variable_t                 *v;
    ngx_http_script_var_code_t          *vcode;
    ngx_http_script_var_handler_code_t  *vhcode;
    ngx_postgres_rewrite_loc_conf_t     *rlcf;
    ngx_postgres_escape_t               *pge;
    ngx_str_t                            dst;
    ngx_uint_t                           empty;

    dd("entering");

    if ((src.len != 0) && (src.data[0] == '=')) {
        empty = 1;
        src.len--;
        src.data++;
    } else {
        empty = 0;
    }

    if (src.len == 0) {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "postgres: empty value in \"%V\" directive",
                           &cmd->name);

        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    if (cf->args->nelts == 2) {
        dst = src;
    } else {
        dst = value[1];
    }

    if (dst.len < 2) {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "postgres: empty variable name in \"%V\" directive",
                           &cmd->name);

        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    if (dst.data[0] != '$') {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "postgres: invalid variable name \"%V\""
                           " in \"%V\" directive", &dst, &cmd->name);

        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    dst.len--;
    dst.data++;

    v = ngx_http_add_variable(cf, &dst, NGX_HTTP_VAR_CHANGEABLE);
    if (v == NULL) {
        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    index = ngx_http_get_variable_index(cf, &dst);
    if (index == NGX_ERROR) {
        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    if (v->get_handler == NULL
        && ngx_strncasecmp(dst.data, (u_char *) "http_", 5) != 0
        && ngx_strncasecmp(dst.data, (u_char *) "sent_http_", 10) != 0
        && ngx_strncasecmp(dst.data, (u_char *) "upstream_http_", 14) != 0)
    {
        v->get_handler = ngx_postgres_rewrite_var;
        v->data = index;
    }

    rlcf = ngx_http_conf_get_module_loc_conf(cf, ngx_http_rewrite_module);

    if (ngx_postgres_rewrite_value(cf, rlcf, &src) != NGX_CONF_OK) {
        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    pge = ngx_http_script_start_code(cf->pool, &rlcf->codes,
                                     sizeof(ngx_postgres_escape_t));
    if (pge == NULL) {
        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    pge->code = ngx_postgres_escape_string;
    pge->empty = empty;

    if (v->set_handler) {
        vhcode = ngx_http_script_start_code(cf->pool, &rlcf->codes,
                                   sizeof(ngx_http_script_var_handler_code_t));
        if (vhcode == NULL) {
            dd("returning NGX_CONF_ERROR");
            return NGX_CONF_ERROR;
        }

        vhcode->code = ngx_http_script_var_set_handler_code;
        vhcode->handler = v->set_handler;
        vhcode->data = v->data;

        dd("returning NGX_CONF_OK");
        return NGX_CONF_OK;
    }

    vcode = ngx_http_script_start_code(cf->pool, &rlcf->codes,
                                       sizeof(ngx_http_script_var_code_t));
    if (vcode == NULL) {
        dd("returning NGX_CONF_ERROR");
        return NGX_CONF_ERROR;
    }

    vcode->code = ngx_http_script_set_var_code;
    vcode->index = (uintptr_t) index;

    dd("returning NGX_CONF_OK");
    return NGX_CONF_OK;
}

ngx_http_upstream_srv_conf_t *
ngx_postgres_find_upstream(ngx_http_request_t *r, ngx_url_t *url)
{
    ngx_http_upstream_main_conf_t   *umcf;
    ngx_http_upstream_srv_conf_t   **uscfp;
    ngx_uint_t                       i;

    dd("entering");

    umcf = ngx_http_get_module_main_conf(r, ngx_http_upstream_module);

    uscfp = umcf->upstreams.elts;

    for (i = 0; i < umcf->upstreams.nelts; i++) {

        if ((uscfp[i]->host.len != url->host.len)
            || (ngx_strncasecmp(uscfp[i]->host.data, url->host.data,
                                url->host.len) != 0))
        {
            dd("host doesn't match");
            continue;
        }

        if (uscfp[i]->port != url->port) {
            dd("port doesn't match: %d != %d",
               (int) uscfp[i]->port, (int) url->port);
            continue;
        }

        if (uscfp[i]->default_port && url->default_port
            && (uscfp[i]->default_port != url->default_port))
        {
            dd("default_port doesn't match");
            continue;
        }

        dd("returning");
        return uscfp[i];
    }

    dd("returning NULL");
    return NULL;
}
