#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include <ndk.h>
#include "ngx_http_set_rotate.h"
#include "ngx_http_set_misc_module.h"
#include <stdlib.h>


ngx_int_t
ngx_http_set_misc_set_rotate(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{
    ngx_http_variable_value_t   *rotate_from, *rotate_to, *rotate_num;
    ngx_int_t                    int_from, int_to, tmp, int_current;

    ngx_http_set_misc_loc_conf_t        *conf;

    rotate_num = &v[0];
    rotate_from = &v[1];
    rotate_to = &v[2];

    int_from = ngx_atoi(rotate_from->data, rotate_from->len);
    if (int_from == NGX_ERROR) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "set_rotate: bad \"from\" argument value: \"%v\"",
                      rotate_from);
        return NGX_ERROR;
    }

    int_to = ngx_atoi(rotate_to->data, rotate_to->len);
    if (int_to == NGX_ERROR) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "set_rotate: bad \"to\" argument value: \"%v\"",
                      rotate_to);
        return NGX_ERROR;
    }

    if (int_from > int_to) {
        tmp = int_from;
        int_from = int_to;
        int_to = tmp;
    }

    conf = ngx_http_get_module_loc_conf(r, ngx_http_set_misc_module);

    dd("current value not found: %d", (int) rotate_num->not_found);

    if (rotate_num->len == 0) {
        if (conf->current != NGX_CONF_UNSET) {
            int_current = conf->current;

        } else {
            int_current = int_from - 1;
        }

    } else {

        int_current = ngx_atoi(rotate_num->data, rotate_num->len);
        if (int_current == NGX_ERROR) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "set_rotate: bad current value: \"%v\"", rotate_num);

            if (conf->current != NGX_CONF_UNSET) {
                int_current = conf->current;

            } else {
                int_current = int_from - 1;
            }
        }
    }

    int_current++;

    if (int_current > int_to || int_current < int_from) {
        int_current = int_from;
    }

    conf->current = int_current;

    res->data = ngx_palloc(r->pool, NGX_INT_T_LEN);
    if (res->data == NULL) {
        return NGX_ERROR;
    }

    res->len = ngx_sprintf(res->data, "%i", int_current) - res->data;

    v->valid = 1;
    v->no_cacheable = 0;
    v->not_found = 0;

    return NGX_OK;
}


char *
ngx_http_set_rotate(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_str_t               *value;
    ndk_set_var_t            filter;

    value = cf->args->elts;

    filter.type = NDK_SET_VAR_MULTI_VALUE;
    filter.func = (void *) ngx_http_set_misc_set_rotate;
    filter.size = 3;
    filter.data = NULL;

    return ndk_set_var_multi_value_core(cf, &value[1], &value[1], &filter);
}

