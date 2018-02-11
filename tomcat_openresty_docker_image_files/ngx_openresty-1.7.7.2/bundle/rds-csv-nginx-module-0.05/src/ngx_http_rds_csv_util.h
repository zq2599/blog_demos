
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef NGX_HTTP_RDS_CSV_UTIL_H
#define NGX_HTTP_RDS_CSV_UTIL_H


#include <ngx_core.h>
#include <ngx_http.h>


#ifndef NGX_UINT64_LEN
#define NGX_UINT64_LEN (sizeof("18446744073709551615") - 1)
#endif

#ifndef NGX_UINT16_LEN
#define NGX_UINT16_LEN (sizeof("65535") - 1)
#endif

#ifndef ngx_copy_literal
#define ngx_copy_literal(p, s)  ngx_copy(p, s, sizeof(s) - 1)
#endif


uintptr_t ngx_http_rds_csv_escape_csv_str(u_char field_sep, u_char *dst,
    u_char *src, size_t size, unsigned *need_quotes);
ngx_int_t ngx_http_rds_csv_test_content_type(ngx_http_request_t *r);
void ngx_http_rds_csv_discard_bufs(ngx_pool_t *pool, ngx_chain_t *in);


#endif /* NGX_HTTP_RDS_CSV_UTIL_H */
