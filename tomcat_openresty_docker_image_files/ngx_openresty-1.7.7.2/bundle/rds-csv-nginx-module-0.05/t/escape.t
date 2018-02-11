# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * 3 * blocks();

$ENV{TEST_NGINX_MYSQL_PORT} ||= 3306;

our $http_config = <<'_EOC_';
    upstream backend {
        drizzle_server 127.0.0.1:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
    }
_EOC_

#no_diff();
no_long_string();

run_tests();

__DATA__

=== TEST 1: sanity
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_query "
            select * from dogs order by name asc;
        ";
        drizzle_pass backend;
        rds_csv on;
    }
--- request
POST /mysql
sql=select%20*%20from%20cats;
--- response_headers
Content-Type: text/csv; header=presence
--- response_body eval
qq{male,name,height\r
0,"\rkay",0.005\r
0,ab;c,0.005\r
0,foo\tbar,21\r
0,"hello ""tom",3.14\r
0,"hey
dad",7\r
1,"hi,ya",-3\r
}



=== TEST 2: using ; as the field separator
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_query "
            select * from dogs order by name asc;
        ";
        drizzle_pass backend;
        rds_csv_field_separator ';';
        rds_csv on;
    }
--- request
POST /mysql
sql=select%20*%20from%20cats;
--- response_headers
Content-Type: text/csv; header=presence
--- response_body eval
qq{male;name;height\r
0;"\rkay";0.005\r
0;"ab;c";0.005\r
0;foo\tbar;21\r
0;"hello ""tom";3.14\r
0;"hey
dad";7\r
1;hi,ya;-3\r
}



=== TEST 3: using tab as the field separator
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_query "
            select * from dogs order by name asc;
        ";
        drizzle_pass backend;
        rds_csv_field_separator '\t';
        rds_csv on;
    }
--- request
POST /mysql
sql=select%20*%20from%20cats;
--- response_headers
Content-Type: text/csv; header=presence
--- response_body eval
qq{male\tname\theight\r
0\t"\rkay"\t0.005\r
0\tab;c\t0.005\r
0\t"foo\tbar"\t21\r
0\t"hello ""tom"\t3.14\r
0\t"hey
dad"\t7\r
1\thi,ya\t-3\r
}



=== TEST 4: explicitly using \r\n as the row terminator
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_query "
            select * from dogs order by name asc;
        ";
        drizzle_pass backend;
        rds_csv_field_separator '\t';
        rds_csv_row_terminator '\r\n';
        rds_csv on;
    }
--- request
POST /mysql
sql=select%20*%20from%20cats;
--- response_headers
Content-Type: text/csv; header=presence
--- response_body eval
qq{male\tname\theight\r
0\t"\rkay"\t0.005\r
0\tab;c\t0.005\r
0\t"foo\tbar"\t21\r
0\t"hello ""tom"\t3.14\r
0\t"hey
dad"\t7\r
1\thi,ya\t-3\r
}



=== TEST 5: using \n as the row terminator
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_query "
            select * from dogs order by name asc;
        ";
        drizzle_pass backend;
        rds_csv_field_separator '\t';
        rds_csv_row_terminator '\n';
        rds_csv on;
    }
--- request
POST /mysql
sql=select%20*%20from%20cats;
--- response_headers
Content-Type: text/csv; header=presence
--- response_body eval
qq{male\tname\theight
0\t"\rkay"\t0.005
0\tab;c\t0.005
0\t"foo\tbar"\t21
0\t"hello ""tom"\t3.14
0\t"hey
dad"\t7
1\thi,ya\t-3
}



=== TEST 6: explicitly field name header on
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_query "
            select * from dogs order by name asc;
        ";
        drizzle_pass backend;
        rds_csv_field_separator '\t';
        rds_csv_row_terminator '\n';
        rds_csv_field_name_header on;
        rds_csv on;
    }
--- request
POST /mysql
sql=select%20*%20from%20cats;
--- response_headers
Content-Type: text/csv; header=presence
--- response_body eval
qq{male\tname\theight
0\t"\rkay"\t0.005
0\tab;c\t0.005
0\t"foo\tbar"\t21
0\t"hello ""tom"\t3.14
0\t"hey
dad"\t7
1\thi,ya\t-3
}



=== TEST 7: explicitly field name header off
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_query "
            select * from dogs order by name asc;
        ";
        drizzle_pass backend;
        rds_csv_field_separator '\t';
        rds_csv_row_terminator '\n';
        rds_csv_field_name_header off;
        rds_csv on;
    }
--- request
POST /mysql
sql=select%20*%20from%20cats;
--- response_headers
Content-Type: text/csv; header=absence
--- response_body eval
qq{0\t"\rkay"\t0.005
0\tab;c\t0.005
0\t"foo\tbar"\t21
0\t"hello ""tom"\t3.14
0\t"hey
dad"\t7
1\thi,ya\t-3
}



=== TEST 8: the "charset" directive does not affect us
--- http_config eval: $::http_config
--- config
    location /mysql {
        charset "gbk";
        drizzle_query "
            select * from dogs order by name asc;
        ";
        drizzle_pass backend;
        rds_csv_field_separator '\t';
        rds_csv_row_terminator '\n';
        rds_csv_field_name_header off;
        rds_csv on;
    }
--- request
POST /mysql
sql=select%20*%20from%20cats;
--- response_headers
Content-Type: text/csv; header=absence
--- response_body eval
qq{0\t"\rkay"\t0.005
0\tab;c\t0.005
0\t"foo\tbar"\t21
0\t"hello ""tom"\t3.14
0\t"hey
dad"\t7
1\thi,ya\t-3
}



=== TEST 9: set content type
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_query "
            select * from dogs order by name asc;
        ";
        drizzle_pass backend;
        rds_csv_field_separator '\t';
        rds_csv_row_terminator '\n';
        rds_csv_field_name_header off;
        rds_csv_content_type "text/tab-separated-values";
        rds_csv on;
    }
--- request
GET /mysql
--- response_headers
Content-Type: text/tab-separated-values
--- response_body eval
qq{0\t"\rkay"\t0.005
0\tab;c\t0.005
0\t"foo\tbar"\t21
0\t"hello ""tom"\t3.14
0\t"hey
dad"\t7
1\thi,ya\t-3
}



=== TEST 10: set content type (bigger buffer size)
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_query "
            select * from dogs order by name asc;
        ";
        drizzle_pass backend;
        rds_csv_field_separator '\t';
        rds_csv_row_terminator '\n';
        rds_csv_field_name_header off;
        rds_csv_content_type "text/tab-separated-values";
        rds_csv on;
        rds_csv_buffer_size 8k;
    }
--- request
GET /mysql
--- response_headers
Content-Type: text/tab-separated-values
--- response_body eval
qq{0\t"\rkay"\t0.005
0\tab;c\t0.005
0\t"foo\tbar"\t21
0\t"hello ""tom"\t3.14
0\t"hey
dad"\t7
1\thi,ya\t-3
}



=== TEST 11: escaping column names
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_query "
            select `\"name\"`, height from birds order by height limit 1;
        ";
        drizzle_pass backend;
        rds_csv on;
        rds_csv_row_terminator "\n";
    }
--- request
GET /mysql
--- response_headers
Content-Type: text/csv; header=presence
--- response_body
"""name""",height
"hi,ya",-3

