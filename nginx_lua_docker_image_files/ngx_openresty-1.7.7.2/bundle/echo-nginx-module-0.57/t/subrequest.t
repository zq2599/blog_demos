# vi:filetype=

use lib 'lib';

use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (2 * blocks() + 1);

$ENV{TEST_NGINX_HTML_DIR} = html_dir;
$ENV{TEST_NGINX_CLIENT_PORT} ||= server_port();

run_tests();

__DATA__

=== TEST 1: sanity
--- config
    location /main {
        echo_subrequest GET /sub;
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
        echo_subrequest GET /sub;
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
        echo_subrequest GET /sub;
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
        echo_subrequest GET /sub;
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
        echo_subrequest GET /sub;
        echo after sr 1;
        echo before sr 2;
        echo_subrequest GET /sub;
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
        echo_subrequest GET /sub1;
        echo_subrequest GET /sub2;
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



=== TEST 7: timed multiple subrequests (non-blocking sleep)
--- config
    location /main {
        echo_reset_timer;
        echo_subrequest GET /sub1;
        echo_subrequest GET /sub2;
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



=== TEST 8: location with args
--- config
    location /main {
        echo_subrequest GET /sub -q 'foo=Foo&bar=Bar';
    }
    location /sub {
        echo $arg_foo $arg_bar;
    }
--- request
    GET /main
--- response_body
Foo Bar



=== TEST 9: chained subrequests
--- config
    location /main {
        echo 'pre main';
        echo_subrequest GET /sub;
        echo 'post main';
    }

    location /sub {
        echo 'pre sub';
        echo_subrequest GET /subsub;
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



=== TEST 10: chained subrequests using named locations
as of 0.8.20, ngx_http_subrequest still does not support
named location. sigh. this case is a TODO.
--- config
    location /main {
        echo 'pre main';
        echo_subrequest GET @sub;
        echo 'post main';
    }

    location @sub {
        echo 'pre sub';
        echo_subrequest GET @subsub;
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



=== TEST 11: explicit flush in main request
--- config
    location /main {
        echo 'pre main';
        echo_subrequest GET /sub;
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



=== TEST 12: DELETE subrequest
--- config
    location /main {
        echo_subrequest DELETE /sub;
    }
    location /sub {
        echo "sub method: $echo_request_method";
        echo "main method: $echo_client_request_method";
    }
--- request
    GET /main
--- response_body
sub method: DELETE
main method: GET



=== TEST 13: DELETE subrequest
--- config
    location /main {
        echo "main method: $echo_client_request_method";
        echo_subrequest GET /proxy;
        echo_subrequest DELETE /proxy;
    }
    location /proxy {
        proxy_pass $scheme://127.0.0.1:$server_port/sub;
    }
    location /sub {
        echo "sub method: $echo_request_method";
    }
--- request
    GET /main
--- response_body
main method: GET
sub method: GET
sub method: DELETE



=== TEST 14: POST subrequest with body
--- config
    location /main {
        echo_subrequest POST /sub -b 'hello, world';
    }
    location /sub {
        echo "sub method: $echo_request_method";
        # we don't need to call echo_read_client_body explicitly here
        echo "sub body: $echo_request_body";
    }
--- request
    GET /main
--- response_body
sub method: POST
sub body: hello, world



=== TEST 15: POST subrequest with body (explicitly read the body)
--- config
    location /main {
        echo_subrequest POST /sub -b 'hello, world';
    }
    location /sub {
        echo "sub method: $echo_request_method";
        # we call echo_read_client_body explicitly here even
        #   though it's not necessary.
        echo_read_request_body;
        echo "sub body: $echo_request_body";
    }
--- request
    GET /main
--- response_body
sub method: POST
sub body: hello, world



=== TEST 16: POST subrequest with body (with proxy in the middle) and without read body explicitly
--- config
    location /main {
        echo_subrequest POST /proxy -b 'hello, world';
    }
    location /proxy {
        proxy_pass $scheme://127.0.0.1:$server_port/sub;
    }
    location /sub {
        echo "sub method: $echo_request_method.";
        # we need to read body explicitly here...or $echo_request_body
        #   will evaluate to empty ("")
        echo "sub body: $echo_request_body.";
    }
--- request
    GET /main
--- response_body
sub method: POST.
sub body: .



=== TEST 17: POST subrequest with body (with proxy in the middle) and read body explicitly
--- config
    location /main {
        echo_subrequest POST /proxy -b 'hello, world';
    }
    location /proxy {
        proxy_pass $scheme://127.0.0.1:$server_port/sub;
    }
    location /sub {
        echo "sub method: $echo_request_method.";
        # we need to read body explicitly here...or $echo_request_body
        #   will evaluate to empty ("")
        echo_read_request_body;
        echo "sub body: $echo_request_body.";
    }
--- request
    GET /main
--- response_body
sub method: POST.
sub body: hello, world.



=== TEST 18: multiple subrequests
--- config
    location /multi {
        echo_subrequest POST '/sub' -q 'foo=Foo' -b 'hi';
        echo_subrequest PUT '/sub' -q 'bar=Bar' -b 'hello';
    }
    location /sub {
        echo "querystring: $query_string";
        echo "method: $echo_request_method";
        echo "body: $echo_request_body";
        echo "content length: $http_content_length";
        echo '///';
    }
--- request
    GET /multi
--- response_body
querystring: foo=Foo
method: POST
body: hi
content length: 2
///
querystring: bar=Bar
method: PUT
body: hello
content length: 5
///



=== TEST 19: unsafe uri
--- config
    location /unsafe {
        echo_subrequest GET '/../foo';
    }
--- request
    GET /unsafe
--- ignore_response
--- error_log
echo_subrequest sees unsafe uri: "/../foo"
--- no_error_log
[error]



=== TEST 20: querystring in url
--- config
    location /main {
        echo_subrequest GET /sub?foo=Foo&bar=Bar;
    }
    location /sub {
        echo $arg_foo $arg_bar;
    }
--- request
    GET /main
--- response_body
Foo Bar



=== TEST 21: querystring in url *AND* an explicit querystring
--- config
    location /main {
        echo_subrequest GET /sub?foo=Foo&bar=Bar -q blah=Blah;
    }
    location /sub {
        echo $arg_foo $arg_bar $arg_blah;
    }
--- request
    GET /main
--- response_body
  Blah



=== TEST 22: let subrequest to read the main request's request body
--- SKIP
--- config
    location /main {
        echo_subrequest POST /sub;
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



=== TEST 23: deep nested echo_subrequest/echo_subrequest_async
--- config
    location /main {
        echo_subrequest GET /bar;
        echo_subrequest_async GET /bar;
        echo_subrequest_async GET /bar;
        echo_subrequest GET /group;
        echo_subrequest_async GET /group;
    }

    location /group {
        echo_subrequest GET /bar;
        echo_subrequest_async GET /bar;
    }

    location /bar {
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



=== TEST 24: deep nested echo_subrequest/echo_subrequest_async
--- config
    location /main {
        echo_subrequest GET /bar?a;
        echo_subrequest_async GET /bar?b;
        echo_subrequest_async GET /bar?c;
        echo_subrequest GET /group?a=d&b=e;
        echo_subrequest_async GET /group?a=f&b=g;
    }

    location /group {
        echo_subrequest GET /bar?$arg_a;
        echo_subrequest_async GET /bar?$arg_b;
    }

    location /bar {
        echo -n $query_string;
    }
--- request
GET /main
--- response_body: abcdefg
--- timeout: 2



=== TEST 25: POST subrequest with file body (relative paths)
--- config
    location /main {
        echo_subrequest POST /sub -f html/blah.txt;
    }
    location /sub {
        echo "sub method: $echo_request_method";
        # we don't need to call echo_read_client_body explicitly here
        echo_request_body;
    }
--- user_files
>>> blah.txt
Hello, world
--- request
    GET /main
--- response_body
sub method: POST
Hello, world



=== TEST 26: POST subrequest with file body (absolute paths)
--- config
    location /main {
        echo_subrequest POST /sub -f $TEST_NGINX_HTML_DIR/blah.txt;
    }
    location /sub {
        echo "sub method: $echo_request_method";
        # we don't need to call echo_read_client_body explicitly here
        echo_request_body;
    }
--- user_files
>>> blah.txt
Hello, world!
Haha
--- request
    GET /main
--- response_body
sub method: POST
Hello, world!
Haha



=== TEST 27: POST subrequest with file body (file not found)
--- config
    location /main {
        echo_subrequest POST /sub -f html/blah/blah.txt;
    }
    location /sub {
        echo "sub method: $echo_request_method";
        # we don't need to call echo_read_client_body explicitly here
        echo_request_body;
    }
--- user_files
>>> blah.txt
Hello, world
--- request
    GET /main
--- ignore_response
--- error_log eval
qr/open\(\) ".*?" failed/
--- no_error_log
[alert]



=== TEST 28: leading subrequest & echo_before_body
--- config
    location /main {
        echo_before_body hello;
        echo_subrequest GET /foo;
    }
    location /foo {
        echo world;
    }
--- request
    GET /main
--- response_body
hello
world



=== TEST 29: leading subrequest & xss
--- config
    location /main {
        default_type 'application/json';
        xss_get on;
        xss_callback_arg c;
        echo_subrequest GET /foo;
    }
    location /foo {
        echo -n world;
    }
--- request
    GET /main?c=hi
--- response_body chop
hi(world);



=== TEST 30: multiple leading subrequest & xss
--- config
    location /main {
        default_type 'application/json';
        xss_get on;
        xss_callback_arg c;
        echo_subrequest GET /foo;
        echo_subrequest GET /bar;
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



=== TEST 31: sanity (HEAD)
--- config
    location /main {
        echo_subrequest GET /sub;
        echo_subrequest GET /sub;
    }
    location /sub {
        echo hello;
    }
--- request
    HEAD /main
--- response_body



=== TEST 32: POST subrequest to ngx_proxy
--- config
    location /hello {
       default_type text/plain;
       echo_subrequest POST '/proxy' -q 'foo=Foo&bar=baz' -b 'request_body=test&test=3';
    }

    location /proxy {
        proxy_pass http://127.0.0.1:$TEST_NGINX_CLIENT_PORT/sub;
        #proxy_pass http://127.0.0.1:1113/sub;
    }

    location /sub {
        echo_read_request_body;
        echo "sub method: $echo_request_method";
        # we don't need to call echo_read_client_body explicitly here
        echo "sub body: $echo_request_body";
    }
--- request
    GET /hello
--- response_body
sub method: POST
sub body: request_body=test&test=3



=== TEST 33: HEAD subrequest
--- config
    location /main {
        echo_subrequest HEAD /sub;
        echo_subrequest HEAD /sub;
    }
    location /sub {
        echo hello;
    }
--- request
    GET /main
--- response_body



=== TEST 34: method name as an nginx variable (github issue #34)
--- config
  location ~ ^/delay/(?<delay>[0-9.]+)/(?<originalURL>.*)$ {
      # echo_blocking_sleep $delay;
      echo_subrequest '$echo_request_method' '/$originalURL' -q '$args';
  }

  location /api {
      echo "args: $args";
  }
--- request
    GET /delay/0.343/api/?a=b
--- response_body
args: a=b
--- no_error_log
[error]

