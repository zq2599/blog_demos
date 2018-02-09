# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(1);

plan tests => repeat_each() * blocks();

$ENV{TEST_NGINX_MYSQL_PORT} ||= 3306;
$ENV{TEST_NGINX_MYSQL_HOST} ||= '127.0.0.1';

our $http_config = <<'_EOC_';
    upstream database {
        drizzle_server $TEST_NGINX_MYSQL_HOST:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test
                       charset=utf8;
    }
_EOC_

worker_connections(128);
no_shuffle();
run_tests();

no_diff();

__DATA__

=== TEST 1: cats - drop table
--- http_config eval: $::http_config
--- config
    location = /init {
        drizzle_pass   database;
        drizzle_query  "DROP TABLE IF EXISTS cats";
    }
--- request
GET /init
--- error_code: 200
--- timeout: 10



=== TEST 2: cats - create table
--- http_config eval: $::http_config
--- config
    location = /init {
        drizzle_pass   database;
        drizzle_query  "CREATE TABLE cats (id integer, name text)";
    }
--- request
GET /init
--- error_code: 200
--- timeout: 10



=== TEST 3: cats - insert value
--- http_config eval: $::http_config
--- config
    location = /init {
        drizzle_pass   database;
        drizzle_query  "INSERT INTO cats (id) VALUES (2)";
    }
--- request
GET /init
--- error_code: 200
--- timeout: 10



=== TEST 4: cats - insert value
--- http_config eval: $::http_config
--- config
    location = /init {
        drizzle_pass   database;
        drizzle_query  "INSERT INTO cats (id, name) VALUES (3, 'bob')";
    }
--- request
GET /init
--- error_code: 200
--- timeout: 10
