# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(3);

plan tests => repeat_each() * 2 * blocks();

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
--- request
GET /flush
--- response_body eval: "OK\r\n"



=== TEST 2: cache miss
--- http_config
    upstream backend {
        drizzle_server 127.0.0.1:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
    }

    upstream mem_backend {
        server 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
        #keepalive 100 single;
    }
--- config
    location /test1 {
        srcache_fetch GET /memc $uri;
        srcache_store PUT /memc $uri;
        default_type text/css;
        charset UTF-8;

        drizzle_query "select 1+2 as a;";
        drizzle_pass backend;
        rds_json on;
    }

    location /memc {
        #internal;

        set $memc_key $query_string;
        set $memc_exptime 300;
        memc_pass mem_backend;
    }

    location /pt1 {
        echo "pt1";
    }

    location /proxy {
        eval_subrequest_in_memory off;
        eval_override_content_type text/plain;

        eval $res {
            #proxy_pass http://127.0.0.1:$server_port/test1;
            proxy_pass $scheme://127.0.0.1:$server_port/pt1;
            #echo "ab";
        }

        if ($res ~ '1') {
            #proxy_pass $scheme://127.0.0.1:$server_port/test1;
            #echo_exec /pi;
            echo_exec /test1;
            #echo_exec /pt1;
            #echo "okay $res";
            break;
        }

        echo "[$res] error";
    }

    location /pi {
        proxy_pass $scheme://127.0.0.1:$server_port/test1;
        #echo hi;
    }

--- request
GET /proxy
--- response_body chomp
[{"a":3}]

