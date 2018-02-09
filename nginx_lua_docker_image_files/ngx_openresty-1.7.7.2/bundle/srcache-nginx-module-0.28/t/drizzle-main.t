# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(100);

plan tests => repeat_each() * 4 * blocks();

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;
$ENV{TEST_NGINX_MYSQL_PORT}     ||= 3306;

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
--- request
GET /flush
--- response_body eval: "OK\r\n"
--- no_error_log
[error]



=== TEST 2: cache miss
--- http_config
    upstream backend {
        drizzle_server 127.0.0.1:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
    }
--- config
    location /cats {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        default_type text/css;

        drizzle_pass backend;
        drizzle_query 'select * from cats';

        rds_json on;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /cats
--- response_headers
Content-Type: application/json
--- response_body chomp
[{"id":2,"name":null},{"id":3,"name":"bob"}]
--- no_error_log
[error]



=== TEST 3: cache hit
--- http_config
    upstream backend {
        drizzle_server 127.0.0.1:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
    }
--- config
    location /cats {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        default_type text/css;

        drizzle_pass backend;
        drizzle_query 'invalid sql here';

        rds_json on;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /cats
--- response_headers
Content-Type: application/json
--- response_body chomp
[{"id":2,"name":null},{"id":3,"name":"bob"}]
--- no_error_log
[error]

