# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(10);
no_shuffle();

repeat_each(2);

plan tests => repeat_each() * (2 * blocks() + 7);

$ENV{TEST_NGINX_MYSQL_PORT} ||= 3306;

our $http_config = <<'_EOC_';
    upstream backend {
        drizzle_server 127.0.0.1:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
    }
_EOC_

no_long_string();
no_diff();

run_tests();

__DATA__

=== TEST 1: sanity
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        #drizzle_dbname $dbname;
        drizzle_query 'select * from cats';
        rds_csv on;
        rds_csv_row_terminator "\n";
        rds_csv_field_name_header off;
    }
--- request
GET /mysql
--- response_headers_like
X-Resty-DBD-Module: ngx_drizzle \d+\.\d+\.\d+
Content-Type: text/csv; header=absence
--- response_body
2,
3,bob
--- timeout: 15



=== TEST 2: keep-alive
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        #drizzle_dbname $dbname;
        drizzle_query 'select * from cats';
        rds_csv on;
        rds_csv_row_terminator "\n";
    }
--- request
GET /mysql
--- response_body
id,name
2,
3,bob



=== TEST 3: update
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        #drizzle_dbname $dbname;
        drizzle_query "update cats set name='bob' where name='bob'";
        rds_csv on;
        rds_csv_row_terminator "\n";
    }
--- request
GET /mysql
--- response_body
errcode,errstr,insert_id,affected_rows
0,Rows matched: 1  Changed: 0  Warnings: 0,0,0



=== TEST 4: select empty result
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        drizzle_query "select * from cats where name='tom'";
        rds_csv on;
    }
--- request
GET /mysql
--- response_body eval
"id,name\r
"



=== TEST 5: update & no module header
--- http_config eval: $::http_config
--- config
    location /mysql {
        if ($arg_name ~ '[^A-Za-z0-9]') {
            return 400;
        }

        drizzle_pass backend;
        drizzle_module_header off;
        drizzle_query "update cats set name='$arg_name' where name='$arg_name'";

        rds_csv on;
    }
--- request
GET /mysql?name=bob
--- response_headers
X-Resty-DBD-Module: 
Content-Type: text/csv; header=presence
--- response_body eval
qq{errcode,errstr,insert_id,affected_rows\r
0,Rows matched: 1  Changed: 0  Warnings: 0,0,0\r
}
--- no_error_log
[error]



=== TEST 6: invalid SQL
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        drizzle_module_header off;
        drizzle_query "select '32";
        rds_csv on;
        rds_csv_row_terminator "\n";
    }
--- response_headers
X-Resty-DBD-Module:
Content-Type: text/html
--- request
GET /mysql
--- error_code: 500
--- response_body_like: 500 Internal Server Error



=== TEST 7: single row, single col
--- http_config eval: $::http_config
--- config
    location /test {
        echo_location /mysql "drop table if exists singles";
        echo_location /mysql "create table singles (name varchar(15));";
        echo_location /mysql "insert into singles values ('marry');";
        echo_location /mysql "select * from singles;";
    }
    location /mysql {
        drizzle_pass backend;
        drizzle_module_header off;
        drizzle_query $query_string;
        rds_csv on;
        rds_csv_row_terminator "\n";
    }
--- request
GET /test
--- response_body
errcode,errstr,insert_id,affected_rows
0,,0,0
errcode,errstr,insert_id,affected_rows
0,,0,0
errcode,errstr,insert_id,affected_rows
0,,0,1
name
marry
--- skip_nginx: 2: < 0.7.46
--- timeout: 5



=== TEST 8: floating number and insert id
--- http_config eval: $::http_config
--- config
    location /test {
        echo_location /mysql "drop table if exists foo";
        echo_location /mysql "create table foo (id serial not null, primary key (id), val real);";
        echo_location /mysql "insert into foo (val) values (3.1415926);";
        echo_location /mysql "select * from foo;";
    }
    location /mysql {
        drizzle_pass backend;
        drizzle_module_header off;
        drizzle_query $query_string;
        rds_csv on;
        rds_csv_row_terminator "\n";
    }
--- request
GET /test
--- response_body
errcode,errstr,insert_id,affected_rows
0,,0,0
errcode,errstr,insert_id,affected_rows
0,,0,0
errcode,errstr,insert_id,affected_rows
0,,1,1
id,val
1,3.1415926
--- skip_nginx: 2: < 0.7.46



=== TEST 9: text blob field
--- http_config eval: $::http_config
--- config
    location /test {
        echo_location /mysql "drop table if exists foo";
        echo_location /mysql "create table foo (id serial, body text);";
        echo_location /mysql "insert into foo (body) values ('hello');";
        echo_location /mysql "select * from foo;";
    }
    location /mysql {
        drizzle_pass backend;
        drizzle_module_header off;
        drizzle_query $query_string;
        rds_csv on;
        rds_csv_row_terminator "\n";
    }
--- request
GET /test
--- response_body
errcode,errstr,insert_id,affected_rows
0,,0,0
errcode,errstr,insert_id,affected_rows
0,,0,0
errcode,errstr,insert_id,affected_rows
0,,1,1
id,body
1,hello
--- skip_nginx: 2: < 0.7.46



