# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (2 * blocks() + 1);

run_tests();

__DATA__

=== TEST 1: sanity
--- config
    location /main {
        echo_location_async /sub;
    }
    location /sub {
        echo hello;
    }
--- request
    GET /main
--- response_body
hello



=== TEST 2: trailing echo
--- config
    location /main {
        echo_location_async /sub;
        echo after subrequest;
    }
    location /sub {
        echo hello;
    }
--- request
    GET /main
--- response_body
hello
after subrequest



=== TEST 3: leading echo
--- config
    location /main {
        echo before subrequest;
        echo_location_async /sub;
    }
    location /sub {
        echo hello;
    }
--- request
    GET /main
--- response_body
before subrequest
hello



=== TEST 4: leading & trailing echo
--- config
    location /main {
        echo before subrequest;
        echo_location_async /sub;
        echo after subrequest;
    }
    location /sub {
        echo hello;
    }
--- request
    GET /main
--- response_body
before subrequest
hello
after subrequest



=== TEST 5: multiple subrequests
--- config
    location /main {
        echo before sr 1;
        echo_location_async /sub;
        echo after sr 1;
        echo before sr 2;
        echo_location_async /sub;
        echo after sr 2;
    }
    location /sub {
        echo hello;
    }
--- request
    GET /main
--- response_body
before sr 1
hello
after sr 1
before sr 2
hello
after sr 2



=== TEST 6: timed multiple subrequests (blocking sleep)
--- config
    location /main {
        echo_reset_timer;
        echo_location_async /sub1;
        echo_location_async /sub2;
        echo "took $echo_timer_elapsed sec for total.";
    }
    location /sub1 {
        echo_blocking_sleep 0.02;
        echo hello;
    }
    location /sub2 {
        echo_blocking_sleep 0.01;
        echo world;
    }

--- request
    GET /main
--- response_body_like
^hello
world
took 0\.00[0-5] sec for total\.$



=== TEST 7: timed multiple subrequests (non-blocking sleep)
--- config
    location /main {
        echo_reset_timer;
        echo_location_async /sub1;
        echo_location_async /sub2;
        echo "took $echo_timer_elapsed sec for total.";
    }
    location /sub1 {
        echo_sleep 0.02;
        echo hello;
    }
    location /sub2 {
        echo_sleep 0.01;
        echo world;
    }

--- request
    GET /main
--- response_body_like
^hello
world
took 0\.00[0-5] sec for total\.$



=== TEST 8: location with args
--- config
    location /main {
        echo_location_async /sub 'foo=Foo&bar=Bar';
    }
    location /sub {
        echo $arg_foo $arg_bar;
    }
--- request
    GET /main
--- response_body
Foo Bar



=== TEST 9: encoded chars in query strings
--- config
    location /main {
        echo_location_async /sub 'foo=a%20b&bar=Bar';
    }
    location /sub {
        echo $arg_foo $arg_bar;
    }
--- request
    GET /main
--- response_body
a%20b Bar



=== TEST 10: UTF-8 chars in query strings
--- config
    location /main {
        echo_location_async /sub 'foo=你好';
    }
    location /sub {
        echo $arg_foo;
    }
--- request
    GET /main
--- response_body
你好



=== TEST 11: encoded chars in location url
--- config
    location /main {
        echo_location_async /sub%31 'foo=Foo&bar=Bar';
    }
    location /sub%31 {
        echo 'sub%31';
    }
    location /sub1 {
        echo 'sub1';
    }
--- request
    GET /main
--- response_body
sub1



=== TEST 12: querystring in url
--- config
    location /main {
        echo_location_async /sub?foo=Foo&bar=Bar;
    }
    location /sub {
        echo $arg_foo $arg_bar;
    }
--- request
    GET /main
--- response_body
Foo Bar



=== TEST 13: querystring in url *AND* an explicit querystring
--- config
    location /main {
        echo_location_async /sub?foo=Foo&bar=Bar blah=Blah;
    }
    location /sub {
        echo $arg_foo $arg_bar $arg_blah;
    }
--- request
    GET /main
--- response_body
  Blah



=== TEST 14: explicit flush in main request
flush won't really flush the buffer...
--- config
    location /main_flush {
        echo 'pre main';
        echo_location_async /sub;
        echo 'post main';
        echo_flush;
    }

    location /sub {
        echo_sleep 0.02;
        echo 'sub';
    }
--- request
    GET /main_flush
--- response_body
pre main
sub
post main



=== TEST 15: no varaiable inheritance
--- config
    location /main {
        echo $echo_cacheable_request_uri;
        echo_location_async /sub;
        echo_location_async /sub2;
    }
    location /sub {
        echo $echo_cacheable_request_uri;
    }
    location /sub2 {
        echo $echo_cacheable_request_uri;
    }

--- request
    GET /main
--- response_body
/main
/sub
/sub2



=== TEST 16: unsafe uri
--- config
    location /unsafe {
        echo_location_async '/../foo';
    }
--- request
    GET /unsafe
--- stap2
F(ngx_http_send_header) {
    printf("send header on req %p (header sent: %d)\n", $r, $r->header_sent)
    print_ubacktrace()
}
--- ignore_response
--- error_log
echo_location_async sees unsafe uri: "/../foo"
--- no_error_log
[error]
[alert]



=== TEST 17: access/deny (access phase handlers skipped in subrequests)
--- config
    location /main {
        echo_location_async /denied;
    }
    location /denied {
        deny all;
        echo No no no;
    }
--- request
    GET /main
--- error_code: 200
--- response_body
No no no



=== TEST 18: rewrite is honored.
--- config
    location /main {
        echo_location_async /rewrite;
    }
    location /rewrite {
        rewrite ^ /foo break;
        echo $uri;
    }
--- request
    GET /main
--- response_body
/foo



=== TEST 19: let subrequest to read the main request's request body
--- SKIP
--- config
    location /main {
        echo_location_async /sub;
    }
    location /sub {
        echo_read_request_body;
        echo_request_body;
    }
--- request
POST /main
hello, body!
--- response_body chomp
hello, body!



=== TEST 20: leading subrequest & echo_before_body
--- config
    location /main {
        echo_before_body hello;
        echo_location_async /foo;
    }
    location /foo {
        echo world;
    }
--- request
    GET /main
--- response_body
hello
world



=== TEST 21: leading subrequest & xss
--- config
    location /main {
        default_type 'application/json';
        xss_get on;
        xss_callback_arg c;
        echo_location_async /foo;
    }
    location /foo {
        echo -n world;
    }
--- request
    GET /main?c=hi
--- response_body chop
hi(world);



=== TEST 22: multiple leading subrequest & xss
--- config
    location /main {
        default_type 'application/json';
        xss_get on;
        xss_callback_arg c;
        echo_location_async /foo;
        echo_location_async /bar;
    }
    location /foo {
        echo -n world;
    }
    location /bar {
        echo -n ' people';
    }
--- request
    GET /main?c=hi
--- response_body chop
hi(world people);



=== TEST 23: sanity (HEAD)
--- config
    location /main {
        echo_location_async /sub;
        echo_location_async /sub;
    }
    location /sub {
        echo hello;
    }
--- request
    HEAD /main
--- response_body

