# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

plan tests => repeat_each() * 2 * blocks();

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#no_diff;

run_tests();

__DATA__

=== TEST 1: the "version" command
--- config
    location /ver {
        set $memc_cmd version;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /ver
--- response_body_like: ^VERSION \d+(\.\d+)+\S*?\r\n$

