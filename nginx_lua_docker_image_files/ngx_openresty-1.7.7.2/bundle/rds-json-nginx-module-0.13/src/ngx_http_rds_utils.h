
/*
 * Copyright (C) agentzh
 */


#ifndef NGX_HTTP_RDS_UTILS_H
#define NGX_HTTP_RDS_UTILS_H


static ngx_inline ngx_int_t
ngx_http_rds_parse_header(ngx_http_request_t *r, ngx_buf_t *b,
    ngx_http_rds_header_t *header)
{
    ssize_t          rest;

    rest = sizeof(uint8_t)        /* endian type */
           + sizeof(uint32_t)     /* format version */
           + sizeof(uint8_t)      /* result type */

           + sizeof(uint16_t)     /* standard error code */
           + sizeof(uint16_t)     /* driver-specific error code */

           + sizeof(uint16_t)     /* driver-specific errstr len */
           + 0                    /* driver-specific errstr data */
           + sizeof(uint64_t)     /* affected rows */
           + sizeof(uint64_t)     /* insert id */
           + sizeof(uint16_t)     /* column count */
           ;

    if (b->last - b->pos < rest) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds: header is incomplete in the buf");
        return NGX_ERROR;
    }

    /* check endian type */

    if (*(uint8_t *) b->pos !=
#if (NGX_HAVE_LITTLE_ENDIAN)
            0
#else /* big endian */
            1
#endif
       )
    {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds: endian type in the header differ");
        return NGX_ERROR;
    }

    b->pos += sizeof(uint8_t);

    /* check RDS format version number */

    if (*(uint32_t *) b->pos != (uint32_t) resty_dbd_stream_version) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds: RDS format version differ");
        return NGX_ERROR;
    }

    dd("RDS format version: %d", (int) *(uint32_t *) b->pos);

    b->pos += sizeof(uint32_t);

    /* check RDS result type */

    if (*b->pos != 0) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds: RDS result type must be 0 for now");
        return NGX_ERROR;
    }

    b->pos++;

    /* save the standard error code */

    header->std_errcode = *(uint16_t *) b->pos;

    b->pos += sizeof(uint16_t);

    /* save the driver-specific error code */

    header->drv_errcode = *(uint16_t *) b->pos;

    b->pos += sizeof(uint16_t);

    /* save the error string length */

    header->errstr.len = *(uint16_t *) b->pos;

    b->pos += sizeof(uint16_t);

    dd("errstr len: %d", (int) header->errstr.len);

    /* check the rest data's size */

    rest = header->errstr.len
           + sizeof(uint64_t)     /* affected rows */
           + sizeof(uint64_t)     /* insert id */
           + sizeof(uint16_t)     /* column count */
           ;

    if (b->last - b->pos < rest) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds: header is incomplete in the buf");
        return NGX_ERROR;
    }

    /* save the error string data */

    header->errstr.data = b->pos;

    b->pos += header->errstr.len;

    /* save affected rows */

    header->affected_rows = *(uint64_t *) b->pos;

    b->pos += sizeof(uint64_t);

    /* save insert id */

    header->insert_id = *(uint64_t *)b->pos;

    b->pos += sizeof(uint64_t);

    /* save column count */

    header->col_count = *(uint16_t *) b->pos;

    b->pos += sizeof(uint16_t);

    dd("saved column count: %d", (int) header->col_count);

    return NGX_OK;
}


static ngx_inline ngx_int_t
ngx_http_rds_parse_col(ngx_http_request_t *r, ngx_buf_t *b,
    ngx_http_rds_column_t *col)
{
    ssize_t         rest;

    rest = sizeof(uint16_t)         /* std col type */
           + sizeof(uint16_t)       /* driver col type */
           + sizeof(uint16_t)       /* col name str len */
           ;

    if (b->last - b->pos < rest) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds: column spec is incomplete in the buf");
        return NGX_ERROR;
    }

    /* save standard column type */
    col->std_type = *(uint16_t *) b->pos;
    b->pos += sizeof(uint16_t);

    /* save driver-specific column type */
    col->drv_type = *(uint16_t *) b->pos;
    b->pos += sizeof(uint16_t);

    /* read column name string length */

    col->name.len = *(uint16_t *) b->pos;
    b->pos += sizeof(uint16_t);

    if (col->name.len == 0) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds_json: column name empty");
        return NGX_ERROR;
    }

    rest = col->name.len;

    if (b->last - b->pos < rest) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                      "rds: column name string is incomplete in the buf");
        return NGX_ERROR;
    }

    /* save the column name string data */

    col->name.data = ngx_palloc(r->pool, col->name.len);
    if (col->name.data == NULL) {
        return NGX_ERROR;
    }

    ngx_memcpy(col->name.data, b->pos, col->name.len);
    b->pos += col->name.len;

    dd("saved column name \"%.*s\" (len %d, offset %d)",
       (int) col->name.len, col->name.data,
       (int) col->name.len, (int) (b->pos - b->start));

    return NGX_OK;
}


#endif /* NGX_HTTP_RDS_UTILS_H */
