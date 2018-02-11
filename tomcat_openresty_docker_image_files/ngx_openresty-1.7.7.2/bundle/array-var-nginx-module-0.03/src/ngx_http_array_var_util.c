
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_array_var_util.h"


static ngx_int_t ngx_http_array_var_variable_not_found(ngx_http_request_t *r,
    ngx_http_variable_value_t *v, uintptr_t data);


/* Modified from the ngx_strlcasestrn function in ngx_string.h
 * Copyright (C) by Igor Sysoev */
u_char *
ngx_http_array_var_strlstrn(u_char *s1, u_char *last, u_char *s2, size_t n)
{
    ngx_uint_t  c1, c2;

    c2 = (ngx_uint_t) *s2++;
    last -= n;

    do {
        do {
            if (s1 >= last) {
                return NULL;
            }

            c1 = (ngx_uint_t) *s1++;

        } while (c1 != c2);

    } while (ngx_strncmp(s1, s2, n) != 0);

    return --s1;
}


ndk_set_var_value_pt
ngx_http_array_var_get_func_from_cmd(u_char *name, size_t name_len)
{
    ndk_set_var_t           *filter;
    ngx_uint_t               i;
    ngx_module_t            *module;
    ngx_command_t           *cmd;

    for (i = 0; ngx_modules[i]; i++) {
        module = ngx_modules[i];
        if (module->type != NGX_HTTP_MODULE) {
            continue;
        }

        cmd = ngx_modules[i]->commands;
        if (cmd == NULL) {
            continue;
        }

        for ( /* void */ ; cmd->name.len; cmd++) {
            if (cmd->set != ndk_set_var_value) {
                continue;
            }

            filter = cmd->post;
            if (filter == NULL) {
                continue;
            }

            if (cmd->name.len != name_len
                || ngx_strncmp(cmd->name.data, name, name_len) != 0)
            {
                continue;
            }

            return (ndk_set_var_value_pt) filter->func;
        }
    }

    return NULL;
}


ngx_int_t
ngx_http_array_var_add_variable(ngx_conf_t *cf, ngx_str_t *name)
{
    ngx_http_variable_t         *v;

    v = ngx_http_add_variable(cf, name, NGX_HTTP_VAR_CHANGEABLE);
    if (v == NULL) {
        return NGX_ERROR;
    }

    v->get_handler = ngx_http_array_var_variable_not_found;

    return ngx_http_get_variable_index(cf, name);
}


static ngx_int_t
ngx_http_array_var_variable_not_found(ngx_http_request_t *r,
    ngx_http_variable_value_t *v, uintptr_t data)
{
    v->not_found = 1;
    return NGX_OK;
}
