# vim:set ft= ts=4 sw=4 et fdm=marker:
use lib 'lib';
use Test::Nginx::Socket::Lua;
use Cwd qw(cwd);

#worker_connections(1014);
#master_process_enabled(1);
#log_level('warn');

repeat_each(2);

plan tests => repeat_each() * (blocks() * 5);

my $pwd = cwd();

our $HttpConfig = <<_EOC_;
    lua_package_path "$pwd/lib/?.lua;../lua-resty-lrucache/lib/?.lua;;";
    init_by_lua '
        local verbose = false
        if verbose then
            local dump = require "jit.dump"
            dump.on(nil, "$Test::Nginx::Util::ErrLogFile")
        else
            local v = require "jit.v"
            v.on("$Test::Nginx::Util::ErrLogFile")
        end

        require "resty.core"
        -- jit.off()
    ';
_EOC_

#no_diff();
#no_long_string();
check_accum_error_log();
run_tests();

__DATA__

=== TEST 1: string
--- http_config eval: $::HttpConfig
--- config
    location = /base64 {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.decode_base64("aGVsbG8=")
            end
            ngx.say(s)
        ';
    }
--- request
GET /base64
--- response_body
hello
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
 -- NYI:



=== TEST 2: set base64 (nil)
--- http_config eval: $::HttpConfig
--- config
    location = /base64 {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.decode_base64("")
            end
            ngx.say(s)
        ';
    }
--- request
GET /base64
--- response_body eval: "\n"
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
 -- NYI:



=== TEST 3: set base64 (number)
--- http_config eval: $::HttpConfig
--- config
    location = /base64 {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.decode_base64("My4xNA==")
            end
            ngx.say(s)
        ';
    }
--- request
GET /base64
--- response_body
3.14
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
 -- NYI:



=== TEST 4: set base64 (boolean)
--- http_config eval: $::HttpConfig
--- config
    location = /base64 {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.decode_base64("dHJ1ZQ==")
            end
            ngx.say(s)
        ';
    }
--- request
GET /base64
--- response_body
true
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
 -- NYI:



=== TEST 5: string (buf size just smaller than 4096)
--- http_config eval: $::HttpConfig
--- config
    location = /base64 {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.decode_base64(string.rep("a", 5460))
            end
            if not s then
                ngx.say("bad base64 string")
            else
                ngx.say(string.len(s))
            end
        ';
    }
--- request
GET /base64
--- response_body
4095
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
 -- NYI:



=== TEST 6: string (buf size just a bit bigger than 4096)
--- http_config eval: $::HttpConfig
--- config
    location = /base64 {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.decode_base64(string.rep("a", 5462))
            end
            if not s then
                ngx.say("bad base64 string")
            else
                ngx.say(string.len(s))
            end
        ';
    }
--- request
GET /base64
--- response_body
4096
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
 -- NYI:

