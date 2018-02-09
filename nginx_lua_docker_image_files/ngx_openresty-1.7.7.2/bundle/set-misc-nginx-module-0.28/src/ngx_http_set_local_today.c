#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include <ndk.h>


#ifndef NGX_HTTP_SET_MISC_FMT_DATE_LEN
#define NGX_HTTP_SET_MISC_FMT_DATE_LEN       256
#endif


ngx_int_t
ngx_http_set_local_today(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{
    time_t           now;
    ngx_tm_t         tm;
    u_char          *p;

    /*t = ngx_timeofday();*/

    now = ngx_time();

    ngx_gmtime(now + ngx_cached_time->gmtoff * 60, &tm);

    dd("tm.ngx_tm_hour:%d", tm.ngx_tm_hour);

    p = ngx_palloc(r->pool, sizeof("yyyy-mm-dd") - 1);
    if (p == NULL) {
        return NGX_ERROR;
    }

    ngx_sprintf(p, "%04d-%02d-%02d", tm.ngx_tm_year, tm.ngx_tm_mon,
            tm.ngx_tm_mday);

    res->data = p;
    res->len = sizeof("yyyy-mm-dd") - 1;

    return NGX_OK;
}


ngx_int_t
ngx_http_set_formatted_gmt_time(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{
    time_t           now;
    u_char          *p;
    struct tm        tm;

    if (v->not_found || v->len == 0) {
        res->data = NULL;
        res->len = 0;
        return NGX_OK;
    }

    now = ngx_time();
    ngx_libc_gmtime(now, &tm);

    p = ngx_palloc(r->pool, NGX_HTTP_SET_MISC_FMT_DATE_LEN);
    if (p == NULL) {
        return NGX_ERROR;
    }

    res->len = strftime((char *) p, NGX_HTTP_SET_MISC_FMT_DATE_LEN,
                        (char *) v->data, &tm);
    if (res->len == 0) {
        return NGX_ERROR;
    }

    res->data = p;

    return NGX_OK;
}


ngx_int_t
ngx_http_set_formatted_local_time(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{
    time_t           now;
    u_char          *p;
    struct tm        tm;

    if (v->not_found || v->len == 0) {
        res->data = NULL;
        res->len = 0;
        return NGX_OK;
    }

    now = ngx_time();
    ngx_libc_localtime(now, &tm);

    p = ngx_palloc(r->pool, NGX_HTTP_SET_MISC_FMT_DATE_LEN);
    if (p == NULL) {
        return NGX_ERROR;
    }

    res->len = strftime((char *) p, NGX_HTTP_SET_MISC_FMT_DATE_LEN,
                        (char *) v->data, &tm);
    if (res->len == 0) {
        return NGX_ERROR;
    }

    res->data = p;

    return NGX_OK;
}
