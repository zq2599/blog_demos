# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (2 * blocks() + 1);

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#no_diff;

run_tests();

__DATA__

=== TEST 1: value required for incr
--- config
    location /memc {
        set $memc_cmd $arg_cmd;
        set $memc_key $arg_key;
        set $memc_value $arg_val;
        set $memc_exptime $arg_exptime;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /memc?cmd=incr&key=foo
--- response_body_like: 400 Bad Request
--- error_code: 400



=== TEST 2: invalid value for incr
--- config
    location /memc {
        set $memc_cmd $arg_cmd;
        set $memc_key $arg_key;
        set $memc_value $arg_val;
        set $memc_exptime $arg_exptime;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /memc?cmd=incr&key=foo&val=nice
--- response_body_like: 400 Bad Request
--- error_code: 400
--- error_log
variable "$memc_value" is invalid for incr/decr: nice,



=== TEST 3: invalid value (negative intenger) for incr
--- config
    location /memc {
        set $memc_cmd $arg_cmd;
        set $memc_key $arg_key;
        set $memc_value $arg_val;
        set $memc_exptime $arg_exptime;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /memc?cmd=incr&key=foo&val=-5
--- response_body_like: 400 Bad Request
--- error_code: 400



=== TEST 4: key required for incr
--- config
    location /memc {
        set $memc_cmd $arg_cmd;
        set $memc_key $arg_key;
        set $memc_value $arg_val;
        set $memc_exptime $arg_exptime;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /memc?cmd=incr&val=2
--- response_body_like: 400 Bad Request
--- error_code: 400
--- SKIP
--- TODO



=== TEST 5: incr
--- config
    location /main {
        echo 'set foo 32';
        echo_location '/memc?cmd=set&key=foo&val=32';

        echo 'incr 51';
        echo_location '/memc?key=foo&cmd=incr&val=51';

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
--- response_body eval
"set foo 32
status: 201
exptime: 0
STORED\r
incr 51
status: 201
exptime: 
83\r
get foo
status: 200
exptime: 
83"



=== TEST 6: decr
--- config
    location /main {
        echo 'set foo 32';
        echo_location '/memc?cmd=set&key=foo&val=32';

        echo 'decr 13';
        echo_location '/memc?key=foo&cmd=decr&val=13';

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
--- response_body eval
"set foo 32
status: 201
exptime: 0
STORED\r
decr 13
status: 201
exptime: 
19\r
get foo
status: 200
exptime: 
19"



=== TEST 7: incr an non-existent key
--- config
    location /main {
        echo 'flush all';
        echo_location '/memc?cmd=flush_all';

        echo 'incr 51';
        echo_location '/memc?key=foo&cmd=incr&val=51';
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
^flush all
status: 200
exptime: 
OK\r
incr 51
status: 404
exptime: 
<html>.*?404 Not Found.*$



=== TEST 8: decr
--- config
    location /main {
        echo 'set foo 32';
        echo_location '/memc?cmd=set&key=foo&val=32';

        echo 'decr 13';
        echo_location '/memc?key=foo&cmd=decr&val=13';

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
--- response_body eval
"set foo 32
status: 201
exptime: 0
STORED\r
decr 13
status: 201
exptime: 
19\r
get foo
status: 200
exptime: 
19"



=== TEST 9: incr an non-existent key (with fallback)
--- config
    location /main {
        echo 'flush all';
        echo_location '/memc?cmd=flush_all';

        echo 'incr 51';
        echo_location '/memc?key=foo&cmd=incr&val=51';
    }

    location /memc {
        echo_before_body "status: $echo_response_status";
        echo_before_body "exptime: $memc_exptime";

        set $memc_cmd $arg_cmd;
        set $memc_key $arg_key;
        set $memc_value $arg_val;
        set $memc_exptime $arg_exptime;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;

        error_page 404 = /set_and_incr?$query_string;
    }

    location /set_and_incr {
        internal;

        echo_location /memc?cmd=add&key=$arg_key&val=0;
        echo_location /memc?$query_string;
    }
--- request
    GET /main
--- response_body eval
"flush all
status: 200
exptime: 
OK\r
incr 51
status: 201
exptime: 0
STORED\r
status: 201
exptime: 
51\r
"

