# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (blocks() + 1);

$ENV{TEST_NGINX_MYSQL_PORT} ||= 3306;
$ENV{TEST_NGINX_MYSQL_HOST} ||= '127.0.0.1';

our $http_config = <<'_EOC_';
    upstream foo {
        drizzle_server $TEST_NGINX_MYSQL_HOST:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
    }
_EOC_

worker_connections(128);
run_tests();

no_diff();

__DATA__

=== TEST 1: two locations
little-endian systems only

--- http_config eval: $::http_config
--- config
    location /mysql {
        set $backend foo;
        drizzle_pass $backend;
        drizzle_module_header off;
        drizzle_query "update table_that_doesnt_exist set name='bob'";
    }
    location /mysql2 {
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

