#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_rds_json_handler.h"
#include "ngx_http_rds_json_util.h"


ngx_int_t
ngx_http_rds_json_ret_handler(ngx_http_request_t *r)
{
    ngx_chain_t                     *cl;
    ngx_buf_t                       *b;
    size_t                           len;
    ngx_str_t                        errstr;
    ngx_int_t                        rc;
    uintptr_t                        escape = 0;
    ngx_str_t                       *values = NULL;
    uintptr_t                       *escapes = NULL;
    ngx_uint_t                       i;
    ngx_http_rds_json_property_t    *prop = NULL;
    ngx_http_rds_json_loc_conf_t    *conf;

    dd("entered ret handler");

    conf = ngx_http_get_module_loc_conf(r,
            ngx_http_rds_json_filter_module);

    /* evaluate the final value of conf->errstr */

    if (ngx_http_complex_value(r, conf->errstr, &errstr) != NGX_OK) {
        return NGX_ERROR;
    }

    /* calculate the buffer size */

    len = sizeof("{") - 1
        + conf->errcode_key.len
        + sizeof(":") - 1
        + conf->errcode.len
        + sizeof("}") - 1
        ;

    if (errstr.len) {
        escape = ngx_http_rds_json_escape_json_str(NULL,
                errstr.data, errstr.len);

        len += sizeof(",") - 1
             + conf->errstr_key.len
             + sizeof(":") - 1
             + sizeof("\"") - 1
             + errstr.len
             + escape
             + sizeof("\"") - 1
             ;
    }

    if (conf->success.len) {
        len += conf->success.len + sizeof(":,") - 1;
        if (ngx_atoi(conf->errcode.data, conf->errcode.len) == 0) {
            len += sizeof("true") - 1;

        } else {
            len += sizeof("false") - 1;
        }
    }

    if (conf->user_props) {
        values = ngx_pnalloc(r->pool,
                    conf->user_props->nelts * (sizeof(ngx_str_t) +
                    sizeof(uintptr_t)));

        if (values == NULL) {
            return NGX_ERROR;
        }

        escapes = (uintptr_t *) ((u_char *) values +
                  conf->user_props->nelts * sizeof(ngx_str_t));

        prop = conf->user_props->elts;
        for (i = 0; i < conf->user_props->nelts; i++) {
            if (ngx_http_complex_value(r, &prop[i].value, &values[i])
                != NGX_OK)
            {
                return NGX_ERROR;
            }

            escapes[i] = ngx_http_rds_json_escape_json_str(NULL, values[i].data,
                values[i].len);

            len += sizeof(":\"\",") - 1 + prop[i].key.len + values[i].len
                  + escapes[i];
        }
    }


    /* create the buffer */

    cl = ngx_alloc_chain_link(r->pool);
    if (cl == NULL) {
        return NGX_ERROR;
    }

    b = ngx_create_temp_buf(r->pool, len);
    if (b == NULL) {
        return NGX_ERROR;
    }

    if (r == r->main) {
        b->last_buf = 1;
    }

    cl->buf = b;
    cl->next = NULL;

    /* copy data over to the buffer */

    *b->last++ = '{';

    b->last = ngx_copy(b->last, conf->errcode_key.data, conf->errcode_key.len);
    *b->last++ = ':';
    b->last = ngx_copy(b->last, conf->errcode.data, conf->errcode.len);

    if (errstr.len) {
        *b->last++ = ',';
        b->last = ngx_copy(b->last,
                conf->errstr_key.data, conf->errstr_key.len);
        *b->last++ = ':';
        *b->last++ = '"';

        if (escape == 0) {
            b->last = ngx_copy(b->last, errstr.data, errstr.len);
        } else {
            b->last = (u_char *) ngx_http_rds_json_escape_json_str(b->last,
                    errstr.data, errstr.len);
        }

        *b->last++ = '"';
    }

    if (conf->success.len) {
        *b->last++ = ',';
        b->last = ngx_copy(b->last, conf->success.data, conf->success.len);

        if (ngx_atoi(conf->errcode.data, conf->errcode.len) == 0) {
            b->last = ngx_copy_literal(b->last, ":true");

        } else {
            b->last = ngx_copy_literal(b->last, ":false");
        }
    }

    if (conf->user_props) {
        for (i = 0; i < conf->user_props->nelts; i++) {
            *b->last++ = ',';
            b->last = ngx_copy(b->last, prop[i].key.data, prop[i].key.len);
            *b->last++ = ':';
            *b->last++ = '"';

            if (escapes[i] == 0) {
                b->last = ngx_copy(b->last, values[i].data, values[i].len);

            } else {
                b->last = (u_char *) ngx_http_rds_json_escape_json_str(b->last,
                    values[i].data, values[i].len);
            }

            *b->last++ = '"';
        }
    }

    *b->last++ = '}';

    if (b->last != b->end) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                "rds_json: rds_json_ret: buffer error");

        return NGX_ERROR;
    }

    /* send headers */

    r->headers_out.status = NGX_HTTP_OK;
    r->headers_out.content_type = conf->content_type;
    r->headers_out.content_type_len = conf->content_type.len;

    rc = ngx_http_send_header(r);
    if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
        return rc;
    }

    dd("output filter...");

    return ngx_http_output_filter(r, cl);
}

