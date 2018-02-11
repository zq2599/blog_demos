#ifndef NGX_HTTP_DRIZZLE_QUOTING_H
#define NGX_HTTP_DRIZZLE_QUOTING_H

#include "ngx_http_drizzle_module.h"

typedef ngx_int_t (* ngx_http_drizzle_type_checker_pt) (ngx_str_t *value,
        void *data);

typedef enum {
    quotes_type_none,
    quotes_type_single,
    quotes_type_double,
    quotes_type_back

} ngx_http_drizzle_quotes_type_t;


typedef struct {
    ngx_str_t                               name;
    ngx_http_drizzle_type_checker_pt        checker;
    void                                   *checker_data;
    ngx_http_drizzle_quotes_type_t          quotes;

} ngx_http_drizzle_var_type_t;


typedef struct {
    ngx_int_t                            src_var;
    ngx_int_t                            dest_var;
    ngx_http_drizzle_var_type_t         *type;
    ngx_str_t                            errstr;

} ngx_http_drizzle_var_to_quote_t;

#endif /* NGX_HTTP_DRIZZLE_QUOTING_H */

