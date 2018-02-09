# vi:filetype=perl

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (blocks() * 5);

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
--- http_config
    upstream database {
        postgres_server     $TEST_NGINX_POSTGRESQL_HOST:$TEST_NGINX_POSTGRESQL_PORT
                            dbname=ngx_test user=ngx_test password=ngx_test;
        postgres_keepalive  off;
    }
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select * from cats";
    }
--- request
GET /postgres
--- error_code: 200
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
--- no_error_log
[alert]
[error]



=== TEST 2: keep-alive
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select * from cats";
    }
--- request
GET /postgres
--- error_code: 200
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
--- no_error_log
[alert]
[error]



=== TEST 3: update
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "update cats set name='bob' where name='bob'";
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}".        # endian
"\x{03}\x{00}\x{00}\x{00}".  # format version 0.0.3
"\x{00}".        # result type
"\x{00}\x{00}".  # std errcode
"\x{01}\x{00}".  # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".              # driver errstr data
"\x{01}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{00}\x{00}"   # col count
--- timeout: 10
--- no_error_log
[alert]
[error]



=== TEST 4: select empty result
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select * from cats where name='tom'";
    }
--- request
GET /postgres
--- error_code: 200
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
"\x{00}"         # row list terminator
--- timeout: 10
--- no_error_log
[alert]
[error]



=== TEST 5: variables in postgres_pass
--- http_config eval: $::http_config
--- config
    location /postgres {
        set                 $backend  database;
        postgres_pass       $backend;
        postgres_query      "update cats set name='bob' where name='bob'";
    }
--- request
GET /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}".        # endian
"\x{03}\x{00}\x{00}\x{00}".  # format version 0.0.3
"\x{00}".        # result type
"\x{00}\x{00}".  # std errcode
"\x{01}\x{00}".  # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".              # driver errstr data
"\x{01}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{00}\x{00}"   # col count
--- timeout: 10
--- no_error_log
[alert]
[error]



=== TEST 6: HEAD request
--- http_config eval: $::http_config
--- config
    location /postgres {
        postgres_pass       database;
        postgres_query      "select * from cats";
    }
--- request
HEAD /postgres
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- response_body eval
""
--- timeout: 10
--- no_error_log
[alert]
[error]



=== TEST 7: "if" pseudo-location
--- http_config eval: $::http_config
--- config
    location /postgres {
        if ($arg_foo) {
            postgres_pass       database;
            postgres_query      "select * from cats";
            break;
        }

        return 404;
    }
--- request
GET /postgres?foo=1
--- error_code: 200
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
--- no_error_log
[alert]
[error]
