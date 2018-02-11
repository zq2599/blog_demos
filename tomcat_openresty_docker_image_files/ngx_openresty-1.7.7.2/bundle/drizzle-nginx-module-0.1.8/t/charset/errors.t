# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (blocks() + 2);

$ENV{TEST_NGINX_MYSQL_PORT} ||= 3306;
$ENV{TEST_NGINX_MYSQL_HOST} ||= '127.0.0.1';

our $http_config = <<'_EOC_';
    upstream foo {
        drizzle_server $TEST_NGINX_MYSQL_HOST:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test
                       charset=utf8;
    }
_EOC_

worker_connections(128);
run_tests();

no_diff();

__DATA__

=== TEST 1: bad query
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        set $backend foo;
        drizzle_pass $backend;
        drizzle_module_header off;
        drizzle_query "update table_that_doesnt_exist set name='bob'";
    }
--- request
GET /mysql
--- error_code: 410
--- response_body_like: 410 Gone
--- timeout: 5



=== TEST 2: wrong credentials
little-endian systems only

--- timeout: 5
--- http_config
    upstream foo {
        drizzle_server 127.0.0.1:$TEST_NGINX_MYSQL_PORT dbname=test
             password=wrong_pass user=monty protocol=mysql
             charset=utf8;
        drizzle_keepalive mode=single max=2 overflow=reject;
    }
--- config
    location /mysql {
        set $backend foo;
        drizzle_pass $backend;
        drizzle_module_header off;
        drizzle_query "update cats set name='bob' where name='bob'";
    }
--- request
GET /mysql
--- error_code: 502



=== TEST 3: no database
little-endian systems only

--- http_config
    upstream foo {
        drizzle_server 127.0.0.1:1 dbname=test
             password=some_pass user=monty protocol=mysql
             charset=utf8;
        drizzle_keepalive mode=single max=2 overflow=reject;
    }
--- config
    location /mysql {
        set $backend foo;
        drizzle_pass $backend;
        drizzle_module_header off;
        drizzle_query "update cats set name='bob' where name='bob'";
    }
--- request
GET /mysql
--- error_code: 502
--- timeout: 1



=== TEST 4: multiple queries
little-endian systems only

--- timeout: 5
--- http_config eval: $::http_config
--- config
    location /mysql {
        set $backend foo;
        drizzle_pass $backend;
        drizzle_module_header off;
        drizzle_query "select * from cats; select * from cats";
    }
--- request
GET /mysql
--- error_code: 500



=== TEST 5: missing query
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        set $backend foo;
        drizzle_pass $backend;
        drizzle_module_header off;
    }
--- request
GET /mysql
--- error_code: 500



=== TEST 6: empty query
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        set $backend foo;
        drizzle_pass $backend;
        drizzle_module_header off;
        set $query "";
        drizzle_query $query;
    }
--- request
GET /mysql
--- error_code: 500
--- timeout: 5



=== TEST 7: empty pass
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        set $backend "";
        drizzle_pass $backend;
        drizzle_module_header off;
        drizzle_query "update cats set name='bob' where name='bob'";
    }
--- request
GET /mysql
--- error_code: 500



=== TEST 8: empty pass
little-endian systems only

--- http_config
    upstream foo {
        drizzle_server $TEST_NGINX_MYSQL_HOST:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test
                       charset=blah-blah;
    }

--- config
    location /mysql {
        drizzle_pass foo;
        drizzle_module_header off;
        drizzle_query "update cats set name='bob' where name='bob'";
    }
--- request
GET /mysql
--- error_code: 500
--- response_body_like: 500 Internal Server Error

