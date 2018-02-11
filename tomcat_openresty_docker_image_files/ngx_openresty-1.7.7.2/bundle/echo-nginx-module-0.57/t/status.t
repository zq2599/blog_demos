# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(1);

plan tests => repeat_each() * (2 * blocks());

#$Test::Nginx::LWP::LogLevel = 'debug';

run_tests();

__DATA__

=== TEST 1: 200
--- config
    location /echo {
        echo_status 200;
        echo hello;
    }
--- request
    GET /echo
--- response_body
hello
--- error_code: 200



=== TEST 2: if location (200)
--- config
    location /echo {
        set $true 1;
        if ($true) {
        }
        echo_status 200;
        echo hello;
    }
--- request
    GET /echo
--- response_body
hello
--- error_code: 200



=== TEST 3: 404
--- config
    location /echo {
        echo_status 404;
        echo hello;
    }
--- request
    GET /echo
--- response_body
hello
--- error_code: 404



=== TEST 4: if location (404)
--- config
    location /echo {
        set $true 1;
        if ($true) {
        }
        echo_status 404;
        echo hello;
    }
--- request
    GET /echo
--- response_body
hello
--- error_code: 404



=== TEST 5: 500
--- config
    location /echo {
        echo_status 500;
        echo hello;
    }
--- request
    GET /echo
--- response_body
hello
--- error_code: 500



=== TEST 6: if location (500)
--- config
    location /echo {
        set $true 1;
        if ($true) {
        }
        echo_status 500;
        echo hello;
    }
--- request
    GET /echo
--- response_body
hello
--- error_code: 500



=== TEST 7: if location (500) no inherit
--- config
    location /echo {
        set $true 1;
        if ($true) {
            echo_status 503;
        }
        echo_status 500;
        echo hello;
    }
--- request
    GET /echo
--- response_body
hello
--- error_code: 503



=== TEST 8: subrequest
--- config
    location /echo {
        echo_location /sub;
        echo_status 503;
    }

    location /sub {
        echo blah blah;
    }
--- request
    GET /echo
--- response_body
blah blah
--- error_code: 503

