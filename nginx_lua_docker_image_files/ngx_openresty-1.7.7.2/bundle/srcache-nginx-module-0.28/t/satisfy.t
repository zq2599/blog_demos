# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);

plan tests => repeat_each() * (3 * blocks() + 2);

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



=== TEST 2: basic fetch (cache miss) - deny all
--- config
    location /foo {
        satisfy any;

        deny all;

        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        echo hello;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
--- response_body_like: 403 Forbidden
--- error_code: 403



=== TEST 3: basic fetch (cache miss)
--- config
    location /foo {
        satisfy any;

        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        echo hello;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
Content-Length:
--- response_body
hello



=== TEST 4: basic fetch (cache hit)
--- config
    location /foo {
        satisfy any;

        default_type text/css;

        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        echo world;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
Content-Length: 6
--- response_body
hello

