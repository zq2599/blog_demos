# vi:filetype=perl

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (blocks() * 3);

worker_connections(128);
run_tests();

no_diff();

__DATA__

=== TEST 1: sanity
--- config
    location /echo {
        echo -n  $remote_passwd;
    }
--- more_headers
Authorization: Basic bW9udHk6c29tZV9wYXNz
--- request
GET /echo
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body chop
some_pass
--- timeout: 10



=== TEST 2: sanity (without Authorization header)
--- config
    location /echo {
        echo -n  $remote_passwd;
    }
--- request
GET /echo
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body:
--- timeout: 10



=== TEST 3: accessible from within lua
--- config
    location /echo {
        content_by_lua '
            ngx.say(ngx.var.remote_passwd)
        ';
    }
--- more_headers
Authorization: Basic bW9udHk6c29tZV9wYXNz
--- request
GET /echo
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
some_pass
--- timeout: 10

