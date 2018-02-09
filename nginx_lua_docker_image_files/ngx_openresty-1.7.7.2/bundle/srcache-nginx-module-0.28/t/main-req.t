# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);

plan tests => repeat_each() * (5 * blocks() + 5);

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#master_on();
no_shuffle();

run_tests();

__DATA__

=== TEST 1: flush all (not using ngx_srcache)
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
        add_header X-Store-Status $srcache_store_status;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
X-Fetch-Status: BYPASS
X-Store-Status: BYPASS

--- stap
F(ngx_http_srcache_access_handler) {
    println("srcache access handler")
}

F(ngx_http_srcache_header_filter) {
    println("srcache header filter")
}

F(ngx_http_srcache_body_filter) {
    println("srcache body filter")
}

--- stap_out
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
        add_header X-Fetch-Status $srcache_fetch_status;
        add_header X-Store-Status $srcache_store_status;
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
X-Fetch-Status: MISS
X-Store-Status: STORE
--- response_body
hello
--- wait: 0.1
--- error_log
srcache_store: subrequest returned status 201
--- log_level: debug



=== TEST 3: basic fetch (cache hit)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        echo world;
        add_header X-Fetch-Status $srcache_fetch_status;
        add_header X-Store-Status $srcache_store_status;
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
X-Fetch-Status: HIT
X-Store-Status: BYPASS
--- response_body
hello



=== TEST 4: rewrite directives run before srcache directives
--- config
    location /foo {
        default_type text/css;
        set $key $uri;
        set $loc /memc;
        srcache_fetch GET $loc $key;
        srcache_store PUT $loc $key;

        echo world;
        add_header X-Fetch-Status $srcache_fetch_status;
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
X-Fetch-Status: HIT
--- response_body
hello

