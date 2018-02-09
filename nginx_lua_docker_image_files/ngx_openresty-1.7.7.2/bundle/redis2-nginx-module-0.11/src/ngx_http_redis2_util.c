#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_redis2_util.h"


static size_t ngx_get_num_size(uint64_t i);


char *
ngx_http_redis2_set_complex_value_slot(ngx_conf_t *cf, ngx_command_t *cmd,
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


ngx_http_upstream_srv_conf_t *
ngx_http_redis2_upstream_add(ngx_http_request_t *r, ngx_url_t *url)
{
    ngx_http_upstream_main_conf_t  *umcf;
    ngx_http_upstream_srv_conf_t  **uscfp;
    ngx_uint_t                      i;

    umcf = ngx_http_get_module_main_conf(r, ngx_http_upstream_module);

    uscfp = umcf->upstreams.elts;

    for (i = 0; i < umcf->upstreams.nelts; i++) {

        if (uscfp[i]->host.len != url->host.len
            || ngx_strncasecmp(uscfp[i]->host.data, url->host.data,
               url->host.len) != 0)
        {
            dd("upstream_add: host not match");
            continue;
        }

        if (uscfp[i]->port != url->port) {
            dd("upstream_add: port not match: %d != %d",
                    (int) uscfp[i]->port, (int) url->port);
            continue;
        }

        if (uscfp[i]->default_port
            && url->default_port
            && uscfp[i]->default_port != url->default_port)
        {
            dd("upstream_add: default_port not match");
            continue;
        }

        return uscfp[i];
    }

    dd("no upstream found: %.*s", (int) url->host.len, url->host.data);

    return NULL;
}


static size_t
ngx_get_num_size(uint64_t i)
{
    size_t          n = 0;

    do {
        i = i / 10;
        n++;
    } while (i > 0);

    return n;
}


ngx_int_t
ngx_http_redis2_build_query(ngx_http_request_t *r, ngx_array_t *queries,
    ngx_buf_t **b)
{
    ngx_uint_t                       i, j;
    ngx_uint_t                       n;
    ngx_str_t                       *arg;
    ngx_array_t                     *args;
    size_t                           len;
    ngx_array_t                    **query_args;
    ngx_http_complex_value_t       **complex_arg;
    u_char                          *p;
    ngx_http_redis2_loc_conf_t      *rlcf;

    rlcf = ngx_http_get_module_loc_conf(r, ngx_http_redis2_module);

    query_args = rlcf->queries->elts;

    n = 0;
    for (i = 0; i < rlcf->queries->nelts; i++) {
        for (j = 0; j < query_args[i]->nelts; j++) {
            n++;
        }
    }

    args = ngx_array_create(r->pool, n, sizeof(ngx_str_t));

    if (args == NULL) {
        return NGX_ERROR;
    }

    len = 0;
    n = 0;

    for (i = 0; i < rlcf->queries->nelts; i++) {
        complex_arg = query_args[i]->elts;

        len += sizeof("*") - 1
             + ngx_get_num_size(query_args[i]->nelts)
             + sizeof("\r\n") - 1
             ;

        for (j = 0; j < query_args[i]->nelts; j++) {
            n++;

            arg = ngx_array_push(args);
            if (arg == NULL) {
                return NGX_ERROR;
            }

            if (ngx_http_complex_value(r, complex_arg[j], arg) != NGX_OK) {
                return NGX_ERROR;
            }

            len += sizeof("$") - 1
                 + ngx_get_num_size(arg->len)
                 + sizeof("\r\n") - 1
                 + arg->len
                 + sizeof("\r\n") - 1
                 ;
        }
    }

    *b = ngx_create_temp_buf(r->pool, len);
    if (*b == NULL) {
        return NGX_ERROR;
    }

    p = (*b)->last;

    arg = args->elts;

    n = 0;
    for (i = 0; i < rlcf->queries->nelts; i++) {
        *p++ = '*';
        p = ngx_sprintf(p, "%uz", query_args[i]->nelts);
        *p++ = '\r'; *p++ = '\n';

        for (j = 0; j < query_args[i]->nelts; j++) {
            *p++ = '$';
            p = ngx_sprintf(p, "%uz", arg[n].len);
            *p++ = '\r'; *p++ = '\n';
            p = ngx_copy(p, arg[n].data, arg[n].len);
            *p++ = '\r'; *p++ = '\n';

            n++;
        }
    }

    dd("query: %.*s", (int) (p - (*b)->pos), (*b)->pos);

    if (p - (*b)->pos != (ssize_t) len) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                "redis2: redis2_query buffer error %uz != %uz",
                (size_t) (p - (*b)->pos), len);

        return NGX_ERROR;
    }

    (*b)->last = p;

    return NGX_OK;
}

