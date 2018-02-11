# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(2);

plan tests => repeat_each() * 2 * blocks();

$ENV{TEST_NGINX_MYSQL_PORT} ||= 3306;
$ENV{TEST_NGINX_MYSQL_HOST} ||= '127.0.0.1';

#master_on();

our $http_config = <<'_EOC_';
    upstream backend {
        drizzle_server $TEST_NGINX_MYSQL_HOST:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
        drizzle_keepalive max=10 overflow=reject mode=single;
    }
    upstream backend2 {
        drizzle_server $TEST_NGINX_MYSQL_HOST:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
        #drizzle_keepalive max=10 overflow=ignore mode=single;
    }
    upstream foo {
        server 127.0.0.1:80;
    }
_EOC_

our $http_config2 = <<'_EOC_';
    upstream backend {
        drizzle_server $TEST_NGINX_MYSQL_HOST:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
        drizzle_keepalive max=10 overflow=reject mode=single;
    }
    upstream backend2 {
        drizzle_server $TEST_NGINX_MYSQL_HOST:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
        drizzle_keepalive max=5 overflow=ignore mode=multi;
    }
_EOC_

worker_connections(128);

no_long_string();
#no_diff();

run_tests();

__DATA__

=== TEST 1: sanity
--- http_config eval: $::http_config
--- config
    location /status {
        drizzle_status;
    }
--- request
    GET /status
--- response_body
upstream backend
  active connections: 0
  connection pool capacity: 10
  overflow: reject
  cached connection queue: 0
  free'd connection queue: 10
  cached connection successfully used count:
  free'd connection successfully used count: 0 0 0 0 0 0 0 0 0 0
  servers: 1
  peers: 1

upstream backend2
  active connections: 0
  connection pool capacity: 0
  servers: 1
  peers: 1



=== TEST 2: single mode and no pools
--- http_config eval: $::http_config
--- config
    location @my_err {
        echo "500 Internal Server Error";
    }
    location ~ ^/mysql(2?)$ {
        drizzle_query "select sum(1) from $args";
        drizzle_pass backend$1;
        error_page 500 = @my_err;
        rds_json on;
    }
    location /status {
        drizzle_status;
    }
    location /main {
        echo_location /mysql cats;
        echo_location /mysql cats;
        echo_location /mysql cats;
        echo;

        echo_location /status;

        echo_location /mysql2 cats;
        echo_location /mysql2 cats;
        echo;

        echo_location /status;
    }
--- request
    GET /main
--- response_body
[{"sum(1)":2}][{"sum(1)":2}][{"sum(1)":2}]
upstream backend
  active connections: 1
  connection pool capacity: 10
  overflow: reject
  cached connection queue: 1
  free'd connection queue: 9
  cached connection successfully used count: 3
  free'd connection successfully used count: 0 0 0 0 0 0 0 0 0
  servers: 1
  peers: 1

upstream backend2
  active connections: 0
  connection pool capacity: 0
  servers: 1
  peers: 1
[{"sum(1)":2}][{"sum(1)":2}]
upstream backend
  active connections: 1
  connection pool capacity: 10
  overflow: reject
  cached connection queue: 1
  free'd connection queue: 9
  cached connection successfully used count: 3
  free'd connection successfully used count: 0 0 0 0 0 0 0 0 0
  servers: 1
  peers: 1

upstream backend2
  active connections: 0
  connection pool capacity: 0
  servers: 1
  peers: 1



=== TEST 3: single & multi mode pools
--- http_config eval: $::http_config2
--- config
    location @my_err {
        echo "500 Internal Server Error";
    }
    location ~ ^/mysql(2?)$ {
        drizzle_query "select sum(1) from $args";
        drizzle_pass backend$1;
        error_page 500 = @my_err;
        rds_json on;
    }
    location /status {
        drizzle_status;
    }
    location /main {
        echo_location /mysql cats;
        echo_location /mysql cats;
        echo_location /mysql cats;
        echo;

        echo_location /status;

        echo_location /mysql2 cats;
        echo_location /mysql2 cats;
        echo;

        echo_location /status;
    }
--- request
    GET /main
--- response_body
[{"sum(1)":2}][{"sum(1)":2}][{"sum(1)":2}]
upstream backend
  active connections: 1
  connection pool capacity: 10
  overflow: reject
  cached connection queue: 1
  free'd connection queue: 9
  cached connection successfully used count: 3
  free'd connection successfully used count: 0 0 0 0 0 0 0 0 0
  servers: 1
  peers: 1

upstream backend2
  active connections: 0
  connection pool capacity: 5
  overflow: ignore
  cached connection queue: 0
  free'd connection queue: 5
  cached connection successfully used count:
  free'd connection successfully used count: 0 0 0 0 0
  servers: 1
  peers: 1
[{"sum(1)":2}][{"sum(1)":2}]
upstream backend
  active connections: 1
  connection pool capacity: 10
  overflow: reject
  cached connection queue: 1
  free'd connection queue: 9
  cached connection successfully used count: 3
  free'd connection successfully used count: 0 0 0 0 0 0 0 0 0
  servers: 1
  peers: 1

upstream backend2
  active connections: 1
  connection pool capacity: 5
  overflow: ignore
  cached connection queue: 1
  free'd connection queue: 4
  cached connection successfully used count: 2
  free'd connection successfully used count: 0 0 0 0
  servers: 1
  peers: 1



=== TEST 4: single mode and bad request
--- http_config eval: $::http_config
--- config
    location @my_err {
        echo "500 Internal Server Error";
    }
    location ~ ^/mysql(2?)$ {
        drizzle_query "select sum(1) from $args";
        drizzle_pass backend$1;
        error_page 500 = @my_err;
        rds_json on;
    }
    location /status {
        drizzle_status;
    }
    location /main {
        echo_location /mysql cats;
        echo_location /mysql cats;
        echo_location /mysql cats;
        echo_location /mysql select;
        echo;

        echo_location /status;

        echo_location /mysql cats;
        echo_location /mysql cats;
        echo;

        echo_location /status;
    }
--- request
    GET /main
--- response_body
[{"sum(1)":2}][{"sum(1)":2}][{"sum(1)":2}]500 Internal Server Error

upstream backend
  active connections: 0
  connection pool capacity: 10
  overflow: reject
  cached connection queue: 0
  free'd connection queue: 10
  cached connection successfully used count:
  free'd connection successfully used count: 3 0 0 0 0 0 0 0 0 0
  servers: 1
  peers: 1

upstream backend2
  active connections: 0
  connection pool capacity: 0
  servers: 1
  peers: 1
[{"sum(1)":2}][{"sum(1)":2}]
upstream backend
  active connections: 1
  connection pool capacity: 10
  overflow: reject
  cached connection queue: 1
  free'd connection queue: 9
  cached connection successfully used count: 2
  free'd connection successfully used count: 0 0 0 0 0 0 0 0 0
  servers: 1
  peers: 1

upstream backend2
  active connections: 0
  connection pool capacity: 0
  servers: 1
  peers: 1

