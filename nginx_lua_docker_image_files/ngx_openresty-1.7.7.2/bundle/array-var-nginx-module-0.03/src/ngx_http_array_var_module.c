
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_array_var_util.h"
#include <ndk.h>


static ngx_str_t  ngx_http_array_it_key = ngx_string("array_it");


typedef struct {
    ngx_uint_t          nargs;
} ngx_http_array_split_data_t;


typedef struct {
    unsigned                         in_place;
    ngx_http_complex_value_t        *template;
    ngx_int_t                        array_it_index;
} ngx_http_array_map_data_t;


typedef struct {
    unsigned                         in_place;
} ngx_http_array_map_op_data_t;


static char * ngx_http_array_split(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf);
static char * ngx_http_array_map(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf);
static char * ngx_http_array_map_op(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf);
static char * ngx_http_array_join(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf);
static ngx_int_t ngx_http_array_var_split(ngx_http_request_t *r,
    ngx_str_t *res, ngx_http_variable_value_t *v, void *data);
static ngx_int_t ngx_http_array_var_map(ngx_http_request_t *r,
    ngx_str_t *res, ngx_http_variable_value_t *v, void *data);
static ngx_int_t ngx_http_array_var_map_op(ngx_http_request_t *r,
    ngx_str_t *res, ngx_http_variable_value_t *v, void *data);
static ngx_int_t ngx_http_array_var_join(ngx_http_request_t *r,
    ngx_str_t *res, ngx_http_variable_value_t *v);


