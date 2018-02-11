#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include <ndk.h>
#include "ngx_http_set_default_value.h"


ngx_int_t
ngx_http_set_misc_set_if_empty(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{
    ngx_http_variable_value_t   *cur_v, *default_v;

    cur_v = &v[0];
    default_v = &v[1];

    if (cur_v->not_found || cur_v->len == 0) {
        res->data = default_v->data;
        res->len = default_v->len;

        return NGX_OK;
    }

    res->data = cur_v->data;
    res->len = cur_v->len;

    return NGX_OK;
}


char *
ngx_http_set_if_empty(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_str_t               *value;
    ndk_set_var_t            filter;

    value = cf->args->elts;

    filter.type = NDK_SET_VAR_MULTI_VALUE;
    filter.func = (void *) ngx_http_set_misc_set_if_empty;
    filter.size = 2;
    filter.data = NULL;

    return  ndk_set_var_multi_value_core(cf, &value[1], &value[1], &filter);
}

