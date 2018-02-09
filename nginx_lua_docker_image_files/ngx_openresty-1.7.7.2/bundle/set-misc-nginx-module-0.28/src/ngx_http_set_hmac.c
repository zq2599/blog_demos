#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include <ndk.h>

#include "ngx_http_set_hmac.h"
#include <openssl/evp.h>
#include <openssl/hmac.h>


/* this function's implementation is partly borrowed from
 * https://github.com/anomalizer/ngx_aws_auth */
ngx_int_t
ngx_http_set_misc_set_hmac_sha1(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{
    ngx_http_variable_value_t   *secret, *string_to_sign;
    unsigned int                 md_len;
    unsigned char                md[EVP_MAX_MD_SIZE];
    const EVP_MD                *evp_md;

    evp_md = EVP_sha1();

    secret = v;
    string_to_sign = v + 1;

    dd("secret=%.*s, string_to_sign=%.*s", (int) secret->len, secret->data,
            (int) string_to_sign->len, string_to_sign->data);

    HMAC(evp_md, secret->data, secret->len, string_to_sign->data,
         string_to_sign->len, md, &md_len);

    res->len = md_len;
    ndk_palloc_re(res->data, r->pool, md_len);

    ngx_memcpy(res->data,
               &md,
               md_len);

    return NGX_OK;
}

