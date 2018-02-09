#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_set_hash.h"

#if NGX_HAVE_SHA1
#include "ngx_sha1.h"

#ifndef SHA_DIGEST_LENGTH
#define SHA_DIGEST_LENGTH 20
#endif

#endif

#include "ngx_md5.h"


#ifndef MD5_DIGEST_LENGTH
#define MD5_DIGEST_LENGTH 16
#endif

enum {
#if NGX_HAVE_SHA1
    SHA_HEX_LENGTH = SHA_DIGEST_LENGTH * 2,
#endif
    MD5_HEX_LENGTH = MD5_DIGEST_LENGTH * 2
};


#if NGX_HAVE_SHA1
ngx_int_t
ngx_http_set_misc_set_sha1(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{
    u_char                  *p;
    ngx_sha1_t               sha;
    u_char                   sha_buf[SHA_DIGEST_LENGTH];

    p = ngx_palloc(r->pool, SHA_HEX_LENGTH);
    if (p == NULL) {
        return NGX_ERROR;
    }

    ngx_sha1_init(&sha);
    ngx_sha1_update(&sha, v->data, v->len);
    ngx_sha1_final(sha_buf, &sha);

    ngx_hex_dump(p, sha_buf, sizeof(sha_buf));

    res->data = p;
    res->len = SHA_HEX_LENGTH;

    return NGX_OK;
}
#endif


ngx_int_t
ngx_http_set_misc_set_md5(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{
    u_char                  *p;
    ngx_md5_t                md5;
    u_char                   md5_buf[MD5_DIGEST_LENGTH];

    p = ngx_palloc(r->pool, MD5_HEX_LENGTH);
    if (p == NULL) {
        return NGX_ERROR;
    }

    ngx_md5_init(&md5);
    ngx_md5_update(&md5, v->data, v->len);
    ngx_md5_final(md5_buf, &md5);

    ngx_hex_dump(p, md5_buf, sizeof(md5_buf));

    res->data = p;
    res->len = MD5_HEX_LENGTH;

    return NGX_OK;
}
