
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_memc_util.h"

ngx_http_memc_cmd_t
ngx_http_memc_parse_cmd(u_char *data, size_t len, ngx_flag_t *is_storage_cmd)
{
    switch (len) {
        case 3:
            if (ngx_http_memc_strcmp_const(data, "set") == 0) {
                *is_storage_cmd = 1;
                return ngx_http_memc_cmd_set;
            }

            if (ngx_http_memc_strcmp_const(data, "add") == 0) {
                *is_storage_cmd = 1;
                return ngx_http_memc_cmd_add;
            }

            /*
            if (ngx_str3cmp(data, 'c', 'a', 's')) {
                *is_storage_cmd = 1;
                return ngx_http_memc_cmd_cas;
            }
            */

            if (ngx_http_memc_strcmp_const(data, "get") == 0) {
                return ngx_http_memc_cmd_get;
            }

            break;

        case 4:
            /*
            if (ngx_str4cmp(data, 'g', 'e', 't', 's')) {
                return ngx_http_memc_cmd_gets;
            }
            */

            if (ngx_http_memc_strcmp_const(data, "incr") == 0) {
                return ngx_http_memc_cmd_incr;
            }

            if (ngx_http_memc_strcmp_const(data, "decr") == 0) {
                return ngx_http_memc_cmd_decr;
            }

            break;

        case 5:
            if (ngx_http_memc_strcmp_const(data, "stats") == 0) {
                return ngx_http_memc_cmd_stats;
            }

            break;

        case 6:
            if (ngx_http_memc_strcmp_const(data, "append") == 0) {
                *is_storage_cmd = 1;
                return ngx_http_memc_cmd_append;
            }

            if (ngx_http_memc_strcmp_const(data, "delete") == 0) {
                return ngx_http_memc_cmd_delete;
            }

            break;

        case 7:
            if (ngx_http_memc_strcmp_const(data, "replace") == 0) {
                *is_storage_cmd = 1;
                return ngx_http_memc_cmd_replace;
            }

            if (ngx_http_memc_strcmp_const(data, "prepend") == 0) {
                *is_storage_cmd = 1;
                return ngx_http_memc_cmd_prepend;
            }

            if (ngx_http_memc_strcmp_const(data, "version") == 0) {
                return ngx_http_memc_cmd_version;
            }

            break;

        case 9:
            if (ngx_http_memc_strcmp_const(data, "flush_all") == 0) {
                return ngx_http_memc_cmd_flush_all;
            }

            /*
            if (ngx_str9cmp(data, 'v', 'e', 'r', 'b', 'o', 's', 'i', 't', 'y'))
            {
                return ngx_http_memc_cmd_verbosity;
            }
            */

            break;

        default:
            break;
    }

    return ngx_http_memc_cmd_unknown;
}


ngx_http_upstream_srv_conf_t *
ngx_http_memc_upstream_add(ngx_http_request_t *r, ngx_url_t *url)
{
    ngx_http_upstream_main_conf_t  *umcf;
    ngx_http_upstream_srv_conf_t  **uscfp;
    ngx_uint_t                      i;

    umcf = ngx_http_get_module_main_conf(r, ngx_http_upstream_module);

    uscfp = umcf->upstreams.elts;

    for (i = 0; i < umcf->upstreams.nelts; i++) {

        if (uscfp[i]->host.len != url->host.len
            || ngx_strncasecmp(uscfp[i]->host.data, url->host.data,
                url->host.len) != 0)
        {
            dd("upstream_add: host not match");
            continue;
        }

        if (uscfp[i]->port != url->port) {
            dd("upstream_add: port not match: %d != %d",
                    (int) uscfp[i]->port, (int) url->port);
            continue;
        }

        if (uscfp[i]->default_port && url->default_port
            && uscfp[i]->default_port != url->default_port)
        {
            dd("upstream_add: default_port not match");
            continue;
        }

        return uscfp[i];
    }

    dd("No upstream found: %.*s", (int) url->host.len, url->host.data);

    return NULL;
}

