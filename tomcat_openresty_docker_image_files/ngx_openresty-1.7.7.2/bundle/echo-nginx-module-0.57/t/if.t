# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

plan tests => 2 * blocks();

#$Test::Nginx::LWP::LogLevel = 'debug';

run_tests();

__DATA__

=== TEST 1: sanity (hit)
--- config
    location ^~ /if {
        set $res miss;
        if ($arg_val ~* '^a') {
            set $res hit;
            echo $res;
        }
        echo $res;
    }
--- request
    GET /if?val=abc
--- response_body
hit



=== TEST 2: sanity (miss)
--- config
    location ^~ /if {
        set $res miss;
        if ($arg_val ~* '^a') {
            set $res hit;
            echo $res;
        }
        echo $res;
    }
--- request
    GET /if?val=bcd
--- response_body
miss



=== TEST 3: proxy in if (hit)
--- config
    location ^~ /if {
        set $res miss;
        if ($arg_val ~* '^a') {
            set $res hit;
            proxy_pass $scheme://127.0.0.1:$server_port/foo?res=$res;
        }
        proxy_pass $scheme://127.0.0.1:$server_port/foo?res=$res;
    }
    location /foo {
        echo "res = $arg_res";
    }
--- request
    GET /if?val=abc
--- response_body
res = hit



=== TEST 4: proxy in if (miss)
--- config
    location ^~ /if {
        set $res miss;
        if ($arg_val ~* '^a') {
            set $res hit;
            proxy_pass $scheme://127.0.0.1:$server_port/foo?res=$res;
        }
        proxy_pass $scheme://127.0.0.1:$server_port/foo?res=$res;
    }
    location /foo {
        echo "res = $arg_res";
    }
--- request
    GET /if?val=bcd
--- response_body
res = miss



=== TEST 5: if too long url (hit)
--- config
    location /foo {
        if ($request_uri ~ '.{20,}') {
            echo too long;
        }
        echo ok;
    }
--- request
    GET /foo?a=12345678901234567890
--- response_body
too long



=== TEST 6: if too long url (miss)
--- config
    location /foo {
        if ($request_uri ~ '.{20,}') {
            echo too long;
        }
        echo ok;
    }
--- request
    GET /foo?a=1234567890
--- response_body
ok



=== TEST 7: echo should be inherited by if blocks
--- config
    location /foo {
        if ($uri ~ 'foo') {
        }
        echo ok;
    }
--- request
    GET /foo
--- response_body
ok



=== TEST 8: echo_after_body and echo_before_body should be inherited by if blocks
--- config
    location /foo {
        if ($uri ~ 'foo') {
        }
        echo_before_body -n 'hello';
        echo_location /comma;
        echo_after_body 'world';
    }

    location = /comma {
        internal;
        echo -n ', ';
    }
--- request
    GET /foo
--- response_body
hello, world

