# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(3);

plan tests => repeat_each() * 2 * blocks();

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#no_diff;
no_shuffle();
no_long_string();

run_tests();

__DATA__

=== TEST 1: set only
--- http_config
   upstream mc {
        server 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
   }

--- config
   location = "/set" {
       set $memc_cmd 'set';
       set $memc_key 'jalla';
       set $memc_value 'myvalue';
       set $memc_exptime 24;
       memc_pass mc;
   }


   location = "/get" {
       set $memc_cmd 'get';
       set $memc_key 'jalla';
       memc_pass mc;
   }

   location = "/delete" {
       set $memc_cmd 'delete';
       set $memc_key 'jalla';
       memc_pass mc;
   }
   location = "/flush" {
       echo_location /get;
       #echo "";
       echo_location /delete;
   }
--- request
    GET /flush
--- response_body eval
"STORED\r\n"
--- error_code: 201
--- SKIP



=== TEST 2: set in a subrequest issued from an output filter
--- config
    location /memc {
        set $memc_cmd 'set';
        set $memc_key 'foo';
        set $memc_value 'blah';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
    location /main {
        default_type 'text/html';
        add_before_body '/memc';
        echo '~~';
        add_after_body '/memc';
    }
--- request
GET /main
--- response_body eval
"STORED\r
~~
STORED\r
"



=== TEST 3: reuse request body
--- config
    location /main {
        echo_read_request_body;

        echo 'flush_all';
        echo_location '/memc?cmd=flush_all';

        echo 'set foo';
        echo_subrequest POST '/memc?key=foo&cmd=set';

        echo 'set bar';
        echo_subrequest POST '/memc?key=bar&cmd=set';

        echo 'get bar';
        echo_location '/memc?key=bar&cmd=get';

        echo 'get foo';
        echo_location '/memc?key=foo&cmd=get';
    }
    location /memc {
        echo_before_body "status: $echo_response_status";
        echo_before_body "exptime: $memc_exptime";

        set $memc_cmd $arg_cmd;
        set $memc_key $arg_key;
        #set $memc_value $arg_val;
        set $memc_exptime $arg_exptime;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
POST /main
Hello
--- response_body eval
"flush_all
status: 200
exptime: 
OK\r
set foo
status: 201
exptime: 0
STORED\r
set bar
status: 201
exptime: 0
STORED\r
get bar
status: 200
exptime: 
Helloget foo
status: 200
exptime: 
Hello"



=== TEST 4: zero buf when $memc_value is empty
http://github.com/agentzh/memc-nginx-module/issues#issue/2
--- config
    location /memc {
        set $memc_cmd 'set';
        set $memc_key 'foo';
        set $memc_value '';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /memc
--- response_body eval
"STORED\r
"
--- error_code: 201



=== TEST 5: zero buf when $memc_value is empty
http://github.com/agentzh/memc-nginx-module/issues#issue/2
--- config
    location /memc {
        set $memc_cmd 'set';
        set $memc_key 'foo';
        set $memc_value '';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /memc
--- response_body eval
"STORED\r
"
--- error_code: 201



=== TEST 6: set too long keys
--- config
    location /memc {
        set $memc_cmd 'set';
        set $memc_key 'foooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo';
        set $memc_value 'hi';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /memc
--- response_body eval
"CLIENT_ERROR bad command line format\r
"
--- error_code: 502



=== TEST 7: get too long keys
--- config
    location /memc {
        set $memc_cmd 'get';
        set $memc_key 'foooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /memc
--- response_body_like: 502 Bad Gateway
--- error_code: 502



=== TEST 8: set only
--- config
    location /memc {
        set $memc_cmd $arg_cmd;
        set $memc_key $arg_key;
        set $memc_value $echo_request_body;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
POST /memc?key=foo&cmd=set
hello, world
--- response_body eval
"STORED\r\n"
--- error_code: 201
--- SKIP



=== TEST 9: get
--- config
    location /memc {
        set $memc_cmd $arg_cmd;
        set $memc_key $arg_key;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /memc?key=foo&cmd=get
--- response_body
hello, world
--- error_code: 200
--- SKIP

