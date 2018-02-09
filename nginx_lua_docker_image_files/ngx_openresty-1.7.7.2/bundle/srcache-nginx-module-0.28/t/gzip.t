# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);

plan tests => repeat_each() * 4 * blocks();

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



=== TEST 2: basic fetch (cache miss), and not stored due to Content-Encoding
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        content_by_lua '
            ngx.header.content_encoding = "gzip"
            ngx.say("hello")
        ';
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
Content-Length:
Content-Encoding: gzip
--- response_body
hello



=== TEST 3: basic fetch (cache miss again, not stored in the previous case)
--- config
    location /foo {
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
Content-Length: 
--- response_body
world



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



=== TEST 5: basic fetch (cache miss), and stored due to Content-Encoding + srcache_ignore_content_encoding
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        srcache_ignore_content_encoding on;

        content_by_lua '
            ngx.header.content_encoding = "gzip"
            ngx.say("hello")
        ';
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
Content-Length:
Content-Encoding: gzip
--- response_body
hello



=== TEST 6: basic fetch (cache miss again, not stored in the previous case)
--- config
    location /foo {
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

