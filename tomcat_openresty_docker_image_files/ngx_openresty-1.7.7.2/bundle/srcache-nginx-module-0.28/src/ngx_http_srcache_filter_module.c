
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_srcache_filter_module.h"
#include "ngx_http_srcache_util.h"
#include "ngx_http_srcache_var.h"
#include "ngx_http_srcache_fetch.h"
#include "ngx_http_srcache_store.h"
#include "ngx_http_srcache_headers.h"


static void *ngx_http_srcache_create_loc_conf(ngx_conf_t *cf);
static char *ngx_http_srcache_merge_loc_conf(ngx_conf_t *cf, void *parent,
    void *child);
static ngx_int_t ngx_http_srcache_post_config(ngx_conf_t *cf);
static char *ngx_http_srcache_conf_set_request(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);
static void *ngx_http_srcache_create_main_conf(ngx_conf_t *cf);
static char *ngx_http_srcache_init_main_conf(ngx_conf_t *cf, void *conf);
static char * ngx_http_srcache_store_statuses(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);


static volatile ngx_cycle_t  *ngx_http_srcache_prev_cycle = NULL;


static ngx_str_t  ngx_http_srcache_hide_headers[] = {
    ngx_string("Connection"),
    ngx_string("Keep-Alive"),
    ngx_string("Proxy-Authenticate"),
    ngx_string("Proxy-Authorization"),
    ngx_string("TE"),
    ngx_string("Trailers"),
    ngx_string("Transfer-Encoding"),
    ngx_string("Upgrade"),
    ngx_string("Set-Cookie"),
    ngx_null_string
};


static ngx_conf_bitmask_t  ngx_http_srcache_cache_method_mask[] = {
   { ngx_string("GET"),  NGX_HTTP_GET},
   { ngx_string("HEAD"), NGX_HTTP_HEAD },
   { ngx_string("POST"), NGX_HTTP_POST },
   { ngx_string("PUT"), NGX_HTTP_PUT },
   { ngx_string("DELETE"), NGX_HTTP_DELETE },
   { ngx_null_string, 0 }
};


static ngx_command_t  ngx_http_srcache_commands[] = {

    { ngx_string("srcache_buffer"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE1,
      ngx_conf_set_size_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, buf_size),
      NULL },

    { ngx_string("srcache_fetch"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE23,
      ngx_http_srcache_conf_set_request,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, fetch),
      NULL },

    { ngx_string("srcache_store"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE23,
      ngx_http_srcache_conf_set_request,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, store),
      NULL },

    { ngx_string("srcache_store_max_size"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE1,
      ngx_conf_set_size_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, store_max_size),
      NULL },

    { ngx_string("srcache_fetch_skip"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE1,
      ngx_http_set_complex_value_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, fetch_skip),
      NULL },

    { ngx_string("srcache_store_skip"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_TAKE1,
      ngx_http_set_complex_value_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, store_skip),
      NULL },

    { ngx_string("srcache_store_statuses"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF
          |NGX_CONF_1MORE,
      ngx_http_srcache_store_statuses,
      NGX_HTTP_LOC_CONF_OFFSET,
      0,
      NULL },

    { ngx_string("srcache_methods"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_1MORE,
      ngx_conf_set_bitmask_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, cache_methods),
      &ngx_http_srcache_cache_method_mask },

    { ngx_string("srcache_request_cache_control"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_FLAG,
      ngx_conf_set_flag_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, req_cache_control),
      NULL },

    { ngx_string("srcache_store_private"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_FLAG,
      ngx_conf_set_flag_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, store_private),
      NULL },

    { ngx_string("srcache_store_no_store"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_FLAG,
      ngx_conf_set_flag_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, store_no_store),
      NULL },

    { ngx_string("srcache_store_no_cache"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_FLAG,
      ngx_conf_set_flag_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, store_no_cache),
      NULL },

    { ngx_string("srcache_response_cache_control"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_FLAG,
      ngx_conf_set_flag_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, resp_cache_control),
      NULL },

    { ngx_string("srcache_store_hide_header"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_str_array_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, hide_headers),
      NULL },

    { ngx_string("srcache_store_pass_header"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_str_array_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, pass_headers),
      NULL },

    { ngx_string("srcache_store_ranges"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_FLAG,
      ngx_conf_set_flag_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, store_ranges),
      NULL },

    { ngx_string("srcache_ignore_content_encoding"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_CONF_FLAG,
      ngx_conf_set_flag_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, ignore_content_encoding),
      NULL },

    { ngx_string("srcache_header_buffer_size"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF
          |NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_size_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, header_buf_size),
      NULL },

    { ngx_string("srcache_max_expire"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF
          |NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_sec_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, max_expire),
      NULL },

    { ngx_string("srcache_default_expire"),
      NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF
          |NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
      ngx_conf_set_sec_slot,
      NGX_HTTP_LOC_CONF_OFFSET,
      offsetof(ngx_http_srcache_loc_conf_t, default_expire),
      NULL },

      ngx_null_command
};


static ngx_http_module_t  ngx_http_srcache_filter_module_ctx = {
    NULL,                                  /* preconfiguration */
    ngx_http_srcache_post_config,          /* postconfiguration */

    ngx_http_srcache_create_main_conf,     /* create main configuration */
    ngx_http_srcache_init_main_conf,       /* init main configuration */

    NULL,                                  /* create server configuration */
    NULL,                                  /* merge server configuration */

    ngx_http_srcache_create_loc_conf,      /* create location configuration */
    ngx_http_srcache_merge_loc_conf        /* merge location configuration */
};


ngx_module_t  ngx_http_srcache_filter_module = {
    NGX_MODULE_V1,
    &ngx_http_srcache_filter_module_ctx,   /* module context */
    ngx_http_srcache_commands,             /* module directives */
    NGX_HTTP_MODULE,                       /* module type */
    NULL,                                  /* init master */
    NULL,                                  /* init module */
    NULL,                                  /* init process */
    NULL,                                  /* init thread */
    NULL,                                  /* exit thread */
    NULL,                                  /* exit process */
    NULL,                                  /* exit master */
    NGX_MODULE_V1_PADDING
};


static void *
ngx_http_srcache_create_loc_conf(ngx_conf_t *cf)
{
    ngx_http_srcache_loc_conf_t  *conf;

    conf = ngx_pcalloc(cf->pool, sizeof(ngx_http_srcache_loc_conf_t));
    if (conf == NULL) {
        return NULL;
    }

    /*
     * set by ngx_pcalloc():
     *
     *      conf->fetch_skip = NULL;
     *      conf->store_skip = NULL;
     *      conf->cache_methods = 0;
     *      conf->hide_headers_hash = { NULL, 0 };
     *      conf->skip_content_type = 0;
     *      conf->store_statuses = NULL;
     */

    conf->fetch = NGX_CONF_UNSET_PTR;
    conf->store = NGX_CONF_UNSET_PTR;

    conf->buf_size = NGX_CONF_UNSET_SIZE;
    conf->store_max_size = NGX_CONF_UNSET_SIZE;
    conf->header_buf_size = NGX_CONF_UNSET_SIZE;

    conf->req_cache_control = NGX_CONF_UNSET;
    conf->resp_cache_control = NGX_CONF_UNSET;

    conf->store_private = NGX_CONF_UNSET;
    conf->store_no_store = NGX_CONF_UNSET;
    conf->store_no_cache = NGX_CONF_UNSET;
    conf->store_ranges = NGX_CONF_UNSET;

    conf->max_expire = NGX_CONF_UNSET;
    conf->default_expire = NGX_CONF_UNSET;

    conf->ignore_content_encoding = NGX_CONF_UNSET;

    conf->hide_headers = NGX_CONF_UNSET_PTR;
    conf->pass_headers = NGX_CONF_UNSET_PTR;

    return conf;
}


static char *
ngx_http_srcache_merge_loc_conf(ngx_conf_t *cf, void *parent, void *child)
{
    ngx_http_srcache_loc_conf_t     *prev = parent;
    ngx_http_srcache_loc_conf_t     *conf = child;
    ngx_hash_init_t                  hash;

    ngx_conf_merge_ptr_value(conf->fetch, prev->fetch, NULL);
    ngx_conf_merge_ptr_value(conf->store, prev->store, NULL);

    ngx_conf_merge_size_value(conf->buf_size, prev->buf_size,
            (size_t) ngx_pagesize);

    ngx_conf_merge_size_value(conf->store_max_size, prev->store_max_size, 0);

    ngx_conf_merge_size_value(conf->header_buf_size, prev->header_buf_size,
            (size_t) ngx_pagesize);

    if (conf->fetch_skip == NULL) {
        conf->fetch_skip = prev->fetch_skip;
    }

    if (conf->store_skip == NULL) {
        conf->store_skip = prev->store_skip;
    }

    if (conf->store_statuses == NULL) {
        conf->store_statuses = prev->store_statuses;
    }

    if (conf->cache_methods == 0) {
        conf->cache_methods = prev->cache_methods;
    }

    conf->cache_methods |= NGX_HTTP_GET|NGX_HTTP_HEAD;

    ngx_conf_merge_value(conf->req_cache_control, prev->req_cache_control, 0);
    ngx_conf_merge_value(conf->resp_cache_control, prev->resp_cache_control, 1);

    ngx_conf_merge_value(conf->store_private, prev->store_private, 0);
    ngx_conf_merge_value(conf->store_no_store, prev->store_no_store, 0);
    ngx_conf_merge_value(conf->store_no_cache, prev->store_no_cache, 0);
    ngx_conf_merge_value(conf->store_ranges, prev->store_ranges, 0);

    ngx_conf_merge_value(conf->max_expire, prev->max_expire, 0);
    ngx_conf_merge_value(conf->default_expire, prev->default_expire, 60);

    ngx_conf_merge_value(conf->ignore_content_encoding,
            prev->ignore_content_encoding, 0);

    hash.max_size = 512;
    hash.bucket_size = ngx_align(64, ngx_cacheline_size);
    hash.name = "srcache_store_hide_headers_hash";

    if (ngx_http_srcache_hide_headers_hash(cf, conf,
             prev, ngx_http_srcache_hide_headers, &hash)
        != NGX_OK)
    {
        return NGX_CONF_ERROR;
    }

    return NGX_CONF_OK;
}


static char *
ngx_http_srcache_conf_set_request(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf)
{
    char  *p = conf;

    ngx_http_srcache_request_t      **rpp;
    ngx_http_srcache_request_t       *rp;
    ngx_str_t                        *value;
    ngx_str_t                        *method_name;
    ngx_http_compile_complex_value_t  ccv;
    ngx_http_srcache_main_conf_t     *smcf;

    rpp = (ngx_http_srcache_request_t **) (p + cmd->offset);

    if (*rpp != NGX_CONF_UNSET_PTR) {
        return "is duplicate";
    }

    smcf = ngx_http_conf_get_module_main_conf(cf,
                                              ngx_http_srcache_filter_module);

    smcf->module_used = 1;

    value = cf->args->elts;

    *rpp = ngx_pcalloc(cf->pool, sizeof(ngx_http_srcache_request_t));
    if (*rpp == NULL) {
        return NGX_CONF_ERROR;
    }

    rp = *rpp;

    method_name = &value[1];

    rp->method = ngx_http_srcache_parse_method_name(&method_name);

    if (rp->method == NGX_HTTP_UNKNOWN) {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                "%V specifies bad HTTP method %V",
                &cmd->name, method_name);

        return NGX_CONF_ERROR;
    }

    rp->method_name = *method_name;

    /* compile the location arg */

    if (value[2].len == 0) {
        ngx_memzero(&rp->location, sizeof(ngx_http_complex_value_t));

    } else {
        ngx_memzero(&ccv, sizeof(ngx_http_compile_complex_value_t));

        ccv.cf = cf;
        ccv.value = &value[2];
        ccv.complex_value = &rp->location;

        if (ngx_http_compile_complex_value(&ccv) != NGX_OK) {
            return NGX_CONF_ERROR;
        }
    }

    if (cf->args->nelts == 2 + 1) {
        return NGX_CONF_OK;
    }

    /* compile the args arg */

    if (value[3].len == 0) {
        ngx_memzero(&rp->location, sizeof(ngx_http_complex_value_t));
        return NGX_CONF_OK;
    }

    ngx_memzero(&ccv, sizeof(ngx_http_compile_complex_value_t));

    ccv.cf = cf;
    ccv.value = &value[3];
    ccv.complex_value = &rp->args;

    if (ngx_http_compile_complex_value(&ccv) != NGX_OK) {
        return NGX_CONF_ERROR;
    }

    return NGX_CONF_OK;
}


