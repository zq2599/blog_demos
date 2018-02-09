# vi:filetype=perl

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (blocks() * 2 + 1 * 1);

$ENV{TEST_NGINX_POSTGRESQL_HOST} ||= '127.0.0.1';
$ENV{TEST_NGINX_POSTGRESQL_PORT} ||= 5432;

our $http_config = <<'_EOC_';
    upstream database {
        postgres_server  $TEST_NGINX_POSTGRESQL_HOST:$TEST_NGINX_POSTGRESQL_PORT
                         dbname=ngx_test user=ngx_test password=ngx_test;
    }
_EOC_

run_tests();

__DATA__

=== TEST 1: no changes (SELECT)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select * from cats";
        postgres_rewrite    no_changes 500;
        postgres_rewrite    changes 500;
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- timeout: 10



=== TEST 2: no changes (UPDATE)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "update cats set id=3 where name='noone'";
        postgres_rewrite    no_changes 206;
        postgres_rewrite    changes 500;
    }
--- request
GET /postgres
--- error_code: 206
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- timeout: 10



=== TEST 3: one change
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "update cats set id=3 where name='bob'";
        postgres_rewrite    no_changes 500;
        postgres_rewrite    changes 206;
    }
--- request
GET /postgres
--- error_code: 206
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- timeout: 10



=== TEST 4: rows
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select * from cats";
        postgres_rewrite    no_changes 500;
        postgres_rewrite    changes 500;
        postgres_rewrite    no_rows 410;
        postgres_rewrite    rows 206;
    }
--- request
GET /postgres
--- error_code: 206
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- timeout: 10



=== TEST 5: no rows
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select * from cats where name='noone'";
        postgres_rewrite    no_changes 500;
        postgres_rewrite    changes 500;
        postgres_rewrite    no_rows 410;
        postgres_rewrite    rows 206;
    }
--- request
GET /postgres
--- error_code: 410
--- response_headers
Content-Type: text/html
--- timeout: 10



=== TEST 6: inheritance
--- http_config eval: $::http_config
--- config
    postgres_rewrite  no_changes 500;
    postgres_rewrite  changes 500;
    postgres_rewrite  no_rows 410;
    postgres_rewrite  rows 206;

    location /postgres {
        postgres_pass       database;
        postgres_query      "select * from cats";
    }
--- request
GET /postgres
--- error_code: 206
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- timeout: 10



=== TEST 7: inheritance (mixed, don't inherit)
--- http_config eval: $::http_config
--- config
    postgres_rewrite  no_changes 500;
    postgres_rewrite  changes 500;
    postgres_rewrite  no_rows 410;
    postgres_rewrite  rows 206;

    location /postgres {
        postgres_pass       database;
        postgres_query      "select * from cats";
        postgres_rewrite    rows 206;
    }
--- request
GET /postgres
--- error_code: 206
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- timeout: 10



=== TEST 8: rows (method-specific)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select * from cats";
        postgres_rewrite    no_changes 500;
        postgres_rewrite    changes 500;
        postgres_rewrite    no_rows 410;
        postgres_rewrite    POST PUT rows 201;
        postgres_rewrite    HEAD GET rows 206;
        postgres_rewrite    rows 206;
    }
--- request
GET /postgres
--- error_code: 206
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- timeout: 10



=== TEST 9: rows (default)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select * from cats";
        postgres_rewrite    no_changes 500;
        postgres_rewrite    changes 500;
        postgres_rewrite    no_rows 410;
        postgres_rewrite    POST PUT rows 201;
        postgres_rewrite    rows 206;
    }
--- request
GET /postgres
--- error_code: 206
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- timeout: 10



=== TEST 10: rows (none)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select * from cats";
        postgres_rewrite    no_changes 500;
        postgres_rewrite    changes 500;
        postgres_rewrite    no_rows 410;
        postgres_rewrite    POST PUT rows 201;
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- timeout: 10



=== TEST 11: no changes (UPDATE) with 202 response
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "update cats set id=3 where name='noone'";
        postgres_rewrite    no_changes 202;
        postgres_rewrite    changes 500;
    }
--- request
GET /postgres
--- error_code: 202
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- timeout: 10
--- skip_nginx: 2: < 0.8.41



=== TEST 12: no changes (UPDATE) with 409 response
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "update cats set id=3 where name='noone'";
        postgres_rewrite    no_changes 409;
        postgres_rewrite    changes 500;
    }
--- request
GET /postgres
--- error_code: 409
--- response_headers
Content-Type: text/html
--- timeout: 10



=== TEST 13: no changes (UPDATE) with 409 status and our body
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "update cats set id=3 where name='noone'";
        postgres_rewrite    no_changes =409;
        postgres_rewrite    changes 500;
    }
--- request
GET /postgres
--- error_code: 409
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- timeout: 10



=== TEST 14: rows with 409 status and our body (with integrity check)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select * from cats";
        postgres_rewrite    no_rows 500;
        postgres_rewrite    rows =409;
    }
--- request
GET /postgres
--- error_code: 409
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}".        # endian
"\x{03}\x{00}\x{00}\x{00}".  # format version 0.0.3
"\x{00}".        # result type
"\x{00}\x{00}".  # std errcode
"\x{02}\x{00}".  # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".              # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{02}\x{00}".  # col count
"\x{09}\x{00}".  # std col type (integer/int)
"\x{17}\x{00}".  # driver col type
"\x{02}\x{00}".  # col name len
"id".            # col name data
"\x{06}\x{80}".  # std col type (varchar/str)
"\x{19}\x{00}".  # driver col type
"\x{04}\x{00}".  # col name len
"name".          # col name data
"\x{01}".        # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"2".             # field data
"\x{ff}\x{ff}\x{ff}\x{ff}".  # field len
"".              # field data
"\x{01}".        # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"3".             # field data
"\x{03}\x{00}\x{00}\x{00}".  # field len
"bob".           # field data
"\x{00}"         # row list terminator
--- timeout: 10



=== TEST 15: rows - "if" pseudo-location
--- http_config eval: $::http_config
--- config
    location /postgres {
        if ($arg_foo) {
            postgres_pass       database;
            postgres_query      "select * from cats";
            postgres_rewrite    no_changes 500;
            postgres_rewrite    changes 500;
            postgres_rewrite    no_rows 410;
            postgres_rewrite    rows 206;
            break;
        }

        return 404;
    }
--- request
GET /postgres?foo=1
--- error_code: 206
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- timeout: 10
