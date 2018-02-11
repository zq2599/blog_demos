#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include <ndk.h>
#include "ngx_http_set_misc_module.h"
#include "ngx_http_set_base32.h"
#include "ngx_http_set_default_value.h"
#include "ngx_http_set_hashed_upstream.h"
#include "ngx_http_set_unescape_uri.h"
#include "ngx_http_set_quote_sql.h"
#include "ngx_http_set_quote_json.h"
#include "ngx_http_set_escape_uri.h"
#include "ngx_http_set_local_today.h"
#include "ngx_http_set_hash.h"
#include "ngx_http_set_hex.h"
#include "ngx_http_set_base64.h"
#if NGX_OPENSSL
#include "ngx_http_set_hmac.h"
#endif
#include "ngx_http_set_random.h"
#include "ngx_http_set_secure_random.h"
#include "ngx_http_set_rotate.h"


#define NGX_UNESCAPE_URI_COMPONENT  0
#define BASE32_ALPHABET_LEN         32


static void *ngx_http_set_misc_create_loc_conf(ngx_conf_t *cf);
static char *ngx_http_set_misc_merge_loc_conf(ngx_conf_t *cf, void *parent,
    void *child);
static char * ngx_http_set_misc_base32_alphabet(ngx_conf_t *cf,
    ngx_command_t *cmd, void *conf);


static ngx_conf_deprecated_t  ngx_conf_deprecated_set_misc_base32_padding = {
    ngx_conf_deprecated, "set_misc_base32_padding", "set_base32_padding"
};


static ndk_set_var_t  ngx_http_set_misc_set_encode_base64_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_misc_set_encode_base64,
    1,
    NULL
};

static ndk_set_var_t  ngx_http_set_misc_set_decode_base64_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_misc_set_decode_base64,
    1,
    NULL
};


static ndk_set_var_t  ngx_http_set_misc_set_decode_hex_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_misc_set_decode_hex,
    1,
    NULL
};


static ndk_set_var_t  ngx_http_set_misc_set_encode_hex_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_misc_set_encode_hex,
    1,
    NULL
};


#if NGX_OPENSSL
static ndk_set_var_t  ngx_http_set_misc_set_hmac_sha1_filter = {
    NDK_SET_VAR_MULTI_VALUE,
    (void *) ngx_http_set_misc_set_hmac_sha1,
    2,
    NULL
};
#endif


#ifndef NGX_HTTP_SET_HASH
static ndk_set_var_t  ngx_http_set_misc_set_md5_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_misc_set_md5,
    1,
    NULL
};


#if NGX_HAVE_SHA1
static ndk_set_var_t  ngx_http_set_misc_set_sha1_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_misc_set_sha1,
    1,
    NULL
};
#endif
#endif


static ndk_set_var_t  ngx_http_set_misc_unescape_uri_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_misc_unescape_uri,
    1,
    NULL
};


static ndk_set_var_t ngx_http_set_misc_escape_uri_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_misc_escape_uri,
    1,
    NULL
};


static ndk_set_var_t  ngx_http_set_misc_decode_base32_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_misc_decode_base32,
    1,
    NULL
};


static ndk_set_var_t  ngx_http_set_misc_quote_sql_str_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_misc_quote_sql_str,
    1,
    NULL
};


static ndk_set_var_t  ngx_http_set_misc_quote_pgsql_str_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_misc_quote_pgsql_str,
    1,
    NULL
};


static ndk_set_var_t  ngx_http_set_misc_quote_json_str_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_misc_quote_json_str,
    1,
    NULL
};


static ndk_set_var_t  ngx_http_set_misc_encode_base32_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_misc_encode_base32,
    1,
    NULL
};


static ndk_set_var_t ngx_http_set_misc_local_today_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_local_today,
    0,
    NULL
};


static ndk_set_var_t ngx_http_set_misc_formatted_gmt_time_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_formatted_gmt_time,
    2,
    NULL
};


