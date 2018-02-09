# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(3);

plan tests => repeat_each() * 2 * blocks();

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#no_diff;

run_tests();

__DATA__

=== TEST 1: keepalive
--- http_config
    upstream backend {
      server 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
      #server 127.0.0.1:11985;
      #server 127.0.0.1:11986;
      keepalive 2;
      #hash $arg_key;
    }

--- config
    location /memc {
        set $memc_cmd set;
        set $memc_key $arg_key;
        set $memc_value 'value';
        memc_pass backend;
    }
--- request
    GET /memc?key=dog
--- response_body eval
"STORED\r
"
--- error_code: 201

