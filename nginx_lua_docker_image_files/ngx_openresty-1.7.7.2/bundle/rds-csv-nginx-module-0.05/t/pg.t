# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * 2 * blocks();

$ENV{TEST_NGINX_POSTGRESQL_PORT} ||= 5432;

no_long_string();

no_diff();

run_tests();

__DATA__

=== TEST 1: bool blob field (keepalive off)
--- http_config
    upstream backend {
        postgres_server     127.0.0.1:$TEST_NGINX_POSTGRESQL_PORT
                            dbname=ngx_test user=ngx_test password=ngx_test;
        postgres_keepalive  off;
    }
--- config
    location /test {
        echo_location /pgignore "drop table if exists foo";
        echo_location /pg "create table foo (id serial, flag bool);";
        echo_location /pg "insert into foo (flag) values (true);";
        echo_location /pg "insert into foo (flag) values (false);";
        echo_location /pg "select * from foo order by id;";
    }
    location /pg {
        postgres_pass backend;
        postgres_query $query_string;
        rds_csv on;
    }
    location = /pgignore {
        postgres_pass backend;
        postgres_query $query_string;
        rds_csv on;
        error_page 500 = /ignore;
    }
    location /ignore { echo "ignore"; }
--- request
GET /test
--- response_body eval
qq{errcode,errstr,insert_id,affected_rows\r
0,,0,0\r
errcode,errstr,insert_id,affected_rows\r
0,,0,0\r
errcode,errstr,insert_id,affected_rows\r
0,,0,1\r
errcode,errstr,insert_id,affected_rows\r
0,,0,1\r
id,flag\r
1,t\r
2,f\r
}
--- skip_nginx: 2: < 0.7.46



=== TEST 2: bool blob field (keepalive on)
--- http_config
    upstream backend {
        postgres_server     127.0.0.1:$TEST_NGINX_POSTGRESQL_PORT
                            dbname=ngx_test user=ngx_test password=ngx_test;
    }
--- config
    location /test {
        echo_location /pg "drop table if exists foo";
        echo_location /pg "create table foo (id serial, flag bool);";
        echo_location /pg "insert into foo (flag) values (true);";
        echo_location /pg "insert into foo (flag) values (false);";
        echo_location /pg "select * from foo order by id;";
    }
    location /pg {
        postgres_pass backend;
        postgres_query $query_string;
        rds_csv on;
    }
    location = /pgignore {
        postgres_pass backend;
        postgres_query $query_string;
        rds_csv on;
        error_page 500 = /ignore;
    }
    location /ignore { echo "ignore"; }
--- request
GET /test
--- response_body eval
qq{errcode,errstr,insert_id,affected_rows\r
0,,0,0\r
errcode,errstr,insert_id,affected_rows\r
0,,0,0\r
errcode,errstr,insert_id,affected_rows\r
0,,0,1\r
errcode,errstr,insert_id,affected_rows\r
0,,0,1\r
id,flag\r
1,t\r
2,f\r
}
--- skip_nginx: 2: < 0.7.46

