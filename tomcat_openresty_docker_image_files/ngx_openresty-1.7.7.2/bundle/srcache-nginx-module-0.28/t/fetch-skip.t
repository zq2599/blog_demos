# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);

plan tests => repeat_each() * (3 * blocks() + 10);

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

no_shuffle();

run_tests();

__DATA__

=== TEST 1: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
    }
--- request
GET /flush
--- response_body eval: "OK\r\n"
--- response_headers
X-Fetch-Status: BYPASS



=== TEST 2: skip is false
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        set $skip '';
        srcache_fetch_skip $skip;

        echo hello;
        add_header X-Fetch-Status $srcache_fetch_status;
    }

    location /memc {
        internal;

        set $memc_cmd set;
        set $memc_key key;
        set $memc_value hello;
        set $memc_value 'hello';
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /foo
--- response_body
hello
--- response_headers
X-Fetch-Status: MISS



=== TEST 3: check if /memc was invoked (just equal)
--- config
     location /memc {
        set $memc_cmd get;
        set $memc_key key;
        set $memc_value hello;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
    }
--- request
    GET /memc
--- response_body chomp
hello
--- response_headers
X-Fetch-Status: BYPASS



=== TEST 4: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
    }
--- request
GET /flush
--- response_body eval: "OK\r\n"
--- response_headers
X-Fetch-Status: BYPASS



=== TEST 5: fetch_skip is literally false
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_fetch_skip 0;

        echo hello;
        add_header X-Fetch-Status $srcache_fetch_status;
    }

    location /memc {
        internal;

        set $memc_cmd set;
        set $memc_key key;
        set $memc_value hello;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /foo
--- response_body
hello
--- response_headers
X-Fetch-Status: MISS



=== TEST 6: check if /memc was invoked (less than)
--- config
     location /memc {
        set $memc_cmd get;
        set $memc_key key;
        set $memc_value hello;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
    }
--- request
    GET /memc
--- response_body chomp
hello
--- response_headers
X-Fetch-Status: BYPASS



=== TEST 7: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
    }
--- request
GET /flush
--- response_body eval: "OK\r\n"
--- response_headers
X-Fetch-Status: BYPASS



=== TEST 8: fetch_skip is true
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_fetch_skip 1;
        add_header X-Fetch-Status $srcache_fetch_status;

        echo hello;
    }

    location /memc {
        internal;

        set $memc_cmd set;
        set $memc_key key;
        set $memc_value hello;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /foo
--- response_body
hello
--- response_headers
X-Fetch-Status: BYPASS



=== TEST 9: check if /memc was invoked (more than)
--- config
     location /memc {
        set $memc_cmd get;
        set $memc_key key;
        set $memc_value hello;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
    }
--- request
    GET /memc
--- response_body_like: 404 Not Found
--- error_code: 404
--- response_headers
!X-Fetch-Status



=== TEST 10: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
    }
--- request
GET /flush
--- response_body eval: "OK\r\n"
--- response_headers
X-Fetch-Status: BYPASS



=== TEST 11: explicit "true" string
--- config
    location /foo.txt {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_fetch_skip true;
        add_header X-Fetch-Status $srcache_fetch_status;
    }

    location /memc {
        internal;

        set $memc_cmd set;
        set $memc_key key;
        set $memc_value hello;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- user_files
>>> foo.txt
hello
--- request
    GET /foo.txt
--- response_body
hello
--- response_headers
X-Fetch-Status: BYPASS



=== TEST 12: check if /memc was invoked (explicit "true" string)
--- config
     location /memc {
        set $memc_cmd get;
        set $memc_key key;
        set $memc_value hello;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
    }
--- request
    GET /memc
--- response_body_like: 404 Not Found
--- error_code: 404
--- response_headers
!X-Fetch-Status



=== TEST 13: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
    }
--- request
GET /flush
--- response_body eval: "OK\r\n"
--- response_headers
X-Fetch-Status: BYPASS



=== TEST 14: server-side config
--- config
    srcache_fetch_skip 1;
    location /foo.txt {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        add_header X-Fetch-Status $srcache_fetch_status;
    }

    location /memc {
        internal;

        set $memc_cmd set;
        set $memc_key key;
        set $memc_value hello;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- user_files
>>> foo.txt
abc
--- request
    GET /foo.txt
--- response_body
abc
--- response_headers
X-Fetch-Status: BYPASS



=== TEST 15: check if /memc was invoked (server-level config)
--- config
     location /memc {
        set $memc_cmd get;
        set $memc_key key;
        set $memc_value hello;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
    }
--- request
    GET /memc
--- response_body_like: 404 Not Found
--- error_code: 404
--- response_headers
!X-Fetch-Status



=== TEST 16: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
    }
--- request
GET /flush
--- response_body eval: "OK\r\n"
--- response_headers
X-Fetch-Status: BYPASS



=== TEST 17: overriding server-side config
--- config
    srcache_fetch_skip 1;
    location /foo.txt {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_fetch_skip 0;
        add_header X-Fetch-Status $srcache_fetch_status;
    }

    location /memc {
        internal;

        set $memc_cmd set;
        set $memc_key key;
        set $memc_value hello;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- user_files
>>> foo.txt
hello, world
--- request
    GET /foo.txt
--- response_body
hello, world
--- response_headers
X-Fetch-Status: MISS



=== TEST 18: check if /memc was invoked (overriding server config)
--- config
     location /memc {
        set $memc_cmd get;
        set $memc_key key;
        set $memc_value hello;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
    }
--- request
    GET /memc
--- response_body chomp
hello
--- response_headers
X-Fetch-Status: BYPASS



=== TEST 19: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
X-Fetch-Status: BYPASS
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 20: basic fetch (cache miss)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        add_header X-Fetch-Status $srcache_fetch_status;

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
X-Fetch-Status: MISS
--- response_body
hello



=== TEST 21: basic fetch (cache hit)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        add_header X-Fetch-Status $srcache_fetch_status;

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
X-Fetch-Status: HIT
--- response_body
hello



=== TEST 22: fetch skip true
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        srcache_fetch_skip 1;

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
--- more_headers
cache-control: No-Cache
--- response_headers
Content-Type: text/css
Content-Length: 
X-Fetch-Status: BYPASS
--- response_body
world



=== TEST 23: basic fetch (cache hit again)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

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
world

