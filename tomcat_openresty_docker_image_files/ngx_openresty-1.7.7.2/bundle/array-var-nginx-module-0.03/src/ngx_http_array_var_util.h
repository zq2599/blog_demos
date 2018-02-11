#ifndef NGX_HTTP_ARRAY_VAR_UTIL_C
#define NGX_HTTP_ARRAY_VAR_UTIL_C

#include <ndk.h>
#include <ngx_core.h>
#include <ngx_http.h>


ngx_int_t ngx_http_array_var_add_variable(ngx_conf_t *cf, ngx_str_t *name);

u_char * ngx_http_array_var_strlstrn(u_char *s1, u_char *last, u_char *s2,
        size_t n);

ndk_set_var_value_pt ngx_http_array_var_get_func_from_cmd(u_char *name,
        size_t name_len);


#ifndef ngx_str3cmp

#  define ngx_str3cmp(m, c0, c1, c2)                                       \
    m[0] == c0 && m[1] == c1 && m[2] == c2

#endif /* ngx_str3cmp */

#endif /* NGX_HTTP_ARRAY_VAR_UTIL_C */

