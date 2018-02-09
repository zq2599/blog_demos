# vi:filetype=

use lib 'lib';
use Test::Nginx::LWP skip_all =>
    'not working at all';

plan tests => 2 * blocks();

run_tests();

__DATA__

=== TEST 1: sanity
--- config
    location /abort {
        echo hello;
        echo_flush;
        echo_location_async '/foo';
        echo_location_async '/bar';
        echo_location_async '/baz';
        echo world;
        echo_flush;
    }

    location /proxy {
        proxy_pass "http://127.0.0.1:$server_port/sleep?$query_string";
    }

    location /sleep {
        echo_sleep $arg_sleep;
        echo $arg_echo;
        echo_flush;
    }

    location /foo {
        echo_location '/proxy?sleep=1&echo=foo';
        #echo_flush;
        echo_abort_parent;
    }

    location /bar {
        proxy_pass 'http://127.0.0.1:$server_port/sleep_bar';
    }

    location /baz {
        proxy_pass 'http://127.0.0.1:$server_port/sleep_baz';
    }

    location /sleep_bar {
        echo_sleep 2;
        echo bar;
    }

    location /sleep_baz {
        echo_sleep 3;
        echo baz;
    }
--- request
    GET /abort
--- response_body
hello
bar

