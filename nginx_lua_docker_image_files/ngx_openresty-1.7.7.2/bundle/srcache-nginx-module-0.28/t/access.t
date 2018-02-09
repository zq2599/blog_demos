# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);

plan tests => repeat_each() * 3 * blocks();

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

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
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 2: basic fetch
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        #deny all;

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
--- response_body
hello



=== TEST 3: basic fetch
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        deny all;

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
Content-Type: text/html
--- response_body_like: 403 Forbidden
--- error_code: 403

