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

=== TEST 1: 0 in bulk size
--- config
    location /set {
        redis2_query set 'counters::stats::list' '[["mafiaclans.eu", 12], ["picfu.net", 5], ["www.test.com", 0], ["www.ayom.com", 0], ["www.21dezember2012.org", 0], ["the-indie.ch", 0], ["spiele-check.de", 0], ["online-right-now.net", 0], ["google.com", 0]]';

        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /get {
        redis2_raw_query 'get counters::stats::list\r\n';
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /main2 {
        content_by_lua '
            local res = ngx.location.capture("/set");
            ngx.print(res.body)
            res = ngx.location.capture("/get");
            ngx.print(res.body)
        ';
    }
    location /main {
        # echo_location is buggy...sigh.
        echo_location /set;
        echo_location /get;
    }
--- request
    GET /main2
--- response_body eval
qq{+OK\r
\$207\r
[["mafiaclans.eu", 12], ["picfu.net", 5], ["www.test.com", 0], ["www.ayom.com", 0], ["www.21dezember2012.org", 0], ["the-indie.ch", 0], ["spiele-check.de", 0], ["online-right-now.net", 0], ["google.com", 0]]\r
}
--- no_error_log
[error]



=== TEST 2: "0" digit in bulk count number
--- config
    location /set {
        redis2_query set a hello;

        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /get {
        redis2_query mget a a a a a a a a a a;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /main {
        content_by_lua '
            local res = ngx.location.capture("/set");
            ngx.print(res.body)
            res = ngx.location.capture("/get");
            ngx.print(res.body)
        ';
    }
--- request
    GET /main
--- response_body eval
qq{+OK\r
\*10\r
} . "\$5\r
hello\r
" x 10
--- no_error_log
[error]



=== TEST 3: zero bulk count
--- config
    location /set {
        redis2_query flushall;

        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /get {
        redis2_query lrange blah 0 0;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /main {
        content_by_lua '
            local res = ngx.location.capture("/set");
            ngx.print(res.body)
            res = ngx.location.capture("/get");
            ngx.print(res.body)
        ';
    }
--- request
    GET /main
--- response_body eval
qq{+OK\r
\*0\r
}
--- no_error_log
[error]



=== TEST 4: \r\n in content: sanity check
--- config
    location /set {
        redis2_query set A 'hello\r\nworld';
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /get {
        redis2_query get A;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /main {
        content_by_lua '
            local res = ngx.location.capture("/set");
            ngx.print(res.body)
            res = ngx.location.capture("/get");
            ngx.print(res.body)
        ';
    }
--- request
    GET /main
--- response_body eval
qq{+OK\r
\$12\r
hello\r
world\r
}
--- no_error_log
[error]



=== TEST 5: github issue #5: Get content from Redis with "\r\n" in text fails with "Redis server returned extra bytes
--- config
    location /set {
        redis2_query flushall;
        redis2_query hset "/step2redisallhget?auctionid=2l" "content" "a 1line \r\n and 2nd";
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /get {
        redis2_query hget "/step2redisallhget?auctionid=2l" "content";
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /main {
        content_by_lua '
            local res = ngx.location.capture("/set");
            ngx.print(res.body)
            res = ngx.location.capture("/get");
            ngx.print(res.body)
        ';
    }
--- request
    GET /main
--- response_body eval
qq{+OK\r
:1\r
\$18\r\na 1line \r\n and 2nd\r
}
--- no_error_log
[error]



=== TEST 6: set empty param
--- config
    location /set {
        redis2_query flushall;
        redis2_query set foo "";
        redis2_query get foo;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /set
--- response_body eval
qq{+OK\r
+OK\r
\$0\r
\r
}
--- no_error_log
[error]



=== TEST 7: two pipelined multi-bulk replies
--- config
    location /set {
        redis2_query subscribe foo;
        redis2_query unsubscribe foo;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /set
--- response_body eval
qq{*3\r
\$9\r
subscribe\r
\$3\r
foo\r
:1\r
*3\r
\$11\r
unsubscribe\r
\$3\r
foo\r
:0\r
}
--- no_error_log
[error]



=== TEST 8: empty multi-bulk replies
--- config
    location /set {
        redis2_query flushall;
        redis2_query blpop key 1;
        redis2_query set key 3;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /set
--- response_body eval
qq{+OK\r
*-1\r
+OK\r
}
--- timeout: 5
--- no_error_log
[error]