static void *
ngx_http_srcache_create_main_conf(ngx_conf_t *cf)
{
    ngx_http_srcache_main_conf_t *smcf;

    smcf = ngx_pcalloc(cf->pool, sizeof(ngx_http_srcache_main_conf_t));
    if (smcf == NULL) {
        return NULL;
    }

    /* set by ngx_pcalloc:
     *      smcf->postponed_to_access_phase_end = 0;
     *      smcf->module_used = 0;
     *      smcf->headers_in_hash = { NULL, 0 };
     */

    return smcf;
}


static char *
ngx_http_srcache_init_main_conf(ngx_conf_t *cf, void *conf)
{
    ngx_http_srcache_main_conf_t *smcf = conf;

    ngx_array_t                     headers_in;
    ngx_hash_key_t                 *hk;
    ngx_hash_init_t                 hash;
    ngx_http_srcache_header_t      *header;

    /* srcache_headers_in_hash */

    if (ngx_array_init(&headers_in, cf->temp_pool, 32, sizeof(ngx_hash_key_t))
        != NGX_OK)
    {
        return NGX_CONF_ERROR;
    }

    for (header = ngx_http_srcache_headers_in; header->name.len; header++) {
        hk = ngx_array_push(&headers_in);
        if (hk == NULL) {
            return NGX_CONF_ERROR;
        }

        hk->key = header->name;
        hk->key_hash = ngx_hash_key_lc(header->name.data, header->name.len);
        hk->value = header;
    }

    hash.hash = &smcf->headers_in_hash;
    hash.key = ngx_hash_key_lc;
    hash.max_size = 512;
    hash.bucket_size = ngx_align(64, ngx_cacheline_size);
    hash.name = "srcache_headers_in_hash";
    hash.pool = cf->pool;
    hash.temp_pool = NULL;

    if (ngx_hash_init(&hash, headers_in.elts, headers_in.nelts) != NGX_OK) {
        return NGX_CONF_ERROR;
    }

    return NGX_CONF_OK;
}


