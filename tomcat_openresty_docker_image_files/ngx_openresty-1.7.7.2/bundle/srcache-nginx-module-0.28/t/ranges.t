# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);

plan tests => repeat_each() * (5 * blocks() + 12);

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#master_on();
no_shuffle();
no_long_string();

run_tests();

__DATA__

=== TEST 1: flush all (not using ngx_srcache)
--- config
    location = /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
        add_header X-Store-Status $srcache_store_status;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
X-Fetch-Status: BYPASS
X-Store-Status: BYPASS

--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 2: range fetch (cache miss)
--- config
    location = /index.html {
        default_type text/css;
        srcache_store_ranges on;
        srcache_fetch GET /memc $uri$http_range;
        srcache_store PUT /memc $uri$http_range;

        add_header X-Fetch-Status $srcache_fetch_status;
        add_header X-Store-Status $srcache_store_status;
    }

    location = /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /index.html
--- more_headers
Range: bytes=2-5
--- response_headers
Content-Type: text/html
Content-Length: 4
X-Fetch-Status: MISS
X-Store-Status: STORE
--- response_body chop
tml>
--- wait: 0.1
--- error_log
srcache_store: subrequest returned status 201
--- error_code: 206
--- log_level: debug



=== TEST 3: range fetch (cache hit)
--- config
    location = /index.html {
        default_type text/css;
        srcache_fetch GET /memc $uri$http_range;
        srcache_store PUT /memc $uri$http_range;

        echo world;
        add_header X-Fetch-Status $srcache_fetch_status;
        add_header X-Store-Status $srcache_store_status;
    }

    location = /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /index.html
--- more_headers
Range: bytes=2-5

--- response_headers
X-Fetch-Status: HIT
X-Store-Status: BYPASS
Content-Length: 4
--- response_body chop
tml>
--- error_code: 206



=== TEST 4: full fetch (cache miss)
--- config
    location = /index.html {
        default_type text/css;
        srcache_fetch GET /memc $uri$http_range;
        srcache_store PUT /memc $uri$http_range;

        add_header X-Fetch-Status $srcache_fetch_status;
        add_header X-Store-Status $srcache_store_status;
    }

    location = /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /index.html

--- response_headers
X-Fetch-Status: MISS
X-Store-Status: STORE
Content-Length: 72
--- response_body_like: It works!
--- error_code: 200



=== TEST 5: flush all (not using ngx_srcache)
--- config
    location = /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
        add_header X-Store-Status $srcache_store_status;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
X-Fetch-Status: BYPASS
X-Store-Status: BYPASS

--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 6: full fetch (cache miss)
--- config
    location = /index.html {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        add_header X-Fetch-Status $srcache_fetch_status;
        add_header X-Store-Status $srcache_store_status;
    }

    location = /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /index.html
--- response_headers
Content-Type: text/html
Content-Length: 72
X-Fetch-Status: MISS
X-Store-Status: STORE
--- response_body_like: It works!
--- wait: 0.1
--- error_log
srcache_store: subrequest returned status 201
--- error_code: 200
--- log_level: debug



=== TEST 7: range fetch (cache hit)
--- config
    location = /index.html {
        default_type text/css;
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        echo world;
        add_header X-Fetch-Status $srcache_fetch_status;
        add_header X-Store-Status $srcache_store_status;
    }

    location = /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /index.html
--- more_headers
Range: bytes=2-5

--- response_headers
X-Fetch-Status: HIT
X-Store-Status: BYPASS
Content-Length: 4
--- response_body chop
tml>
--- error_code: 206



=== TEST 8: flush all (not using ngx_srcache)
--- config
    location = /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
        add_header X-Store-Status $srcache_store_status;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
X-Fetch-Status: BYPASS
X-Store-Status: BYPASS

--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 9: range fetch (cache miss) - no store ranges (default off)
--- config
    location = /index.html {
        default_type text/css;
        #srcache_store_ranges on;
        srcache_fetch GET /memc $uri$http_range;
        srcache_store PUT /memc $uri$http_range;

        #add_header X-Fetch-Status $srcache_fetch_status;
        #add_header X-Store-Status $srcache_store_status;
    }

    location = /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /index.html
--- more_headers
Range: bytes=2-5
--- response_headers
Content-Type: text/html
Content-Length: 4
!X-Fetch-Status
!X-Store-Status
--- response_body chop
tml>
--- wait: 0.1
--- no_error_log
srcache_store: subrequest returned status 201
--- error_code: 206
--- log_level: debug



=== TEST 10: range fetch (cache hit)
--- config
    location = /index.html {
        default_type text/css;
        srcache_fetch GET /memc $uri$http_range;
        srcache_store PUT /memc $uri$http_range;

        echo world;
        add_header X-Fetch-Status $srcache_fetch_status;
        add_header X-Store-Status $srcache_store_status;
    }

    location = /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /index.html
--- more_headers
Range: bytes=2-5

--- response_headers
X-Fetch-Status: MISS
X-Store-Status: STORE
!Content-Length
--- response_body
world
--- error_code: 200



=== TEST 11: flush all (not using ngx_srcache)
--- config
    location = /flush {
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        add_header X-Fetch-Status $srcache_fetch_status;
        add_header X-Store-Status $srcache_store_status;
    }
--- response_headers
Content-Type: text/plain
Content-Length: 4
X-Fetch-Status: BYPASS
X-Store-Status: BYPASS

--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 12: range fetch (cache miss) - no store ranges (explicit off)
--- config
    location = /index.html {
        default_type text/css;
        srcache_store_ranges off;
        srcache_fetch GET /memc $uri$http_range;
        srcache_store PUT /memc $uri$http_range;

        #add_header X-Fetch-Status $srcache_fetch_status;
        #add_header X-Store-Status $srcache_store_status;
    }

    location = /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /index.html
--- more_headers
Range: bytes=2-5
--- response_headers
Content-Type: text/html
Content-Length: 4
!X-Fetch-Status
!X-Store-Status
--- response_body chop
tml>
--- wait: 0.1
--- no_error_log
srcache_store: subrequest returned status 201
--- error_code: 206
--- log_level: debug



=== TEST 13: range fetch (cache hit)
--- config
    location = /index.html {
        default_type text/css;
        srcache_fetch GET /memc $uri$http_range;
        srcache_store PUT /memc $uri$http_range;

        echo world;
        add_header X-Fetch-Status $srcache_fetch_status;
        add_header X-Store-Status $srcache_store_status;
    }

    location = /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /index.html
--- more_headers
Range: bytes=2-5

--- response_headers
X-Fetch-Status: MISS
X-Store-Status: STORE
!Content-Length
--- response_body
world
--- error_code: 200

