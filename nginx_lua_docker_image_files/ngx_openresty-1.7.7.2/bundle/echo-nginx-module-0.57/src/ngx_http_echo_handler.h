#ifndef ECHO_HANDLER_H
#define ECHO_HANDLER_H

#include "ngx_http_echo_module.h"


void ngx_http_echo_wev_handler(ngx_http_request_t *r);

ngx_int_t ngx_http_echo_handler(ngx_http_request_t *r);

ngx_int_t ngx_http_echo_run_cmds(ngx_http_request_t *r);

ngx_int_t ngx_http_echo_post_subrequest(ngx_http_request_t *r,
        void *data, ngx_int_t rc);


#endif /* ECHO_HANDLER_H */

