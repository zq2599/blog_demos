
/*
 * Copyright (C) agentzh
 */


#ifndef NGX_HTTP_RDS_CSV_FILTER_MODULE_H
#define NGX_HTTP_RDS_CSV_FILTER_MODULE_H


#include "ngx_http_rds.h"

#include <ngx_core.h>
#include <ngx_http.h>
#include <nginx.h>


#ifndef NGX_HTTP_RESET_CONTENT
#define NGX_HTTP_RESET_CONTENT 205
#endif


extern ngx_module_t  ngx_http_rds_csv_filter_module;

extern ngx_http_output_header_filter_pt  ngx_http_rds_csv_next_header_filter;
extern ngx_http_output_body_filter_pt    ngx_http_rds_csv_next_body_filter;


typedef struct {
    ngx_flag_t                       enabled;
    ngx_str_t                        row_term;
    ngx_uint_t                       field_sep;
    size_t                           buf_size;
    ngx_flag_t                       field_name_header;
    ngx_str_t                        content_type;
} ngx_http_rds_csv_loc_conf_t;


typedef struct {
    ngx_int_t           requires_filter;
} ngx_http_rds_csv_main_conf_t;


typedef enum {
    state_expect_header,
    state_expect_col,
    state_expect_row,
    state_expect_field,
    state_expect_more_field_data,
    state_done

} ngx_http_rds_csv_state_t;


typedef struct {
    ngx_http_rds_csv_state_t            state;

    ngx_str_t                           *col_name;
    ngx_uint_t                           col_count;
    ngx_uint_t                           cur_col;

    ngx_http_rds_column_t               *cols;
    size_t                               row;

    uint32_t                             field_offset;
    uint32_t                             field_total;

    ngx_buf_tag_t                        tag;

    ngx_chain_t                         *out;
    ngx_chain_t                        **last_out;
    ngx_chain_t                         *busy_bufs;
    ngx_chain_t                         *free_bufs;

    ngx_buf_t                           *out_buf;
    ngx_buf_t                            cached;
    ngx_buf_t                            postponed;

    size_t                               avail_out;

    uint32_t                             field_data_rest;

    ngx_flag_t                           header_sent:1;
    ngx_flag_t                           seen_stream_end:1;
    ngx_flag_t                           generated_col_names:1;
} ngx_http_rds_csv_ctx_t;


#endif /* NGX_HTTP_RDS_CSV_FILTER_MODULE_H */
