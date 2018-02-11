# vim:set ft= ts=4 sw=4 et fdm=marker:
use lib 'lib';
use Test::Nginx::Socket::Lua;
use Cwd qw(cwd);

#worker_connections(1014);
#master_process_enabled(1);
#log_level('warn');

repeat_each(2);

plan tests => repeat_each() * (blocks() * 5 + 3);

my $pwd = cwd();

our $HttpConfig = <<_EOC_;
    lua_package_path "$pwd/lib/?.lua;../lua-resty-lrucache/lib/?.lua;;";
    init_by_lua '
        -- local verbose = true
        local verbose = false
        local outfile = "$Test::Nginx::Util::ErrLogFile"
        -- local outfile = "/tmp/v.log"
        if verbose then
            local dump = require "jit.dump"
            dump.on(nil, outfile)
        else
            local v = require "jit.v"
            v.on(outfile)
        end

        require "resty.core"
        -- jit.opt.start("hotloop=1")
        -- jit.opt.start("loopunroll=1000000")
        -- jit.off()
    ';
_EOC_

#no_diff();
no_long_string();
check_accum_error_log();
run_tests();

__DATA__

=== TEST 1: matched, no submatch, no jit compile, no regex cache
--- http_config eval: $::HttpConfig
--- config
    location = /re {
        access_log off;
        content_by_lua '
            local m, err
            local match = ngx.re.match
            for i = 1, 400 do
                m, err = match("a", "a")
            end
            if err then
                ngx.log(ngx.ERR, "failed: ", err)
                return
            end
            if not m then
                ngx.log(ngx.ERR, "no match")
                return
            end
            ngx.say("matched: ", m[0])
            ngx.say("$1: ", m[1])
            -- ngx.say("$2: ", m[2])
            -- ngx.say("$3: ", m[3])
            -- collectgarbage()
        ';
    }
--- request
GET /re
--- response_body
matched: a
$1: nil
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):4 loop\]/
--- no_error_log
[error]
bad argument type



=== TEST 2: matched, no submatch, jit compile, regex cache
--- http_config eval: $::HttpConfig
--- config
    location = /re {
        access_log off;
        content_by_lua '
            local m, err
            local match = ngx.re.match
            for i = 1, 400 do
                m, err = match("a", "a", "jo")
            end
            if err then
                ngx.log(ngx.ERR, "failed: ", err)
                return
            end
            if not m then
                ngx.log(ngx.ERR, "no match")
                return
            end
            ngx.say("matched: ", m[0])
            ngx.say("$1: ", m[1])
            -- collectgarbage()
        ';
    }
--- request
GET /re
--- response_body
matched: a
$1: nil
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):4 loop\]/
--- no_error_log
[error]
NYI



=== TEST 3: not matched, no submatch, jit compile, regex cache
--- http_config eval: $::HttpConfig
--- config
    location = /re {
        access_log off;
        content_by_lua '
            local m, err
            local match = ngx.re.match
            for i = 1, 200 do
                m, err = match("b", "a", "jo")
            end
            if err then
                ngx.log(ngx.ERR, "failed: ", err)
                return
            end
            if not m then
                ngx.say("no match")
                return
            end
            ngx.say("matched: ", m[0])
            ngx.say("$1: ", m[1])
            -- collectgarbage()
        ';
    }
--- request
GET /re
--- response_body
no match
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):4 loop\]/
--- no_error_log
[error]



=== TEST 4: not matched, no submatch, no jit compile, no regex cache
--- http_config eval: $::HttpConfig
--- config
    location = /re {
        access_log off;
        content_by_lua '
            local m, err
            local match = ngx.re.match
            for i = 1, 100 do
                m, err = match("b", "a")
            end
            if err then
                ngx.log(ngx.ERR, "failed: ", err)
                return
            end
            if not m then
                ngx.say("no match")
                return
            end
            ngx.say("matched: ", m[0])
            ngx.say("$1: ", m[1])
            -- collectgarbage()
        ';
    }
--- request
GET /re
--- response_body
no match
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):4 loop\]/
--- no_error_log
[error]
bad argument type



=== TEST 5: submatches, matched, no regex cache
--- http_config eval: $::HttpConfig
--- config
    location = /re {
        access_log off;
        content_by_lua '
            local m, err
            local match = ngx.re.match
            for i = 1, 100 do
                m, err = match("hello, 1234", [[(\\d)(\\d+)]])
            end
            if err then
                ngx.log(ngx.ERR, "failed: ", err)
                return
            end
            if not m then
                ngx.log(ngx.ERR, "no match")
                return
            end
            ngx.say("matched: ", m[0])
            ngx.say("$1: ", m[1])
            ngx.say("$2: ", m[2])
            ngx.say("$3: ", m[3])
            -- collectgarbage()
        ';
    }
--- request
GET /re
--- response_body
matched: 1234
$1: 1
$2: 234
$3: nil
--- no_error_log
[error]
bad argument type
NYI



