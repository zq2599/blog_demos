# vim:set ft= ts=4 sw=4 et fdm=marker:
use lib 'lib';
use Test::Nginx::Socket::Lua;
use Cwd qw(cwd);

#worker_connections(1014);
#master_process_enabled(1);
#log_level('warn');

repeat_each(2);

plan tests => repeat_each() * (blocks() * 5 + 10);

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

=== TEST 1: ngx.req.get_headers
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        set $foo hello;
        content_by_lua '
            local ffi = require "ffi"
            local headers
            for i = 1, 200 do
                headers = ngx.req.get_headers()
            end
            local keys = {}
            for k, _ in pairs(headers) do
                keys[#keys + 1] = k
            end
            table.sort(keys)
            for _, k in ipairs(keys) do
                ngx.say(k, ": ", headers[k])
            end
        ';
    }
--- request
GET /t
--- response_body
bar: bar
baz: baz
connection: close
foo: foo
host: localhost
--- more_headers
Foo: foo
Bar: bar
Baz: baz
--- error_log eval
qr/\[TRACE   \d+ .*? -> 1\]/
--- no_error_log eval
[
"[error]",
qr/ -- NYI: (?!return to lower frame)/,
]



=== TEST 2: ngx.req.get_headers (raw)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        set $foo hello;
        content_by_lua '
            local ffi = require "ffi"
            local headers
            for i = 1, 200 do
                headers = ngx.req.get_headers(100, true)
            end
            local keys = {}
            for k, _ in pairs(headers) do
                keys[#keys + 1] = k
            end
            table.sort(keys)
            for _, k in ipairs(keys) do
                ngx.say(k, ": ", headers[k])
            end
        ';
    }
--- request
GET /t
--- response_body
Bar: bar
Baz: baz
Connection: close
Foo: foo
Host: localhost
--- more_headers
Foo: foo
Bar: bar
Baz: baz
--- error_log eval
qr/\[TRACE   \d+ .*? -> 1\]/
--- no_error_log
[error]
 -- NYI:



=== TEST 3: ngx.req.get_headers (count is 2)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        set $foo hello;
        content_by_lua '
            local ffi = require "ffi"
            local headers
            for i = 1, 200 do
                headers = ngx.req.get_headers(2, true)
            end
            local keys = {}
            for k, _ in pairs(headers) do
                keys[#keys + 1] = k
            end
            table.sort(keys)
            for _, k in ipairs(keys) do
                ngx.say(k, ": ", headers[k])
            end
        ';
    }
--- request
GET /t
--- response_body
Connection: close
Host: localhost
--- more_headers
Foo: foo
Bar: bar
Baz: baz
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):4 loop\]/
--- no_error_log
[error]
 -- NYI:



=== TEST 4: ngx.req.get_headers (metatable)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        set $foo hello;
        content_by_lua '
            local ffi = require "ffi"
            local headers, header
            for i = 1, 100 do
                headers = ngx.req.get_headers()
                header = headers["foo_BAR"]
            end
            ngx.say("foo_BAR: ", header)
            local keys = {}
            for k, _ in pairs(headers) do
                keys[#keys + 1] = k
            end
            table.sort(keys)
            for _, k in ipairs(keys) do
                ngx.say(k, ": ", headers[k])
            end
        ';
    }
--- request
GET /t
--- response_body
foo_BAR: foo
baz: baz
connection: close
foo-bar: foo
host: localhost
--- more_headers
Foo-Bar: foo
Baz: baz
--- error_log eval
qr/\[TRACE   \d+ .*? -> \d\]/
--- no_error_log eval
["[error]",
qr/ -- NYI: (?!return to lower frame at)/,
]



=== TEST 5: ngx.req.get_uri_args
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        set $foo hello;
        content_by_lua '
            local ffi = require "ffi"
            local args
            for i = 1, 100 do
                args = ngx.req.get_uri_args()
            end
            if type(args) ~= "table" then
                ngx.say("bad args type found: ", args)
                return
            end
            local keys = {}
            for k, _ in pairs(args) do
                keys[#keys + 1] = k
            end
            table.sort(keys)
            for _, k in ipairs(keys) do
                local v = args[k]
                if type(v) == "table" then
                    ngx.say(k, ": ", table.concat(v, ", "))
                else
                    ngx.say(k, ": ", v)
                end
            end
        ';
    }
--- request
GET /t?a=3%200&foo%20bar=&a=hello&blah
--- response_body
a: 3 0, hello
blah: true
foo bar: 
--- error_log eval
qr/\[TRACE   \d+ .*? -> \d+\]/
--- no_error_log
[error]
 -- NYI:



