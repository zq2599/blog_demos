#ifndef RESTY_DBD_STREAME_H
#define RESTY_DBD_STREAME_H

#define resty_dbd_stream_version 3
#define resty_dbd_stream_version_string \
    "0.0.3"

#define rds_content_type \
    "application/x-resty-dbd-stream"

#define rds_content_type_len \
    (sizeof(rds_content_type) - 1)


typedef enum {
    rds_rough_col_type_int = 0 << 14,
    rds_rough_col_type_float = 1 << 14,
    rds_rough_col_type_str = 2 << 14,
    rds_rough_col_type_bool = 3 << 14

} rds_rough_col_type_t;


/* The following types (or spellings thereof) are specified
 * by SQL:
 * bigint, bit, bit varying, boolean, char, character varying,
 * character, varchar, date, double precision, integer,
 * interval, numeric, decimal, real, smallint,
 * time (with or without time zone),
 * timestamp (with or without time zone), xml */

typedef enum {
    rds_col_type_unknown = 0 | rds_rough_col_type_str,
    rds_col_type_bigint = 1 | rds_rough_col_type_int,
    rds_col_type_bit = 2 | rds_rough_col_type_str,
    rds_col_type_bit_varying = 3 | rds_rough_col_type_str,

    rds_col_type_bool = 4 | rds_rough_col_type_bool,
    rds_col_type_char = 5 | rds_rough_col_type_str,
    rds_col_type_varchar = 6 | rds_rough_col_type_str,
    rds_col_type_date = 7 | rds_rough_col_type_str,
    rds_col_type_double = 8 | rds_rough_col_type_float,
    rds_col_type_integer = 9 | rds_rough_col_type_int,
    rds_col_type_interval = 10 | rds_rough_col_type_float,
    rds_col_type_decimal = 11 | rds_rough_col_type_float,
    rds_col_type_real = 12 | rds_rough_col_type_float,
    rds_col_type_smallint = 13 | rds_rough_col_type_int,
    rds_col_type_time_with_time_zone = 14 | rds_rough_col_type_str,
    rds_col_type_time = 15 | rds_rough_col_type_str,
    rds_col_type_timestamp_with_time_zone = 16 | rds_rough_col_type_str,
    rds_col_type_timestamp = 17 | rds_rough_col_type_str,
    rds_col_type_xml = 18 | rds_rough_col_type_str,

    /* our additions */
    rds_col_type_blob = 19 | rds_rough_col_type_str

} rds_col_type_t;

#endif /* RESTY_DBD_STREAME_H */

