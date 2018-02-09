
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef NGX_HTTP_MEMC_UTIL_H
#define NGX_HTTP_MEMC_UTIL_H

#include <ngx_core.h>
#include <ngx_http.h>
#include "ngx_http_memc_module.h"

#ifndef NGX_UINT32_LEN
#define NGX_UINT32_LEN (NGX_INT32_LEN - 1)
#endif

#ifndef NGX_UINT64_LEN
#define NGX_UINT64_LEN (NGX_INT64_LEN - 1)
#endif

#define ngx_http_memc_strcmp_const(a, b) \
    ngx_strncmp(a, b, sizeof(b) - 1)

ngx_http_memc_cmd_t ngx_http_memc_parse_cmd(u_char *data, size_t len,
        ngx_flag_t *is_storage_cmd);

ngx_http_upstream_srv_conf_t * ngx_http_memc_upstream_add(
        ngx_http_request_t *r, ngx_url_t *url);


#endif /* NGX_HTTP_MEMC_UTIL_H */