=== TEST 6: ngx.req.get_uri_args (empty)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        set $foo hello;
        content_by_lua '
            local ffi = require "ffi"
            local args
            for i = 1, 200 do
                args = ngx.req.get_uri_args()
            end
            if type(args) ~= "table" then
                ngx.say("bad args type found: ", args)
                return
            end
            local keys = {}
            for k, _ in pairs(args) do
                keys[#keys + 1] = k
            end
            table.sort(keys)
            for _, k in ipairs(keys) do
                local v = args[k]
                if type(v) == "table" then
                    ngx.say(k, ": ", table.concat(v, ", "))
                else
                    ngx.say(k, ": ", v)
                end
            end
        ';
    }
--- request
GET /t?
--- response_body
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):4 loop\]/
--- no_error_log
[error]
 -- NYI:



=== TEST 7: ngx.req.start_time()
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        access_log off;
        content_by_lua '
            local t
            for i = 1, 500 do
                t = ngx.req.start_time()
            end
            ngx.sleep(0.10)
            local elapsed = ngx.now() - t
            ngx.say(t > 1399867351)
            ngx.say(">= 0.099: ", elapsed >= 0.099)
            ngx.say("< 0.11: ", elapsed < 0.11)
            -- ngx.say(t, " ", elapsed)
        ';
    }
--- request
GET /t
--- response_body
true
>= 0.099: true
< 0.11: true

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
bad argument type
stitch



=== TEST 8: ngx.req.get_method (GET)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        access_log off;
        content_by_lua '
            local t
            for i = 1, 500 do
                t = ngx.req.get_method()
            end
            ngx.say("method: ", t)
        ';
    }
--- request
GET /t
--- response_body
method: GET

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
bad argument type
stitch



=== TEST 9: ngx.req.get_method (OPTIONS)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        access_log off;
        content_by_lua '
            local t
            for i = 1, 500 do
                t = ngx.req.get_method()
            end
            ngx.say("method: ", t)
        ';
    }
--- request
OPTIONS /t
--- response_body
method: OPTIONS

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
bad argument type
stitch



=== TEST 10: ngx.req.get_method (POST)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        access_log off;
        content_by_lua '
            local t
            for i = 1, 500 do
                t = ngx.req.get_method()
            end
            ngx.say("method: ", t)
            ngx.req.discard_body()
        ';
    }
--- request
POST /t
hello
--- response_body
method: POST

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
bad argument type
stitch



=== TEST 11: ngx.req.get_method (unknown method)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        access_log off;
        content_by_lua '
            local t
            for i = 1, 500 do
                t = ngx.req.get_method()
            end
            ngx.say("method: ", t)
            ngx.req.discard_body()
        ';
    }
--- request
BLAH /t
hello
--- response_body
method: BLAH

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
bad argument type
stitch



=== TEST 12: ngx.req.get_method (CONNECT)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        access_log off;
        content_by_lua '
            local t
            for i = 1, 500 do
                t = ngx.req.get_method()
            end
            ngx.say("method: ", t)
            ngx.req.discard_body()
        ';
    }
--- request
CONNECT /t
hello
--- response_body
method: CONNECT

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
bad argument type
stitch



=== TEST 13: ngx.req.set_method (GET -> PUT)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        access_log off;
        content_by_lua '
            local t
            for i = 1, 500 do
                ngx.req.set_method(ngx.HTTP_PUT)
            end
            ngx.say("method: ", ngx.req.get_method())
        ';
    }
--- request
GET /t
--- response_body
method: PUT

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
bad argument type
stitch



=== TEST 14: ngx.req.set_header (single number value)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        access_log off;
        content_by_lua '
            local t
            for i = 1, 500 do
                ngx.req.set_header("foo", i)
            end
            ngx.say("header foo: ", ngx.var.http_foo)
        ';
    }
--- request
GET /t
--- response_body
header foo: 500

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
bad argument type
stitch



=== TEST 15: ngx.req.set_header (nil value)
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        access_log off;
        content_by_lua '
            local t
            for i = 1, 500 do
                ngx.req.set_header("foo", nil)
            end
            ngx.say("header foo: ", type(ngx.var.http_foo))
        ';
    }
--- request
GET /t
--- response_body
header foo: nil

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
bad argument type
stitch



=== TEST 16: ngx.req.clear_header
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        access_log off;
        content_by_lua '
            ngx.req.set_header("foo", "hello")
            local t
            for i = 1, 500 do
                t = ngx.req.clear_header("foo")
            end
            ngx.say("header foo: ", type(ngx.var.http_foo))
        ';
    }
--- request
GET /t
--- response_body
header foo: nil

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):4 loop\]/
--- no_error_log
[error]
bad argument type
stitch

