# vi:filetype=

use lib 'lib';

use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (2 * blocks() + 2);

#$Test::Nginx::LWP::LogLevel = 'debug';

#no_diff();

run_tests();

__DATA__

=== TEST 1: sanity
--- config
    location /main {
        echo_location /sub;
    }
    location /sub {
        echo hello;
    }
--- request
    GET /main
--- response_body
hello



=== TEST 2: sanity with proxy in the middle
--- config
    location /main {
        echo_location /proxy;
    }
    location /proxy {
        proxy_pass $scheme://127.0.0.1:$server_port/sub;
    }
    location /sub {
        echo hello;
    }
--- request
    GET /main
--- response_body
hello



=== TEST 3: trailing echo
--- config
    location /main {
        echo_location /sub;
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



=== TEST 4: leading echo
--- config
    location /main {
        echo before subrequest;
        echo_location /sub;
    }
    location /sub {
        echo hello;
    }
--- request
    GET /main
--- response_body
before subrequest
hello



=== TEST 5: leading & trailing echo
--- config
    location /main {
        echo before subrequest;
        echo_location /sub;
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



=== TEST 6: multiple subrequests
--- config
    location /main {
        echo before sr 1;
        echo_location /sub;
        echo after sr 1;
        echo before sr 2;
        echo_location /sub;
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



=== TEST 7: timed multiple subrequests (blocking sleep)
--- config
    location /main {
        echo_reset_timer;
        echo_location /sub1;
        echo_location /sub2;
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
took 0\.0(?:2[5-9]|3[0-5]) sec for total\.$



=== TEST 8: timed multiple subrequests (non-blocking sleep)
--- config
    location /main {
        echo_reset_timer;
        echo_location /sub1;
        echo_location /sub2;
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
took 0\.0(?:2[5-9]|3[0-6]) sec for total\.$



=== TEST 9: location with args
--- config
    location /main {
        echo_location /sub 'foo=Foo&bar=Bar';
    }
    location /sub {
        echo $arg_foo $arg_bar;
    }
--- request
    GET /main
--- response_body
Foo Bar



=== TEST 10: chained subrequests
--- config
    location /main {
        echo 'pre main';
        echo_location /sub;
        echo 'post main';
    }

    location /sub {
        echo 'pre sub';
        echo_location /subsub;
        echo 'post sub';
    }

    location /subsub {
        echo 'subsub';
    }
--- request
    GET /main
--- response_body
pre main
pre sub
subsub
post sub
post main



=== TEST 11: chained subrequests using named locations
as of 0.8.20, ngx_http_subrequest still does not support
named location. sigh. this case is a TODO.
--- config
    location /main {
        echo 'pre main';
        echo_location @sub;
        echo 'post main';
    }

    location @sub {
        echo 'pre sub';
        echo_location @subsub;
        echo 'post sub';
    }

    location @subsub {
        echo 'subsub';
    }
--- request
    GET /main
--- response_body
pre main
pre sub
subsub
post sub
post main
--- SKIP



=== TEST 12: explicit flush in main request
--- config
    location /main {
        echo 'pre main';
        echo_location /sub;
        echo 'post main';
        echo_flush;
    }

    location /sub {
        echo_sleep 0.02;
        echo 'sub';
    }
--- request
    GET /main
--- response_body
pre main
sub
post main



=== TEST 13: no varaiable inheritance
--- config
    location /main {
        echo $echo_cacheable_request_uri;
        echo_location /sub;
        echo_location /sub2;
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



=== TEST 14: unsafe uri
--- config
    location /unsafe {
        echo_location '/../foo';
    }
--- request
    GET /unsafe
--- ignore_response
--- error_log
echo_location sees unsafe uri: "/../foo"
--- no_error_log
[error]
[alert]



=== TEST 15: querystring in url
--- config
    location /main {
        echo_location /sub?foo=Foo&bar=Bar;
    }
    location /sub {
        echo $arg_foo $arg_bar;
    }
--- request
    GET /main
--- response_body
Foo Bar



=== TEST 16: querystring in url *AND* an explicit querystring
--- config
    location /main {
        echo_location /sub?foo=Foo&bar=Bar blah=Blah;
    }
    location /sub {
        echo $arg_foo $arg_bar $arg_blah;
    }
--- request
    GET /main
--- response_body
  Blah



=== TEST 17: let subrequest to read the main request's request body
--- SKIP
--- config
    location /main {
        echo_location /sub;
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



=== TEST 18: sleep after location
--- config
    location /main {
        echo_location /sub;
        echo_sleep 0.001;
        echo_location /sub;
    }
    location /sub {
        echo_sleep 0.001;
        echo sub;
    }
--- request
    GET /main
--- response_body
sub
sub
--- skip_nginx: 2: < 0.8.11



=== TEST 19: deep nested echo_location/echo_location_async
--- config
    location /main {
        echo_location /bar;
        echo_location_async /bar;
        echo_location_async /bar;
        echo_location /group;
        echo_location_async /group;
    }

    location /group {
        echo_location /bar;
        echo_location_async /bar;
    }

    location /bar {
        #echo_sleep 0.001;
        echo $echo_incr;
    }
--- request
GET /main
--- response_body
1
2
3
4
5
6
7
--- timeout: 2



=== TEST 20: deep nested echo_location/echo_location_async (with sleep)
--- config
    location /main {
        echo_location /bar;
        echo_location_async /bar;
        echo_location_async /bar;
        echo_location /group;
        echo_location_async /group;
    }

    location /group {
        echo_location /baz;
        echo_location_async /bah;
    }

    location ~ '^/ba[rzh]' {
        echo_sleep 0.001;
        echo $echo_incr;
    }
--- request
GET /main
--- response_body
1
2
3
4
5
6
7
--- timeout: 2



=== TEST 21: deep nested echo_location (with sleep)
--- config
    location /main {
        echo_location /bar;
        echo_location /bar;
        echo_location /bar;
        echo_location /group;
        echo_location /group;
    }

    location /group {
        echo_location /bar;
        echo_location /bar;
    }

    location /incr {
        echo_sleep 0.001;
        echo $echo_incr;
    }

    location /bar {
        proxy_pass $scheme://127.0.0.1:$server_port/incr;
    }
--- request
GET /main
--- response_body
1
1
1
1
1
1
1
--- timeout: 5
--- no_error_log
[error]



=== TEST 22: leading subrequest & echo_before_body
--- config
    location /main {
        echo_before_body hello;
        echo_location /foo;
    }
    location /foo {
        echo world;
    }
--- request
    GET /main
--- response_body
hello
world



=== TEST 23: leading subrequest & xss
--- config
    location /main {
        default_type 'application/json';
        xss_get on;
        xss_callback_arg c;
        echo_location /foo;
    }
    location /foo {
        echo -n world;
    }
--- request
    GET /main?c=hi
--- response_body chop
hi(world);



=== TEST 24: multiple leading subrequest & xss
--- config
    location /main {
        default_type 'application/json';
        xss_get on;
        xss_callback_arg c;
        echo_location /foo;
        echo_location /bar;
    }
    location /main2 {
        content_by_lua '
            local res = ngx.location.capture("/foo")
            local res2 = ngx.location.capture("/bar")
            ngx.say(res.body)
            ngx.say(res2.body)
        ';
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



=== TEST 25: sanity (HEAD)
--- config
    location /main {
        echo_location /sub;
        echo_location /sub;
    }
    location /sub {
        echo hello;
    }
--- request
    HEAD /main
--- response_body

