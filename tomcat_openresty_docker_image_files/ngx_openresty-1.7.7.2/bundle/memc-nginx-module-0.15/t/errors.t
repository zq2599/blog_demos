# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

plan tests => repeat_each() * 2 * blocks();

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#no_diff;

run_tests();

__DATA__

=== TEST 1: empty pass
little-endian systems only

--- config
    location /memc {
        set $memc_key foo;
        set $backend "not-exist";
        memc_pass $backend;
    }
--- request
GET /memc
--- error_code: 500
--- response_body_like: 500 Internal Server Error



=== TEST 2: connection refused
little-endian systems only

--- config
    location /memc {
        set $memc_key foo;
        set $backend "not-exist";
        memc_pass 127.0.0.1:1;
    }
--- request
GET /memc
--- error_code: 502
--- response_body_like: 502 Bad Gateway