=== TEST 10: bool blob field
--- http_config eval: $::http_config
--- config
    location /test {
        echo_location /mysql "drop table if exists foo";
        echo_location /mysql "create table foo (id serial, flag bool);";
        echo_location /mysql "insert into foo (flag) values (true);";
        echo_location /mysql "insert into foo (flag) values (false);";
        echo_location /mysql "select * from foo order by id;";
    }
    location /mysql {
        drizzle_pass backend;
        drizzle_module_header off;
        drizzle_query $query_string;
        rds_csv on;
        rds_csv_row_terminator "\n";
    }
--- request
GET /test
--- response_body eval
qq{errcode,errstr,insert_id,affected_rows
0,,0,0
errcode,errstr,insert_id,affected_rows
0,,0,0
errcode,errstr,insert_id,affected_rows
0,,1,1
errcode,errstr,insert_id,affected_rows
0,,2,1
id,flag
1,1
2,0
}
--- skip_nginx: 2: < 0.7.46
--- timeout: 10



=== TEST 11: bit field
--- http_config eval: $::http_config
--- config
    location /test {
        echo_location /mysql "drop table if exists foo";
        echo_location /mysql "create table foo (id serial, flag bit);";
        echo_location /mysql "insert into foo (flag) values (1);";
        echo_location /mysql "insert into foo (flag) values (0);";
        echo_location /mysql "select * from foo order by id;";
    }
    location /mysql {
        drizzle_pass backend;
        drizzle_module_header off;
        drizzle_query $query_string;
        rds_csv on;
        rds_csv_row_terminator "\n";
    }
--- request
GET /test
--- response_body eval
qq{errcode,errstr,insert_id,affected_rows
0,,0,0
errcode,errstr,insert_id,affected_rows
0,,0,0
errcode,errstr,insert_id,affected_rows
0,,1,1
errcode,errstr,insert_id,affected_rows
0,,2,1
id,flag
1,\x01
2,\x00
}
--- skip_nginx: 2: < 0.7.46
--- timeout: 10



=== TEST 12: date type
--- http_config eval: $::http_config
--- config
    location /test {
        echo_location /mysql "drop table if exists foo";
        echo_location /mysql "create table foo (id serial, created date);";
        echo_location /mysql "insert into foo (created) values ('2007-05-24');";
        echo_location /mysql "select * from foo";
    }
    location /mysql {
        drizzle_pass backend;
        drizzle_module_header off;
        drizzle_query $query_string;
        rds_csv on;
    }
--- request
GET /test
--- response_body eval
qq{errcode,errstr,insert_id,affected_rows\r
0,,0,0\r
errcode,errstr,insert_id,affected_rows\r
0,,0,0\r
errcode,errstr,insert_id,affected_rows\r
0,,1,1\r
id,created\r
1,2007-05-24\r
}



=== TEST 13: strings need to be escaped (forcing utf8)
--- http_config
    upstream backend {
        drizzle_server 127.0.0.1:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test
                       charset=utf8;
    }

--- config
    location /test {
        echo_location /mysql "drop table if exists foo";
        echo_location /mysql "create table foo (id serial, body char(25));";
        echo_location /mysql "insert into foo (body) values ('a\\r\\nb\\b你好\Z');";
        echo_location /mysql "select * from foo";
    }
    location /mysql {
        drizzle_pass backend;
        drizzle_module_header off;
        drizzle_query $query_string;
        rds_csv on;
    }
--- request
GET /test
--- response_body eval
qq{errcode,errstr,insert_id,affected_rows\r
0,,0,0\r
errcode,errstr,insert_id,affected_rows\r
0,,0,0\r
errcode,errstr,insert_id,affected_rows\r
0,,1,1\r
id,body\r
1,"a\r
b\b??\cZ"\r
}
--- timeout: 5



=== TEST 14: strings need to be escaped
--- http_config eval: $::http_config
--- config
    location /test {
        echo_location /mysql "drop table if exists foo";
        echo_location /mysql "create table foo (id serial, body char(25));";
        echo_location /mysql "insert into foo (body) values ('a\\r\\nb\\b你好\Z');";
        echo_location /mysql "select * from foo";
    }
    location /mysql {
        drizzle_pass backend;
        drizzle_module_header off;
        drizzle_query $query_string;
        rds_csv on;
    }
--- request
GET /test
--- response_body eval
qq{errcode,errstr,insert_id,affected_rows\r
0,,0,0\r
errcode,errstr,insert_id,affected_rows\r
0,,0,0\r
errcode,errstr,insert_id,affected_rows\r
0,,1,1\r
id,body\r
1,"a\r
b\b你好\x1a"\r
}



=== TEST 15: null values
--- http_config eval: $::http_config
--- config
    location /test {
        echo_location /mysql "drop table if exists foo";
        echo_location /mysql "create table foo (id serial, name char(10), age integer);";
        echo_location /mysql "insert into foo (name, age) values ('', null);";
        echo_location /mysql "insert into foo (name, age) values (null, 0);";
        echo_location /mysql "select * from foo order by id";
    }
    location /mysql {
        drizzle_pass backend;
        drizzle_module_header off;
        drizzle_query $query_string;
        rds_csv on;
    }
--- request
GET /test
--- response_body eval
qq{errcode,errstr,insert_id,affected_rows\r
0,,0,0\r
errcode,errstr,insert_id,affected_rows\r
0,,0,0\r
errcode,errstr,insert_id,affected_rows\r
0,,1,1\r
errcode,errstr,insert_id,affected_rows\r
0,,2,1\r
id,name,age\r
1,,\r
2,,0\r
}
--- timeout: 10

