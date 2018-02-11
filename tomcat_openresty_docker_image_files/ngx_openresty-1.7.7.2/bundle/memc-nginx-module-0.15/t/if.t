# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

plan tests => repeat_each() * (3 * blocks());

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#no_diff;

run_tests();

__DATA__

=== TEST 1: bad cmd
--- config
    location /foo {
        if ($uri ~* 'foo') {
            set $memc_cmd flush_all;
        }
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
    GET /foo
--- response_body eval
"OK\r\n"
--- no_error_log
[error]

