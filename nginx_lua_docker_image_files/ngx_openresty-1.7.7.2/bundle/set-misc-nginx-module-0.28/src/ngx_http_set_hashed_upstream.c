#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_set_hashed_upstream.h"


ngx_uint_t
ngx_http_set_misc_apply_distribution(ngx_log_t *log, ngx_uint_t hash,
    ndk_upstream_list_t *ul, ngx_http_set_misc_distribution_t type)
{
    switch (type) {
    case ngx_http_set_misc_distribution_modula:
        return (uint32_t) hash % (uint32_t) ul->nelts;

    default:
        ngx_log_error(NGX_LOG_ERR, log, 0, "apply_distribution: "
                "unknown distribution: %d", type);

        return 0;
    }

    /* impossible to reach here */
}


ngx_int_t
ngx_http_set_misc_set_hashed_upstream(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v, void *data)
{
    ngx_str_t                  **u;
    ndk_upstream_list_t         *ul = data;
    ngx_str_t                    ulname;
    ngx_uint_t                   hash, index;
    ngx_http_variable_value_t   *key;

    if (ul == NULL) {
        ulname.data = v->data;
        ulname.len = v->len;

        dd("ulname: %.*s", (int) ulname.len, ulname.data);

        ul = ndk_get_upstream_list(ndk_http_get_main_conf(r),
                                            ulname.data, ulname.len);

        if (ul == NULL) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                    "set_hashed_upstream: upstream list \"%V\" "
                    "not defined yet", &ulname);
            return NGX_ERROR;
        }

        key = v + 1;
    } else {
        key = v;
    }

    if (ul->nelts == 0) {
        res->data = NULL;
        res->len = 0;

        return NGX_OK;
    }

    u = ul->elts;

    dd("upstream list: %d upstreams found", (int) ul->nelts);

    if (ul->nelts == 1) {
        dd("only one upstream found in the list");

        res->data = u[0]->data;
        res->len = u[0]->len;

        return NGX_OK;
    }

    dd("key: \"%.*s\"", key->len, key->data);

    hash = ngx_hash_key_lc(key->data, key->len);

    index = ngx_http_set_misc_apply_distribution(r->connection->log, hash, ul,
            ngx_http_set_misc_distribution_modula);

    res->data = u[index]->data;
    res->len = u[index]->len;

    return NGX_OK;
}


char *
ngx_http_set_hashed_upstream(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_str_t               *value;
    ndk_set_var_t            filter;
    ngx_uint_t               n;
    ngx_str_t               *var;
    ngx_str_t               *ulname;
    ndk_upstream_list_t     *ul;
    ngx_str_t               *v;

    value = cf->args->elts;

    var = &value[1];
    ulname = &value[2];

    n = ngx_http_script_variables_count(ulname);

    filter.func = (void *) ngx_http_set_misc_set_hashed_upstream;

    if (n) {
        /* upstream list name contains variables */
        v = &value[2];
        filter.size = 2;
        filter.data = NULL;
        filter.type = NDK_SET_VAR_MULTI_VALUE_DATA;

        return  ndk_set_var_multi_value_core(cf, var, v, &filter);
    }

    ul = ndk_get_upstream_list(ndk_http_conf_get_main_conf(cf),
                                            ulname->data, ulname->len);
    if (ul == NULL) {
        ngx_log_error(NGX_LOG_ERR, cf->log, 0,
                      "set_hashed_upstream: upstream list \"%V\" "
                      "not defined yet", ulname);
        return NGX_CONF_ERROR;
    }

    v = &value[3];

    filter.size = 1;
    filter.data = ul;
    filter.type = NDK_SET_VAR_VALUE_DATA;

    return ndk_set_var_value_core(cf, var, v, &filter);
}

