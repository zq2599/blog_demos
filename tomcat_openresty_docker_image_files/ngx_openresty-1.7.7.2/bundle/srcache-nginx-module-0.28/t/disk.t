# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(100);

plan tests => repeat_each() * (2 * blocks() + 1);

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

no_shuffle();

run_tests();

__DATA__

=== TEST 1: flush all
--- config
    location /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 2: cache miss
--- config
    location /index.html {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /index.html
--- response_headers
Accept-Ranges: bytes
--- response_body_like: It works!



=== TEST 3: cache hit
--- config
    location /index.html {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /index.html
--- response_body_like: It works!

