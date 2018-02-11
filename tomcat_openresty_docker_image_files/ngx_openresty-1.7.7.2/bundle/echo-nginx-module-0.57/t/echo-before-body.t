# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

plan tests => 2 * blocks();

#$Test::Nginx::LWP::LogLevel = 'debug';

run_tests();

__DATA__

=== TEST 1: sanity
--- config
    location /echo {
        echo_before_body hello;
        echo world;
    }
--- request
    GET /echo
--- response_body
hello
world



=== TEST 2: echo before proxy
--- config
    location /echo {
        echo_before_body hello;
        proxy_pass $scheme://127.0.0.1:$server_port$request_uri/more;
    }
    location /echo/more {
        echo world;
    }
--- request
    GET /echo
--- response_body
hello
world



=== TEST 3: with variables
--- config
    location /echo {
        echo_before_body $request_method;
        echo world;
    }
--- request
    GET /echo
--- response_body
GET
world



=== TEST 4: w/o args
--- config
    location /echo {
        echo_before_body;
        echo world;
    }
--- request
    GET /echo
--- response_body eval
"\nworld\n"



=== TEST 5: order is not important
--- config
    location /reversed {
        echo world;
        echo_before_body hello;
    }
--- request
    GET /reversed
--- response_body
hello
world



=== TEST 6: multiple echo_before_body instances
--- config
    location /echo {
        echo_before_body hello;
        echo_before_body world;
        echo !;
    }
--- request
    GET /echo
--- response_body
hello
world
!



=== TEST 7: multiple echo_before_body instances with multiple echo cmds
--- config
    location /echo {
        echo_before_body hello;
        echo_before_body world;
        echo i;
        echo say;
    }
--- request
    GET /echo
--- response_body
hello
world
i
say



=== TEST 8: with $echo_response_status
--- config
    location /status {
        echo_before_body "status: $echo_response_status";
        return 404;
    }
--- request
    GET /status
--- response_body_like
status: 404
<html>.*404 Not Found.*$
--- error_code: 404



=== TEST 9: $echo_response_status in echo_before_body in subrequests
--- config
    location /main {
        echo_location '/status?val=403';
        echo_location '/status?val=500';
    }
    location /status {
        if ($arg_val = 500) {
            echo_before_body "status: $echo_response_status";
            return 500;
            break;
        }
        if ($arg_val = 403) {
            echo_before_body "status: $echo_response_status";
            return 403;
            break;
        }
        return 200;
    }
--- request
    GET /main
--- response_body_like
^status: 403.*?status: 500.*$



=== TEST 10: echo -n
--- config
    location /echo {
        echo_before_body -n hello;
        echo_before_body -n world;
        echo ==;
    }
--- request
    GET /echo
--- response_body
helloworld==



=== TEST 11: echo a -n
--- config
    location /echo {
        echo_before_body a -n hello;
        echo_before_body b -n world;
        echo ==;
    }
--- request
    GET /echo
--- response_body
a -n hello
b -n world
==



=== TEST 12: -n in a var
--- config
    location /echo {
        set $opt -n;
        echo_before_body $opt hello;
        echo_before_body $opt world;
        echo ==;
    }
--- request
    GET /echo
--- response_body
-n hello
-n world
==



=== TEST 13: -n only
--- config
    location /echo {
        echo_before_body -n;
        echo_before_body -n;
        echo ==;
    }
--- request
    GET /echo
--- response_body
==



=== TEST 14: -n with an empty string
--- config
    location /echo {
        echo_before_body -n "";
        set $empty "";
        echo_before_body -n $empty;
        echo ==;
    }
--- request
    GET /echo
--- response_body
==



=== TEST 15: -- -n
--- config
    location /echo {
        echo_before_body -- -n hello;
        echo_before_body -- -n world;
        echo ==;
    }
--- request
    GET /echo
--- response_body
-n hello
-n world
==



=== TEST 16: -n -n
--- config
    location /echo {
        echo_before_body -n -n hello;
        echo_before_body -n -n world;
        echo ==;
    }
--- request
    GET /echo
--- response_body
helloworld==



=== TEST 17: -n -- -n
--- config
    location /echo {
        echo_before_body -n -- -n hello;
        echo_before_body -n -- -n world;
        echo ==;
    }
--- request
    GET /echo
--- response_body
-n hello-n world==

