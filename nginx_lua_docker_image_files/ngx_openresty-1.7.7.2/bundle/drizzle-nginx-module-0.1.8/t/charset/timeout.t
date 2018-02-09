# vi:ft=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);
repeat_each(1);

plan tests => repeat_each() * blocks() * 2;

our $http_config = <<'_EOC_';
    upstream foo {
        drizzle_server www.taobao.com:1234;
    }
_EOC_

worker_connections(128);
#log_level('error');

$ENV{TEST_NGINX_MYSQL_PORT} ||= 3306;

no_diff();

run_tests();

__DATA__

=== TEST 1: loc_config connect timeout
--- http_config eval: $::http_config
--- config
    location /upstream {
        set $backend foo;
        drizzle_pass $backend;
        drizzle_module_header off;
        drizzle_query 'select * from xx';
        drizzle_connect_timeout 10ms;
    }
--- request
GET /upstream
--- error_code: 504
--- response_body_like: 504 Gateway Time-out
--- timeout: 0.5



=== TEST 2: http_config connect timeout
--- http_config eval: $::http_config
--- config
    drizzle_connect_timeout 3;
    location /upstream {
        set $backend foo;
        drizzle_pass $backend;
        drizzle_module_header off;
        drizzle_query 'select * from xx';
        drizzle_connect_timeout 10ms;
    }
--- request
GET /upstream
--- error_code: 504
--- response_body_like: 504 Gateway Time-out
--- timeout: 0.5



=== TEST 3: serv_config connect timeout
--- http_config eval: $::http_config
--- config
    drizzle_connect_timeout 10ms;
    location /upstream {
        set $backend foo;
        drizzle_pass $backend;
        drizzle_module_header off;
        drizzle_query 'select * from xx';
    }
--- request
GET /upstream
--- error_code: 504
--- response_body_like: 504 Gateway Time-out
--- timeout: 0.5



=== TEST 4: serv_config connect timeout
--- http_config
    upstream backend {
        drizzle_server 127.0.0.1:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test
                       charset=utf8;
    }

--- config
    #drizzle_connect_timeout 1;
    drizzle_send_query_timeout 10ms;
    #drizzle_recv_cols_timeout 10ms;
    #drizzle_recv_rows_timeout 10ms;

    location /upstream {
        drizzle_pass backend;
        drizzle_module_header off;
        drizzle_query 'select sql_no_cache * from cats as a, cats as b, cats as c, cats as d, cats as e, cats as f, cats as g, cats as h, cats as i, cats as j order by a.id, b.id, c.id, d.id';
    }
--- request
GET /upstream
--- error_code: 504
--- response_body_like: 504 Gateway Time-out
--- timeout: 1
--- SKIP

