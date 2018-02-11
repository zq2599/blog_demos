# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);

plan tests => repeat_each() * (5 * blocks() + 1);

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
        add_header X-Fetch-Status $srcache_fetch_status;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
X-Fetch-Status: BYPASS
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 2: set key in memcached (good content)
--- config
    location /memc {
        set $memc_key '/foo';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 8
!X-Fetch-Status
--- request eval
"PUT /memc
HTTP/1.1 200 OK\r
Content-Type: foo/bar\r
Foo: Bar\r
\r
hello
"
--- response_body eval: "STORED\r\n"
--- error_code: 201



=== TEST 3: basic fetch (cache hit)
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
Content-Type: foo/bar
Content-Length: 5
Foo: Bar
X-Fetch-Status: HIT
--- response_body chop
hello



=== TEST 4: set key in memcached (bad content, syntax error in status line)
--- config
    location /memc {
        set $memc_key '/foo';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 8
!X-Fetch-Status
--- request eval
"PUT /memc
HTTP 200 OK\r
Content-Type: foo/bar\r
Foo: Bar\r
\r
hello
"
--- response_body eval: "STORED\r\n"
--- error_code: 201



=== TEST 5: basic fetch (cache hit, but found syntax error in status line)
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
Content-Length: 
X-Fetch-Status: MISS
--- response_body
world



=== TEST 6: set key in memcached (bad content, unexpected eof in status line)
--- config
    location /memc {
        set $memc_key '/foo';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 8
!X-Fetch-Status
--- request eval
"PUT /memc
HTTP/1.1 200"
--- response_body eval: "STORED\r\n"
--- error_code: 201



=== TEST 7: basic fetch (cache hit, but found unexpected eof in status line)
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
Content-Length: 
X-Fetch-Status: MISS
--- response_body
world



=== TEST 8: set key in memcached (bad content, unexpected eof in header)
--- config
    location /memc {
        set $memc_key '/foo';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 8
!X-Fetch-Status
--- request eval
"PUT /memc
HTTP/1.1 200 OK\r
Content-Ty"
--- response_body eval: "STORED\r\n"
--- error_code: 201



=== TEST 9: basic fetch (cache hit, but found unexpected eof in status line)
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
Content-Length: 
X-Fetch-Status: MISS
--- response_body
world

