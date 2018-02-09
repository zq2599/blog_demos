# vim:set ft= ts=4 sw=4 et:

use Test::Nginx::Socket::Lua;
use Cwd qw(cwd);
use Protocol::WebSocket::Frame;

repeat_each(2);

plan tests => repeat_each() * (blocks() * 3);

my $pwd = cwd();

our $HttpConfig = qq{
    lua_package_path "$pwd/lib/?.lua;;";
    lua_package_cpath "/usr/local/openresty-debug/lualib/?.so;/usr/local/openresty/lualib/?.so;;";
};

$ENV{TEST_NGINX_RESOLVER} = '8.8.8.8';
$ENV{TEST_NGINX_REDIS_PORT} ||= 6379;

no_long_string();
#no_diff();

run_tests();

__DATA__

=== TEST 1: module size of resty.websocket.protocol
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        content_by_lua '
            local proto = require "resty.websocket.protocol"
            n = 0
            for _, _ in pairs(proto) do
                n = n + 1
            end
            ngx.say("size: ", n)
        ';
    }
--- request
GET /t
--- response_body
size: 5
--- no_error_log
[error]



=== TEST 2: module size of resty.websocket.client
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        content_by_lua '
            local client = require "resty.websocket.client"
            n = 0
            for _, _ in pairs(client) do
                n = n + 1
            end
            ngx.say("size: ", n)
        ';
    }
--- request
GET /t
--- response_body
size: 13
--- no_error_log
[error]



=== TEST 3: module size of resty.websocket.server
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        content_by_lua '
            local server = require "resty.websocket.server"
            n = 0
            for _, _ in pairs(server) do
                n = n + 1
            end
            ngx.say("size: ", n)
        ';
    }
--- request
GET /t
--- response_body
size: 10
--- no_error_log
[error]

