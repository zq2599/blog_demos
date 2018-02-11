# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (2 * blocks() + 5);

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#no_diff;
no_shuffle;

run_tests();

__DATA__

=== TEST 1: set flags and get flags
--- config
    location /flags {
        echo 'set foo BAR (flag: 1234567890)';
        echo_subrequest PUT '/memc?key=foo&flags=1234567890' -b BAR;

        echo 'get foo';
        echo_subrequest GET '/memc?key=foo';
    }
    location /memc {
        echo_before_body "status: $echo_response_status";
        echo_before_body "flags: $memc_flags";

        set $memc_key $arg_key;
        set $memc_flags $arg_flags;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /flags
--- response_body eval
"set foo BAR (flag: 1234567890)
status: 201
flags: 1234567890
STORED\r
get foo
status: 200
flags: 1234567890
BAR"



=== TEST 2: test empty flags (default to 0)
--- config
    location /flags {
        echo 'set foo BAR (flag: EMPTY)';
        echo_subrequest PUT '/memc?key=foo' -b BAR;

        echo 'get foo';
        echo_subrequest GET '/memc?key=foo';
    }

    location /memc {
        echo_before_body "status: $echo_response_status";
        echo_before_body "flags: $memc_flags";

        set $memc_key $arg_key;
        set $memc_flags $arg_flags;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /flags
--- response_body eval
"set foo BAR (flag: EMPTY)
status: 201
flags: 0
STORED\r
get foo
status: 200
flags: 0
BAR"



=== TEST 3: test empty flags (default to 0) (another form)
--- config
    location /flags {
        echo 'set foo BAR (flag: EMPTY)';
        echo_subrequest PUT '/memc?key=foo&flags=' -b BAR;

        echo 'get foo';
        echo_subrequest GET '/memc?key=foo';
    }

    location /memc {
        echo_before_body "status: $echo_response_status";
        echo_before_body "flags: $memc_flags";

        set $memc_key $arg_key;
        set $memc_flags $arg_flags;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /flags
--- response_body eval
"set foo BAR (flag: EMPTY)
status: 201
flags: 0
STORED\r
get foo
status: 200
flags: 0
BAR"



=== TEST 4: add flags and get flags
--- config
    location /flags {
        echo 'flush_all';
        echo_subrequest GET '/memc?cmd=flush_all';

        echo 'add foo BAR (flag: 54321)';
        echo_subrequest POST '/memc?key=foo&flags=54321' -b BAR;

        echo 'get foo';
        echo_subrequest GET '/memc?key=foo';
    }
    location /memc {
        echo_before_body "status: $echo_response_status";
        echo_before_body "flags: $memc_flags";

        set $memc_cmd $arg_cmd;
        set $memc_key $arg_key;
        set $memc_flags $arg_flags;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /flags
--- response_body eval
"flush_all
status: 200
flags: 
OK\r
add foo BAR (flag: 54321)
status: 201
flags: 54321
STORED\r
get foo
status: 200
flags: 54321
BAR"



=== TEST 5: set invalid flags
--- config
    location /allow {
        set $memc_cmd 'set';
        set $memc_key 'foo';
        set $memc_value 'nice';
        set $memc_flags 'invalid';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /allow
--- response_body_like: 400 Bad Request
--- error_code: 400
--- error_log
variable "$memc_flags" takes invalid value: invalid,



=== TEST 6: set negative flags
--- config
    location /allow {
        set $memc_cmd 'set';
        set $memc_key 'sun';
        set $memc_value 'tree';
        set $memc_flags '-1';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /allow
--- response_body_like: 400 Bad Request
--- error_code: 400



=== TEST 7: set flags and get flags in http time
--- config
    location /flags {
        echo 'set foo BAR (flag: 1264680563)';
        echo_subrequest PUT '/memc?key=foo&flags=1264680563' -b BAR;

        echo 'get foo';
        echo_subrequest GET '/memc?key=foo';
    }
    location /memc {
        echo_before_body "status: $echo_response_status";
        echo_before_body "flags: $memc_flags $memc_flags_as_http_time";

        set $memc_key $arg_key;
        set $memc_flags $arg_flags;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /flags
--- response_body eval
"set foo BAR (flag: 1264680563)
status: 201
flags: 1264680563 Thu, 28 Jan 2010 12:09:23 GMT
STORED\r
get foo
status: 200
flags: 1264680563 Thu, 28 Jan 2010 12:09:23 GMT
BAR"



=== TEST 8: last-modified (conditional GET)
--- config
    location /memc {
        set $memc_key $arg_key;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;

        memc_flags_to_last_modified on;
        add_header X-Flags $memc_flags;
    }
--- request
    GET /memc?key=foo
--- more_headers
If-Modified-Since: Thu, 28 Jan 2010 12:09:23 GMT
--- response_headers
Last-Modified: Thu, 28 Jan 2010 12:09:23 GMT
X-Flags: 1264680563
--- error_code: 304
--- response_body:



=== TEST 9: last-modified (unconditional GET)
--- config
    location /memc {
        set $memc_key $arg_key;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;

        memc_flags_to_last_modified on;
        add_header X-Flags $memc_flags;
    }
--- request
    GET /memc?key=foo
--- response_headers
Last-Modified: Thu, 28 Jan 2010 12:09:23 GMT
X-Flags: 1264680563
--- error_code: 200
--- response_body: BAR

