#ifndef ECHO_FILTER_H
#define ECHO_FILTER_H

#include "ngx_http_echo_module.h"


extern ngx_http_output_header_filter_pt ngx_http_echo_next_header_filter;

extern ngx_http_output_body_filter_pt ngx_http_echo_next_body_filter;


ngx_int_t ngx_http_echo_filter_init (ngx_conf_t *cf);

#endif /* ECHO_FILTER_H */

