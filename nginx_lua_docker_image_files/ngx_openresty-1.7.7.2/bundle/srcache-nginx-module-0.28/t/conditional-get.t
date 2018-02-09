# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(100);

plan tests => repeat_each() * (6 * blocks());

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;
#$ENV{TEST_NGINX_MYSQL_PORT}     ||= 3306;

no_shuffle();

run_tests();

__DATA__

=== TEST 1: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
!Last-Modified
--- request
GET /flush
--- response_body eval: "OK\r\n"
--- no_error_log
[error]



=== TEST 2: cache miss
--- config
    location /cats {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        default_type application/json;

        content_by_lua '
            ngx.header.last_modified = "Thu, 10 May 2012 07:50:59 GMT"
            ngx.say("hello")
        ';
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 3000;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /cats
--- response_headers
Content-Type: application/json
Last-Modified: Thu, 10 May 2012 07:50:59 GMT
!Content-Length
--- response_body
hello
--- no_error_log
[error]



=== TEST 3: cache hit
--- config
    location /cats {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        default_type text/css;

        proxy_pass http://agentzh.org:12345/;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 3000;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /cats
--- response_headers
Content-Type: application/json
Content-Length: 6
Last-Modified: Thu, 10 May 2012 07:50:59 GMT
--- response_body
hello
--- no_error_log
[error]



=== TEST 4: cache hit (I-M-S conditional GET, exact)
--- config
    location /cats {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        default_type text/css;

        proxy_pass http://agentzh.org:12345/;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 3000;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /cats
--- more_headers
If-Modified-Since: Thu, 10 May 2012 07:50:59 GMT
--- response_headers
!Content-Type
!Content-Length
Last-Modified: Thu, 10 May 2012 07:50:59 GMT
--- stap2
F(ngx_http_core_content_phase) {
    printf("r content handler: %s\n", usymname($r->content_handler))
}

--- error_code: 304
--- response_body
--- no_error_log
[error]



=== TEST 5: cache hit (I-M-S conditional GET, exact failed)
--- config
    location /cats {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        default_type text/css;

        proxy_pass http://agentzh.org:12345/;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 3000;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /cats
--- more_headers
If-Modified-Since: Thu, 10 May 2012 07:51:00 GMT
--- response_headers
Content-Type: application/json
Content-Length: 6
Last-Modified: Thu, 10 May 2012 07:50:59 GMT
--- response_body
hello
--- no_error_log
[error]



=== TEST 6: cache hit (I-M-S conditional GET, exact failed, before suceeded)
--- config
    location /cats {
        if_modified_since before;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        default_type text/css;

        proxy_pass http://agentzh.org:12345/;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 3000;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /cats
--- more_headers
If-Modified-Since: Thu, 10 May 2012 07:51:00 GMT
--- response_headers
!Content-Type
!Content-Length
Last-Modified: Thu, 10 May 2012 07:50:59 GMT
--- response_body
--- error_code: 304
--- no_error_log
[error]



=== TEST 7: cache hit (I-U-S conditional GET, 412)
--- config
    server_tokens off;
    location /cats {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        default_type text/css;

        proxy_pass http://agentzh.org:12345/;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 3000;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /cats
--- more_headers
If-Unmodified-Since: Thu, 10 May 2012 07:50:58 GMT
--- response_headers
Content-Type: text/html
Content-Length: 182
!Last-Modified
--- response_body_like: 412 Precondition Failed
--- error_code: 412
--- no_error_log
[error]



=== TEST 8: cache hit (I-U-S conditional GET, precondition succeeded)
--- config
    location /cats {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        default_type text/css;

        proxy_pass http://agentzh.org:12345/;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 3000;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /cats
--- more_headers
If-Unmodified-Since: Thu, 10 May 2012 07:50:59 GMT
--- response_headers
Content-Type: application/json
Content-Length: 6
Last-Modified: Thu, 10 May 2012 07:50:59 GMT
--- response_body
hello
--- no_error_log
[error]



=== TEST 9: cache hit (I-U-S conditional GET, precondition succeeded, newer)
--- config
    location /cats {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        default_type text/css;

        proxy_pass http://agentzh.org:12345/;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 3000;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /cats
--- more_headers
If-Unmodified-Since: Thu, 10 May 2012 07:51:00 GMT
--- response_headers
Content-Type: application/json
Content-Length: 6
Last-Modified: Thu, 10 May 2012 07:50:59 GMT
--- response_body
hello
--- no_error_log
[error]

