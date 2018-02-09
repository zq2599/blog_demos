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



=== TEST 2: basic fetch (Set-Cookie and Proxy-Authenticate hide by default)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        content_by_lua '
            ngx.header["Set-Cookie"] = "foo=baz"
            ngx.header["Proxy-Authenticate"] = "blah"
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
Content-Type: text/css
Content-Length:
Set-Cookie: foo=baz
Proxy-Authenticate: blah
--- response_body
hello



=== TEST 3: basic fetch (cache hit)
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
!Set-Cookie
!Proxy-Authenticate
--- response_body
hello



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



=== TEST 5: basic fetch (Set-Cookie hide by default)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        srcache_store_pass_header Set-Cookie;
        srcache_store_pass_header Proxy-Authenticate;

        content_by_lua '
            ngx.header["Set-Cookie"] = "foo=baz"
            ngx.header["Proxy-Authenticate"] = "blah"
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
Content-Type: text/css
Content-Length:
Set-Cookie: foo=baz
Proxy-Authenticate: blah
--- response_body
hello



=== TEST 6: basic fetch (cache hit)
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
Set-Cookie: foo=baz
Proxy-Authenticate: blah
--- response_body
hello

