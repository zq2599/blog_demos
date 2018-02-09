# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(100);

plan tests => repeat_each() * 3 * blocks();

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;
$ENV{TEST_NGINX_POSTGRESQL_PORT} ||= 5432;

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



=== TEST 2: cache miss
--- http_config
    upstream backend {
        postgres_server 127.0.0.1:$TEST_NGINX_POSTGRESQL_PORT
                       dbname=ngx_test user=ngx_test password=ngx_test;
    }
--- config
    location /cats {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        default_type text/css;

        postgres_pass backend;
        postgres_query 'select * from cats';

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



=== TEST 3: cache hit
--- http_config
    upstream backend {
        postgres_server 127.0.0.1:$TEST_NGINX_POSTGRESQL_PORT
                       dbname=ngx_test user=ngx_test password=ngx_test;
    }
--- config
    location /cats {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        default_type text/css;

        postgres_pass backend;
        postgres_query 'invalid sql here';

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



=== TEST 4: SSL packet issue (bug)
--- SKIP
--- http_config
    upstream backend {
        postgres_server 127.0.0.1:$TEST_NGINX_POSTGRESQL_PORT
                       dbname=ngx_test user=ngx_test password=ngx_test;
    }
--- config
    location /cats {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;

        default_type application/json;

        postgres_escape $token $arg_token;
        postgres_escape $limit $arg_limit;
        postgres_pass backend;
        postgres_query HEAD GET "select $token,$limit";

        rds_json on;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /cats?token=3&limit=10
--- response_headers
Content-Type: text/css
--- response_body chomp
[{"id":2,"name":null},{"id":3,"name":"bob"}]
--- SKIP

