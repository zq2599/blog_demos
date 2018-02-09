# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (3 * blocks());

no_long_string();
log_level('warn');

#master_on();
#workers(1);

run_tests();

__DATA__

=== TEST 1: filter indeed used
--- http_config
    postpone_output 1;
--- config
    location /echo {
        echo_after_body hello;
        echo world;
    }
--- request
    GET /echo
--- stap
F(ngx_http_echo_header_filter) {
    println("echo header filter called")
}
--- stap_out
echo header filter called
--- response_body
world
hello



=== TEST 2: filter not used
--- http_config
    postpone_output 1;
--- config
    location /echo {
        #echo_after_body hello;
        echo world;
    }
--- request
    GET /echo
--- stap
F(ngx_http_echo_header_filter) {
    println("echo header filter called")
}
--- stap_out
--- response_body
world