static ngx_command_t  ngx_http_array_var_commands[] = {
    {
        ngx_string ("array_split"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
                          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_2MORE,
        ngx_http_array_split,
        0,
        0,
        NULL
    },
    {
        ngx_string ("array_map"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
                          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
                          |NGX_CONF_TAKE23,
        ngx_http_array_map,
        0,
        0,
        NULL
    },
    {
        ngx_string ("array_map_op"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
                          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
                          |NGX_CONF_TAKE23,
        ngx_http_array_map_op,
        0,
        0,
        (void *) ngx_http_array_var_map_op
    },
    {
        ngx_string("array_join"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
                          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
                          |NGX_CONF_TAKE23,
        ngx_http_array_join,
        0,
        0,
        (void *) ngx_http_array_var_join
    },

    ngx_null_command
};


static ngx_http_module_t  ngx_http_array_var_module_ctx = {
    NULL,                                  /* preconfiguration */
    NULL,                                  /* postconfiguration */

    NULL,                                  /* create main configuration */
    NULL,                                  /* init main configuration */

    NULL,                                  /* create server configuration */
    NULL,                                  /* merge server configuration */

    NULL,                                  /* create location configuration */
    NULL,                                  /* merge location configuration */
};


ngx_module_t  ngx_http_array_var_module = {
    NGX_MODULE_V1,
    &ngx_http_array_var_module_ctx,          /* module context */
    ngx_http_array_var_commands,             /* module directives */
    NGX_HTTP_MODULE,                         /* module type */
    NULL,                                    /* init master */
    NULL,                                    /* init module */
    NULL,                                    /* init process */
    NULL,                                    /* init thread */
    NULL,                                    /* exit thread */
    NULL,                                    /* exit process */
    NULL,                                    /* exit master */
    NGX_MODULE_V1_PADDING
};


static char *
ngx_http_array_split(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ndk_set_var_t                    filter;
    ngx_str_t                        target;
    ngx_str_t                       *value;
    ngx_str_t                       *bad_arg;
    ngx_http_array_split_data_t     *data = NULL;

    data = ngx_palloc(cf->pool, sizeof(ngx_http_array_split_data_t));
    if (data == NULL) {
        return NGX_CONF_ERROR;
    }

    filter.type = NDK_SET_VAR_MULTI_VALUE_DATA;
    filter.func = (void *) ngx_http_array_var_split;
    filter.data = data;

    value = cf->args->elts;

    if (cf->args->nelts == 2 + 1) {
        dd("array_split $sep $var");
        data->nargs = filter.size = 2;
        target = value[2];
        return ndk_set_var_multi_value_core(cf, &target, &value[1], &filter);
    }

    /* cf->args->nelts >= 3 + 1 */

    if (value[3].len >= sizeof("to=") - 1
            && ngx_str3cmp(value[3].data, 't', 'o', '='))
    {
        dd("array_split $sep $str to=$array");
        data->nargs = filter.size = 2;

        target.data = value[3].data + sizeof("to=") - 1;
        target.len = value[3].len - (sizeof("to=") - 1);
        dd("split target: %.*s", (int) target.len, target.data);

        if (cf->args->nelts > 3 + 1) {
            bad_arg = &value[4];
            goto unexpected_arg;
        }

        return ndk_set_var_multi_value_core(cf, &target, &value[1], &filter);
    }

    /* the 3rd argument is max_items */

    if (cf->args->nelts > 4 + 1) {
        bad_arg = &value[5];
        goto unexpected_arg;
    }

    if (cf->args->nelts == 4 + 1) {
        /* array_split $sep $str $max to=$array */

        if (value[4].len < sizeof("to=") - 1
                || ! (ngx_str3cmp(value[4].data, 't', 'o', '=')))
        {
            ngx_conf_log_error(NGX_LOG_ERR, cf, 0,
                    "%V: expecting the \"to\" option at the "
                    "4th argument: \"%V\"",
                    &cmd->name, &value[4]);

            return NGX_CONF_ERROR;
        }

        data->nargs = filter.size = 3;

        target.data = value[4].data + sizeof("to=") - 1;
        target.len = value[4].len - (sizeof("to=") - 1);

        return ndk_set_var_multi_value_core(cf, &target, &value[1], &filter);
    }

    /* cf->args->nelts == 3 + 1 */

    /* array_split $sep $var $max */

    target = value[2];
    data->nargs = filter.size = 3;

    return ndk_set_var_multi_value_core(cf, &target, &value[1], &filter);

unexpected_arg:

    ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
            "%V: unexpected argument \"%V\"",
            &cmd->name, bad_arg);

    return NGX_CONF_ERROR;
}


static char *
ngx_http_array_map(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ndk_set_var_t                        filter;
    ngx_str_t                            target;
    ngx_str_t                           *value;
    ngx_http_array_map_data_t           *data;
    ngx_http_compile_complex_value_t     ccv;

    data = ngx_palloc(cf->pool, sizeof(ngx_http_array_map_data_t));
    if (data == NULL) {
        return NGX_CONF_ERROR;
    }

    data->template = ngx_palloc(cf->pool, sizeof(ngx_http_complex_value_t));
    if (data->template == NULL) {
        return NGX_CONF_ERROR;
    }

    value = cf->args->elts;

    if (value[1].len == 0) {
        ngx_memzero(data->template, sizeof(ngx_http_complex_value_t));

    } else {
        ngx_memzero(&ccv, sizeof(ngx_http_compile_complex_value_t));

        ccv.cf = cf;
        ccv.value = &value[1];
        ccv.complex_value = data->template;

        if (ngx_http_compile_complex_value(&ccv) != NGX_OK) {
            return NGX_CONF_ERROR;
        }
    }

    filter.type = NDK_SET_VAR_VALUE_DATA;
    filter.func = (void *) ngx_http_array_var_map;
    filter.data = data;
    filter.size = 1;

    data->array_it_index = ngx_http_array_var_add_variable(cf,
            &ngx_http_array_it_key);

    if (data->array_it_index == NGX_ERROR) {
        return NGX_CONF_ERROR;
    }

    if (cf->args->nelts == 2 + 1) {
        /* array_map $template $array */
        data->in_place = 1;
        target = value[2];

    } else {
        /* cf->args->nelts == 3 + 1 */

        if (value[3].len < sizeof("to=") - 1
                || ! (ngx_str3cmp(value[3].data, 't', 'o', '=')))
        {
            ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                    "%V: expecting the \"to\" option at "
                    "the 3rd argument: \"%V\"",
                    &cmd->name, &value[3]);

            return NGX_CONF_ERROR;
        }

        target.data = value[3].data + sizeof("to=") - 1;
        target.len = value[3].len - (sizeof("to=") - 1);
        data->in_place = 0;
    }

    return ndk_set_var_value_core(cf, &target, &value[2], &filter);
}


static char *
ngx_http_array_map_op(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ngx_http_array_map_op_data_t    *data;
    ndk_set_var_t                    filter;
    ngx_str_t                        target;
    ngx_str_t                       *value;
    ngx_str_t                       *bad_arg;

    data = ngx_palloc(cf->pool, sizeof(ngx_http_array_map_op_data_t));
    if (data == NULL) {
        return NGX_CONF_ERROR;
    }

    filter.type = NDK_SET_VAR_MULTI_VALUE_DATA;
    filter.func = cmd->post;
    filter.data = data;

    value = cf->args->elts;

    if (cf->args->nelts == 2 + 1) {
        dd("array_join $sep $var");

        filter.size = 2;
        data->in_place = 1;

        target = value[2];

        dd("array join target: %.*s", (int) target.len, target.data);

        return ndk_set_var_multi_value_core(cf, &target, &value[1], &filter);
    }

    /* cf->args->nelts == 3 + 1 */

    if (value[3].len >= sizeof("to=") - 1
            && ngx_str3cmp(value[3].data, 't', 'o', '='))
    {
        /* array_join $sep $str to=$array */
        filter.size = 2;
        data->in_place = 0;

        target.data = value[3].data + sizeof("to=") - 1;
        target.len = value[3].len - (sizeof("to=") - 1);

        if (cf->args->nelts > 3 + 1) {
            bad_arg = &value[4];

        } else {
            return ndk_set_var_multi_value_core(cf, &target, &value[1],
                    &filter);
        }

    } else {
        bad_arg = &value[3];
    }

    ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
            "%V: unexpected argument \"%V\"",
            &cmd->name, bad_arg);

    return NGX_CONF_ERROR;
}


static char *
ngx_http_array_join(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)
{
    ndk_set_var_t                    filter;
    ngx_str_t                        target;
    ngx_str_t                       *value;
    ngx_str_t                       *bad_arg;

    filter.type = NDK_SET_VAR_MULTI_VALUE;
    filter.func = cmd->post;

    value = cf->args->elts;

    if (cf->args->nelts == 2 + 1) {
        dd("array_join $sep $var");

        filter.size = 2;
        target = value[2];

        dd("array join target: %.*s", (int) target.len, target.data);

        return ndk_set_var_multi_value_core(cf, &target, &value[1], &filter);
    }

    /* cf->args->nelts == 3 + 1 */

    if (value[3].len >= sizeof("to=") - 1
            && ngx_str3cmp(value[3].data, 't', 'o', '='))
    {
        /* array_join $sep $str to=$array */
        filter.size = 2;

        target.data = value[3].data + sizeof("to=") - 1;
        target.len = value[3].len - (sizeof("to=") - 1);

        if (cf->args->nelts > 3 + 1) {
            bad_arg = &value[4];
        } else {
            return ndk_set_var_multi_value_core(cf, &target, &value[1],
                    &filter);
        }

    } else {
        bad_arg = &value[3];
    }

    ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
            "%V: unexpected argument \"%V\"",
            &cmd->name, bad_arg);

    return NGX_CONF_ERROR;
}


