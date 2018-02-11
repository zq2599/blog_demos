
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include <ngx_core.h>
#include <ngx_http.h>
#include "ngx_http_drizzle_quoting.h"


ngx_int_t
ngx_http_drizzle_check_int(ngx_str_t *value, void *data)
{
    /* TODO */
    return NGX_OK;
}


ngx_int_t
ngx_http_drizzle_check_float(ngx_str_t *value, void *data)
{
    /* TODO */
    return NGX_OK;
}


ngx_int_t
ngx_http_drizzle_check_bool(ngx_str_t *value, void *data)
{
    /* TODO */
    return NGX_OK;
}


ngx_int_t
ngx_http_drizzle_check_col(ngx_str_t *value, void *data)
{
    /* TODO */
    return NGX_OK;
}


ngx_int_t
ngx_http_drizzle_check_table(ngx_str_t *value, void *data)
{
    /* TODO */
    return NGX_OK;
}


ngx_int_t
ngx_http_drizzle_check_keyword(ngx_str_t *value, void *data)
{
    /* TODO */
    return NGX_OK;
}
