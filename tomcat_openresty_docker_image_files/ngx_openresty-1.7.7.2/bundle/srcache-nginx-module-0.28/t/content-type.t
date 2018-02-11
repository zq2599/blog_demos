# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

plan tests => repeat_each() * (4 * blocks());

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
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 2: basic fetch (Set-Cookie and Proxy-Authenticate hide by default)
--- config
    location /foo {
        charset UTF-8;
        charset_types application/x-javascript;
        default_type application/json;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        xss_get on;
        xss_callback_arg c;

        echo hello;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo?c=bar
--- response_headers
Content-Type: application/x-javascript; charset=UTF-8
Content-Length:
--- response_body chop
bar(hello
);



=== TEST 3: basic fetch (cache hit)
--- config
    location /foo {
        charset UTF-8;
        charset_types application/x-javascript;
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        xss_get on;
        xss_callback_arg c;

        echo world;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo?c=bar
--- response_headers
Content-Type: application/x-javascript; charset=UTF-8
Content-Length: 
!Set-Cookie
!Proxy-Authenticate
--- response_body chop
bar(hello
);

