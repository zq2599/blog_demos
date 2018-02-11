#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include    <ndk.h>
#include "ngx_http_set_base64.h"


ngx_int_t
ngx_http_set_misc_set_decode_base64(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{

    ngx_str_t        src;

    src.len = v->len;
    src.data = v->data;

    res->len = ngx_base64_decoded_length(v->len);
    ndk_palloc_re(res->data, r->pool, res->len);

    if (ngx_decode_base64(res, &src) != NGX_OK) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "set_decode_base64: invalid value");
        return NGX_ERROR;
    }

    return NGX_OK;
}


ngx_int_t
ngx_http_set_misc_set_encode_base64(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{

    ngx_str_t        src;

    src.len = v->len;
    src.data = v->data;

    res->len = ngx_base64_encoded_length(v->len);
    ndk_palloc_re(res->data, r->pool, res->len);

    ngx_encode_base64(res, &src);

    return NGX_OK;
}