=== TEST 6: submatches, matched, with regex cache
--- http_config eval: $::HttpConfig
--- config
    location = /re {
        access_log off;
        content_by_lua '
            local m, err
            local match = ngx.re.match
            for i = 1, 100 do
                m, err = match("hello, 1234", [[(\\d)(\\d+)]], "jo")
            end
            if err then
                ngx.log(ngx.ERR, "failed: ", err)
                return
            end
            if not m then
                ngx.log(ngx.ERR, "no match")
                return
            end
            ngx.say("matched: ", m[0])
            ngx.say("$1: ", m[1])
            ngx.say("$2: ", m[2])
            ngx.say("$3: ", m[3])
            -- ngx.say(table.maxn(m))
            -- collectgarbage()
        ';
    }
--- request
GET /re
--- response_body
matched: 1234
$1: 1
$2: 234
$3: nil
--- error_log eval
qr/\[TRACE   \d+/
--- no_error_log
[error]
bad argument type
NYI



=== TEST 7: named subpatterns w/ extraction (matched)
--- http_config eval: $::HttpConfig
--- config
    location /re {
        content_by_lua '
            local m, err
            local match = ngx.re.match
            for i = 1, 100 do
                m, err = match("hello, 1234", "(?<first>[a-z]+), [0-9]+", "jo")
            end
            if m then
                ngx.say(m[0])
                ngx.say(m[1])
                ngx.say(m.first)
                ngx.say(m.second)
            else
                if err then
                    ngx.say("error: ", err)
                    return
                end
                ngx.say("not matched!")
            end
        ';
    }
--- request
    GET /re
--- response_body
hello, 1234
hello
hello
nil

--- error_log eval
qr/\[TRACE   \d+/
--- no_error_log
[error]
bad argument type
NYI



=== TEST 8: named subpatterns w/ extraction (use of duplicate names in non-duplicate mode)
--- http_config eval: $::HttpConfig
--- config
    location /re {
        content_by_lua '
            local m, err
            local match = ngx.re.match
            for i = 1, 200 do
                m, err = match("hello, 1234", "(?<first>[a-z])(?<first>[a-z]+), [0-9]+", "jo")
            end
            if m then
                ngx.say(m[0])
                ngx.say(m[1])
                ngx.say(m.first)
                ngx.say(m.second)
            else
                if err then
                    ngx.say("error: ", err)
                    return
                end
                ngx.say("not matched!")
            end
        ';
    }
--- request
    GET /re
--- response_body_like chop
error: pcre_compile\(\) failed: two named subpatterns have the same name

--- error_log eval
qr/\[TRACE   \d+/
--- no_error_log
[error]
bad argument type
NYI



=== TEST 9: named subpatterns w/ extraction (use of duplicate names in duplicate mode)
--- http_config eval: $::HttpConfig
--- config
    location /re {
        content_by_lua '
            local m, err
            local match = ngx.re.match
            for i = 1, 100 do
                m, err = match("hello, 1234", "(?<first>[a-z])(?<first>[a-z]+), [0-9]+", "joD")
                m, err = match("hello, 1234", "(?<first>[a-z])(?<first>[a-z]+), [0-9]+", "joD")
                m, err = match("hello, 1234", "(?<first>[a-z])(?<first>[a-z]+), [0-9]+", "joD")
                m, err = match("hello, 1234", "(?<first>[a-z])(?<first>[a-z]+), [0-9]+", "joD")
                m, err = match("hello, 1234", "(?<first>[a-z])(?<first>[a-z]+), [0-9]+", "joD")
            end
            if m then
                ngx.say(m[0])
                ngx.say(m[1])
                ngx.say(m[2])
                ngx.say(table.concat(m.first, "|"))
                ngx.say(m.second)
            else
                if err then
                    ngx.say("error: ", err)
                    return
                end
                ngx.say("not matched!")
            end
        ';
    }
--- request
    GET /re
--- response_body_like
hello, 1234
h
ello
h|ello
nil

--- error_log eval
qr/\[TRACE   \d+/
--- no_error_log
[error]
bad argument type
NYI



=== TEST 10: captures input table in ngx.re.match
--- http_config eval: $::HttpConfig
--- config
    location /re {
        content_by_lua '
            local new_tab = require "table.new"
            local clear_tab = require "table.clear"
            local m
            local res = new_tab(5, 0)
            res[5] = "hello"
            for i = 1, 100 do
                m = ngx.re.match("hello, 1234", "([0-9])([0-9])([0-9])([0-9])", "jo", nil, res)
            end

            if m then
                ngx.say(m[0])
                ngx.say(m[1])
                ngx.say(m[2])
                ngx.say(m[3])
                ngx.say(m[4])
                ngx.say(m[5])
            else
                ngx.say("not matched!")
            end
        ';
    }
--- request
    GET /re
--- response_body
1234
1
2
3
4
hello
--- no_error_log
[error]
NYI
--- error_log eval
qr/\[TRACE\s+\d+\s+/

