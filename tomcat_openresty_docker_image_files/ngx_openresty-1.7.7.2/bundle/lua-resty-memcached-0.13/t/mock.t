# vim:set ft= ts=4 sw=4 et:

use Test::Nginx::Socket::Lua;
use Cwd qw(cwd);

repeat_each(2);

plan tests => repeat_each() * (4 * blocks() + 2);

my $pwd = cwd();

our $HttpConfig = qq{
    lua_package_path "$pwd/lib/?.lua;;";
};

$ENV{TEST_NGINX_RESOLVER} = '8.8.8.8';
$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

no_long_string();

run_tests();

__DATA__

=== TEST 1: fail to flush
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local memcached = require "resty.memcached"
            local memc = memcached:new()

            memc:set_timeout(1000) -- 1 sec

            local ok, err = memc:connect("127.0.0.1", 1921);
            if not ok then
                ngx.say("failed to connect: ", err)
                return
            end

            local ok, err = memc:flush_all()
            if not ok then
                ngx.say("failed to flush all: ", err)
                return
            end

            ngx.say("flush: ", ok);

            memc:close()
        ';
    }
--- request
GET /t
--- tcp_listen: 1921
--- tcp_query_len: 11
--- tcp_query eval
"flush_all\r\n"
--- tcp_reply eval
"SOME ERROR\r\n"
--- response_body
failed to flush all: SOME ERROR
--- no_error_log
[error]



=== TEST 2: continue using the obj when read timeout happens
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local memcached = require "resty.memcached"
            local memc = memcached:new()

            local ok, err = memc:connect("127.0.0.1", 1921);
            if not ok then
                ngx.say("failed to connect: ", err)
                return
            end

            memc:set_timeout(100) -- 0.1 sec

            for i = 1, 2 do
                local data, flags, err = memc:get("foo")
                if not data and err then
                    ngx.say("failed to get: ", err)
                else
                    ngx.say("get: ", data);
                end
                ngx.sleep(0.1)
            end

            memc:close()
        ';
    }
--- request
GET /t
--- tcp_listen: 1921
--- tcp_query_len: 9
--- tcp_query eval
"get foo\r\n"
--- tcp_reply eval
"VALUE foo 0 5\r\nhello\r\nEND\r\n"
--- tcp_reply_delay: 150ms
--- response_body
failed to get: failed to receive 1st line: timeout
failed to get: failed to send command: closed
--- error_log
lua tcp socket read timed out

