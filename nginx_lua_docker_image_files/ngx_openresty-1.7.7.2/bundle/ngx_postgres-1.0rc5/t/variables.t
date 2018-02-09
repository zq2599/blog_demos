# vi:filetype=perl

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (blocks() * 3 + 1 * 4 + 1 * 1 - 5 * 2);

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

=== TEST 1: sanity
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select 'test' as echo";
        postgres_set        $test 0 0;
        add_header          "X-Test" $test;
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
X-Test: test
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
"\x{01}\x{00}".  # col count
"\x{00}\x{80}".  # std col type (unknown/str)
"\x{c1}\x{02}".  # driver col type
"\x{04}\x{00}".  # col name len
"echo".          # col name data
"\x{01}".        # valid row flag
"\x{04}\x{00}\x{00}\x{00}".  # field len
"test".          # field data
"\x{00}"         # row list terminator
--- timeout: 10



=== TEST 2: out-of-range value (optional)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select 'test' as echo";
        postgres_set        $test 0 1;
        add_header          "X-Test" $test;
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
! X-Test
--- timeout: 10



=== TEST 3: NULL value (optional)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select NULL as echo";
        postgres_set        $test 0 0;
        add_header          "X-Test" $test;
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
! X-Test
--- timeout: 10



=== TEST 4: zero-length value (optional)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select '' as echo";
        postgres_set        $test 0 0;
        add_header          "X-Test" $test;
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
! X-Test
--- timeout: 10



=== TEST 5: out-of-range value (required)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select 'test' as echo";
        postgres_set        $test 0 1 required;
        add_header          "X-Test" $test;
    }
--- request
GET /postgres
--- error_code: 500
--- timeout: 10



=== TEST 6: NULL value (required)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select NULL as echo";
        postgres_set        $test 0 0 required;
        add_header          "X-Test" $test;
    }
--- request
GET /postgres
--- error_code: 500
--- timeout: 10



=== TEST 7: zero-length value (required)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select '' as echo";
        postgres_set        $test 0 0 required;
        add_header          "X-Test" $test;
    }
--- request
GET /postgres
--- error_code: 500
--- timeout: 10



=== TEST 8: $postgres_columns
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select 'a', 'b', 'c'";
        add_header          "X-Columns" $postgres_columns;
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
X-Columns: 3
--- timeout: 10



=== TEST 9: $postgres_rows
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select 'a', 'b', 'c'";
        add_header          "X-Rows" $postgres_rows;
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
X-Rows: 1
--- timeout: 10



=== TEST 10: $postgres_query (simple value)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select 'test' as echo";
        add_header          "X-Query" $postgres_query;
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
X-Query: select 'test' as echo
--- timeout: 10



=== TEST 11: $postgres_query (simple value)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select '$request_method' as echo";
        add_header          "X-Query" $postgres_query;
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
X-Query: select 'GET' as echo
--- timeout: 10



=== TEST 12: variables used in non-ngx_postgres location
--- http_config
--- config
    location /etc {
        root                /;
        add_header          "X-Columns" $postgres_columns;
        add_header          "X-Rows" $postgres_rows;
        add_header          "X-Affected" $postgres_affected;
        add_header          "X-Query" $postgres_query;
        postgres_set        $pg 0 0 required;
        add_header          "X-Custom" $pg;
    }
--- request
GET /etc/passwd
--- error_code: 200
--- response_headers
Content-Type: text/plain
! X-Columns
! X-Rows
! X-Affected
! X-Query
! X-Custom
--- timeout: 10



=== TEST 13: $postgres_affected (SELECT)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select '$request_method' as echo";
        add_header          "X-Affected" $postgres_affected;
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
! X-Affected
--- timeout: 10



=== TEST 14: $postgres_affected (UPDATE, no changes)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "update cats set id=3 where name='noone'";
        add_header          "X-Affected" $postgres_affected;
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
X-Affected: 0
--- timeout: 10



=== TEST 15: $postgres_affected (UPDATE, one change)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "update cats set id=3 where name='bob'";
        add_header          "X-Affected" $postgres_affected;
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
X-Affected: 1
--- timeout: 10



=== TEST 16: inheritance
--- http_config eval: $::http_config
--- config
    postgres_set  $test 0 0 required;

    location /postgres {
        postgres_pass       database;
        postgres_query      "select NULL as echo";
        add_header          "X-Test" $test;
    }
--- request
GET /postgres
--- error_code: 500
--- timeout: 10



=== TEST 17: inheritance (mixed, don't inherit)
--- http_config eval: $::http_config
--- config
    postgres_set  $test 0 0 required;

    location /postgres {
        postgres_pass       database;
        postgres_query      "select NULL as echo";
        postgres_set        $test2 2 2;
        add_header          "X-Test" $test2;
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
! X-Test
--- timeout: 10



=== TEST 18: column by name (existing)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select 'test' as echo";
        postgres_set        $test 0 "echo";
        add_header          "X-Test" $test;
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
X-Test: test
--- timeout: 10



=== TEST 19: column by name (not existing, optional)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select 'test' as echo";
        postgres_set        $test 0 "test" optional;
        add_header          "X-Test" $test;
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
! X-Test
--- timeout: 10



=== TEST 20: column by name (not existing, required)
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select 'test' as echo";
        postgres_set        $test 0 "test" required;
        add_header          "X-Test" $test;
    }
--- request
GET /postgres
--- error_code: 500
--- timeout: 10
