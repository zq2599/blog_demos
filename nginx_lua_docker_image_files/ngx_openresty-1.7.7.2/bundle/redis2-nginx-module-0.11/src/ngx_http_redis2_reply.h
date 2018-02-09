#ifndef NGX_HTTP_REDIS2_REPLY_H
#define NGX_HTTP_REDIS2_REPLY_H


#include "ngx_http_redis2_module.h"


ngx_int_t ngx_http_redis2_process_reply(
        ngx_http_redis2_ctx_t *ctx, ssize_t bytes);


#endif /* NGX_HTTP_REDIS2_REPLY_H */

