# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (2 * blocks() + 1);

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#no_diff;

run_tests();

__DATA__

=== TEST 1: set exptime
--- config
    location /exptime {
        echo 'flush_all';
        echo_location '/memc?cmd=flush_all';

        echo 'set foo BAR';
        echo_subrequest PUT '/memc?key=foo&exptime=1' -b BAR;

        echo 'get foo - 0 sec';
        echo_location '/memc?key=foo';
        echo;

        echo_blocking_sleep 1.1;

        echo 'get foo - 1.1 sec';
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
^flush_all
status: 200
exptime: 
OK\r
set foo BAR
status: 201
exptime: 1
STORED\r
get foo - 0 sec
status: 200
exptime: 
BAR
get foo - 1\.1 sec
status: 404
exptime: 
<html>.*?404 Not Found.*$



=== TEST 2: test empty flags (default to 0)
--- config
    location /flags {
        echo 'set foo BAR (exptime: EMPTY)';
        echo_subrequest PUT '/memc?key=foo' -b BAR;

        echo_blocking_sleep 1;

        echo 'get foo';
        echo_subrequest GET '/memc?key=foo';
    }

    location /memc {
        echo_before_body "status: $echo_response_status";
        echo_before_body "exptime: $memc_exptime";

        set $memc_key $arg_key;
        set $memc_exptime $arg_exptime;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /flags
--- response_body eval
"set foo BAR (exptime: EMPTY)
status: 201
exptime: 0
STORED\r
get foo
status: 200
exptime: 
BAR"



=== TEST 3: invalid exptime in set
--- config
    location /allow {
        set $memc_cmd 'set';
        set $memc_key 'foo';
        set $memc_value 'nice';
        set $memc_exptime 'my invalid';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /allow
--- response_body_like: 400 Bad Request
--- error_code: 400
--- error_log
variable "$memc_exptime" takes invalid value: my invalid,



=== TEST 4: invalid exptime in flush_all
--- config
    location /allow {
        set $memc_cmd 'flush_all';
        set $memc_key 'foo';
        set $memc_value 'nice';
        set $memc_exptime 'invalid';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /allow
--- response_body_like: 400 Bad Request
--- error_code: 400



=== TEST 5: invalid exptime in delete
--- config
    location /allow {
        set $memc_cmd 'delete';
        set $memc_key 'foo';
        set $memc_value 'nice';
        set $memc_exptime 'invalid';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /allow
--- response_body_like: 400 Bad Request
--- error_code: 400



=== TEST 6: set negative exptime
--- config
    location /allow {
        set $memc_cmd 'set';
        set $memc_key 'sun';
        set $memc_value 'tree';
        set $memc_exptime '-1';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /allow
--- response_body_like: 400 Bad Request
--- error_code: 400