static ndk_set_var_t ngx_http_set_misc_formatted_local_time_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_formatted_local_time,
    2,
    NULL
};


static ndk_set_var_t  ngx_http_set_misc_set_random_filter = {
    NDK_SET_VAR_MULTI_VALUE,
    (void *) ngx_http_set_misc_set_random,
    2,
    NULL
};


static ndk_set_var_t  ngx_http_set_misc_set_secure_random_alphanum_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_misc_set_secure_random_alphanum,
    1,
    NULL
};


static ndk_set_var_t  ngx_http_set_misc_set_secure_random_lcalpha_filter = {
    NDK_SET_VAR_VALUE,
    (void *) ngx_http_set_misc_set_secure_random_lcalpha,
    1,
    NULL
};


static ngx_command_t  ngx_http_set_misc_commands[] = {
    {   ngx_string ("set_encode_base64"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_set_encode_base64_filter
    },
    {   ngx_string ("set_decode_base64"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_set_decode_base64_filter
    },
    {   ngx_string ("set_decode_hex"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_set_decode_hex_filter
    },
    {   ngx_string ("set_encode_hex"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_set_encode_hex_filter
    },
#if NGX_OPENSSL
    {   ngx_string ("set_hmac_sha1"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE3,
        ndk_set_var_multi_value,
        0,
        0,
        &ngx_http_set_misc_set_hmac_sha1_filter
    },
#endif
#ifndef NGX_HTTP_SET_HASH
    {   ngx_string ("set_md5"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_set_md5_filter
    },
#if NGX_HAVE_SHA1
    {
        ngx_string ("set_sha1"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_set_sha1_filter
    },
#endif
#endif
    {
        ngx_string ("set_unescape_uri"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_unescape_uri_filter
    },
    {
        ngx_string ("set_escape_uri"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_escape_uri_filter
    },
    {
        ngx_string ("set_quote_sql_str"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_quote_sql_str_filter
    },
    {
        ngx_string ("set_quote_pgsql_str"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_quote_pgsql_str_filter
    },
    {
        ngx_string ("set_quote_json_str"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_quote_json_str_filter
    },
    {
        ngx_string ("set_if_empty"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE2,
        ngx_http_set_if_empty,
        0,
        0,
        NULL
    },
    {
        ngx_string("set_hashed_upstream"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE3,
        ngx_http_set_hashed_upstream,
        0,
        0,
        NULL
    },
    {
        /* this is now deprecated; use set_base32_padding instead */
        ngx_string("set_misc_base32_padding"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
                          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_FLAG,
        ngx_conf_set_flag_slot,
        NGX_HTTP_LOC_CONF_OFFSET,
        offsetof(ngx_http_set_misc_loc_conf_t, base32_padding),
        &ngx_conf_deprecated_set_misc_base32_padding,
    },
    {
        ngx_string("set_base32_padding"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
                          |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_FLAG,
        ngx_conf_set_flag_slot,
        NGX_HTTP_LOC_CONF_OFFSET,
        offsetof(ngx_http_set_misc_loc_conf_t, base32_padding),
        NULL
    },
    {
        ngx_string("set_base32_alphabet"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
        ngx_http_set_misc_base32_alphabet,
        NGX_HTTP_LOC_CONF_OFFSET,
        offsetof(ngx_http_set_misc_loc_conf_t, base32_alphabet),
        NULL
    },
    {
        ngx_string("set_encode_base32"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_encode_base32_filter
    },
    {
        ngx_string("set_decode_base32"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_decode_base32_filter
    },
    {
        ngx_string("set_local_today"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE1,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_local_today_filter
    },
    {
        ngx_string("set_formatted_gmt_time"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE2,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_formatted_gmt_time_filter
    },
    {
        ngx_string("set_formatted_local_time"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE2,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_formatted_local_time_filter
    },
    {   ngx_string ("set_random"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE3,
        ndk_set_var_multi_value,
        0,
        0,
        &ngx_http_set_misc_set_random_filter
    },
    {   ngx_string ("set_secure_random_alphanum"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_set_secure_random_alphanum_filter
    },
    {   ngx_string ("set_secure_random_lcalpha"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE12,
        ndk_set_var_value,
        0,
        0,
        &ngx_http_set_misc_set_secure_random_lcalpha_filter
    },
    {   ngx_string ("set_rotate"),
        NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_SIF_CONF
            |NGX_HTTP_LOC_CONF|NGX_HTTP_LIF_CONF|NGX_CONF_TAKE3,
        ngx_http_set_rotate,
        0,
        0,
        NULL
    },

    ngx_null_command
};


static ngx_http_module_t  ngx_http_set_misc_module_ctx = {
    NULL,                                 /* preconfiguration */
    NULL,                                 /* postconfiguration */

    NULL,                                  /* create main configuration */
    NULL,                                  /* init main configuration */

    NULL,                                  /* create server configuration */
    NULL,                                  /* merge server configuration */

    ngx_http_set_misc_create_loc_conf,     /* create location configuration */
    ngx_http_set_misc_merge_loc_conf       /*  merge location configuration */
};


ngx_module_t  ngx_http_set_misc_module = {
    NGX_MODULE_V1,
    &ngx_http_set_misc_module_ctx,          /* module context */
    ngx_http_set_misc_commands,             /* module directives */
    NGX_HTTP_MODULE,                        /* module type */
    NULL,                                   /* init master */
    NULL,                                   /* init module */
    NULL,                                   /* init process */
    NULL,                                   /* init thread */
    NULL,                                   /* exit thread */
    NULL,                                   /* exit process */
    NULL,                                   /* exit master */
    NGX_MODULE_V1_PADDING
};


void *
ngx_http_set_misc_create_loc_conf(ngx_conf_t *cf)
{
    ngx_http_set_misc_loc_conf_t *conf;

    conf = ngx_palloc(cf->pool, sizeof(ngx_http_set_misc_loc_conf_t));
    if (conf == NULL) {
        return NULL;
    }

    conf->base32_padding = NGX_CONF_UNSET;
    conf->base32_alphabet.data = NULL;
    conf->base32_alphabet.len = 0;
    conf->current = NGX_CONF_UNSET;

    return conf;
}


char *
ngx_http_set_misc_merge_loc_conf(ngx_conf_t *cf, void *parent, void *child)
{
    ngx_uint_t               i;

    ngx_http_set_misc_loc_conf_t *prev = parent;
    ngx_http_set_misc_loc_conf_t *conf = child;

    ngx_conf_merge_value(conf->base32_padding, prev->base32_padding, 1);

    ngx_conf_merge_str_value(conf->base32_alphabet, prev->base32_alphabet,
                             "0123456789abcdefghijklmnopqrstuv");

    ngx_conf_merge_value(conf->current, prev->current, NGX_CONF_UNSET);

    for (i = 0; i < BASE32_ALPHABET_LEN; i++) {
        conf->basis32[conf->base32_alphabet.data[i]] = (u_char) i;
    }

    return NGX_CONF_OK;
}


static char *
ngx_http_set_misc_base32_alphabet(ngx_conf_t *cf, ngx_command_t *cmd,
    void *conf)
{
    ngx_str_t       *value;

    value = cf->args->elts;

    if (value[1].len != BASE32_ALPHABET_LEN) {
        ngx_conf_log_error(NGX_LOG_EMERG, cf, 0,
                           "\"set_base32_alphabet\" directive takes an "
                           "alphabet of %uz bytes but %d expected",
                           value[1].len, BASE32_ALPHABET_LEN);
        return NGX_CONF_ERROR;
    }

    return ngx_conf_set_str_slot(cf, cmd, conf);
}
