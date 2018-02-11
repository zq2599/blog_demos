/*
 * Copyright (c) 2010, FRiCKLE Piotr Sikora <info@frickle.com>
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

#include "ngx_coolkit_handlers.h"
#include "ngx_coolkit_module.h"
#include "ngx_coolkit_variables.h"


static ngx_command_t ngx_coolkit_module_commands[] = {

    { ngx_string("override_method"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_1MORE,
      ngx_coolkit_conf_override_method,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    ngx_null_command
};

static ngx_http_variable_t ngx_coolkit_module_variables[] = {

    { ngx_string("remote_passwd"), NULL,
      ngx_coolkit_variable_remote_passwd, 0,
      NGX_HTTP_VAR_NOCACHEABLE, 0 },

    { ngx_string("location"), NULL,
      ngx_coolkit_variable_location, 0,
      NGX_HTTP_VAR_NOCACHEABLE, 0 },

    { ngx_null_string, NULL, NULL, 0, 0, 0 }
};

static ngx_http_module_t ngx_coolkit_module_ctx = {
    ngx_coolkit_add_variables,    /* preconfiguration */
    ngx_coolkit_init,             /* postconfiguration */

    NULL,                         /* create main configuration */
    NULL,                         /* init main configuration */

    NULL,                         /* create server configuration */
    NULL,                         /* merge server configuration */

    ngx_coolkit_create_loc_conf,  /* create location configuration */
    ngx_coolkit_merge_loc_conf    /* merge location configuration */
};

ngx_module_t ngx_coolkit_module = {
    NGX_MODULE_V1,
    &ngx_coolkit_module_ctx,      /* module context */
    ngx_coolkit_module_commands,  /* module directives */
    NGX_HTTP_MODULE,              /* module type */
    NULL,                         /* init master */
    NULL,                         /* init module */
    NULL,                         /* init process */
    NULL,                         /* init thread */
    NULL,                         /* exit thread */
    NULL,                         /* exit process */
    NULL,                         /* exit master */
    NGX_MODULE_V1_PADDING
};

/*
 * nginx assumes that HTTP method name is followed by space, so add it here
 * instead of allocating memory for copy with added space for each request.
 */
ngx_conf_bitmask_t ngx_coolkit_http_methods[] = {
   { ngx_string("GET "),       NGX_HTTP_GET },
   { ngx_string("HEAD "),      NGX_HTTP_HEAD },
   { ngx_string("POST "),      NGX_HTTP_POST },
   { ngx_string("PUT "),       NGX_HTTP_PUT },
   { ngx_string("DELETE "),    NGX_HTTP_DELETE },
   { ngx_string("MKCOL "),     NGX_HTTP_MKCOL },
   { ngx_string("COPY "),      NGX_HTTP_COPY },
   { ngx_string("MOVE "),      NGX_HTTP_MOVE },
   { ngx_string("OPTIONS "),   NGX_HTTP_OPTIONS },
   { ngx_string("PROPFIND "),  NGX_HTTP_PROPFIND },
   { ngx_string("PROPPATCH "), NGX_HTTP_PROPPATCH },
   { ngx_string("LOCK "),      NGX_HTTP_LOCK },
   { ngx_string("UNLOCK "),    NGX_HTTP_UNLOCK },
#if defined(nginx_version) && (nginx_version >= 8041)
   { ngx_string("PATCH "),     NGX_HTTP_PATCH },
#endif
    { ngx_null_string, 0 }
};


ngx_int_t
ngx_coolkit_add_variables(ngx_conf_t *cf)
{
    ngx_http_variable_t  *var, *v;

    for (v = ngx_coolkit_module_variables; v->name.len; v++) {
        var = ngx_http_add_variable(cf, &v->name, v->flags);
        if (var == NULL) {
            return NGX_ERROR;
        }

        var->get_handler = v->get_handler;
        var->data = v->data;
    }

    return NGX_OK;
}

