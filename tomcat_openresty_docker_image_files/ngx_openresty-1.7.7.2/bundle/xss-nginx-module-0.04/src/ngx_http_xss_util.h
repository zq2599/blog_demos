#ifndef NGX_HTTP_XSS_UTIL_H
#define NGX_HTTP_XSS_UTIL_H

#include <ngx_core.h>
#include <ngx_http.h>


#ifndef ngx_copy_const_str
#define ngx_copy_const_str(p, s)  ngx_copy(p, s, sizeof(s) - 1)
#endif

#ifndef NGX_UNESCAPE_URI_COMPONENT
#define NGX_UNESCAPE_URI_COMPONENT 0
#endif


ngx_int_t ngx_http_xss_test_callback(u_char *data, size_t len);


#endif /* NGX_HTTP_XSS_UTIL_H */

