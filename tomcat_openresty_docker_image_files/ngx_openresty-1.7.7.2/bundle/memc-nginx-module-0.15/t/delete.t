# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

plan tests => repeat_each() * 2 * blocks();

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#no_diff;

run_tests();

__DATA__

=== TEST 1: delete non-existent item
--- config
    location /main {
        echo 'flush_all';
        echo_location '/memc?cmd=flush_all';

        echo 'delete foo';
        echo_location '/memc?key=foo&cmd=delete';
    }
    location /memc {
        echo_before_body "status: $echo_response_status";
        echo_before_body "exptime: $memc_exptime";

        set $memc_cmd $arg_cmd;
        set $memc_key $arg_key;
        set $memc_value $arg_val;
        set $memc_exptime $arg_exptime;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /main
--- response_body_like
^flush_all
status: 200
exptime: 
OK\r
delete foo
status: 404
exptime: 
<html>.*?404 Not Found.*$



=== TEST 2: set and delete and set
--- config
    location /main {
        echo 'set foo bar';
        echo_location '/memc?cmd=set&key=foo&val=bar';

        echo 'delete foo';
        echo_location '/memc?key=foo&cmd=delete';

        echo 'get foo';
        echo_location '/memc?key=foo&cmd=get';
    }
    location /memc {
        echo_before_body "status: $echo_response_status";
        echo_before_body "exptime: $memc_exptime";

        set $memc_cmd $arg_cmd;
        set $memc_key $arg_key;
        set $memc_value $arg_val;
        set $memc_exptime $arg_exptime;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /main
--- response_body_like
^set foo bar
status: 201
exptime: 0
STORED\r
delete foo
status: 200
exptime: 
DELETED\r
get foo
status: 404
exptime: 
<html>.*?404 Not Found.*$



=== TEST 3: set and delete and set (with exptime)
--- config
    location /main {
        echo 'set foo bar';
        echo_location '/memc?cmd=set&key=foo&val=bar';

        echo 'delete foo 1';
        echo_location '/memc?key=foo&cmd=delete&exptime=1';

        echo 'add foo cat';
        echo_location '/memc?key=foo&cmd=add&val=cat';

        echo_blocking_sleep 1.6;

        echo 'add foo cat - 2nd';
        echo_location '/memc?key=foo&cmd=add&val=cat';
    }
    location /memc {
        echo_before_body "status: $echo_response_status";
        echo_before_body "exptime: $memc_exptime";

        set $memc_cmd $arg_cmd;
        set $memc_key $arg_key;
        set $memc_value $arg_val;
        set $memc_exptime $arg_exptime;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /main
--- response_body eval
"set foo bar
status: 201
exptime: 0
STORED\r
delete foo 1
status: 200
exptime: 1
DELETED\r
add foo cat
status: 200
exptime: 0
NOT_STORED\r
add foo cat - 2nd
status: 201
exptime: 0
STORED\r
"
--- SKIP

