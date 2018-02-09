# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);

plan tests => repeat_each() * (4 * blocks() + 4);

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

no_long_string();
no_shuffle();

run_tests();

__DATA__

=== TEST 1: basic fetch
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc;
        srcache_store PUT /memc;

        echo hello;
    }

    location /memc {
        internal;

        set $memc_key 'foooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo';
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
Content-Length:
--- response_body
hello
--- timeout: 15



=== TEST 2: internal redirect in fetch subrequest
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /fetch;

        echo hello;
    }
    location /fetch {
        echo_exec /bar;
    }
    location /bar {
        default_type 'text/css';
        echo "HTTP/1.1 200 OK\r\nContent-Type: text/css\r\n\r\nbar";
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
Content-Length: 4
--- response_body
bar



=== TEST 3: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 4: internal redirect in main request (no caching happens) (cache miss)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        echo_exec /bar;
    }

    location /bar {
        default_type text/javascript;
        echo hello;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/javascript
! Content-Length
--- response_body
hello



=== TEST 5: internal redirect happends in the main request (cache miss as well)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        #srcache_store PUT /memc $uri;

        echo world;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
!Content-Length
--- response_body
world



=== TEST 6: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 7: internal redirect in store subrequest
--- config
    location /foo {
        default_type text/css;
        srcache_store GET /store;

        echo blah;
    }
    location /store {
        echo_exec /set-value;
    }
    location /set-value {
        set $memc_key foo;
        set $memc_value "HTTP/1.0 201 Created\r\nContent-Type: text/blah\r\n\r\nbar";
        set $memc_cmd set;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/css
!Content-Length
--- response_body
blah



=== TEST 8: internal redirect in store subrequest (check if value has been stored)
--- config
    location /foo {
        default_type text/css;
        srcache_fetch GET /fetch;

        echo blah;
    }
    location /fetch {
        set $memc_key foo;
        set $memc_cmd get;

        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- response_headers
Content-Type: text/blah
Content-Length: 3
--- response_body chop
bar
--- error_code: 201



=== TEST 9: skipped in subrequests
--- config
    location /sub {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        echo hello;
    }

    location /main {
        echo_location /sub;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 128.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /main
--- response_headers
Content-Type: text/plain
Content-Length:
--- response_body
hello



=== TEST 10: multi-buffer response resulted in incorrect request length header
--- config
    location /foo {
        default_type text/css;
        srcache_store POST /store;

        echo hello;
        echo world;
    }

    location /store {
        content_by_lua '
            ngx.log(ngx.WARN, "srcache_store: request Content-Length: ", ngx.var.http_content_length)
            -- local body = ngx.req.get_body_data()
            -- ngx.log(ngx.WARN, "srcache_store: request body len: ", #body)
        ';
    }
--- request
GET /foo
--- response_headers
!Content-Length
--- response_body
hello
world
--- error_log
srcache_store: request Content-Length: 55



=== TEST 11: read timed out when receiving upstream response body in srcache_fetch
--- config
    memc_read_timeout 200ms;
    location /foo {
        satisfy any;

        default_type text/css;

        srcache_fetch GET /memc $uri;
        #srcache_store PUT /memc $uri;

        echo I do like you;
    }

    location /memc {
        internal;

        set $memc_key 'foo';
        #set $memc_exptime 300;
        memc_pass 127.0.0.1:19112; #$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- stap
F(ngx_http_upstream_finalize_request) {
    printf("upstream fin req: error=%d eof=%d rc=%d\n",
        $r->upstream->peer->connection->read->error,
        $r->upstream->peer->connection->read->eof,
        $rc)
    #print_ubacktrace()
}
F(ngx_connection_error) {
    printf("conn err: %d: %s\n", $err, user_string($text))
    #print_ubacktrace()
}
F(ngx_http_srcache_fetch_post_subrequest) {
    printf("post subreq: rc=%d, status=%d\n", $rc, $r->headers_out->status)
    #print_ubacktrace()
}
F(ngx_http_finalize_request) {
    printf("finalize: %d\n", $rc)
}

--- stap_out_like chop
^finalize: -4
conn err: 110: upstream timed out
upstream fin req: error=0 eof=0 rc=504
finalize: 0
post subreq: rc=0, status=200
finalize: 0$

--- tcp_listen: 19112
--- tcp_no_close
--- tcp_reply eval
"VALUE foo 0 1024\r\nHTTP/1.1 200 OK\r\n\r\nhello world"
--- response_headers
Content-Type: text/css
--- response_body
I do like you
--- error_log
upstream timed out



=== TEST 12: exit(ngx.ERROR) in srcache_fetch
--- config
    memc_read_timeout 200ms;
    location /foo {
        satisfy any;

        default_type text/css;

        srcache_fetch GET /sub;
        #srcache_store PUT /memc $uri;

        echo I do like you;
    }

    location /sub {
        content_by_lua '
            ngx.exit(ngx.ERROR)
        ';
    }
--- request
GET /foo
--- stap2
F(ngx_http_upstream_finalize_request) {
    printf("upstream fin req: error=%d eof=%d rc=%d\n",
        $r->upstream->peer->connection->read->error,
        $r->upstream->peer->connection->read->eof,
        $rc)
    print_ubacktrace()
}
F(ngx_connection_error) {
    printf("conn err: %d: %s\n", $err, user_string($text))
    #print_ubacktrace()
}
F(ngx_http_srcache_fetch_post_subrequest) {
    printf("post subreq: rc=%d, status=%d\n", $rc, $r->headers_out->status)
    #print_ubacktrace()
}
F(ngx_http_finalize_request) {
    printf("finalize: %d\n", $rc)
}
--- response_headers
Content-Type: text/css
--- response_body
I do like you
--- no_error_log
[error]



=== TEST 13: exit(500) in srcache_fetch
--- config
    memc_read_timeout 200ms;
    location /foo {
        satisfy any;

        default_type text/css;

        srcache_fetch GET /sub;
        #srcache_store PUT /memc $uri;

        echo I do like you;
    }

    location /sub {
        return 500;
    }
--- request
GET /foo
--- stap2
F(ngx_http_upstream_finalize_request) {
    printf("upstream fin req: error=%d eof=%d rc=%d\n",
        $r->upstream->peer->connection->read->error,
        $r->upstream->peer->connection->read->eof,
        $rc)
    print_ubacktrace()
}
F(ngx_connection_error) {
    printf("conn err: %d: %s\n", $err, user_string($text))
    #print_ubacktrace()
}
F(ngx_http_srcache_fetch_post_subrequest) {
    printf("post subreq: rc=%d, status=%d\n", $rc, $r->headers_out->status)
    #print_ubacktrace()
}
F(ngx_http_finalize_request) {
    printf("finalize: %d\n", $rc)
}
--- response_headers
Content-Type: text/css
--- response_body
I do like you
--- no_error_log
[error]



=== TEST 14: upstream closes the connection prematurely in srcache_fetch
--- config
    memc_read_timeout 200ms;
    location /foo {
        satisfy any;

        default_type text/css;

        srcache_fetch GET /memc $uri;
        #srcache_store PUT /memc $uri;

        echo I do like you;
    }

    location /memc {
        internal;

        set $memc_key 'foo';
        #set $memc_exptime 300;
        memc_pass 127.0.0.1:19112; #$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /foo
--- stap
F(ngx_http_upstream_finalize_request) {
    printf("upstream fin req: error=%d eof=%d rc=%d\n",
        $r->upstream->peer->connection->read->error,
        $r->upstream->peer->connection->read->eof,
        $rc)
    #print_ubacktrace()
}
F(ngx_connection_error) {
    printf("conn err: %d: %s\n", $err, user_string($text))
    #print_ubacktrace()
}
F(ngx_http_srcache_fetch_post_subrequest) {
    printf("post subreq: rc=%d, status=%d\n", $rc, $r->headers_out->status)
    #print_ubacktrace()
}
F(ngx_http_finalize_request) {
    printf("finalize: %d\n", $rc)
}
--- stap_out_like
finalize: -4
(?:upstream fin req: error=0 eof=1 rc=502
finalize: 0
post subreq: rc=0, status=200
|conn err: \d+: writev\(\) failed
upstream fin req: error=0 eof=0 rc=502
finalize: (?:0|502)
post subreq: rc=(?:0|502), status=(?:0|200)
(?:finalize: 0
)?)?finalize: 0

--- tcp_listen: 19112
--- tcp_reply eval
"VALUE foo 0 1024\r\nHTTP/1.1 200 OK\r\n\r\nhello world"
--- response_headers
Content-Type: text/css
--- response_body
I do like you
--- wait: 0.1
--- error_log
upstream prematurely closed connection



=== TEST 15: exit(ngx.ERROR) in srcache_store
--- config
    memc_read_timeout 200ms;
    location /foo {
        satisfy any;

        default_type text/css;

        srcache_store PUT /sub;

        echo I do like you;
    }

    location /sub {
        content_by_lua '
            ngx.exit(ngx.ERROR)
        ';
    }
--- request
GET /foo
--- stap
F(ngx_http_upstream_finalize_request) {
    printf("upstream fin req: error=%d eof=%d rc=%d\n",
        $r->upstream->peer->connection->read->error,
        $r->upstream->peer->connection->read->eof,
        $rc)
    print_ubacktrace()
}
F(ngx_connection_error) {
    printf("conn err: %d: %s\n", $err, user_string($text))
    #print_ubacktrace()
}
F(ngx_http_srcache_store_post_subrequest) {
    printf("post subreq: rc=%d, status=%d\n", $rc, $r->headers_out->status)
    #print_ubacktrace()
}
F(ngx_http_finalize_request) {
    printf("finalize: %d\n", $rc)
}

--- stap_out
finalize: 0
finalize: -1
post subreq: rc=-1, status=0
finalize: 0
--- response_headers
Content-Type: text/css
--- response_body
I do like you
--- error_log
srcache_store subrequest failed: rc=-1 status=0



=== TEST 16: exit(500) in srcache_store
--- config
    memc_read_timeout 200ms;
    location /foo {
        satisfy any;

        default_type text/css;

        srcache_store PUT /sub;

        echo I do like you;
    }

    location /sub {
        return 500;
    }
--- request
GET /foo
--- stap
F(ngx_http_upstream_finalize_request) {
    printf("upstream fin req: error=%d eof=%d rc=%d\n",
        $r->upstream->peer->connection->read->error,
        $r->upstream->peer->connection->read->eof,
        $rc)
    print_ubacktrace()
}
F(ngx_connection_error) {
    printf("conn err: %d: %s\n", $err, user_string($text))
    #print_ubacktrace()
}
F(ngx_http_srcache_store_post_subrequest) {
    printf("post subreq: rc=%d, status=%d\n", $rc, $r->headers_out->status)
    #print_ubacktrace()
}
F(ngx_http_finalize_request) {
    printf("finalize: %d\n", $rc)
}

--- stap_out
finalize: 0
finalize: 500
post subreq: rc=500, status=0
finalize: 0

--- response_headers
Content-Type: text/css
--- response_body
I do like you
--- error_log
srcache_store subrequest failed: rc=500 status=0,



=== TEST 17: skipped in subrequests
--- config
    location /t {
        srcache_fetch GET /f;
        echo hello;
    }

    location /f {
        echo "HTTP/1.1 200 OK\r\n\r\ncached data";
        body_filter_by_lua '
            if ngx.arg[2] then
                ngx.arg[2] = false
            end
        ';
    }
--- request
GET /t
--- response_body
hello
--- error_log
srcache_fetch: cache sent truncated response body
--- no_error_log
[alert]



=== TEST 18: store subrequest failure
--- http_config
    upstream local {
        server localhost:$TEST_NGINX_MEMCACHED_PORT;
    }

--- config
    memc_connect_timeout 100ms;
    memc_send_timeout 100ms;
    memc_read_timeout 100ms;

    location = /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 1d;
        memc_pass local;
    }

    location = /tblah {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        proxy_pass http://127.0.0.1:$server_port/back;
    }

    location = /back {
        echo ok;
    }
--- request
    GET /tblah
--- response_body
ok
--- error_log
variable "$memc_exptime" takes invalid value: 1d,
srcache_store subrequest failed: rc=400 status=0,

