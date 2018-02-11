# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);

plan tests => repeat_each() * (3 * blocks());

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#master_on();
no_long_string();
no_shuffle();

run_tests();

__DATA__

=== TEST 1: module used
--- config
    location /foo {
        srcache_store PUT /store;
        echo hello;
    }

    location /store {
        echo stored;
    }
--- request
GET /foo
--- stap
F(ngx_http_srcache_header_filter) {
    println("srcache header filter called")
}

F(ngx_http_srcache_access_handler) {
    println("srcache access handler called")
}

--- stap_out
srcache access handler called
srcache access handler called
srcache header filter called
srcache header filter called
--- response_body
hello



=== TEST 2: module unused
--- config
    location /foo {
        #srcache_store PUT /store;
        echo hello;
    }

    location /store {
        echo stored;
    }
--- request
GET /foo
--- stap
F(ngx_http_srcache_header_filter) {
    println("srcache header filter called")
}

F(ngx_http_srcache_access_handler) {
    println("srcache access handler called")
}

--- stap_out
--- response_body
hello



=== TEST 3: module used (multiple http {} blocks)
--- config
    location /foo {
        srcache_store PUT /store;
        echo hello;
    }

    location /store {
        echo stored;
    }

--- post_main_config
    http {
    }

--- request
GET /foo
--- stap
F(ngx_http_srcache_header_filter) {
    println("srcache header filter called")
}

F(ngx_http_srcache_access_handler) {
    println("srcache access handler called")
}

--- stap_out
srcache access handler called
srcache access handler called
srcache header filter called
srcache header filter called
--- response_body
hello

