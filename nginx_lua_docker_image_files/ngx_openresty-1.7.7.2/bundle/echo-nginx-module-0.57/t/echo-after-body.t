# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (2 * blocks() + 1);

no_long_string();
log_level('warn');

#master_on();
#workers(1);

run_tests();

__DATA__

=== TEST 1: sanity
--- http_config
    postpone_output 1;
--- config
    location /echo {
        echo_after_body hello;
        echo world;
    }
--- request
    GET /echo
--- response_body
world
hello



=== TEST 2: echo after proxy
--- config
    location /echo {
        echo_after_body hello;
        proxy_pass http://127.0.0.1:$server_port$request_uri/more;
    }
    location /echo/more {
        echo world;
    }
--- request
    GET /echo
--- response_body
world
hello



=== TEST 3: with variables
--- config
    location /echo {
        echo_after_body $request_method;
        echo world;
    }
--- request
    GET /echo
--- response_body
world
GET



=== TEST 4: w/o args
--- config
    location /echo {
        echo_after_body;
        echo world;
    }
--- request
    GET /echo
--- response_body eval
"world\n\n"



=== TEST 5: order is not important
--- config
    location /reversed {
        echo world;
        echo_after_body hello;
    }
--- request
    GET /reversed
--- response_body
world
hello



=== TEST 6: multiple echo_after_body instances
--- config
    location /echo {
        echo_after_body hello;
        echo_after_body world;
        echo !;
    }
--- request
    GET /echo
--- response_body
!
hello
world



=== TEST 7: multiple echo_after_body instances with multiple echo cmds
--- config
    location /echo {
        echo_after_body hello;
        echo_after_body world;
        echo i;
        echo say;
    }
--- request
    GET /echo
--- response_body
i
say
hello
world



=== TEST 8: echo-after-body & echo-before-body
--- config
    location /mixed {
        echo_before_body hello;
        echo_after_body world;
        echo_before_body hiya;
        echo_after_body igor;
        echo ////////;
    }
--- request
    GET /mixed
--- response_body
hello
hiya
////////
world
igor



=== TEST 9: echo around proxy
--- config
    location /echo {
        echo_before_body hello;
        echo_before_body world;
        #echo $scheme://$host:$server_port$request_uri/more;
        proxy_pass $scheme://127.0.0.1:$server_port$request_uri/more;
        echo_after_body hiya;
        echo_after_body igor;
    }
    location /echo/more {
        echo blah;
    }
--- request
    GET /echo
--- response_body
hello
world
blah
hiya
igor



=== TEST 10: with $echo_response_status
--- config
    location /status {
        echo_after_body "status: $echo_response_status";
        return 404;
    }
--- request
    GET /status
--- response_body_like
.*404 Not Found.*
status: 404$
--- error_code: 404



=== TEST 11: in subrequests
--- config
    location /main {
        echo_location_async /hello;
    }
    location /hello {
        echo_after_body 'world!';
        echo 'hello';
    }
--- request
    GET /main
--- response_body
hello
world!



=== TEST 12: echo_after_body + gzip
--- config
    gzip             on;
    gzip_min_length  1;
    location /main {
        echo_after_body 'world!';
        echo_duplicate 1024 'hello';
    }
--- request
    GET /main
--- response_body_like
hello
--- SKIP



=== TEST 13: echo_after_body + proxy output
--- config
    #gzip             on;
    #gzip_min_length  1;
    location /main {
        echo_after_body 'world';
        proxy_pass http://127.0.0.1:$server_port/foo;
    }
    location /foo {
        echo_duplicate 10 hello;
    }
--- request
    GET /main
--- response_body_like
^(?:hello){10}world$



=== TEST 14: in subrequests (we get last_in_chain set properly)
--- config
    location /main {
        echo_location_async /hello;
    }
    location /hello {
        echo 'hello';
        echo_after_body 'world!';
        body_filter_by_lua '
            local eof = ngx.arg[2]
            if eof then
                print("lua: eof found in body")
            end
        ';
    }
--- request
    GET /main
--- response_body
hello
world!
--- log_level: notice
--- error_log
lua: eof found in body

