# vi:filetype=perl

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(1);

plan tests => repeat_each() * (blocks() * 3);

$ENV{TEST_NGINX_POSTGRESQL_HOST} ||= '127.0.0.1';
$ENV{TEST_NGINX_POSTGRESQL_PORT} ||= 5432;

our $http_config = <<'_EOC_';
    upstream database {
        postgres_server  $TEST_NGINX_POSTGRESQL_HOST:$TEST_NGINX_POSTGRESQL_PORT
                         dbname=ngx_test user=ngx_test password=ngx_test;
    }
_EOC_

our $config = <<'_EOC_';
    set $random  123;

    location = /auth {
        internal;

        postgres_escape     $user $remote_user;
        postgres_escape     $pass $remote_passwd;

        postgres_pass       database;
        postgres_query      "SELECT login FROM users WHERE login=$user AND pass=$pass";
        postgres_rewrite    no_rows 403;
        postgres_output     none;
    }

    location = /numbers/ {
        auth_request        /auth;
        postgres_pass       database;
        rds_json            on;

        postgres_query      HEAD GET  "SELECT * FROM numbers";

        postgres_query      POST      "INSERT INTO numbers VALUES('$random') RETURNING *";
        postgres_rewrite    POST      changes 201;

        postgres_query      DELETE    "DELETE FROM numbers";
        postgres_rewrite    DELETE    no_changes 204;
        postgres_rewrite    DELETE    changes 204;
    }

    location ~ /numbers/(\d+) {
        auth_request        /auth;
        postgres_pass       database;
        rds_json            on;

        postgres_query      HEAD GET  "SELECT * FROM numbers WHERE number='$1'";
        postgres_rewrite    HEAD GET  no_rows 410;

        postgres_query      PUT       "UPDATE numbers SET number='$1' WHERE number='$1' RETURNING *";
        postgres_rewrite    PUT       no_changes 410;

        postgres_query      DELETE    "DELETE FROM numbers WHERE number='$1'";
        postgres_rewrite    DELETE    no_changes 410;
        postgres_rewrite    DELETE    changes 204;
    }
_EOC_

our $request_headers = <<'_EOC_';
Authorization: Basic bmd4X3Rlc3Q6bmd4X3Rlc3Q=
_EOC_

no_shuffle();
run_tests();

__DATA__

=== TEST 1: clean collection
--- http_config eval: $::http_config
--- config eval: $::config
--- more_headers eval: $::request_headers
--- request
DELETE /numbers/
--- error_code: 204
--- response_headers
! Content-Type
--- response_body eval
""
--- timeout: 10



=== TEST 2: list empty collection
--- http_config eval: $::http_config
--- config eval: $::config
--- more_headers eval: $::request_headers
--- request
GET /numbers/
--- error_code: 200
--- response_headers
Content-Type: application/json
--- response_body chomp
[]
--- timeout: 10



=== TEST 3: insert resource into collection
--- http_config eval: $::http_config
--- config eval: $::config
--- more_headers eval: $::request_headers
--- request
POST /numbers/
--- error_code: 201
--- response_headers
Content-Type: application/json
--- response_body chomp
[{"number":123}]
--- timeout: 10
--- skip_slave: 3: CentOS



=== TEST 4: list collection
--- http_config eval: $::http_config
--- config eval: $::config
--- more_headers eval: $::request_headers
--- request
GET /numbers/
--- error_code: 200
--- response_headers
Content-Type: application/json
--- response_body chomp
[{"number":123}]
--- timeout: 10
--- skip_slave: 3: CentOS



=== TEST 5: get resource
--- http_config eval: $::http_config
--- config eval: $::config
--- more_headers eval: $::request_headers
--- request
GET /numbers/123
--- error_code: 200
--- response_headers
Content-Type: application/json
--- response_body chomp
[{"number":123}]
--- timeout: 10
--- skip_slave: 3: CentOS



=== TEST 6: update resource
--- http_config eval: $::http_config
--- config eval: $::config
--- more_headers
Authorization: Basic bmd4X3Rlc3Q6bmd4X3Rlc3Q=
Content-Length: 0
--- request
PUT /numbers/123
--- error_code: 200
--- response_headers
Content-Type: application/json
--- response_body chomp
[{"number":123}]
--- timeout: 10
--- skip_slave: 3: CentOS



=== TEST 7: remove resource
--- http_config eval: $::http_config
--- config eval: $::config
--- more_headers eval: $::request_headers
--- request
DELETE /numbers/123
--- error_code: 204
--- response_headers
! Content-Type
--- response_body eval
""
--- timeout: 10
--- skip_slave: 3: CentOS



=== TEST 8: update non-existing resource
--- http_config eval: $::http_config
--- config eval: $::config
--- more_headers
Authorization: Basic bmd4X3Rlc3Q6bmd4X3Rlc3Q=
Content-Length: 0
--- request
PUT /numbers/123
--- error_code: 410
--- response_headers
Content-Type: text/html
--- response_body_like: 410 Gone
--- timeout: 10
--- skip_slave: 3: CentOS



=== TEST 9: get non-existing resource
--- http_config eval: $::http_config
--- config eval: $::config
--- more_headers eval: $::request_headers
--- request
GET /numbers/123
--- error_code: 410
--- response_headers
Content-Type: text/html
--- response_body_like: 410 Gone
--- timeout: 10



=== TEST 10: remove non-existing resource
--- http_config eval: $::http_config
--- config eval: $::config
--- more_headers eval: $::request_headers
--- request
DELETE /numbers/123
--- error_code: 410
--- response_headers
Content-Type: text/html
--- response_body_like: 410 Gone
--- timeout: 10



=== TEST 11: list empty collection (done)
--- http_config eval: $::http_config
--- config eval: $::config
--- more_headers eval: $::request_headers
--- request
GET /numbers/
--- error_code: 200
--- response_headers
Content-Type: application/json
--- response_body chomp
[]
--- timeout: 10
