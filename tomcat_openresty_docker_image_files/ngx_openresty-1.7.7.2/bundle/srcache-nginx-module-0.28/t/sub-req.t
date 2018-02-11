# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket skip_all => 'subrequests not working';
;

no_long_string();
#repeat_each(2);

plan tests => repeat_each() * 2 * blocks();

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

run_tests();

__DATA__

=== TEST 1: simple fetch
--- config
    location /main {
        echo_location /pre /foo;
        echo_location /foo;
        echo_location /foo;
    }

    location /foo {
        srcache_fetch GET /memc $uri;

        echo $echo_incr;
    }

    location /pre {
        set $memc_cmd 'set';
        set $memc_key $query_string;
        set $memc_value "hello\n";
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }

    location /memc {
        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /main
--- response_body eval
"STORED\r
hello
hello
"



=== TEST 2: simple fetch (without fetch)
--- config
    location /main {
        echo_location /foo?1;
        echo_location /foo?2;
    }

    location /foo {
        srcache_store PUT /memc $uri;

        echo $echo_incr;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /main
--- response_body
1
2



=== TEST 3: simple fetch (flush fetch)
--- config
    location /main {
        echo_location /flush;
        echo_location /bar;
        echo_location /flush;
        echo_location /bar;
    }

    location /bar {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        echo $echo_incr;
    }

    location /flush {
        internal;
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /main
--- response_body eval
"OK\r
1
OK\r
2
"



=== TEST 4: fetch & store
--- config
    location /main {
        echo_location /flush;
        echo_location /bar;
        echo_location /bar;
        echo_location /bar;
    }

    location /bar {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        echo $echo_incr;
    }

    location /flush {
        internal;
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /main
--- response_body eval
"OK\r
1
1
1
"
--- timeout: 2



=== TEST 5: fetch & store (mixture of echo_location & echo_location_async)
--- config
    location /main {
        echo_location /flush;
        #echo_location /bar?;
        echo_location /group?a=1&b=2;
        echo_location_async /group?a=3&b=4;
    }

    location /group {
        #echo_location /bar;
        echo_location /bar $arg_a;
        echo_location_async /bar $arg_b;
    }

    location /bar {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        echo $query_string;
    }

    location /flush {
        internal;
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }

    location /memc {
        #internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /main
--- response_body eval
"OK\r
1
1
1
1
"
--- timeout: 2



=== TEST 6: deep nested echo_location/echo_location_async
--- config
    location /main {
        echo_location /flush;
        echo_location /bar;
        echo_location_async /bar;
        echo_location_async /bar;
        echo_location /group;
        echo_location_async /group;
    }

    location /group {
        echo_location /bar;
        echo_location_async /bar;
    }

    location /bar {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        echo $echo_incr;
    }

    location /flush {
        internal;
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /main
--- response_body eval
"OK\r
1
1
1
1
1
1
1
"
--- timeout: 2
--- SKIP



=== TEST 7: deep nested pure echo_location
--- config
    location /main {
        echo_location /flush;
        echo_location /bar;
        echo_location /bar;
        echo_location /bar;
        echo_location /group;
        echo_location /group;
    }

    location /group {
        echo_location /bar;
        echo_location /bar;
    }

    location /bar {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        echo $echo_incr;
    }

    location /flush {
        internal;
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /main
--- response_body eval
"OK\r
1
1
1
1
1
1
1
"
--- timeout: 2



=== TEST 8: deep nested echo_location/echo_location_async
--- config
    location /main {
        echo_location /flush;
        echo_location /bar;
        echo_location_async /bar;
        echo_location_async /bar;
        echo_location_async /group;
        echo_location_async /group;
    }

    location /group {
        echo_location_async /bar;
        echo_location_async /bar;
    }

    location /bar {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        echo $echo_incr;
    }

    location /flush {
        internal;
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /main
--- response_body eval
"OK\r
1
1
1
1
1
1
1
"
--- timeout: 2



=== TEST 9: deep nested echo_location/echo_location_async
--- config
    location /main {
        echo_location /flush;
        echo_location /bar;
        echo_location_async /bar;
        echo_location /group;
    }

    location /group {
        echo_location /bar;
        echo_location_async /bar;
    }

    location /bar {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        echo $echo_incr;
    }

    location /flush {
        internal;
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /main
--- response_body eval
"OK\r
1
1
1
1
"
--- timeout: 1



=== TEST 10: simple store
--- config
    location /main {
        echo_location /flush;
        echo_location /bar;
    }

    location /bar {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        echo $echo_incr;
    }

    location /flush {
        internal;
        set $memc_cmd 'flush_all';
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /main
--- response_body eval
"OK\r
1
"