static ngx_int_t
ngx_http_array_var_split(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v, void *data)
{
    ngx_http_array_split_data_t             *conf = data;
    ngx_http_variable_value_t               *sep, *str;
    ngx_str_t                               *s;
    u_char                                  *pos, *end, *last = NULL;
    ssize_t                                  max, i, len = 4;
    ngx_array_t                             *array;

    if (conf->nargs == 3) {
        max = ngx_atosz(v[2].data, v[2].len);
        if (max == NGX_ERROR) {
            ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                          "array_split: invalid max items: \"%V\"",
                          &v[2]);

            return NGX_ERROR;
        }
    } else {
        max = 0;
    }

    if (max) {
        len = max;
    }

    array = ngx_array_create(r->pool, len, sizeof(ngx_str_t));
    if (array == NULL) {
        return NGX_ERROR;
    }

    sep = &v[0];
    str = &v[1];

    pos = str->data;
    end = str->data + str->len;

    i = 0;

    if (sep->len == 0) {
        /* split each char into an array elem */

        while (i != max - 1 && pos < end - 1) {
            s = ngx_array_push(array);
            if (s == NULL) {
                return NGX_ERROR;
            }

            s->data = pos;
            s->len = 1;

            pos++;
            i++;
        }

        goto done;
    }

    while (i != max - 1) {
        last = ngx_http_array_var_strlstrn(pos, end, sep->data,
                                           sep->len - 1);
        if (last == NULL) {
            break;
        }

        s = ngx_array_push(array);
        if (s == NULL) {
            return NGX_ERROR;
        }

        s->data = pos;
        s->len = last - pos;

        dd("split item %.*s", (int) s->len, s->data);

        pos = last + sep->len;
        i++;
    }

done:
    dd("pos %p, last %p, end %p", pos, last, end);

    s = ngx_array_push(array);
    if (s == NULL) {
        return NGX_ERROR;
    }

    s->data = pos;
    s->len = end - pos;

    dd("split item %.*s", (int) s->len, s->data);

    dd("split: array size: %d", (int) array->nelts);
    dd("split array ptr: %p", array);

    res->data = (u_char *) array;
    res->len = sizeof(ngx_array_t);

    return NGX_OK;
}


