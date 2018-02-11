# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);
#repeat_each(1);

plan tests => repeat_each() * (5 * blocks());

log_level('debug');

no_long_string();

run_tests();

__DATA__

=== TEST 1: not using this module's directives at all
--- config
    location /foo {
        echo Hello;
    }
--- request
    GET /foo
--- response_headers
Content-Type: text/plain
--- response_body
Hello
--- no_error_log
rds json header filter, "/foo"
rds json body filter, "/foo"



=== TEST 2: using rds_json_ret, but not rds_json
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
--- no_error_log
rds json header filter, "/foo"
rds json body filter, "/foo"



=== TEST 3: using rds_json
--- config
    location /foo {
        echo Hello;
        rds_json on;
    }
--- request
    GET /foo
--- response_headers
Content-Type: text/plain
--- response_body
Hello
--- error_log
rds json header filter, "/foo"
rds json body filter, "/foo"



=== TEST 4: multiple http {} blocks
--- config
    location /foo {
        echo Hello;
        rds_json on;
    }

--- post_main_config
    http {
    }

--- request
    GET /foo
--- response_headers
Content-Type: text/plain
--- response_body
Hello
--- error_log
rds json header filter, "/foo"
rds json body filter, "/foo"

