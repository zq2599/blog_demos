# vi:ft=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);

plan tests => repeat_each() * 5 * blocks();

$ENV{TEST_NGINX_REDIS_PORT} ||= 6379;

#master_on();
#no_long_string();
no_shuffle();

#log_level('warn');
run_tests();

__DATA__

=== TEST 1: flush all
--- config
    location /flush {
        redis2_query flushall;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- response_headers
Content-Type: text/plain
!Content-Length
--- request
GET /flush
--- response_body eval: "+OK\r\n"
--- no_error_log
[error]



=== TEST 2: basic fetch (cache miss)
--- timeout: 3
--- config
    location /foo {
        default_type text/css;

        set $key $uri;
        set_escape_uri $escaped_key $key;
        srcache_fetch GET /redis $key;
        srcache_store POST /redis2 key=$escaped_key&exptime=120;

        echo hello;
        echo hiya;
    }

    location = /redis {
        internal;

        set $redis_key $args;
        redis_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location = /redis2 {
        internal;

        set_unescape_uri $exptime $arg_exptime;
        set_unescape_uri $key $arg_key;

        redis2_query set $key $echo_request_body;
        redis2_query expire $key $exptime;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
!Content-Length
--- response_body
hello
hiya
--- no_error_log
[error]



=== TEST 3: basic fetch (cache hit)
--- config
    location /foo {
        default_type text/css;

        set $key $uri;
        srcache_fetch GET /redis $key;
        srcache_store PUT /redis2 key=$key&exptime=10;

        echo world;
    }

    location = /redis {
        #internal;

        #redis2_query get $args;
        #redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
        #echo "args = $args";

        set $redis_key $args;
        redis_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location = /redis2 {
        internal;

        set_unescape_uri $exptime $arg_exptime;
        set_unescape_uri $key $arg_key;

        redis2_query set $key $echo_request_body;
        redis2_query expire $key $exptime;

        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
Content-Length: 11
--- response_body
hello
hiya
--- no_error_log
[error]



=== TEST 4: flush all - cluster
--- config
    location /flush {
        redis2_query flushall;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- response_headers
Content-Type: text/plain
!Content-Length
--- request
GET /flush
--- response_body eval: "+OK\r\n"
--- no_error_log
[error]



=== TEST 5: basic fetch (cache miss) - cluster
--- timeout: 3
--- http_config
    upstream foo {
        server 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- config
    location /foo {
        default_type text/css;

        set $key $uri;
        set_escape_uri $escaped_key $key;
        #srcache_fetch GET /redis $key;
        srcache_store POST /redis2 key=$escaped_key&exptime=120;

        echo hello;
        echo hiya;
    }

    location = /redis {
        internal;

        set_md5 $redis_key $args;
        set $backend foo;
        redis_pass $backend;
    }

    location = /redis2 {
        internal;

        set_unescape_uri $exptime $arg_exptime;
        set_unescape_uri $key $arg_key;
        set_md5 $key;
        set $backend foo;

        redis2_query set $key $echo_request_body;
        redis2_query expire $key $exptime;
        redis2_pass $backend;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
!Content-Length
--- response_body
hello
hiya
--- no_error_log
[error]



=== TEST 6: basic fetch (cache hit) - cluster
--- http_config
    upstream foo {
        server 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

--- config
    location /foo {
        default_type text/css;

        set $key $uri;
        srcache_fetch GET /redis $key;
        srcache_store PUT /redis2 key=$key&exptime=10;

        echo world;
    }

    location = /redis {
        #internal;

        set_md5 $redis_key $args;
        set $backend foo;
        redis_pass $backend;
    }

    location = /redis2 {
        internal;

        set_unescape_uri $exptime $arg_exptime;
        set_unescape_uri $key $arg_key;
        set_md5 $key;

        redis2_query set $key $echo_request_body;
        redis2_query expire $key $exptime;

        set $backend foo;
        redis2_pass $backend;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
Content-Length: 11
--- response_body
hello
hiya
--- no_error_log
[error]

