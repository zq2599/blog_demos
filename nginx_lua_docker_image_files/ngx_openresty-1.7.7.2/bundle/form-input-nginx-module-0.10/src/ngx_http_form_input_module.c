#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include <ndk.h>
#include <nginx.h>
#include <ngx_config.h>
#include <ngx_core.h>
#include <ngx_http.h>


#define form_urlencoded_type "application/x-www-form-urlencoded"
#define form_urlencoded_type_len (sizeof(form_urlencoded_type) - 1)


ngx_flag_t ngx_http_form_input_used = 0;

#if 0

typedef struct {
    ngx_flag_t    enabled;
} ngx_http_form_input_loc_conf_t;

#endif

typedef struct {
    unsigned          done:1;
    unsigned          waiting_more_body:1;
} ngx_http_form_input_ctx_t;


static ngx_int_t ngx_http_set_form_input(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v);
static char *ngx_http_set_form_input_conf_handler(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);


#if 0

static void *ngx_http_form_input_create_loc_conf(ngx_conf_t *cf);
static char *ngx_http_form_input_merge_loc_conf(ngx_conf_t *cf, void *parent,
    void *child);

#endif

static ngx_int_t ngx_http_form_input_init(ngx_conf_t *cf);
static ngx_int_t ngx_http_form_input_handler(ngx_http_request_t *r);
static void ngx_http_form_input_post_read(ngx_http_request_t *r);
static ngx_int_t ngx_http_form_input_arg(ngx_http_request_t *r, u_char *name,
    size_t len, ngx_str_t *value, ngx_flag_t multi);