static ngx_int_t
ngx_http_array_var_map(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v, void *data)
{
    ngx_http_array_map_data_t       *conf = data;
    ngx_http_variable_value_t       *array_it;
    ngx_uint_t                       i;
    ngx_str_t                       *value, *new_value;
    ngx_array_t                     *array, *new_array;

    dd("entered array var map");

    if (conf->template == NULL) {
        dd("template empty");

        return NGX_OK;
    }

    if (v[0].len != sizeof(ngx_array_t)) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "array_join: invalid array variable value in the 2nd "
                      "argument: \"%.*s\"", &v[0]);

        return NGX_ERROR;
    }

    array = (ngx_array_t *) v[0].data;

    value = array->elts;

    array_it = ngx_http_get_indexed_variable(r, conf->array_it_index);

    if ( conf->in_place) {
        new_array = array;

    } else {
        new_array = ngx_array_create(r->pool, array->nelts,
                                     sizeof(ngx_str_t));
        if (new_array == NULL) {
            return NGX_ERROR;
        }
    }

    dd("array var map: array size: %d", (int) array->nelts);

    array_it->not_found = 0;
    array_it->valid = 1;

    for (i = 0; i < array->nelts; i++) {
        array_it->data = value[i].data;
        array_it->len = value[i].len;

        dd("array it: %.*s", array_it->len, array_it->data);

        if (conf->in_place) {
            new_value = &value[i];

        } else {
            new_value = ngx_array_push(new_array);
            if (new_value == NULL) {
                return NGX_ERROR;
            }
        }

        if (ngx_http_complex_value(r, conf->template, new_value) != NGX_OK) {
            return NGX_ERROR;
        }

        dd("array var map: new item: %.*s", (int) new_value->len,
           new_value->data);
    }

    array_it->not_found = 1;
    array_it->valid = 0;

    res->data = (u_char *) new_array;
    res->len = sizeof(ngx_array_t);

    return NGX_OK;
}


static ngx_int_t
ngx_http_array_var_map_op(ngx_http_request_t *r,
    ngx_str_t *res, ngx_http_variable_value_t *v, void *data)
{
    ngx_http_variable_value_t            arg;
    ngx_http_array_map_op_data_t        *conf = data;
    ndk_set_var_value_pt                 func;
    ngx_int_t                            rc;
    ngx_uint_t                           i;
    ngx_str_t                           *value;
    ngx_str_t                           *new_value;
    ngx_array_t                         *array;
    ngx_array_t                         *new_array;

    func = ngx_http_array_var_get_func_from_cmd(v[0].data, v[0].len);

    if (func == NULL) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "array_map_op: directive \"%v\" not found "
                      "or does not use ndk_set_var_value",
                      &v[0]);

        return NGX_ERROR;
    }

    if (v[1].len != sizeof(ngx_array_t)) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "array_map_op: invalid array variable value in the 2nd "
                      "argument: \"%.*s\"", &v[0]);

        return NGX_ERROR;
    }

    array = (ngx_array_t *) v[1].data;

    value = array->elts;

    if ( conf->in_place) {
        new_array = array;

    } else {
        new_array = ngx_array_create(r->pool, array->nelts,
                                     sizeof(ngx_str_t));
        if (new_array == NULL) {
            return NGX_ERROR;
        }
    }

    for (i = 0; i < array->nelts; i++) {
        arg.data = value[i].data;
        arg.len = value[i].len;
        arg.valid = 1;
        arg.not_found = 0;

        if (conf->in_place) {
            new_value = &value[i];

        } else {
            new_value = ngx_array_push(new_array);
            if (new_value == NULL) {
                return NGX_ERROR;
            }
        }

        rc = func(r, new_value, &arg);
        if (rc != NGX_OK) {
            return NGX_ERROR;
        }
    }

    res->data = (u_char *) new_array;
    res->len = sizeof(ngx_array_t);

    return NGX_OK;
}


static ngx_int_t
ngx_http_array_var_join(ngx_http_request_t *r,
    ngx_str_t *res, ngx_http_variable_value_t *v)
{
    ngx_http_variable_value_t           *sep;
    ngx_array_t                         *array;
    size_t                               len;
    ngx_str_t                           *value;
    ngx_uint_t                           i;
    u_char                              *p;

    sep = &v[0];

    dd("sep %.*s", sep->len, sep->data);

    if (v[1].len != sizeof(ngx_array_t)) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "array_join: invalid array variable value in the "
                      "2nd argument: \"%V\"", &v[1]);

        return NGX_ERROR;
    }

    array = (ngx_array_t *) v[1].data;

    dd("join array ptr %p", array);
    dd("array->nelts: %d", (int) array->nelts);

    if (array->nelts == 0) {
        res->data = NULL;
        res->len = 0;
        return NGX_OK;
    }

    value = array->elts;

    len = sep->len * (array->nelts - 1);

    for (i = 0; i < array->nelts; i++) {
        len += value[i].len;
    }

    dd("buf len %d", (int) len);

    res->data = ngx_palloc(r->pool, len);
    if (res->data == NULL) {
        return NGX_ERROR;
    }

    res->len = len;

    p = res->data;

    for (i = 0; i < array->nelts; i++) {
        dd("copying elem of size %d", (int) value[i].len);
        p = ngx_copy(p, value[i].data, value[i].len);
        if (i < array->nelts - 1) {
            p = ngx_copy(p, sep->data, sep->len);
        }
    }

    if (p != res->data + res->len) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "array_join: buffer error");

        return NGX_ERROR;
    }

    return NGX_OK;
}
