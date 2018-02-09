# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(1);

plan tests => repeat_each() * blocks();

$ENV{TEST_NGINX_POSTGRESQL_PORT} ||= 5432;
$ENV{TEST_NGINX_POSTGRESQL_HOST} ||= '127.0.0.1';

$ENV{TEST_NGINX_MYSQL_HOST} ||= '127.0.0.1';
$ENV{TEST_NGINX_MYSQL_PORT} ||= 3306;

our $http_config = <<'_EOC_';
    upstream database {
        drizzle_server $TEST_NGINX_MYSQL_HOST:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
    }
_EOC_

our $http_config2 = <<'_EOC_';
    upstream database {
        postgres_server  $TEST_NGINX_POSTGRESQL_HOST:$TEST_NGINX_POSTGRESQL_PORT
                         dbname=ngx_test user=ngx_test password=ngx_test;
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



=== TEST 5: birds - drop table
--- http_config eval: $::http_config
--- config
    location = /init {
        drizzle_pass   database;
        drizzle_query  "DROP TABLE IF EXISTS birds";
    }
--- request
GET /init
--- error_code: 200
--- timeout: 10



=== TEST 6: birds - create table
--- http_config eval: $::http_config
--- config
    location = /init {
        drizzle_pass   database;
        drizzle_query  "CREATE TABLE birds (`\"name\"` text, height real)";
    }
--- request
GET /init
--- error_code: 200
--- timeout: 10



=== TEST 7: birds - insert values
--- http_config eval: $::http_config
--- config
    location = /init {
        drizzle_pass   database;
        drizzle_query  "
INSERT INTO birds (`\"name\"`, height)
VALUES
    ('hello \"tom', 3.14),
    ('hi,ya', -3),
    ('hey\\ndad', 7),
    ('\\rkay', 0.005),
    ('ab;c', 0.005),
    ('foo\\tbar', 21);";
    }
--- request
GET /init
--- error_code: 200
--- timeout: 10



=== TEST 8: cats - drop table - pg
--- http_config eval: $::http_config2
--- config
    location = /init {
        postgres_pass   database;
        postgres_query  "DROP TABLE cats";
        error_page 500  = /ignore;
    }

    location /ignore { echo "ignore"; }
--- request
GET /init
--- error_code: 200
--- timeout: 10



=== TEST 9: cats - create table - pg
--- http_config eval: $::http_config2
--- config
    location = /init {
        postgres_pass   database;
        postgres_query  "CREATE TABLE cats (id integer, name text)";
    }
--- request
GET /init
--- error_code: 200
--- timeout: 10



=== TEST 10: cats - insert value - pg
--- http_config eval: $::http_config2
--- config
    location = /init {
        postgres_pass   database;
        postgres_query  "INSERT INTO cats (id) VALUES (2)";
    }
--- request
GET /init
--- error_code: 200
--- timeout: 10



=== TEST 11: cats - insert value - pg
--- http_config eval: $::http_config2
--- config
    location = /init {
        postgres_pass   database;
        postgres_query  "INSERT INTO cats (id, name) VALUES (3, 'bob')";
    }
--- request
GET /init
--- error_code: 200
--- timeout: 10