static ngx_int_t
ngx_http_srcache_post_config(ngx_conf_t *cf)
{
    int                              multi_http_blocks;
    ngx_int_t                        rc;
    ngx_http_handler_pt             *h;
    ngx_http_core_main_conf_t       *cmcf;
    ngx_http_srcache_main_conf_t    *smcf;

    rc = ngx_http_srcache_add_variables(cf);
    if (rc != NGX_OK) {
        return rc;
    }

    smcf = ngx_http_conf_get_module_main_conf(cf,
                                              ngx_http_srcache_filter_module);

    if (ngx_http_srcache_prev_cycle != ngx_cycle) {
        ngx_http_srcache_prev_cycle = ngx_cycle;
        multi_http_blocks = 0;

    } else {
        multi_http_blocks = 1;
    }

    if (multi_http_blocks || smcf->module_used) {

        dd("using ngx-srcache");

        /* register our output filters */
        rc = ngx_http_srcache_filter_init(cf);
        if (rc != NGX_OK) {
            return rc;
        }

        cmcf = ngx_http_conf_get_module_main_conf(cf, ngx_http_core_module);

        /* register our access phase handler */

        h = ngx_array_push(&cmcf->phases[NGX_HTTP_ACCESS_PHASE].handlers);
        if (h == NULL) {
            return NGX_ERROR;
        }

        *h = ngx_http_srcache_access_handler;
    }

    return NGX_OK;
}


static char *
ngx_http_srcache_store_statuses(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf)
{
    ngx_http_srcache_loc_conf_t     *slcf = conf;

    ngx_uint_t       i, n;
    ngx_int_t        status;
    ngx_str_t       *value;

    value = cf->args->elts;

    if (slcf->store_statuses) {
        return "is duplicate";
    }

    n = cf->args->nelts - 1;

    slcf->store_statuses = ngx_pnalloc(cf->pool, (n + 1) * sizeof(ngx_int_t));
    if (slcf->store_statuses == NULL) {
        return NGX_CONF_ERROR;
    }

    for (i = 1; i <= n; i++) {
        status = ngx_atoi(value[i].data, value[i].len);
        if (status == NGX_ERROR) {
            ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                    "status code \"%V\" is an invalid number",
                    &value[i]);

            return NGX_CONF_ERROR;
        }

        if (status < 0) {
            ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                    "status code \"%V\" is not a positive number",
                    &value[i]);

            return NGX_CONF_ERROR;
        }

        slcf->store_statuses[i - 1] = status;
    }

    slcf->store_statuses[i - 1] = 0;

    ngx_sort(slcf->store_statuses, n, sizeof(ngx_int_t),
            ngx_http_srcache_cmp_int);

#if 0
    for (i = 0; i < n; i++) {
        dd("status: %d", (int) slcf->store_statuses[i]);
    }
#endif

    return NGX_CONF_OK;
}

/* vi:set ft=c ts=4 sw=4 et fdm=marker: */
