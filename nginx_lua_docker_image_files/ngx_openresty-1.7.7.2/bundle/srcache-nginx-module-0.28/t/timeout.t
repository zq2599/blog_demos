# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (4 * blocks() - 2);

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#master_on();
no_shuffle();

run_tests();

__DATA__

=== TEST 1: basic fetch (cache miss)
--- config
    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
       root   html;
    }

    location = /foo {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        proxy_pass http://127.0.0.1:$server_port/hello;
    }

    location = /hello {
        echo hello;
        default_type text/css;
    }

    memc_connect_timeout 1ms;
    location = /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass www.google.com:1234;
    }
--- user_files
>>> 50x.html
bad bad
--- request
GET /foo
--- response_headers
Content-Type: text/css
!Content-Length
--- response_body
hello



=== TEST 2: main req upstream truncation (with content-length)
--- config
    location = /t {
        srcache_store PUT /err;

        proxy_read_timeout 100ms;
        proxy_pass http://127.0.0.1:11945/echo;
    }

    location = /err {
        content_by_lua '
            ngx.log(ngx.ERR, "location /err is called")
        ';
    }

--- tcp_listen: 11945
--- tcp_no_close
--- tcp_reply eval
"HTTP/1.0 200 OK\r\nContent-Length: 120\r\n\r\nhello world"

--- request
GET /t
--- ignore_response
--- no_error_log
location /err is called
--- no_error_log
srcache_store: subrequest returned status



=== TEST 3: main req upstream truncation (without content-length)
--- config
    location = /t {
        srcache_store PUT /err;

        proxy_read_timeout 100ms;
        proxy_pass http://127.0.0.1:11945/echo;
    }

    location = /err {
        content_by_lua '
            ngx.log(ngx.ERR, "location /err is called")
        ';
    }

--- tcp_listen: 11945
--- tcp_no_close
--- tcp_reply eval
"HTTP/1.0 200 OK\r\n\r\nhello world"

--- request
GET /t
--- ignore_response
--- no_error_log
location /err is called
srcache_store: subrequest returned status
[alert]
[crit]

--- error_log
upstream timed out

