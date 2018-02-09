# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

plan tests => repeat_each() * 92;

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

no_long_string();

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
!Foo-Bar
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 2: basic fetch (http 1.0)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        content_by_lua 'ngx.say("hello")';
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo HTTP/1.0
--- response_headers
Content-Type: text/css
Content-Length: 6
--- response_body
hello



=== TEST 3: inspect the cached item
--- config
    location /memc {
        set $memc_key "/foo";
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /memc
--- response_headers
Content-Type: text/plain
Content-Length: 49
!Set-Cookie
!Proxy-Authenticate
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
--- response_headers
Content-Type: text/plain
Content-Length: 4
!Foo-Bar
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 5: basic fetch (cache 500 404 200 statuses)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        srcache_store_statuses 500 200 404;

        content_by_lua '
            ngx.exit(404)
        ';
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo HTTP/1.0
--- response_headers
Content-Type: text/html
--- response_body_like: 404 Not Found
--- error_code: 404



=== TEST 6: inspect the cached item
--- config
    location /memc {
        set $memc_key "/foo";
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /memc
--- response_headers
Content-Type: text/plain
--- response_body_like
^HTTP/1\.1 404 Not Found\r
Content-Type: text/html\r
\r
.*?404 Not Found.*



=== TEST 7: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
!Foo-Bar
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 8: basic fetch (404 not listed in store_statuses)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        srcache_store_statuses 500 200 410;

        content_by_lua '
            ngx.exit(404)
        ';
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo HTTP/1.0
--- response_headers
Content-Type: text/html
--- response_body_like: 404 Not Found
--- error_code: 404



=== TEST 9: inspect the cached item
--- config
    location /memc {
        set $memc_key "/foo";
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /memc
--- response_headers
Content-Type: text/html
--- response_body_like: 404 Not Found
--- error_code: 404



=== TEST 10: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
!Foo-Bar
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 11: basic fetch (cache 301 by default)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        content_by_lua '
            ngx.redirect("/bah", 301)
        ';
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo HTTP/1.0
--- response_headers
Content-Type: text/html
--- response_body_like: 301 Moved Permanently
--- error_code: 301



=== TEST 12: inspect the cached item
--- config
    location /memc {
        set $memc_key "/foo";
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /memc
--- response_headers
Content-Type: text/html
--- response_headers
Content-Type: text/plain
--- response_body_like
^HTTP/1\.1 301 Moved Permanently\r
Content-Type: text/html\r
Location: /bah\r
\r
.*?301 Moved Permanently.*



=== TEST 13: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
!Foo-Bar
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 14: basic fetch (cache 302 by default)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        content_by_lua '
            ngx.redirect("/bah", 302)
        ';
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo HTTP/1.0
--- response_headers
Content-Type: text/html
--- response_body_like: 302 Found
--- error_code: 302



=== TEST 15: inspect the cached item
--- config
    location /memc {
        set $memc_key "/foo";
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /memc
--- response_headers
Content-Type: text/html
--- response_headers
Content-Type: text/plain
--- response_body_like
^HTTP/1\.1 302 Moved Temporarily\r
Content-Type: text/html\r
Location: /bah\r
\r
.*?302 Found.*



=== TEST 16: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
!Foo-Bar
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 17: basic fetch (201 not cached by default)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        content_by_lua '
            ngx.status = 201
            ngx.say("Dog created")
        ';
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo HTTP/1.0
--- response_headers
Content-Type: text/css
--- response_body
Dog created
--- error_code: 201



=== TEST 18: inspect the cached item
--- config
    location /memc {
        set $memc_key "/foo";
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /memc
--- response_headers
Content-Type: text/html
--- response_headers
Content-Type: text/html
--- response_body_like: 404 Not Found
--- error_code: 404



=== TEST 19: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
!Foo-Bar
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 20: basic fetch (explicitly do not cache 302)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        srcache_store_statuses 301 200;

        content_by_lua '
            ngx.redirect("/bah", 302)
        ';
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo HTTP/1.0
--- response_headers
Content-Type: text/html
--- response_body_like: 302 Found
--- error_code: 302



=== TEST 21: inspect the cached item
--- config
    location /memc {
        set $memc_key "/foo";
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /memc
--- response_headers
Content-Type: text/html
--- response_headers
Content-Type: text/html
--- response_body_like: 404 Not Found
--- error_code: 404



=== TEST 22: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
!Foo-Bar
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 23: basic fetch (explicitly do not cache 302, and store_statuses are all bigger than 302)
github pull #19
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        srcache_store_statuses 303 304;

        content_by_lua '
            ngx.redirect("/bah", 302)
        ';
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo HTTP/1.0
--- response_headers
Content-Type: text/html
--- response_body_like: 302 Found
--- error_code: 302



=== TEST 24: inspect the cached item
--- config
    location /memc {
        set $memc_key "/foo";
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /memc
--- response_headers
Content-Type: text/html
--- response_headers
Content-Type: text/html
--- response_body_like: 404 Not Found
--- error_code: 404

