#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_echo_var.h"
#include "ngx_http_echo_timer.h"
#include "ngx_http_echo_request_info.h"
#include "ngx_http_echo_foreach.h"


static ngx_int_t ngx_http_echo_incr_variable(ngx_http_request_t *r,
        ngx_http_variable_value_t *v, uintptr_t data);


static ngx_http_variable_t ngx_http_echo_variables[] = {

    { ngx_string("echo_timer_elapsed"), NULL,
      ngx_http_echo_timer_elapsed_variable, 0,
      NGX_HTTP_VAR_NOCACHEABLE, 0 },

    { ngx_string("echo_request_method"), NULL,
      ngx_http_echo_request_method_variable, 0,
      NGX_HTTP_VAR_NOCACHEABLE, 0 },

    { ngx_string("echo_cacheable_request_uri"), NULL,
      ngx_http_echo_cacheable_request_uri_variable, 0,
      0, 0 },

    { ngx_string("echo_request_uri"), NULL,
      ngx_http_echo_request_uri_variable, 0,
      0, 0 },

    { ngx_string("echo_client_request_method"), NULL,
      ngx_http_echo_client_request_method_variable, 0,
      NGX_HTTP_VAR_NOCACHEABLE, 0 },

    { ngx_string("echo_request_body"), NULL,
      ngx_http_echo_request_body_variable, 0,
      NGX_HTTP_VAR_NOCACHEABLE, 0 },

    { ngx_string("echo_client_request_headers"), NULL,
      ngx_http_echo_client_request_headers_variable, 0,
      NGX_HTTP_VAR_NOCACHEABLE, 0 },

    { ngx_string("echo_it"), NULL,
      ngx_http_echo_it_variable, 0,
      NGX_HTTP_VAR_NOCACHEABLE, 0 },

    { ngx_string("echo_incr"), NULL,
      ngx_http_echo_incr_variable, 0,
      NGX_HTTP_VAR_NOCACHEABLE, 0 },

    { ngx_string("echo_response_status"), NULL,
      ngx_http_echo_response_status_variable, 0,
      NGX_HTTP_VAR_NOCACHEABLE, 0 },

    { ngx_null_string, NULL, NULL, 0, 0, 0 }
};


ngx_int_t
ngx_http_echo_add_variables(ngx_conf_t *cf)
{
    ngx_http_variable_t *var, *v;

    for (v = ngx_http_echo_variables; v->name.len; v++) {
        var = ngx_http_add_variable(cf, &v->name, v->flags);
        if (var == NULL) {
            return NGX_ERROR;
        }

        var->get_handler = v->get_handler;
        var->data = v->data;
    }

    return NGX_OK;
}


static ngx_int_t
ngx_http_echo_incr_variable(ngx_http_request_t *r,
    ngx_http_variable_value_t *v, uintptr_t data)
{
    ngx_http_echo_ctx_t         *ctx;
    u_char                      *p;

    ctx = ngx_http_get_module_ctx(r->main, ngx_http_echo_module);

    if (ctx == NULL) {
        return NGX_ERROR;
    }

    ctx->counter++;

    p = ngx_palloc(r->pool, NGX_INT_T_LEN);
    if (p == NULL) {
        return NGX_ERROR;
    }

    v->len = ngx_sprintf(p, "%ui", ctx->counter) - p;
    v->data = p;

    v->valid = 1;
    v->not_found = 0;
    v->no_cacheable = 1;

    return NGX_OK;
}

