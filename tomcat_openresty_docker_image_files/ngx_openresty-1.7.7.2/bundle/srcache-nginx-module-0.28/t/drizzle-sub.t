# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket skip_all => 'subrequests not working';

#repeat_each(2);

plan tests => repeat_each() * 3 * blocks();

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



=== TEST 2: basic fetch (cache miss)
--- http_config
    upstream backend {
        drizzle_server 127.0.0.1:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
    }
--- config
    location /main {
        echo_location /cats dir=asc;
        echo_location /cats dir=desc;
    }

    location /cats {
        internal;

        srcache_fetch GET /memc $uri-$arg_dir;
        srcache_store PUT /memc $uri-$arg_dir;

        default_type text/css;

        drizzle_pass backend;
        drizzle_query 'select * from cats order by id $arg_dir';

        rds_json on;
    }

    location /memc {
        internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- request
GET /main
--- response_headers
Content-Type: text/plain
--- response_body chomp
[{"id":2,"name":null},{"id":3,"name":"bob"}][{"id":3,"name":"bob"},{"id":2,"name":null}]



=== TEST 3: basic fetch (cache hit)
--- http_config
    upstream backend {
        drizzle_server 127.0.0.1:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
    }
--- config
    location /main {
        echo_location /cats dir=asc;
        echo_location /cats dir=desc;
    }

    location /cats {
        internal;

        srcache_fetch GET /memc $uri-$arg_dir;
        srcache_store PUT /memc $uri-$arg_dir;

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
GET /main
--- response_headers
Content-Type: text/plain
--- response_body chomp
[{"id":2,"name":null},{"id":3,"name":"bob"}][{"id":3,"name":"bob"},{"id":2,"name":null}]

