# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);

plan tests => repeat_each() * (2 * blocks() + 5);

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



=== TEST 2: just hit store_max_size
--- config
    location /foo {
        default_type text/css;
        srcache_store PUT /memc $uri;
        srcache_store_max_size 49;

        echo hello;
        add_header X-Store-Status $srcache_store_status;
    }

    location /memc {
        internal;

        set $memc_cmd set;
        set $memc_key hit_store_max_size;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /foo
--- response_body
hello
--- response_headers
X-Store-Status: STORE



=== TEST 3: check if /memc was invoked (just equal)
--- config
     location /memc {
        set $memc_cmd get;
        set $memc_key hit_store_max_size;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /memc
--- response_body eval
"HTTP/1.1 200 OK\r
Content-Type: text/css\r
\r
hello
"



=== TEST 4: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 5: less than store_max_size
--- config
    location /foo {
        default_type text/css;
        srcache_store PUT /memc $uri;
        srcache_store_max_size 50;

        echo hello;
        add_header X-Store-Status $srcache_store_status;
    }

    location /memc {
        internal;

        set $memc_cmd set;
        set $memc_key less_than_store_max_size;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /foo
--- response_body
hello
--- response_headers
X-Store-Status: STORE



=== TEST 6: check if /memc was invoked (less than)
--- config
     location /memc {
        set $memc_cmd get;
        set $memc_key less_than_store_max_size;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /memc
--- response_body eval
"HTTP/1.1 200 OK\r
Content-Type: text/css\r
\r
hello
"



=== TEST 7: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 8: just more than store_max_size
--- config
    location /foo {
        default_type text/css;
        srcache_store PUT /memc $uri;
        srcache_store_max_size 48;

        echo hello;
        add_header X-Store-Status $srcache_store_status;
        log_by_lua 'ngx.log(ngx.WARN, "store status: ", ngx.var.srcache_store_status)';
    }

    location /memc {
        internal;

        set $memc_cmd set;
        set $memc_key more_than_store_max_size;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /foo
--- response_body
hello
--- wait: 0.1
--- response_headers
X-Store-Status: STORE
--- error_log
store status: BYPASS



=== TEST 9: check if /memc was invoked (more than)
--- config
     location /memc {
        set $memc_cmd get;
        set $memc_key more_than_store_max_size;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /memc
--- response_body_like: 404 Not Found
--- error_code: 404



=== TEST 10: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 11: just more than store_max_size (explicit content-length)
--- config
    location /foo.txt {
        default_type text/css;
        srcache_store PUT /memc $uri;
        srcache_store_max_size 48;

        content_by_lua '
            ngx.header.content_length = 40;
            ngx.say("hello")
        ';
        add_header X-Store-Status $srcache_store_status;
    }

    location /memc {
        internal;

        set $memc_cmd set;
        set $memc_key more_than_store_max_size;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /foo.txt
--- response_body
hello
--- response_headers
X-Store-Status: BYPASS



=== TEST 12: check if /memc was invoked (more than)
--- config
     location /memc {
        set $memc_cmd get;
        set $memc_key more_than_store_max_size;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /memc
--- response_body_like: 404 Not Found
--- error_code: 404



=== TEST 13: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 14: server-side config
--- config
    srcache_store_max_size 46;
    location /foo.txt {
        default_type text/css;
        srcache_store PUT /memc $uri;
    }

    location /memc {
        internal;

        set $memc_cmd set;
        set $memc_key more_than_store_max_size;
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



=== TEST 15: check if /memc was invoked (server-level config)
--- config
     location /memc {
        set $memc_cmd get;
        set $memc_key more_than_store_max_size;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /memc
--- response_body_like: 404 Not Found
--- error_code: 404



=== TEST 16: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 17: 0 means unlimited
--- config
    srcache_store_max_size 3;
    location /foo.txt {
        default_type text/css;
        srcache_store PUT /memc $uri;
        srcache_store_max_size 0;
    }

    location /memc {
        internal;

        set $memc_cmd set;
        set $memc_key more_than_store_max_size;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- user_files
>>> foo.txt 199801171935.33
hello, world
--- request
    GET /foo.txt
--- response_body
hello, world



=== TEST 18: check if /memc was invoked (explicit unlimited)
--- config
     location /memc {
        set $memc_cmd get;
        set $memc_key more_than_store_max_size;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /memc
--- response_body_like eval
qr{^HTTP/1.1 200 OK\r
Content-Type: text/css\r
Last-Modified: Sat, 17 Jan 1998 19:35:33 GMT\r
X-SRCache-Allow-Ranges: 1\r
(?:ETag: "[^"]+"\r
)?\r
hello, world$}

