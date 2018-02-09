# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (5 * blocks());

no_long_string();
log_level('warn');

#master_on();
#workers(1);

run_tests();

__DATA__

=== TEST 1: filters used
--- http_config
    postpone_output 1;
--- config
    location /echo {
        echo world;
        echo_after_body hello;
    }
--- request
    GET /echo?blah
--- response_body
world
hello
--- error_log
echo header filter, uri "/echo?blah"
echo body filter, uri "/echo?blah"
--- no_error_log
[error]
--- log_level: debug



=== TEST 2: filters not used
--- http_config
    postpone_output 1;
--- config
    location /echo {
        echo world;
        #echo_after_body hello;
    }
--- request
    GET /echo?blah
--- response_body
world
--- no_error_log
echo header filter, uri "/echo?blah"
echo body filter, uri "/echo?blah"
[error]
--- log_level: debug



=== TEST 3: (after) filters used (multiple http {} blocks)
--- http_config
    postpone_output 1;
--- config
    location /echo {
        echo world;
        echo_after_body hello;
    }

--- post_main_config
    http {
    }

--- request
    GET /echo?blah
--- response_body
world
hello
--- error_log
echo header filter, uri "/echo?blah"
echo body filter, uri "/echo?blah"
--- no_error_log
[error]
--- log_level: debug



=== TEST 4: (before) filters used (multiple http {} blocks)
--- http_config
    postpone_output 1;
--- config
    location /echo {
        echo world;
        echo_before_body hello;
    }

--- post_main_config
    http {
    }

--- request
    GET /echo?blah
--- response_body
hello
world
--- error_log
echo header filter, uri "/echo?blah"
echo body filter, uri "/echo?blah"
--- no_error_log
[error]
--- log_level: debug

