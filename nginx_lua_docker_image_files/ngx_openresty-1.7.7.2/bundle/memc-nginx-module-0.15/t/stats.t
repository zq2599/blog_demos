# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(1);

plan tests => repeat_each() * 2 * blocks();

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#no_diff;
log_level('error');

run_tests();

__DATA__

=== TEST 1: sanity
--- timeout: 5
--- config
    location /stats {
        set $memc_cmd stats;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /stats
--- response_body_like: ^(?:STAT [^\r]*\r\n)*END\r\n$



=== TEST 2: timeout
--- config
    memc_connect_timeout 10ms;
    memc_send_timeout 10ms;
    location /stats {
        set $memc_cmd stats;
        memc_pass www.taobao.com:12345;
    }
--- request
    GET /stats
--- response_body_like: 504 Gateway Time-out
--- timeout: 1
--- error_code: 504

