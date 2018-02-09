# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);

plan tests => repeat_each() * (4 * blocks() + 3);

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



=== TEST 2: basic fetch (cache miss)
--- config
    location /foo {
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



=== TEST 3: basic fetch (cache hit)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        proxy_pass http://agentzh.org:12345/;
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



=== TEST 4: basic fetch (POST cache miss for POST by default)
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
POST /foo
hiya, china
--- response_headers
Content-Type: text/css
! Content-Length
--- response_body
world



=== TEST 5: basic fetch (POST cache hit if we enable POST explicitly)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        srcache_methods POST;

        proxy_pass http://agentzh.org:12345/;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
POST /foo
hiya, china
--- response_headers
Content-Type: text/css
Content-Length: 6
--- response_body
hello



=== TEST 6: basic fetch (GET still cache hit if we enable POST  and PUT explicitly)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        srcache_methods POST PUT;

        proxy_pass http://agentzh.org:12345/;
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



=== TEST 7: basic fetch (HEAD still cache hit if we enable POST explicitly)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        srcache_methods POST;

        proxy_pass http://agentzh.org:12345/;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
HEAD /foo
--- response_headers
Content-Type: text/css
Content-Length: 6
--- response_body
--- no_error_log
[error]



=== TEST 8: flush all
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



=== TEST 9: basic fetch (cache miss), POST not store
--- config
    location /foo {
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
POST /foo
howdy
--- response_headers
Content-Type: text/css
Content-Length:
--- response_body
hello



=== TEST 10: basic fetch (cache miss)
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



=== TEST 11: flush all
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



=== TEST 12: basic fetch (cache miss), POST stored when POST is enabled in srcache_methods
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        srcache_methods POST;

        echo hello;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
POST /foo
howdy
--- response_headers
Content-Type: text/css
Content-Length:
--- response_body
hello



=== TEST 13: basic fetch (cache hit)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        proxy_pass http://agentzh.org:12345/;
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



=== TEST 14: flush all
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



=== TEST 15: basic fetch (cache miss) - HEAD
--- config
    location /foo {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        proxy_pass http://127.0.0.1:$server_port/back;
    }

    location /back {
        return 200 "hello world";
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
HEAD /foo
--- response_headers
Content-Type: text/plain
Content-Length: 11
--- response_body
--- error_log
srcache_store skipped due to request method HEAD
--- no_error_log
[error]
--- log_level: debug
--- wait: 0.1

