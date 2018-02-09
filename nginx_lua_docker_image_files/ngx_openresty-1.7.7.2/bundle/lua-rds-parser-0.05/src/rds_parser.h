
/*
 * Copyright (C) agentzh
 */

#ifndef RDS_PARSER_H
#define RDS_PARSER_H


#include "resty_dbd_stream.h"
#include "sys/types.h"
#include <stdint.h>


typedef struct {
    u_char      *data;
    size_t       len;
} rds_str_t;


typedef struct {
    u_char      *start;
    u_char      *pos;
    u_char      *last;
    u_char      *end;
} rds_buf_t;


typedef struct {
    uint16_t        std_errcode;
    uint16_t        drv_errcode;
    rds_str_t       errstr;

    uint64_t        affected_rows;
    uint64_t        insert_id;
    uint16_t        col_count;

} rds_header_t;


typedef struct rds_column_s {
    rds_col_type_t      std_type;
    uint16_t            drv_type;

    rds_str_t           name;

} rds_column_t;


#endif /* RDS_PARSER_H */

