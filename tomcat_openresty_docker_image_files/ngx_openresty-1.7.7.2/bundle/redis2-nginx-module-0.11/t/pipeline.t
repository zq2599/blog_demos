# vi:ft=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (2 * blocks() + 1);

$ENV{TEST_NGINX_REDIS_PORT} ||= 6379;

#master_on;
#worker_connections 1024;

#no_diff;

log_level 'warn';

run_tests();

__DATA__

=== TEST 1: advanced query (2 pipelined)
--- config
    location /a {
        redis2_query set hello world;
        redis2_query get hello;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /a
--- response_body eval
"+OK\r\n\$5\r\nworld\r\n"



=== TEST 2: 4 pipelined
--- config
    location /a {
        redis2_query set hello world;
        redis2_query get hello;
        redis2_query set hello agentzh;
        redis2_query get hello;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /a
--- response_body eval
"+OK\r\n\$5\r\nworld\r\n+OK\r\n\$7\r\nagentzh\r\n"



=== TEST 3: redis2_raw_queries (2 queries)
--- config
    location /pipelined2 {
        set_unescape_uri $n $arg_n;
        set_unescape_uri $cmds $arg_cmds;

        redis2_raw_queries $n $cmds;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /pipelined2?n=2&cmds=flushall%0D%0Aget%20key%0D%0A
--- response_body eval
"+OK\r\n\$-1\r\n"



=== TEST 4: redis2_raw_queries (2 queries, smaller N arg, bad)
--- config
    location /pipelined2 {
        set_unescape_uri $n $arg_n;
        set_unescape_uri $cmds $arg_cmds;

        redis2_raw_queries $n $cmds;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /pipelined2?n=1&cmds=flushall%0D%0Aget%20key%0D%0A
--- response_body eval
"+OK\r\n"



=== TEST 5: redis2_raw_queries (2 queries, larger N arg, bad)
--- config
    redis2_read_timeout 100ms;
    location /pipelined2 {
        set_unescape_uri $n $arg_n;
        set_unescape_uri $cmds $arg_cmds;

        redis2_raw_queries $n $cmds;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /pipelined2?n=3&cmds=flushall%0D%0Aget%20key%0D%0A
--- ignore_response
--- no_error_log
[alert]
[crit]
--- error_log eval
qr/upstream timed out .*? while reading upstream/



=== TEST 6: advanced query (2 pipelined) - keepalive
--- http_config
    upstream backend {
        server 127.0.0.1:$TEST_NGINX_REDIS_PORT;
        keepalive 100;
    }
--- config
    location /a {
        redis2_query set hello world;
        redis2_query get hello;
        redis2_pass backend;
    }
--- request
    GET /a
--- response_body eval
"+OK\r\n\$5\r\nworld\r\n"

