# vi:filetype=perl

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (blocks() * 3);

worker_connections(128);
run_tests();

no_diff();

__DATA__

=== TEST 1: not changed
--- config
    location /test {
        override_method  $arg_method;
        echo -n          $request_method;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body chop
GET
--- timeout: 10



=== TEST 2: changed
--- config
    location /test {
        override_method  $arg_method;
        echo -n          $request_method;
    }
--- request
GET /test?method=POST
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body chop
POST
--- timeout: 10



=== TEST 3: inherited
--- config
    override_method  $arg_method;

    location /test {
        echo -n          $request_method;
    }
--- request
GET /test?method=POST
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body chop
POST
--- timeout: 10



=== TEST 4: not inherited
--- config
    override_method  $arg_method;

    location /test {
        override_method  PUT;
        echo -n          $request_method;
    }
--- request
GET /test?method=POST
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body chop
PUT
--- timeout: 10



=== TEST 5: restored
--- config
    override_method  $arg_method;

    location /test {
        override_method  off;
        echo -n          $request_method;
    }
--- request
GET /test?method=POST
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body chop
GET
--- timeout: 10



=== TEST 6: method-specific (changed)
--- config
    location /test {
        override_method  GET $arg_method;
        echo -n          $request_method;
    }
--- request
GET /test?method=POST
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body chop
POST
--- timeout: 10



=== TEST 7: method-specific (not changed)
--- config
    location /test {
        override_method  PUT $arg_method;
        echo -n          $request_method;
    }
--- request
GET /test?method=POST
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body chop
GET
--- timeout: 10



=== TEST 8: method-specific (multiple methods)
--- config
    location /test {
        override_method  GET HEAD PUT $arg_method;
        echo -n          $request_method;
    }
--- request
HEAD /test?method=POST
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body chop
POST
--- timeout: 10



=== TEST 9: double dance (blocked)
--- config
    override_method  GET $arg_method;
    if ($request_method = GET) {
        return 400;
    }

    location /test {
        override_method  GET PUT;
        if ($request_method = GET) {
            return 400;
        }

        echo -n          $request_method;
    }
--- request
GET /test
--- error_code: 400
--- response_headers
Content-Type: text/html
--- response_body_like: 400 Bad Request
--- timeout: 10



=== TEST 10: double dance (passed)
--- config
    override_method  GET $arg_method;
    if ($request_method = GET) {
        return 400;
    }

    location /test {
        override_method  GET PUT;
        if ($request_method = GET) {
            return 400;
        }

        echo -n          $request_method;
    }
--- request
GET /test?method=POST
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body chop
PUT
--- timeout: 10
--- skip_nginx: 3: < 0.8.34



=== TEST 11: double dance with "off"
--- config
    override_method  GET $arg_method;
    if ($request_method = GET) {
        return 400;
    }

    location /test {
        override_method  off;
        echo -n          $request_method;
    }
--- request
GET /test?method=POST
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body chop
GET
--- timeout: 10
--- skip_nginx: 3: < 0.8.34



=== TEST 12: changed method passed to upstream
--- http_config
    server {
        listen  8100;
        location / {
            echo -n      $request_method;
        }
    }
--- config
    location /test {
        override_method  $arg_method;
        proxy_pass       http://127.0.0.1:8100;
    }
--- request
GET /test?method=POST
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body chop
POST
--- timeout: 10



=== TEST 13: restored method passed to upstream
--- http_config
    server {
        listen  8100;
        location / {
            echo -n      $request_method;
        }
    }
--- config
    override_method  $arg_method;

    location /test {
        override_method  off;
        proxy_pass       http://127.0.0.1:8100;
    }
--- request
GET /test?method=POST
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body chop
GET
--- timeout: 10
