# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (4 * blocks());

#no_long_string();
log_level 'warn';

run_tests();

#no_diff();

__DATA__

=== TEST 1: used
--- config
    location = /t {
        echo hello;
        xss_get on;
        xss_callback_arg c;
    }
--- more_headers
Accept-Encoding: gzip
--- request
    GET /t
--- stap
F(ngx_http_xss_header_filter) {
    println("xss header filter")
}
F(ngx_http_xss_body_filter) {
    println("xss body filter")
}

--- stap_out
xss header filter
xss body filter
xss body filter

--- response_body
hello
--- no_error_log
[error]



=== TEST 2: not used
--- config
    location = /t {
        echo hello;
    }
--- more_headers
Accept-Encoding: gzip
--- request
    GET /t
--- stap
F(ngx_http_xss_header_filter) {
    println("xss header filter")
}
F(ngx_http_xss_body_filter) {
    println("xss body filter")
}

--- stap_out
--- response_body
hello
--- no_error_log
[error]



=== TEST 3: used (multiple http {} blocks)
--- config
    location = /t {
        default_type application/json;
        xss_callback_arg 'callback';
        echo -n hello;
        xss_get on;
    }
--- more_headers
Accept-Encoding: gzip
--- request
    GET /t?callback=foo
--- stap
F(ngx_http_xss_header_filter) {
    println("xss header filter")
}
F(ngx_http_xss_body_filter) {
    println("xss body filter")
}

--- stap_out
xss header filter
xss body filter
xss body filter

--- post_main_config
    http {
    }

--- response_body chop
foo(hello);
--- no_error_log
[error]

