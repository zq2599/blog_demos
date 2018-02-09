# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (blocks() * 3 - 2 * 2);

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
run_tests();

no_diff();

__DATA__

=== TEST 1: default query
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass       database;
        drizzle_query      "select 'default' as echo";
    }
--- request
GET /mysql
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}".        # endian
"\x{03}\x{00}\x{00}\x{00}".  # format version 0.0.3
"\x{00}".        # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}".  # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".              # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{01}\x{00}".  # col count
"\x{06}\x{80}".  # std col type (varchar/str)
"\x{fd}\x{00}".  # driver col type
"\x{04}\x{00}".  # col name len
"echo".          # col name data
"\x{01}".        # valid row flag
"\x{07}\x{00}\x{00}\x{00}".  # field len
"default".       # field data
"\x{00}"         # row list terminator
--- timeout: 10



=== TEST 2: method-specific query
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass       database;
        drizzle_query      LOCK GET UNLOCK "select 'GET' as echo";
    }
--- request
GET /mysql
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}".        # endian
"\x{03}\x{00}\x{00}\x{00}".  # format version 0.0.3
"\x{00}".        # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}".  # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".              # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{01}\x{00}".  # col count
"\x{06}\x{80}".  # std col type (varchar/str)
"\x{fd}\x{00}".  # driver col type
"\x{04}\x{00}".  # col name len
"echo".          # col name data
"\x{01}".        # valid row flag
"\x{03}\x{00}\x{00}\x{00}".  # field len
"GET".           # field data
"\x{00}"         # row list terminator
--- timeout: 10



=== TEST 3: method-specific complex query (check 1)
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass       database;
        drizzle_query      LOCK GET UNLOCK "select '$request_method' as echo";
    }
--- request
GET /mysql
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}".        # endian
"\x{03}\x{00}\x{00}\x{00}".  # format version 0.0.3
"\x{00}".        # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}".  # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".              # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{01}\x{00}".  # col count
"\x{06}\x{80}".  # std col type (varchar/str)
"\x{fd}\x{00}".  # driver col type
"\x{04}\x{00}".  # col name len
"echo".          # col name data
"\x{01}".        # valid row flag
"\x{03}\x{00}\x{00}\x{00}".  # field len
"GET".           # field data
"\x{00}"         # row list terminator
--- timeout: 10



=== TEST 4: method-specific complex query (check 2)
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass       database;
        drizzle_query      LOCK GET UNLOCK "select '$request_method' as echo";
    }
--- request
LOCK /mysql
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}".        # endian
"\x{03}\x{00}\x{00}\x{00}".  # format version 0.0.3
"\x{00}".        # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}".  # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".              # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{01}\x{00}".  # col count
"\x{06}\x{80}".  # std col type (varchar/str)
"\x{fd}\x{00}".  # driver col type
"\x{04}\x{00}".  # col name len
"echo".          # col name data
"\x{01}".        # valid row flag
"\x{04}\x{00}\x{00}\x{00}".  # field len
"LOCK".          # field data
"\x{00}"         # row list terminator
--- timeout: 10



=== TEST 5: method-specific complex query (using not allowed method)
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass       database;
        drizzle_query      LOCK GET UNLOCK "select '$request_method' as echo";
    }
--- request
HEAD /mysql
--- error_code: 405
--- timeout: 10



=== TEST 6: method-specific query and default query (using defined method)
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass       database;
        drizzle_query      "select 'default' as echo";
        drizzle_query      LOCK GET UNLOCK "select '$request_method' as echo";
    }
--- request
GET /mysql
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}".        # endian
"\x{03}\x{00}\x{00}\x{00}".  # format version 0.0.3
"\x{00}".        # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}".  # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".              # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{01}\x{00}".  # col count
"\x{06}\x{80}".  # std col type (varchar/str)
"\x{fd}\x{00}".  # driver col type
"\x{04}\x{00}".  # col name len
"echo".          # col name data
"\x{01}".        # valid row flag
"\x{03}\x{00}\x{00}\x{00}".  # field len
"GET".           # field data
"\x{00}"         # row list terminator
--- timeout: 10



=== TEST 7: method-specific query and default query (using other method)
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass       database;
        drizzle_query      "select 'default' as echo";
        drizzle_query      LOCK GET UNLOCK "select '$request_method' as echo";
    }
--- request
POST /mysql
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}".        # endian
"\x{03}\x{00}\x{00}\x{00}".  # format version 0.0.3
"\x{00}".        # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}".  # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".              # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{01}\x{00}".  # col count
"\x{06}\x{80}".  # std col type (varchar/str)
"\x{fd}\x{00}".  # driver col type
"\x{04}\x{00}".  # col name len
"echo".          # col name data
"\x{01}".        # valid row flag
"\x{07}\x{00}\x{00}\x{00}".  # field len
"default".       # field data
"\x{00}"         # row list terminator
--- timeout: 10



=== TEST 8: inheritance
little-endian systems only

--- http_config eval: $::http_config
--- config
    drizzle_query      "select 'default' as echo";
    drizzle_query      LOCK GET UNLOCK "select '$request_method' as echo";

    location /mysql {
        drizzle_pass       database;
    }
--- request
GET /mysql
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}".        # endian
"\x{03}\x{00}\x{00}\x{00}".  # format version 0.0.3
"\x{00}".        # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}".  # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".              # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{01}\x{00}".  # col count
"\x{06}\x{80}".  # std col type (varchar/str)
"\x{fd}\x{00}".  # driver col type
"\x{04}\x{00}".  # col name len
"echo".          # col name data
"\x{01}".        # valid row flag
"\x{03}\x{00}\x{00}\x{00}".  # field len
"GET".           # field data
"\x{00}"         # row list terminator
--- timeout: 10



=== TEST 9: inheritance (mixed, not inherited)
little-endian systems only

--- http_config eval: $::http_config
--- config
    drizzle_query      "select 'default' as echo";

    location /mysql {
        drizzle_pass       database;
        drizzle_query      LOCK GET UNLOCK "select '$request_method' as echo";
    }
--- request
HEAD /mysql
--- error_code: 405
--- timeout: 10



=== TEST 10: default query
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass       database;
        drizzle_query      "select '$request_method' as echo";
    }
--- request
PATCH /mysql
--- error_code: 200
--- response_headers
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}".        # endian
"\x{03}\x{00}\x{00}\x{00}".  # format version 0.0.3
"\x{00}".        # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}".  # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".              # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{01}\x{00}".  # col count
"\x{06}\x{80}".  # std col type (varchar/str)
"\x{fd}\x{00}".  # driver col type
"\x{04}\x{00}".  # col name len
"echo".          # col name data
"\x{01}".        # valid row flag
"\x{05}\x{00}\x{00}\x{00}".  # field len
"PATCH".         # field data
"\x{00}"         # row list terminator
--- timeout: 10
--- skip_nginx: 3: < 0.8.41
