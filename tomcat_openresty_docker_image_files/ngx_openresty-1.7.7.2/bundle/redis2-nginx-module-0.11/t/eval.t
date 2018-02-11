# vi:ft=

use lib 'lib';
use Test::Nginx::Socket;

plan tests => repeat_each() * (2 * blocks());

$ENV{TEST_NGINX_REDIS_PORT} ||= 6379;
$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11984;
$ENV{LUA_CPATH} ||= '/home/lz/luax/?.so';

#master_on;
#worker_connections 1024;

#no_diff;

#log_level 'warn';

run_tests();

__DATA__

=== TEST 1: no subrequest in memory
--- config
    location /foo {
        eval_override_content_type 'application/octet-stream';
        eval_subrequest_in_memory off;
        eval $res {
            redis2_query set one first;
            redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
        }
        echo [$res];
    }
--- request
    GET /foo
--- response_body_like: ^\[\+OK\r\n\]$
--- error_code: 200



=== TEST 2: subrequest in memory
--- config
    location /foo {
        eval_override_content_type 'application/octet-stream';
        eval_subrequest_in_memory on;
        eval $res {
            #redis2_literal_raw_query 'set one 5\r\nfirst\r\n';
            #redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
            set $memcached_key foo;
            memcached_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        }
        echo [$res];
    }
--- request
    GET /foo
--- response_body_like: ^\[\+OK\r\n\]$
--- error_code: 200
--- SKIP



=== TEST 3: subrequest in memory
--- config
    location = /redis {
        internal;
        redis2_query get $query_string;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location ~ ^/([a-zA-Z0-9]+)$ {
        set $path $1;
        rewrite_by_lua '
            local resp = ngx.location.capture("/redis",
                { args = ngx.var.path })

            if resp.status ~= ngx.HTTP_OK then
                return ngx.exit(ngx.HTTP_INTERNAL_SERVER_ERROR)
            end

            local parser = require("redis.parser")
            local res, rc = parser.parse_reply(resp.body)
            -- print("rc = ", rc, " ", parser.BULK_REPLY)
            if rc ~= parser.BULK_REPLY then
                return ngx.exit(ngx.HTTP_INTERNAL_SERVER_ERROR)
            end

            print("res = ", res)

            -- ngx.print(ngx.var.path)
            return ngx.redirect(res)
        ';

        content_by_lua return;
     }
--- request
    GET /foo
--- response_headers_like
Location: http://[^/]+/hello/world
--- response_body_like: 302 Found
--- error_code: 302
--- SKIP

