# vim:set ft= ts=4 sw=4 et fdm=marker:
use lib 'lib';
use Test::Nginx::Socket::Lua;
use Cwd qw(cwd);

#worker_connections(1014);
#master_process_enabled(1);
#log_level('warn');

repeat_each(2);

plan tests => repeat_each() * (blocks() * 5 + 5);

my $pwd = cwd();

our $HttpConfig = <<_EOC_;
    lua_shared_dict dogs 1m;
    lua_package_path "$pwd/lib/?.lua;../lua-resty-lrucache/lib/?.lua;;";
    init_by_lua '
        local verbose = false
        if verbose then
            local dump = require "jit.dump"
            dump.on("b", "$Test::Nginx::Util::ErrLogFile")
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

=== TEST 1: write to ngx.header.HEADER (single value)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        set $foo hello;
        content_by_lua '
            for i = 1, 100 do
                ngx.header["Foo"] = i
            end
            ngx.say("Foo: ", ngx.header["Foo"])
        ';
    }
--- request
GET /t
--- response_body
Foo: 100

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):2 loop\]/
--- no_error_log
[error]
 -- NYI:



=== TEST 2: write to ngx.header.HEADER (nil)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        set $foo hello;
        content_by_lua '
            for i = 1, 200 do
                ngx.header["Foo"] = i
                ngx.header["Foo"] = nil
            end
            ngx.say("Foo: ", ngx.header["Foo"])
        ';
    }
--- request
GET /t
--- response_body
Foo: nil

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):2 loop\]/
--- wait: 0.2
--- no_error_log
[error]
 -- NYI:



=== TEST 3: write to ngx.header.HEADER (multi-value)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        set $foo hello;
        content_by_lua '
            for i = 1, 200 do
                ngx.header["Foo"] = {i, i + 1}
            end
            local v = ngx.header["Foo"]
            if type(v) == "table" then
                ngx.say("Foo: ", table.concat(v, ", "))
            else
                ngx.say("Foo: ", v)
            end
        ';
    }
--- request
GET /t
--- response_body
Foo: 200, 201

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):2 loop\]/
--- no_error_log
[error]
 -- NYI:



=== TEST 4: read from ngx.header.HEADER (single value)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        set $foo hello;
        content_by_lua '
            local v
            for i = 1, 100 do
                ngx.header["Foo"] = i
                v = ngx.header["Foo"]
            end
            ngx.say("Foo: ", v)
        ';
    }
--- request
GET /t
--- response_body
Foo: 100

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
 -- NYI:
stitch



=== TEST 5: read from ngx.header.HEADER (not found)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        set $foo hello;
        content_by_lua '
            local v
            for i = 1, 100 do
                v = ngx.header["Foo"]
            end
            ngx.say("Foo: ", v)
        ';
    }
--- request
GET /t
--- response_body
Foo: nil

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
 -- NYI:
stitch



=== TEST 6: read from ngx.header.HEADER (multi-value)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        set $foo hello;
        content_by_lua '
            ngx.header["Foo"] = {"foo", "bar"}
            local v
            for i = 1, 100 do
                v = ngx.header["Foo"]
            end
            ngx.say("Foo: ", table.concat(v, ", "))
        ';
    }
--- request
GET /t
--- response_body
Foo: foo, bar

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):4 loop\]/
--- no_error_log
[error]
 -- NYI:
stitch



=== TEST 7: set multi values to cache-control and override it with multiple values
--- http_config eval: $::HttpConfig
--- config
    location /lua {
        content_by_lua '
            ngx.header.cache_control = { "private", "no-store" }
            ngx.header.cache_control = { "no-cache", "blah", "foo" }
            local v
            for i = 1, 400 do
                v = ngx.header.cache_control
            end
            ngx.say("Cache-Control: ", table.concat(v, ", "))
        ';
    }
--- request
    GET /lua
--- response_headers
Cache-Control: no-cache, blah, foo
--- response_body_like chop
^Cache-Control: no-cache[;,] blah[;,] foo$
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):5 (?:loop|-> \d+)\]/
--- no_error_log
[error]
 -- NYI:
stitch

