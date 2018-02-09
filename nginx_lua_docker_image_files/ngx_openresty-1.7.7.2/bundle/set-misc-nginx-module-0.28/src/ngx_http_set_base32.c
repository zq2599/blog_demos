#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include <ndk.h>

#include "ngx_http_set_base32.h"
#include "ngx_http_set_misc_module.h"


#define base32_encoded_length(len) ((((len)+4)/5)*8)
#define base32_decoded_length(len) ((((len)+7)/8)*5)


static void encode_base32(size_t slen, u_char *src, size_t *dlen, u_char *dst,
    ngx_flag_t padding, ngx_str_t *alphabet);
static int decode_base32(size_t slen, u_char *src, size_t *dlen, u_char *dst,
    u_char *basis32);


ngx_int_t
ngx_http_set_misc_encode_base32(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{
    size_t                   len;
    u_char                  *p;
    u_char                  *src, *dst;

    ngx_http_set_misc_loc_conf_t        *conf;

    conf = ngx_http_get_module_loc_conf(r, ngx_http_set_misc_module);

    len = base32_encoded_length(v->len);

    dd("estimated dst len: %d", (int) len);

    p = ngx_palloc(r->pool, len);
    if (p == NULL) {
        return NGX_ERROR;
    }

    src = v->data; dst = p;

    encode_base32(v->len, src, &len, dst, conf->base32_padding,
                  &conf->base32_alphabet);

    res->data = p;
    res->len = len;

    dd("res (len %d): %.*s", (int) res->len, (int) res->len, res->data);

    return NGX_OK;
}


ngx_int_t
ngx_http_set_misc_decode_base32(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{
    size_t                   len;
    u_char                  *p;
    u_char                  *src, *dst;
    int                      ret;

    ngx_http_set_misc_loc_conf_t        *conf;

    conf = ngx_http_get_module_loc_conf(r, ngx_http_set_misc_module);

    len = base32_decoded_length(v->len);

    dd("estimated dst len: %d", (int) len);

    p = ngx_palloc(r->pool, len);
    if (p == NULL) {
        return NGX_ERROR;
    }

    src = v->data; dst = p;

    ret = decode_base32(v->len, src, &len, dst, conf->basis32);

    if (ret == 0 /* OK */) {
        res->data = p;
        res->len = len;

        return NGX_OK;
    }

    /* failed to decode */

    res->data = NULL;
    res->len = 0;

    return NGX_OK;
}


/* See the implementation in src/core/ngx_string.c's
 * ngx_(encode|decode)_base64() for details. */

static void
encode_base32(size_t slen, u_char *src, size_t *dlen, u_char *dst,
    ngx_flag_t padding, ngx_str_t *alphabet)
{
    unsigned char *basis32 = alphabet->data;

    size_t len;
    u_char *s;
    u_char *d;

    len = slen;
    s = src;
    d = dst;

    while (len > 4) {
        *d++ = basis32[s[0] >> 3];
        *d++ = basis32[((s[0] & 0x07) << 2) | (s[1] >> 6)];
        *d++ = basis32[(s[1] >> 1) & 0x1f];
        *d++ = basis32[((s[1] & 1) << 4) | (s[2] >> 4)];
        *d++ = basis32[((s[2] & 0x0f) << 1) | (s[3] >> 7)];
        *d++ = basis32[(s[3] >> 2) & 0x1f];
        *d++ = basis32[((s[3] & 0x03) << 3) | (s[4] >> 5)];
        *d++ = basis32[s[4] & 0x1f];

        s += 5;
        len -= 5;
    }

    if (len) {
        *d++ = basis32[s[0] >> 3];

        if (len == 1) {
            /* 1 byte left */
            *d++ = basis32[(s[0] & 0x07) << 2];

            /* pad six '='s to the end */
            if (padding) {
                *d++ = '=';
                *d++ = '=';
                *d++ = '=';
                *d++ = '=';
                *d++ = '=';
            }

        } else {
            *d++ = basis32[((s[0] & 0x07) << 2) | (s[1] >> 6)];
            *d++ = basis32[(s[1] >> 1) & 0x1f];

            if (len == 2) {
                /* 2 bytes left */
                *d++ = basis32[(s[1] & 1) << 4];

                /* pad four '='s to the end */
                if (padding) {
                    *d++ = '=';
                    *d++ = '=';
                    *d++ = '=';
                }

            } else {
                *d++ = basis32[((s[1] & 1) << 4) | (s[2] >> 4)];

                if (len == 3) {
                    /* 3 bytes left */
                    *d++ = basis32[(s[2] & 0x0f) << 1];

                    if (padding) {
                        /* pad three '='s to the end */
                        *d++ = '=';
                        *d++ = '=';
                    }

                } else {
                    /* 4 bytes left */
                    *d++ = basis32[((s[2] & 0x0f) << 1) | (s[3] >> 7)];
                    *d++ = basis32[(s[3] >> 2) & 0x1f];
                    *d++ = basis32[(s[3] & 0x03) << 3];

                    /* pad one '=' to the end */
                }
            }
        }

        if (padding) {
            *d++ = '=';
        }
    }

    *dlen = (size_t) (d - dst);
}


static int
decode_base32(size_t slen, u_char *src, size_t *dlen, u_char *dst,
    u_char *basis32)
{
    size_t                   len, mod;
    u_char                  *s = src;
    u_char                  *d = dst;

    for (len = 0; len < slen; len++) {
        if (s[len] == '=') {
            break;
        }

        if (basis32[s[len]] == 77) {
            return -1;
        }
    }

    mod = len % 8;

    if (mod == 1 || mod == 3 || mod == 6) {
        /* bad Base32 digest length */
        return -1;
    }

    while (len > 7) {
        *d++ = (basis32[s[0]] << 3) | ((basis32[s[1]] >> 2) & 0x07);

        *d++ = ((basis32[s[1]] & 0x03) << 6) | (basis32[s[2]] << 1) |
            ((basis32[s[3]] >> 4) & 1);

        *d++ = ((basis32[s[3]] & 0x0f) << 4) | ((basis32[s[4]] >> 1) & 0x0f);

        *d++ = ((basis32[s[4]] & 1) << 7) | ((basis32[s[5]] & 0x1f) << 2) |
            ((basis32[s[6]] >> 3) & 0x03);
        *d++ = ((basis32[s[6]] & 0x07) << 5) | (basis32[s[7]] & 0x1f);

        s += 8;
        len -= 8;
    }

    if (len) {
        /* 2 bytes left */
        *d++ = (basis32[s[0]] << 3) | ((basis32[s[1]] >> 2) & 0x07);

        if (len > 2) {
            /* 4 bytes left */
            *d++ = ((basis32[s[1]] & 0x03) << 6) | ((basis32[s[2]] & 0x1f) << 1)
                | ((basis32[s[3]] >> 4) & 1);

            if (len > 4) {
                /* 5 bytes left */
                *d++ = ((basis32[s[3]] & 0x0f) << 4) |
                    ((basis32[s[4]] >> 1) & 0x0f);

                if (len > 5) {
                    /* 7 bytes left */
                    *d++ = ((basis32[s[4]] & 1) << 7) |
                        ((basis32[s[5]] & 0x1f) << 2) |
                        ((basis32[s[6]] >> 3) & 0x03);
                }
            }
        }
    }

    *dlen = (size_t) (d - dst);

    return 0;
}
