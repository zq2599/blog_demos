
/*
 * Copyright (C) agentzh
 */

#ifndef NGX_HTTP_RDS_H
#define NGX_HTTP_RDS_H


#include "resty_dbd_stream.h"
#include <nginx.h>
#include <ngx_core.h>
#include <ngx_http.h>


typedef struct {
    uint16_t        std_errcode;
    uint16_t        drv_errcode;
    ngx_str_t       errstr;

    uint64_t        affected_rows;
    uint64_t        insert_id;
    uint16_t        col_count;

} ngx_http_rds_header_t;


typedef struct ngx_http_rds_column_s {
    rds_col_type_t      std_type;
    uint16_t            drv_type;

    ngx_str_t           name;

} ngx_http_rds_column_t;





#endif /* NGX_HTTP_RDS_H */

