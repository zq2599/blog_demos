#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include <ndk.h>
#include "ngx_http_set_secure_random.h"
#include <stdlib.h>


enum {
    MAX_RANDOM_STRING = 64,
    ALPHANUM = 1,
    LCALPHA  = 2
};


static ngx_int_t
ngx_http_set_misc_set_secure_random_common(int alphabet_type,
    ngx_http_request_t *r, ngx_str_t *res, ngx_http_variable_value_t *v);


ngx_int_t
ngx_http_set_misc_set_secure_random_alphanum(ngx_http_request_t *r,
    ngx_str_t *res, ngx_http_variable_value_t *v)
{
    return ngx_http_set_misc_set_secure_random_common(ALPHANUM, r, res, v);
}


ngx_int_t
ngx_http_set_misc_set_secure_random_lcalpha(ngx_http_request_t *r,
    ngx_str_t *res, ngx_http_variable_value_t *v)
{
    return ngx_http_set_misc_set_secure_random_common(LCALPHA, r, res, v);
}


static ngx_int_t
ngx_http_set_misc_set_secure_random_common(int alphabet_type,
    ngx_http_request_t *r, ngx_str_t *res, ngx_http_variable_value_t *v)
{
    static u_char  alphabet[] = "abcdefghijklmnopqrstuvwxyz"
                                "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    u_char         entropy[MAX_RANDOM_STRING];
    u_char         output[MAX_RANDOM_STRING];
    ngx_int_t      length, i;
    ngx_fd_t       fd;
    ssize_t        n;

    length = ngx_atoi(v->data, v->len);

    if (length == NGX_ERROR || length < 1 || length > MAX_RANDOM_STRING) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "set_random: bad \"length\" argument: %v", v);
        return NGX_ERROR;
    }

    fd = ngx_open_file((u_char *) "/dev/urandom", NGX_FILE_RDONLY,
                       NGX_FILE_OPEN, 0);
    if (fd == NGX_INVALID_FILE) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "set_secure_random: could not open /dev/urandom");
        return NGX_ERROR;
    }

    n = ngx_read_fd(fd, entropy, length);
    if (n != length) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "set_secure_random: could not read all %i byte(s) from "
                      "/dev/urandom", length);
        return NGX_ERROR;
    }

    ngx_close_file(fd);

    for (i = 0; i < length; i++) {
        if (alphabet_type == LCALPHA) {
            output[i] = entropy[i] % 26 + 'a';

        } else {
            output[i] = alphabet[ entropy[i] % (sizeof alphabet - 1) ];
        }
    }

    res->data = ngx_palloc(r->pool, length);
    if (res->data == NULL) {
        return NGX_ERROR;
    }

    ngx_memcpy(res->data, output, length);

    res->len = length;

    /* set all required params */
    v->valid = 1;
    v->no_cacheable = 0;
    v->not_found = 0;

    return NGX_OK;
}
