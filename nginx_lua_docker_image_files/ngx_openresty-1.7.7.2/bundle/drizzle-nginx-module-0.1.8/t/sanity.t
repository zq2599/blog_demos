# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * 2 * blocks() + 2 * repeat_each() * 6;

$ENV{TEST_NGINX_MYSQL_PORT} ||= 3306;
$ENV{TEST_NGINX_MYSQL_HOST} ||= '127.0.0.1';

our $http_config = <<'_EOC_';
    upstream backend {
        drizzle_server $TEST_NGINX_MYSQL_HOST:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
        #drizzle_keepalive max=10 overflow=ignore mode=single;
    }
_EOC_

worker_connections(128);
#master_on();
#log_level('warn');

no_diff();

run_tests();

__DATA__

=== TEST 1: sanity
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        #drizzle_dbname $dbname;
        drizzle_query 'select * from cats';
    }
--- request
GET /mysql
--- response_headers_like
X-Resty-DBD-Module: ngx_drizzle \d+\.\d+\.\d+
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{02}\x{00}".  # col count
"\x{09}\x{00}".  # std col type (integer)
"\x{03}\x{00}".  # drizzle col type
"\x{02}\x{00}".     # col name len
"id".   # col name data
"\x{13}\x{80}".  # std col type (blob/str)
"\x{fc}\x{00}".  # drizzle col type
"\x{04}\x{00}".  # col name len
"name".  # col name data
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"2".  # field data
"\x{ff}\x{ff}\x{ff}\x{ff}".  # field len
"".  # field data
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"3".  # field data
"\x{03}\x{00}\x{00}\x{00}".  # field len
"bob".  # field data
"\x{00}"  # row list terminator
--- timeout: 60



=== TEST 2: keep-alive
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        #drizzle_dbname $dbname;
        drizzle_query 'select * from cats';
    }
--- request
GET /mysql
--- response_body eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{02}\x{00}".  # col count
"\x{09}\x{00}".  # std col type (integer)
"\x{03}\x{00}".  # drizzle col type
"\x{02}\x{00}".     # col name len
"id".   # col name data
"\x{13}\x{80}".  # std col type (blob/str)
"\x{fc}\x{00}".  # drizzle col type
"\x{04}\x{00}".  # col name len
"name".  # col name data
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"2".  # field data
"\x{ff}\x{ff}\x{ff}\x{ff}".  # field len
"".  # field data
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"3".  # field data
"\x{03}\x{00}\x{00}\x{00}".  # field len
"bob".  # field data
"\x{00}"  # row list terminator



=== TEST 3: update
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        #drizzle_dbname $dbname;
        drizzle_query "update cats set name='bob' where name='bob'";
    }
--- request
GET /mysql
--- response_body eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{28}\x{00}".  # driver errstr len
"Rows matched: 1  Changed: 0  Warnings: 0".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{00}\x{00}"  # col count



=== TEST 4: select empty result
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        drizzle_query "select * from cats where name='tom'";
    }
--- request
GET /mysql
--- response_body eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{02}\x{00}".  # col count
"\x{09}\x{00}".  # std col type (integer)
"\x{03}\x{00}".  # drizzle col type
"\x{02}\x{00}".     # col name len
"id".   # col name data
"\x{13}\x{80}".  # std col type (blob/str)
"\x{fc}\x{00}".  # drizzle col type
"\x{04}\x{00}".  # col name len
"name".  # col name data
"\x{00}"  # row list terminator



=== TEST 5: update & no module header
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        drizzle_module_header off;
        drizzle_query "update cats set name='bob' where name='bob'";
    }
--- request
GET /mysql
--- response_headers
X-Resty-DBD-Module: 
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{28}\x{00}".  # driver errstr len
"Rows matched: 1  Changed: 0  Warnings: 0".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{00}\x{00}"  # col count



=== TEST 6: variables in drizzle_pass
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        set $foo backend;
        drizzle_pass $foo;
        drizzle_module_header off;
        drizzle_query "update cats set name='bob' where name='bob'";
    }
--- request
GET /mysql
--- response_headers
X-Resty-DBD-Module: 
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{28}\x{00}".  # driver errstr len
"Rows matched: 1  Changed: 0  Warnings: 0".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{00}\x{00}"  # col count



=== TEST 7: sanity (using little bufs, size 1)
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        #drizzle_dbname $dbname;
        drizzle_query 'select * from cats';
        drizzle_buffer_size 1;
    }
--- request
GET /mysql
--- response_headers_like
X-Resty-DBD-Module: ngx_drizzle \d+\.\d+\.\d+
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{02}\x{00}".  # col count
"\x{09}\x{00}".  # std col type (integer)
"\x{03}\x{00}".  # drizzle col type
"\x{02}\x{00}".     # col name len
"id".   # col name data
"\x{13}\x{80}".  # std col type (blob/str)
"\x{fc}\x{00}".  # drizzle col type
"\x{04}\x{00}".  # col name len
"name".  # col name data
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"2".  # field data
"\x{ff}\x{ff}\x{ff}\x{ff}".  # field len
"".  # field data
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"3".  # field data
"\x{03}\x{00}\x{00}\x{00}".  # field len
"bob".  # field data
"\x{00}"  # row list terminator
--- timeout: 60



=== TEST 8: sanity (using little bufs, size 2)
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        #drizzle_dbname $dbname;
        drizzle_query 'select * from cats';
        drizzle_buffer_size 2;
    }
--- request
GET /mysql
--- response_headers_like
X-Resty-DBD-Module: ngx_drizzle \d+\.\d+\.\d+
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{02}\x{00}".  # col count
"\x{09}\x{00}".  # std col type (integer)
"\x{03}\x{00}".  # drizzle col type
"\x{02}\x{00}".     # col name len
"id".   # col name data
"\x{13}\x{80}".  # std col type (blob/str)
"\x{fc}\x{00}".  # drizzle col type
"\x{04}\x{00}".  # col name len
"name".  # col name data
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"2".  # field data
"\x{ff}\x{ff}\x{ff}\x{ff}".  # field len
"".  # field data
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"3".  # field data
"\x{03}\x{00}\x{00}\x{00}".  # field len
"bob".  # field data
"\x{00}"  # row list terminator
--- timeout: 60



=== TEST 9: sanity (using little bufs, size 3)
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        #drizzle_dbname $dbname;
        drizzle_query 'select * from cats';
        drizzle_buffer_size 3;
    }
--- request
GET /mysql
--- response_headers_like
X-Resty-DBD-Module: ngx_drizzle \d+\.\d+\.\d+
Content-Type: application/x-resty-dbd-stream
--- response_body eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{02}\x{00}".  # col count
"\x{09}\x{00}".  # std col type (integer)
"\x{03}\x{00}".  # drizzle col type
"\x{02}\x{00}".     # col name len
"id".   # col name data
"\x{13}\x{80}".  # std col type (blob/str)
"\x{fc}\x{00}".  # drizzle col type
"\x{04}\x{00}".  # col name len
"name".  # col name data
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"2".  # field data
"\x{ff}\x{ff}\x{ff}\x{ff}".  # field len
"".  # field data
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"3".  # field data
"\x{03}\x{00}\x{00}\x{00}".  # field len
"bob".  # field data
"\x{00}"  # row list terminator
--- timeout: 60



=== TEST 10: update
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        #drizzle_dbname $dbname;
        drizzle_query "update cats set name='bob' where name='bob'";
    }
--- request
GET /mysql
--- response_body eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{28}\x{00}".  # driver errstr len
"Rows matched: 1  Changed: 0  Warnings: 0".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{00}\x{00}"  # col count



