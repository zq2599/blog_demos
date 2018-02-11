# vi:filetype=perl

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (blocks() * 2);

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

=== TEST 1: synchronous
--- http_config eval: $::http_config
--- config
        location /bigpipe {
            echo                 "<html>(...template with javascript and divs...)";
            echo -n              "<script type=\"text/javascript\">loader.load(";
            echo_location        /_query1;
            echo                 ")</script>";
            echo -n              "<script type=\"text/javascript\">loader.load(";
            echo_location        /_query2;
            echo                 ")</script>";
            echo                 "</html>";
        }

        location /_query1 {
            internal;
            postgres_pass        database;
            postgres_query       "SELECT * FROM cats ORDER BY id ASC";
            rds_json             on;
        }

        location /_query2 {
            internal;
            postgres_pass        database;
            postgres_query       "SELECT * FROM cats ORDER BY id DESC";
            rds_json             on;
        }
--- request
GET /bigpipe
--- error_code: 200
--- response_body
<html>(...template with javascript and divs...)
<script type="text/javascript">loader.load([{"id":2,"name":null},{"id":3,"name":"bob"}])</script>
<script type="text/javascript">loader.load([{"id":3,"name":"bob"},{"id":2,"name":null}])</script>
</html>
--- timeout: 10
--- skip_nginx: 2: < 0.7.46



=== TEST 2: asynchronous (without echo filter)
--- http_config eval: $::http_config
--- config
        location /bigpipe {
            echo                 "<html>(...template with javascript and divs...)";
            echo -n              "<script type=\"text/javascript\">loader.load(";
            echo_location_async  /_query1;
            echo                 ")</script>";
            echo -n              "<script type=\"text/javascript\">loader.load(";
            echo_location_async  /_query2;
            echo                 ")</script>";
            echo                 "</html>";
        }

        location /_query1 {
            internal;
            postgres_pass        database;
            postgres_query       "SELECT * FROM cats ORDER BY id ASC";
            rds_json             on;
        }

        location /_query2 {
            internal;
            postgres_pass        database;
            postgres_query       "SELECT * FROM cats ORDER BY id DESC";
            rds_json             on;
        }
--- request
GET /bigpipe
--- error_code: 200
--- response_body
<html>(...template with javascript and divs...)
<script type="text/javascript">loader.load([{"id":2,"name":null},{"id":3,"name":"bob"}])</script>
<script type="text/javascript">loader.load([{"id":3,"name":"bob"},{"id":2,"name":null}])</script>
</html>
--- timeout: 10
--- skip_nginx: 2: < 0.7.46



=== TEST 3: asynchronous (with echo filter)
--- http_config eval: $::http_config
--- config
        location /bigpipe {
            echo_before_body     "<html>(...template with javascript and divs...)";
            echo_before_body -n  "<script type=\"text/javascript\">loader.load(";
            echo -n              " "; # XXX we need this to help our echo filters
            echo_location_async  /_query1;
            echo                 ")</script>";
            echo -n              "<script type=\"text/javascript\">loader.load(";
            echo_location_async  /_query2;
            echo_after_body      ")</script>";
            echo_after_body      "</html>";
        }

        location /_query1 {
            internal;
            postgres_pass        database;
            postgres_query       "SELECT * FROM cats ORDER BY id ASC";
            rds_json             on;
        }

        location /_query2 {
            internal;
            postgres_pass        database;
            postgres_query       "SELECT * FROM cats ORDER BY id DESC";
            rds_json             on;
        }
--- request
GET /bigpipe
--- error_code: 200
--- response_body
<html>(...template with javascript and divs...)
<script type="text/javascript">loader.load( [{"id":2,"name":null},{"id":3,"name":"bob"}])</script>
<script type="text/javascript">loader.load([{"id":3,"name":"bob"},{"id":2,"name":null}])</script>
</html>
--- timeout: 10
--- skip_nginx: 2: < 0.7.46
