# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);

plan tests => repeat_each() * (5 * blocks() + 4);

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

--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 2: basic fetch (cache miss)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        content_by_lua '
            ngx.header["ETag"] = [["abcd1234"]]
            ngx.header["Last-Modified"] = "Sat, 01 Mar 2014 01:02:38 GMT"
            ngx.say("hello")
        ';
        #echo hello;
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
Last-Modified: Sat, 01 Mar 2014 01:02:38 GMT
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
--- more_headers
If-None-Match: "abcd1234"
--- response_headers
X-Fetch-Status: HIT
X-Store-Status: BYPASS
ETag: "abcd1234"
Last-Modified: Sat, 01 Mar 2014 01:02:38 GMT
--- response_body:
--- error_code: 304



=== TEST 4: flush all (not using ngx_srcache)
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

--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 5: basic fetch (cache miss, 304)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        content_by_lua '
            ngx.header["ETag"] = [["abcd1234"]]
            ngx.header["Last-Modified"] = "Sat, 01 Mar 2014 01:02:38 GMT"
            ngx.say("hello")
        ';
        #echo hello;
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
--- more_headers
If-None-Match: "abcd1234"
--- response_headers
Content-Length:
X-Fetch-Status: MISS
X-Store-Status: BYPASS
Last-Modified: Sat, 01 Mar 2014 01:02:38 GMT
--- response_body:
--- error_code: 304
--- wait: 0.1
--- error_log
--- log_level: debug



=== TEST 6: basic fetch (cache hit)
--- config
    location /memc {
        #internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /memc?/foo
--- response_body_like: 404 Not Found
--- error_code: 404

