# vi:ft=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);

plan tests => repeat_each() * (3 * blocks());

$ENV{TEST_NGINX_REDIS_PORT} ||= 6379;

#master_on;
#worker_connections 1024;

#no_diff;
no_long_string;

#log_level 'warn';

run_tests();

__DATA__

=== TEST 1: location if (query)
--- config
    location /redis {
        set $key 'default';
        if ($uri ~ "(special.*)") {
            set $key $1;
        }
        redis2_query lrange $key 0 -1;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /redis/special
--- response_body eval
"*0\r\n"
--- no_error_log
[error]



=== TEST 2: location if (literal query)
--- config
    location /redis {
        if ($uri ~ "(special.*)") {
        }
        redis2_literal_raw_query 'flushall\r\n';
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /redis/special
--- response_body eval
"+OK\r\n"
--- no_error_log
[error]



=== TEST 3: location if (raw queries)
--- config
    location /redis {
        if ($uri ~ "(special.*)") {
            set $n 2;
        }
        redis2_raw_queries $n 'flushall\r\nget foo\r\n';
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /redis/special
--- response_body eval
"+OK\r\n\$-1\r\n"
--- no_error_log
[error]



=== TEST 4: location if (raw query)
--- config
    location /redis {
        if ($uri ~ "(special.*)") {
            set $n 2;
        }
        redis2_raw_query 'flushall\r\n';
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /redis/special
--- response_body eval
"+OK\r\n"
--- no_error_log
[error]