ngx_int_t
ngx_coolkit_init(ngx_conf_t *cf)
{
    ngx_http_handler_pt        *h;
    ngx_http_core_main_conf_t  *cmcf;

    cmcf = ngx_http_conf_get_module_main_conf(cf, ngx_http_core_module);

    h = ngx_array_push(&cmcf->phases[NGX_HTTP_SERVER_REWRITE_PHASE].handlers);
    if (h == NULL) {
        return NGX_ERROR;
    }

    *h = ngx_coolkit_override_method_handler;

    h = ngx_array_push(&cmcf->phases[NGX_HTTP_REWRITE_PHASE].handlers);
    if (h == NULL) {
        return NGX_ERROR;
    }

    *h = ngx_coolkit_override_method_handler;

    return NGX_OK;
}

void *
ngx_coolkit_create_loc_conf(ngx_conf_t *cf)
{
    ngx_coolkit_loc_conf_t  *conf;

    conf = ngx_palloc(cf->pool, sizeof(ngx_coolkit_loc_conf_t));
    if (conf == NULL) {
        return NULL;
    }

    /*
     * set by ngx_pcalloc():
     *
     *     conf->override_methods = 0
     */

    conf->override_source = NGX_CONF_UNSET_PTR;

    return conf;
}

char *
ngx_coolkit_merge_loc_conf(ngx_conf_t *cf, void *parent, void *child)
{
    ngx_coolkit_loc_conf_t  *prev = parent;
    ngx_coolkit_loc_conf_t  *conf = child;

    if (conf->override_source == NGX_CONF_UNSET_PTR) {
       if (prev->override_source == NGX_CONF_UNSET_PTR) {
           /* default */
           conf->override_methods = 0;
           conf->override_source = NULL;
       } else {
           /* merge */
           conf->override_methods = prev->override_methods;
           conf->override_source = prev->override_source;
       }
    }

    return NGX_CONF_OK;
}

char *
ngx_coolkit_conf_override_method(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_str_t                         *value = cf->args->elts;
    ngx_str_t                          source = value[cf->args->nelts - 1];
    ngx_coolkit_loc_conf_t            *cklcf = conf;
    ngx_http_compile_complex_value_t   ccv;
    ngx_conf_bitmask_t                *b;
    ngx_uint_t                         i, j;

    if (cklcf->override_source != NGX_CONF_UNSET_PTR) {
        return "is duplicate";
    }

    if (ngx_strcmp(value[1].data, "off") == 0) {
        cklcf->override_source = NULL;
        return NGX_CONF_OK;
    }

    if (source.len == 0) {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "coolkit: empty source in \"%V\" directive",
                           &cmd->name);

        return NGX_CONF_ERROR;
    }

    if (cf->args->nelts == 2) {
        /* override method for all methods */
        cklcf->override_methods = 0xFFFF;
    } else {
        /* override method only for specified methods */
        cklcf->override_methods = 0;

        for (i = 1; i < cf->args->nelts - 1; i++) {
            b = ngx_coolkit_http_methods;
            for (j = 0; b[j].name.len; j++) {
                if ((b[j].name.len - 1 == value[i].len)
                    && (ngx_strncasecmp(b[j].name.data,
                                        value[i].data, value[i].len) == 0))
                {
                    cklcf->override_methods |= b[j].mask;
                    break;
                }
            }

            if (b[j].name.len == 0) {
                ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                                   "coolkit: invalid method \"%V\""
                                   " in \"%V\" directive",
                                   &value[i], &cmd->name);

                return NGX_CONF_ERROR;
            }
        }
    }

    cklcf->override_source = ngx_palloc(cf->pool,
                                        sizeof(ngx_http_complex_value_t));
    if (cklcf->override_source == NULL) {
        return NGX_CONF_ERROR;
    }

    ngx_memzero(&ccv, sizeof(ngx_http_compile_complex_value_t));

    ccv.cf = cf;
    ccv.value = &source;
    ccv.complex_value = cklcf->override_source;

    if (ngx_http_compile_complex_value(&ccv) != NGX_OK) {
        return NGX_CONF_ERROR;
    }

    return NGX_CONF_OK;
}
