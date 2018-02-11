#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include <ndk.h>
#include "ngx_http_set_random.h"
#include <stdlib.h>


ngx_int_t
ngx_http_set_misc_set_random(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{
    ngx_http_variable_value_t   *rand_from, *rand_to;
    ngx_int_t                    int_from, int_to, tmp, random;

    rand_from = v;
    rand_to = v + 1;

    int_from = ngx_atoi(rand_from->data, rand_from->len);
    if (int_from == NGX_ERROR) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "set_random: bad \"from\" argument: %v", rand_from);
        return NGX_ERROR;
    }

    int_to = ngx_atoi(rand_to->data, rand_to->len);
    if (int_to == NGX_ERROR) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "set_random: bad \"to\" argument: %v", rand_to);
        return NGX_ERROR;
    }

    if (int_from > int_to) {
        tmp = int_from;
        int_from = int_to;
        int_to = tmp;
    }

    random = rand() % (int_to - int_from + 1) + int_from;

    res->data = ngx_palloc(r->pool, NGX_INT_T_LEN);
    if (res->data == NULL) {
        return NGX_ERROR;
    }

    res->len = ngx_sprintf(res->data, "%i", random) - res->data;

    /* Set all required params */
    v->valid = 1;
    v->no_cacheable = 0;
    v->not_found = 0;

    return NGX_OK;
}
