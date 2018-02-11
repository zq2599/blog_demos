# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * 2 * blocks();

$ENV{TEST_NGINX_MYSQL_HOST} ||= '127.0.0.1';
$ENV{TEST_NGINX_MYSQL_PORT} ||= 3306;

our $http_config = <<'_EOC_';
    upstream backend {
        drizzle_server $TEST_NGINX_MYSQL_HOST:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
    }
_EOC_

no_long_string();
no_diff();

run_tests();

__DATA__

=== TEST 1: escaping column names (normal mode)
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_query "
            select `\"name\"`, height from birds order by height;
        ";
        drizzle_pass backend;
        rds_json on;
    }
--- request
GET /mysql
--- response_body chomp
[{"\"name\"":"hi,ya","height":-3},{"\"name\"":"\rkay","height":0.005},{"\"name\"":"ab;c","height":0.005},{"\"name\"":"hello \"tom","height":3.14},{"\"name\"":"hey\ndad","height":7},{"\"name\"":"foo\tbar","height":21}]



=== TEST 2: escaping column names (compact mode)
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_query "
            select `\"name\"`, height from birds order by height;
        ";
        drizzle_pass backend;
        rds_json on;
        rds_json_format compact;
    }
--- request
GET /mysql
--- response_body chomp
[["\"name\"","height"],["hi,ya",-3],["\rkay",0.005],["ab;c",0.005],["hello \"tom",3.14],["hey\ndad",7],["foo\tbar",21]]

