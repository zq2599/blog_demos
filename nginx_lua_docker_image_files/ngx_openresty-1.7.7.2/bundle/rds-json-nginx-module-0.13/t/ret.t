# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(3);
#repeat_each(1);

plan tests => repeat_each() * 3 * blocks();

no_long_string();

run_tests();

__DATA__

=== TEST 1: sanity
--- config
    location /foo {
        rds_json_ret 400 "Bad request";
    }
--- request
    GET /foo
--- response_headers
Content-Type: application/json
--- response_body chop
{"errcode":400,"errstr":"Bad request"}



=== TEST 2: empty errstr
--- config
    location /foo {
        rds_json_ret 400 "";
    }
--- request
    GET /foo
--- response_headers
Content-Type: application/json
--- response_body chop
{"errcode":400}



=== TEST 3: set content type
--- config
    rds_json_content_type 'text/javascript';
    location /foo {
        rds_json_ret 400 "Bad request";
    }
--- request
    GET /foo
--- response_headers
Content-Type: text/javascript
--- response_body chop
{"errcode":400,"errstr":"Bad request"}



=== TEST 4: JSON escaping
--- config
    location /foo {
        if ($arg_limit !~ '^\d+$') {
            rds_json_ret 400 'Invalid "limit" argument.';
        }
        echo done;
    }
--- request
    GET /foo
--- response_headers
Content-Type: application/json
--- response_body chop
{"errcode":400,"errstr":"Invalid \"limit\" argument."}

