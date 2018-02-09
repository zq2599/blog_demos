# vi:ft=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (2 * blocks() + 1);

$ENV{TEST_NGINX_REDIS_PORT} ||= 6379;

#master_on;
#worker_connections 1024;

#no_diff;

#log_level 'warn';

run_tests();

__DATA__

=== TEST 1: no query
--- config
    location /foo {
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /foo
--- response_body_like: 500 Internal Server Error
--- error_code: 500



=== TEST 2: empty query
--- config
    location /foo {
        redis2_literal_raw_query "";
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /foo
--- response_body_like: 500 Internal Server Error
--- error_code: 500



=== TEST 3: simple set query
--- config
    location /foo {
        redis2_query set one first;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /foo
--- response_body eval
"+OK\r\n"



=== TEST 4: simple get query
--- config
    location /foo {
        redis2_literal_raw_query 'get not_exist_yet\r\n';
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /foo
--- response_body eval
"+OK\r\n"
--- SKIP



=== TEST 5: bad command
--- config
    location /foo {
        redis2_literal_raw_query 'bad_cmd\r\n';
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /foo
--- response_body eval
"-ERR unknown command 'bad_cmd'\r\n"



=== TEST 6: integer reply
--- config
    location /foo {
        redis2_literal_raw_query 'incr counter\r\n';
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /foo
--- response_body_like: ^:\d+\r\n$



=== TEST 7: bulk reply
--- config
    location /foo {
        redis2_literal_raw_query 'get not_exist_yet\r\n';
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /foo
--- response_body eval
"\$-1\r\n"



=== TEST 8: bulk reply
--- config
    location /set {
        redis2_literal_raw_query '*3\r\n$3\r\nset\r\n$3\r\none\r\n$5\r\nfirst\r\n';
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /get {
        redis2_literal_raw_query '*2\r\n$3\r\nget\r\n$3\r\none\r\n';
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /main {
        echo_location /set;
        echo_location /get;
    }
--- request
    GET /main
--- response_body eval
"+OK\r\n\$5\r\nfirst\r\n"



=== TEST 9: bulk reply
--- config
    location /set {
        redis2_query set one first;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /get {
        redis2_query get one;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /main {
        echo_location /set;
        echo_location /get;
    }
--- request
    GET /main
--- stap2
global active = 0
global pool

F(ngx_http_init_request) {
    println("init req")
    active = 1
}

M(create-pool-done) {
    if (active) {
        printf("create pool: %p\n", $arg1)
        print_ubacktrace()
        active = 0;
        pool = $arg1
    }
}

F(ngx_destroy_pool) {
    if ($pool == pool) {
        printf("destroy: %p\n", $pool)
        print_ubacktrace()
    }
}

probe process("/lib64/libc.so.6").function("free") {
    if ($mem == pool) {
        printf("free: %p\n", $mem)
    }
}
--- stap2
global pools

M(create-pool-done) {
    key = sprintf("%p", $arg1)
    pools[key] = ubacktrace()

    # FIXME: the following line is necessary, and i don't know why
    key = sprintf("%p", $arg1)
    printf("create pool: %s\n", key)

    #print_ubacktrace()
    #print_ustack(ubacktrace())
    #printf("%s", sprint_ubacktrace())
}

F(ngx_destroy_pool) {
    key = sprintf("%p", $pool)
    delete pools[key]
    printf("destroy pool: %s\n", key)
}

probe end {
    println("leaked pools:\n")
    foreach (k in pools) {
        printf("%s: [%s]\n", k, pools[k])
    }
}

--- response_body eval
"+OK\r\n\$5\r\nfirst\r\n"



=== TEST 10: bulk reply (empty)
--- config
    location /set {
        set $empty '';
        redis2_query set one $empty;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /get {
        redis2_query get one;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /main {
        echo_location /set;
        echo_location /get;
    }
--- request
    GET /main
--- response_body eval
"+OK\r\n\$0\r\n\r\n"



=== TEST 11: multi bulk reply (empty)
--- config
    location /set_foo {
        set $empty '';
        redis2_query set foo $empty;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /set_bar {
        set $empty '';
        redis2_query set bar $empty;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /mget {
        redis2_literal_raw_query 'mget foo bar\r\n';
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /main {
        echo_location /set_foo;
        echo_location /set_bar;
        echo_location /mget;
    }
--- request
    GET /main
--- response_body eval
"+OK\r
+OK\r
*2\r
\$0\r
\r
\$0\r
\r
"



=== TEST 12: multi bulk reply (empty)
--- config
    location /main {
        set $query 'ping\r\n';
        redis2_raw_query $query;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
    GET /main
--- response_body eval
"+PONG\r\n"



=== TEST 13: multi bulk reply (empty)
--- http_config
    upstream blah {
        server 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- config
    location /main {
        set $query 'ping\r\n';
        redis2_raw_query $query;
        set $backend blah;
        redis2_pass $backend;
    }
--- request
    GET /main
--- response_body eval
"+PONG\r\n"



=== TEST 14: eval compatibility
--- config
    location /main {
        default_type 'application/octet-stream';
        eval $res {
            set $query 'ping\r\n';
            redis2_raw_query $query;
            redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
        }
        echo "[$res]";
    }
--- request
    GET /main
--- response_body eval
"[+PONG\r\n]"
--- timeout: 5
--- SKIP



=== TEST 15: lua compatibility (GET subrequest)
--- config
    location /redis {
        internal;
        set_unescape_uri $query $arg_query;
        redis2_raw_query $query;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /main {
        content_by_lua '
            local res = ngx.location.capture("/redis",
                { args = { query = "ping\\r\\n" } }
            )
            ngx.print("[" .. res.body .. "]")
        ';
    }
--- request
    GET /main
--- response_body eval
"[+PONG\r\n]"



=== TEST 16: lua compatibility (POST subrequest)
--- config
    location /redis {
        internal;
        redis2_raw_query $echo_request_body;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }

    location /main {
        content_by_lua '
            local res = ngx.location.capture("/redis",
                { method = ngx.HTTP_PUT,
                  body = "ping\\r\\n" }
            )
            ngx.print("[" .. res.body .. "]")
        ';
    }
--- request
    GET /main
--- response_body eval
"[+PONG\r\n]"



=== TEST 17: CRLF in data
--- http_config
    upstream backend {
        server 127.0.0.1:$TEST_NGINX_REDIS_PORT;
        keepalive 1;
    }
--- config
    location /a {
        redis2_literal_raw_query '*3\r\n$3\r\nset\r\n$4\r\ncrlf\r\n$2\r\n\r\n\r\n';
        redis2_pass backend;
    }
    location /b {
        redis2_literal_raw_query 'get crlf\r\n';
        redis2_pass backend;
    }
    location /main {
        echo_location /a;
        echo_location /b;
    }
--- request
    GET /main
--- response_body eval
"+OK\r\n\$2\r\n\r\n\r\n"



=== TEST 18: Unicode chars in data
--- http_config
    upstream backend {
        server 127.0.0.1:$TEST_NGINX_REDIS_PORT;
        keepalive 1;
    }
--- config
    location /a {
        redis2_literal_raw_query '*3\r\n$3\r\nset\r\n$4\r\ncrlf\r\n$6\r\n亦春\r\n';
        redis2_pass backend;
    }
    location /b {
        redis2_literal_raw_query 'get crlf\r\n';
        redis2_pass backend;
    }
    location /main {
        echo_location /a;
        echo_location /b;
    }
--- request
    GET /main
--- response_body eval
"+OK\r\n\$6\r\n亦春\r\n"



=== TEST 19: advanced query
--- config
    location /a {
        redis2_query set hello world;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
    location /b {
        redis2_query get hello;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
    location /main {
        echo_location /a;
        echo_location /b;
    }
--- request
    GET /main
--- response_body eval
"+OK\r\n\$5\r\nworld\r\n"



=== TEST 20: request body
--- config
    location /t {
        # these two settings must be the same to prevent
        # automatic buffering large request bodies
        # to temp files:
        client_body_buffer_size 8k;
        client_max_body_size 8k;

        redis2_query flushall;
        redis2_query lpush q1 $echo_request_body;
        redis2_query lpop q1;
        redis2_pass 127.0.0.1:$TEST_NGINX_REDIS_PORT;
    }
--- request
POST /t
hello world
--- response_body eval
"+OK\r\n:1\r\n\$11\r\nhello world\r\n"
--- no_error_log
[error]

