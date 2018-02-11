# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (2 * blocks() + 1);

$ENV{TEST_NGINX_POSTGRESQL_HOST} ||= '127.0.0.1';
$ENV{TEST_NGINX_POSTGRESQL_PORT} ||= 5432;

no_long_string();

run_tests();

__DATA__

=== TEST 1: bool blob field (keepalive off)
--- http_config
    upstream backend {
        postgres_server     $TEST_NGINX_POSTGRESQL_HOST:$TEST_NGINX_POSTGRESQL_PORT
                            dbname=ngx_test user=ngx_test password=ngx_test;
        postgres_keepalive  off;
    }
--- config
    location /test {
        echo_location /pg "drop table if exists foo";
        echo;
        echo_location /pg "create table foo (id serial, flag bool);";
        echo;
        echo_location /pg "insert into foo (flag) values (true);";
        echo;
        echo_location /pg "insert into foo (flag) values (false);";
        echo;
        echo_location /pg "select * from foo order by id;";
        echo;
    }
    location /pg {
        postgres_pass backend;
        postgres_query $query_string;
        rds_json on;
    }
    location = /pgignore {
        postgres_pass backend;
        postgres_query $query_string;
        rds_json on;
        error_page 500 = /ignore;
    }
    location /ignore { echo "ignore"; }
--- request
GET /test
--- response_body
{"errcode":0}
{"errcode":0}
{"errcode":0,"affected_rows":1}
{"errcode":0,"affected_rows":1}
[{"id":1,"flag":true},{"id":2,"flag":false}]
--- skip_nginx: 2: < 0.7.46



=== TEST 2: bool blob field (keepalive on)
--- http_config
    upstream backend {
        postgres_server     $TEST_NGINX_POSTGRESQL_HOST:$TEST_NGINX_POSTGRESQL_PORT
                            dbname=ngx_test user=ngx_test password=ngx_test;
    }
--- config
    location /test {
        echo_location /pgignore "drop table if exists foo";
        echo;
        echo_location /pg "create table foo (id serial, flag bool);";
        echo;
        echo_location /pg "insert into foo (flag) values (true);";
        echo;
        echo_location /pg "insert into foo (flag) values (false);";
        echo;
        echo_location /pg "select * from foo order by id;";
        echo;
    }
    location /pg {
        postgres_pass backend;
        postgres_query $query_string;
        rds_json on;
    }
    location = /pgignore {
        postgres_pass backend;
        postgres_query $query_string;
        rds_json on;
        error_page 500 = /ignore;
    }
    location /ignore { echo "ignore"; }
--- request
GET /test
--- response_body
{"errcode":0}
{"errcode":0}
{"errcode":0,"affected_rows":1}
{"errcode":0,"affected_rows":1}
[{"id":1,"flag":true},{"id":2,"flag":false}]
--- skip_nginx: 2: < 0.7.46



=== TEST 3: sanity (github issue #2)
--- http_config
    upstream backend {
        postgres_server     $TEST_NGINX_POSTGRESQL_HOST:$TEST_NGINX_POSTGRESQL_PORT
                            dbname=ngx_test user=ngx_test password=ngx_test;
    }
--- config
    location /pg {
        rds_json on;
        rds_json_root     url;

        postgres_pass backend;
        postgres_query GET "select * from cats order by id";
        postgres_output   rds;
    }
--- request
GET /pg
--- response_headers
Content-Type: application/json
--- response_body chop
{"url":[{"id":2,"name":null},{"id":3,"name":"bob"}]}
--- skip_nginx: 2: < 0.7.46

