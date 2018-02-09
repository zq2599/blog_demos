# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

plan tests => repeat_each() * 2 * blocks();

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#no_diff;

run_tests();

__DATA__

=== TEST 1: flush_all
--- config
    location /flush {
        set $memc_cmd flush_all;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /flush
--- response_body eval
"OK\r
"



=== TEST 2: set and flush and get
--- config
    location /main {
        echo 'set foo blah';
        echo_location '/memc?key=foo&cmd=set&val=blah';

        echo 'flush_all';
        echo_location '/memc?cmd=flush_all';

        echo 'get foo';
        echo_location '/memc?key=foo&cmd=get';
    }
    location /memc {
        echo_before_body "status: $echo_response_status";

        set $memc_cmd $arg_cmd;
        set $memc_key $arg_key;
        set $memc_value $arg_val;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /main
--- response_body_like
^set foo blah
status: 201
STORED\r
flush_all
status: 200
OK\r
get foo
status: 404.*?404 Not Found.*$



=== TEST 3: set exptime
--- config
    location /exptime {
        echo 'flush_all';
        echo_location '/memc?cmd=flush_all';

        echo 'set foo BAR';
        echo_subrequest PUT '/memc?key=foo' -b BAR;

        echo 'flush_all exptime=2';
        echo_location '/memc?cmd=flush_all&exptime=2';

        echo 'get foo - 0 sec';
        echo_location '/memc?key=foo';
        echo;

        echo_blocking_sleep 2;

        echo 'get foo - 2 sec';
        echo_location '/memc?key=foo';
    }
    location /memc {
        echo_before_body "status: $echo_response_status";
        echo_before_body "exptime: $memc_exptime";

        set $memc_cmd $arg_cmd;
        set $memc_key $arg_key;
        set $memc_exptime $arg_exptime;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /exptime
--- response_body_like
flush_all
status: 200
exptime: 
OK\r
set foo BAR
status: 201
exptime: 0
STORED\r
flush_all exptime=2
status: 200
exptime: 2
OK\r
get foo - 0 sec
status: 200
exptime: 
BAR
get foo - 2 sec
status: 404
exptime: 
<html>.*?404 Not Found.*$
--- timeout: 3