static ngx_command_t ngx_http_form_input_commands[] = {

    { ngx_string("set_form_input"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_TAKE12,
      ngx_http_set_form_input_conf_handler,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("set_form_input_multi"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_TAKE12,
      ngx_http_set_form_input_conf_handler,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

      ngx_null_command
};


static ngx_http_module_t ngx_http_form_input_module_ctx = {
    NULL,                                   /* preconfiguration */
    ngx_http_form_input_init,               /* postconfiguration */

    NULL,                                   /* create main configuration */
    NULL,                                   /* init main configuration */

    NULL,                                   /* create server configuration */
    NULL,                                   /* merge server configuration */

    NULL,                                   /* create location configuration */
    NULL                                   /* merge location configuration */
};


ngx_module_t ngx_http_form_input_module = {
    NGX_MODULE_V1,
    &ngx_http_form_input_module_ctx,        /* module context */
    ngx_http_form_input_commands,           /* module directives */
    NGX_HTTP_MODULE,                        /* module type */
    NULL,                                   /* init master */
    NULL,                                   /* init module */
    NULL,                                   /* init process */
    NULL,                                   /* init thread */
    NULL,                                   /* exit thread */
    NULL,                                   /* exit precess */
    NULL,                                   /* exit master */
    NGX_MODULE_V1_PADDING
};


static ngx_int_t
ngx_http_set_form_input(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{
    ngx_http_form_input_ctx_t           *ctx;
    ngx_int_t                            rc;

    dd_enter();

    dd("set default return value");
    ngx_str_set(res, "");

    if (r->done) {
        dd("request done");
        return NGX_OK;
    }

    ctx = ngx_http_get_module_ctx(r, ngx_http_form_input_module);

    if (ctx == NULL) {
        dd("ndk handler:null ctx");
        return NGX_OK;
    }

    if (!ctx->done) {
        dd("ctx not done");
        return NGX_OK;
    }

    rc = ngx_http_form_input_arg(r, v->data, v->len, res, 0);

    return rc;
}


static ngx_int_t
ngx_http_set_form_input_multi(ngx_http_request_t *r, ngx_str_t *res,
    ngx_http_variable_value_t *v)
{
    ngx_http_form_input_ctx_t           *ctx;
    ngx_int_t                            rc;

    dd_enter();

    dd("set default return value");
    ngx_str_set(res, "");

    /* dd("set default return value"); */

    if (r->done) {
        return NGX_OK;
    }

    ctx = ngx_http_get_module_ctx(r, ngx_http_form_input_module);

    if (ctx == NULL) {
        dd("ndk handler:null ctx");
        return NGX_OK;
    }

    if (!ctx->done) {
        dd("ctx not done");
        return NGX_OK;
    }

    rc = ngx_http_form_input_arg(r, v->data, v->len, res, 1);

    return rc;
}


/* fork from ngx_http_arg.
 * read argument(s) with name arg_name and length arg_len into value variable,
 * if multi flag is set, multi arguments with name arg_name will be read and
 * stored in an ngx_array_t struct, this can be operated by directives in
 * array-var-nginx-module */
static ngx_int_t
ngx_http_form_input_arg(ngx_http_request_t *r, u_char *arg_name, size_t arg_len,
    ngx_str_t *value, ngx_flag_t multi)
{
    u_char              *p, *v, *last, *buf;
    ngx_chain_t         *cl;
    size_t               len = 0;
    ngx_array_t         *array = NULL;
    ngx_str_t           *s;
    ngx_buf_t           *b;

    if (multi) {
        array = ngx_array_create(r->pool, 1, sizeof(ngx_str_t));
        if (array == NULL) {
            return NGX_ERROR;
        }
        value->data = (u_char *)array;
        value->len = sizeof(ngx_array_t);

    } else {
        ngx_str_set(value, "");
    }

    /* we read data from r->request_body->bufs */
    if (r->request_body == NULL || r->request_body->bufs == NULL) {
        dd("empty rb or empty rb bufs");
        return NGX_OK;
    }

    if (r->request_body->bufs->next != NULL) {
        /* more than one buffer...we should copy the data out... */
        len = 0;
        for (cl = r->request_body->bufs; cl; cl = cl->next) {
            b = cl->buf;

            if (b->in_file) {
                ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                        "form-input: in-file buffer found. aborted. "
                        "consider increasing your client_body_buffer_size "
                        "setting");

                return NGX_OK;
            }

            len += b->last - b->pos;
        }

        dd("len=%d", (int) len);

        if (len == 0) {
            return NGX_OK;
        }

        buf = ngx_palloc(r->pool, len);
        if (buf == NULL) {
            return NGX_ERROR;
        }

        p = buf;
        last = p + len;

        for (cl = r->request_body->bufs; cl; cl = cl->next) {
            p = ngx_copy(p, cl->buf->pos, cl->buf->last - cl->buf->pos);
        }

        dd("p - buf = %d, last - buf = %d", (int) (p - buf),
           (int) (last - buf));

        dd("copied buf (len %d): %.*s", (int) len, (int) len,
           buf);

    } else {
        dd("XXX one buffer only");

        b = r->request_body->bufs->buf;
        if (ngx_buf_size(b) == 0) {
            return NGX_OK;
        }

        buf = b->pos;
        last = b->last;
    }

    for (p = buf; p < last; p++) {
        /* we need '=' after name, so drop one char from last */

        p = ngx_strlcasestrn(p, last - 1, arg_name, arg_len - 1);
        if (p == NULL) {
            return NGX_OK;
        }

        dd("found argument name, offset: %d", (int) (p - buf));

        if ((p == buf || *(p - 1) == '&') && *(p + arg_len) == '=') {
            v = p + arg_len + 1;
            dd("v = %d...", (int) (v - buf));

            dd("buf now (len %d): %.*s",
                    (int) (last - v), (int) (last - v), v);
            p = ngx_strlchr(v, last, '&');
            if (p == NULL) {
                dd("& not found, pointing it to last...");
                p = last;

            } else {
                dd("found &, pointing it to %d...", (int) (p - buf));
            }

            if (multi) {
                s = ngx_array_push(array);
                if (s == NULL) {
                    return NGX_ERROR;
                }
                s->data = v;
                s->len = p - v;
                dd("array var:%.*s", (int) s->len, s->data);

            } else {
                value->data = v;
                value->len = p - v;
                dd("value: [%.*s]", (int) value->len, value->data);
                return NGX_OK;
            }
        }
    }

#if 0
    if (multi) {
        value->data = (u_char *) array;
        value->len = sizeof(ngx_array_t);
    }
#endif

    return NGX_OK;
}


static char *
ngx_http_set_form_input_conf_handler(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf)
{
    ndk_set_var_t                            filter;
    ngx_str_t                               *value, s;
    u_char                                  *p;

#if defined(nginx_version) && nginx_version >= 8042 && nginx_version <= 8053
    return "does not work with " NGINX_VER;
#endif

    ngx_http_form_input_used = 1;

    filter.type = NDK_SET_VAR_MULTI_VALUE;
    filter.size = 1;

    value = cf->args->elts;

    if ((value->len == sizeof("set_form_input_multi") - 1) &&
        ngx_strncmp(value->data, "set_form_input_multi", value->len) == 0)
    {
        dd("use ngx_http_form_input_multi");
        filter.func = (void *) ngx_http_set_form_input_multi;

    } else {
        filter.func = (void *) ngx_http_set_form_input;
    }

    value++;

    if (cf->args->nelts == 2) {
        p = value->data;
        p++;
        s.len = value->len - 1;
        s.data = p;

    } else if (cf->args->nelts == 3) {
        s.len = (value + 1)->len;
        s.data = (value + 1)->data;
    }

    return ndk_set_var_multi_value_core (cf, value,  &s, &filter);
}


#if 0

static void *
ngx_http_form_input_create_loc_conf(ngx_conf_t *cf)
{
    ngx_http_form_input_loc_conf_t *conf;

    conf = ngx_palloc(cf->pool, sizeof(ngx_http_form_input_loc_conf_t));

    if (conf == NULL) {
        return NULL;
    }

    return conf;
}


static char *
ngx_http_form_input_merge_loc_conf(ngx_conf_t *cf, void *parent, void *child)
{
    return NGX_CONF_OK;
}

#endif


/* register a new rewrite phase handler */
static ngx_int_t
ngx_http_form_input_init(ngx_conf_t *cf)
{

    ngx_http_handler_pt             *h;
    ngx_http_core_main_conf_t       *cmcf;

    /*
    ngx_http_form_input_loc_conf_t  *lcf;

    lcf = ngx_http_conf_get_module_loc_conf(cf, ngx_http_form_input_module);
    if (lcf->enabled != 1) {
        dd("set_form_input not used");
        return NGX_OK;
    }
    */

    if (!ngx_http_form_input_used) {
        return NGX_OK;
    }

    cmcf = ngx_http_conf_get_module_main_conf(cf, ngx_http_core_module);

    h = ngx_array_push(&cmcf->phases[NGX_HTTP_REWRITE_PHASE].handlers);

    if (h == NULL) {
        return NGX_ERROR;
    }

    *h = ngx_http_form_input_handler;

    return NGX_OK;
}


/* an rewrite phase handler */
static ngx_int_t
ngx_http_form_input_handler(ngx_http_request_t *r)
{
    ngx_http_form_input_ctx_t       *ctx;
    ngx_str_t                        value;
    ngx_int_t                        rc;

    dd_enter();

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "http form_input rewrite phase handler");

    ctx = ngx_http_get_module_ctx(r, ngx_http_form_input_module);

    if (ctx != NULL) {
        if (ctx->done) {
            ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                           "http form_input rewrite phase handler done");

            return NGX_DECLINED;
        }

        return NGX_DONE;
    }

    if (r->method != NGX_HTTP_POST && r->method != NGX_HTTP_PUT) {
        return NGX_DECLINED;
    }

    if (r->headers_in.content_type == NULL
        || r->headers_in.content_type->value.data == NULL)
    {
        dd("content_type is %s", r->headers_in.content_type == NULL?"NULL":
                "NOT NULL");

        return NGX_DECLINED;
    }

    value = r->headers_in.content_type->value;

    dd("r->headers_in.content_length_n:%d",
       (int) r->headers_in.content_length_n);

    /* just focus on x-www-form-urlencoded */

    if (value.len < form_urlencoded_type_len
        || ngx_strncasecmp(value.data, (u_char *) form_urlencoded_type,
                           form_urlencoded_type_len) != 0)
    {
        dd("not application/x-www-form-urlencoded");
        return NGX_DECLINED;
    }

    dd("content type is application/x-www-form-urlencoded");

    dd("create new ctx");

    ctx = ngx_pcalloc(r->pool, sizeof(ngx_http_form_input_ctx_t));
    if (ctx == NULL) {
        return NGX_ERROR;
    }

    /* set by ngx_pcalloc:
     *      ctx->done = 0;
     *      ctx->waiting_more_body = 0;
     */

    ngx_http_set_ctx(r, ctx, ngx_http_form_input_module);

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "http form_input start to read client request body");

    rc = ngx_http_read_client_request_body(r, ngx_http_form_input_post_read);

    if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
#if (nginx_version < 1002006) ||                                             \
        (nginx_version >= 1003000 && nginx_version < 1003009)
        r->main->count--;
#endif

        return rc;
    }

    if (rc == NGX_AGAIN) {
        ctx->waiting_more_body = 1;

        return NGX_DONE;
    }

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "http form_input has read the request body in one run");

    return NGX_DECLINED;
}


static void
ngx_http_form_input_post_read(ngx_http_request_t *r)
{
    ngx_http_form_input_ctx_t     *ctx;

    ngx_log_debug0(NGX_LOG_DEBUG_HTTP, r->connection->log, 0,
                   "http form_input post read request body");

    ctx = ngx_http_get_module_ctx(r, ngx_http_form_input_module);

    ctx->done = 1;

#if defined(nginx_version) && nginx_version >= 8011
    dd("count--");
    r->main->count--;
#endif

    dd("waiting more body: %d", (int) ctx->waiting_more_body);

    /* waiting_more_body my rewrite phase handler */
    if (ctx->waiting_more_body) {
        ctx->waiting_more_body = 0;

        ngx_http_core_run_phases(r);
    }
}
