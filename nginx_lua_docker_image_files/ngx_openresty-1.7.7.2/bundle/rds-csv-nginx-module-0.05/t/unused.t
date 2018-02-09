# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(10);
no_shuffle();

repeat_each(2);

plan tests => repeat_each() * (4 * blocks());

run_tests();

__DATA__

=== TEST 1: used
--- http_config eval: $::http_config
--- config
    location = /t {
        echo ok;
        rds_csv on;
    }
--- request
GET /t
--- stap
F(ngx_http_rds_csv_header_filter) {
    println("rds csv header filter")
}

F(ngx_http_rds_csv_body_filter) {
    println("rds csv body filter")
}

--- stap_out
rds csv header filter
rds csv body filter
rds csv body filter

--- response_body
ok
--- no_error_log
[error]



=== TEST 2: unused
--- http_config eval: $::http_config
--- config
    location = /t {
        echo ok;
        #rds_csv on;
    }
--- request
GET /t
--- stap
F(ngx_http_rds_csv_header_filter) {
    println("rds csv header filter")
}

F(ngx_http_rds_csv_body_filter) {
    println("rds csv body filter")
}

--- stap_out
--- response_body
ok
--- no_error_log
[error]



=== TEST 3: used (multi http {} blocks)
--- http_config eval: $::http_config
--- config
    location = /t {
        echo ok;
        rds_csv on;
    }

--- post_main_config
    http {
    }

--- request
GET /t
--- stap
F(ngx_http_rds_csv_header_filter) {
    println("rds csv header filter")
}

F(ngx_http_rds_csv_body_filter) {
    println("rds csv body filter")
}

--- stap_out
rds csv header filter
rds csv body filter
rds csv body filter

--- response_body
ok
--- no_error_log
[error]

