# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);

plan tests => repeat_each() * (3 * blocks() + 10);

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#master_on();
no_shuffle();

run_tests();

__DATA__

=== TEST 1: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 2: basic fetch (cache miss), exptime specified by Expires
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /fetch;
        srcache_store PUT /store $srcache_expire;

        content_by_lua '
            ngx.header["Cache-Control"] = "max-age=60"
            ngx.say("hello")
        ';
    }

    location /fetch {
        return 404;
    }

    location /store {
        set $memc_key foo;
        set $memc_cmd set;
        set $memc_value $args;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
Content-Length: 
--- response_body
hello



=== TEST 3: check memcached key
--- config
    location /store {
        set $memc_key foo;
        set $memc_cmd get;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /store
--- response_body chop
60



=== TEST 4: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 5: basic fetch (cache miss), exptime specified by Expires
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /fetch;
        srcache_store PUT /store $srcache_expire;

        content_by_lua '
            ngx.header.expires = ngx.http_time(ngx.time() + 12)
            ngx.say("hello")
        ';
    }

    location /fetch {
        return 404;
    }

    location /store {
        set $memc_key foo;
        set $memc_cmd set;
        set $memc_value $args;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
Content-Length: 
--- response_body
hello



=== TEST 6: check memcached key
--- config
    location /store {
        set $memc_key foo;
        set $memc_cmd get;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /store
--- response_body chop
12



=== TEST 7: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 8: basic fetch (cache miss), greater than exptime
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /fetch;
        srcache_store PUT /store $srcache_expire;
        srcache_max_expire 11s;

        content_by_lua '
            ngx.header.expires = ngx.http_time(ngx.time() + 12)
            ngx.say("hello")
        ';
    }

    location /fetch {
        return 404;
    }

    location /store {
        set $memc_key foo;
        set $memc_cmd set;
        set $memc_value $args;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
Content-Length: 
--- response_body
hello



=== TEST 9: check memcached key
--- config
    location /store {
        set $memc_key foo;
        set $memc_cmd get;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /store
--- response_body chop
11



=== TEST 10: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 11: basic fetch (cache miss), no greater than exptime
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /fetch;
        srcache_store PUT /store $srcache_expire;
        srcache_max_expire 12s;

        content_by_lua '
            ngx.header.expires = ngx.http_time(ngx.time() + 12)
            ngx.say("hello")
        ';
    }

    location /fetch {
        return 404;
    }

    location /store {
        set $memc_key foo;
        set $memc_cmd set;
        set $memc_value $args;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
Content-Length: 
--- response_body
hello



=== TEST 12: check memcached key
--- config
    location /store {
        set $memc_key foo;
        set $memc_cmd get;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /store
--- response_body chop
12



=== TEST 13: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 14: basic fetch (cache miss), less than exptime
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /fetch;
        srcache_store PUT /store $srcache_expire;
        srcache_max_expire 13s;

        content_by_lua '
            ngx.header.expires = ngx.http_time(ngx.time() + 12)
            ngx.say("hello")
        ';
    }

    location /fetch {
        return 404;
    }

    location /store {
        set $memc_key foo;
        set $memc_cmd set;
        set $memc_value $args;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
Content-Length: 
--- response_body
hello



=== TEST 15: check memcached key
--- config
    location /store {
        set $memc_key foo;
        set $memc_cmd get;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /store
--- response_body chop
12



=== TEST 16: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 17: basic fetch (cache miss), default expire
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /fetch;
        srcache_store PUT /store $srcache_expire;
        srcache_max_expire 13s;
        srcache_default_expire 6s;

        content_by_lua '
            ngx.say("hello")
        ';
    }

    location /fetch {
        return 404;
    }

    location /store {
        set $memc_key foo;
        set $memc_cmd set;
        set $memc_value $args;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
Content-Length: 
--- response_body
hello



=== TEST 18: check memcached key
--- config
    location /store {
        set $memc_key foo;
        set $memc_cmd get;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /store
--- response_body chop
6



=== TEST 19: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 20: cache-control: max-age
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /fetch;
        srcache_store PUT /store $srcache_expire;
        srcache_max_expire 13s;
        srcache_default_expire 6s;

        content_by_lua '
            ngx.header.cache_control = "max-age=7"
            ngx.say("hello")
        ';
    }

    location /fetch {
        return 404;
    }

    location /store {
        set $memc_key foo;
        set $memc_cmd set;
        set $memc_value $args;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
Content-Length: 
--- response_body
hello



=== TEST 21: check memcached key
--- config
    location /store {
        set $memc_key foo;
        set $memc_cmd get;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /store
--- response_body chop
7



=== TEST 22: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 23: cache-control: max-age (greater than max_expire)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /fetch;
        srcache_store PUT /store $srcache_expire;
        srcache_max_expire 6s;
        srcache_default_expire 5s;

        content_by_lua '
            ngx.header.cache_control = "max-age=7"
            ngx.say("hello")
        ';
    }

    location /fetch {
        return 404;
    }

    location /store {
        set $memc_key foo;
        set $memc_cmd set;
        set $memc_value $args;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
Content-Length: 
--- response_body
hello



=== TEST 24: check memcached key
--- config
    location /store {
        set $memc_key foo;
        set $memc_cmd get;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /store
--- response_body chop
6



=== TEST 25: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 26: $srcache_expire used too early
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /fetch;
        srcache_store PUT /store $srcache_expire;
        srcache_max_expire 8s;
        srcache_default_expire 5s;

        set $saved_expire $srcache_expire;

        content_by_lua '
            ngx.header.cache_control = "max-age=7"
            ngx.say("expire in content before sending header: ", ngx.var.srcache_expire)
            ngx.say("expire in content after sending header: ", ngx.var.srcache_expire)
            ngx.say("expire in rewrite: ", ngx.var.saved_expire)
        ';
    }

    location /fetch {
        return 404;
    }

    location /store {
        set $memc_key foo;
        set $memc_cmd set;
        set $memc_value $args;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
Content-Length: 
--- response_body
expire in content before sending header: nil
expire in content after sending header: 7
expire in rewrite: 

