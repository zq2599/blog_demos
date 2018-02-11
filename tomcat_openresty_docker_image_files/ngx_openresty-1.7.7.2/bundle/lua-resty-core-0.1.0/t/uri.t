# vim:set ft= ts=4 sw=4 et fdm=marker:
use lib 'lib';
use Test::Nginx::Socket::Lua;
use Cwd qw(cwd);

#worker_connections(1014);
#master_process_enabled(1);
#log_level('warn');

repeat_each(2);

plan tests => repeat_each() * (blocks() * 4);

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

=== TEST 1: unescape_uri (string)
--- http_config eval: $::HttpConfig
--- config
    location = /uri {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.unescape_uri("hello%20world")
            end
            ngx.say(s)
        ';
    }
--- request
GET /uri
--- response_body
hello world
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]



=== TEST 2: unescape_uri (nil)
--- http_config eval: $::HttpConfig
--- config
    location = /uri {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.unescape_uri(nil)
            end
            ngx.say(s)
        ';
    }
--- request
GET /uri
--- response_body eval: "\n"
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]



=== TEST 3: unescape_uri (number)
--- http_config eval: $::HttpConfig
--- config
    location = /uri {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.unescape_uri(3.14)
            end
            ngx.say(s)
        ';
    }
--- request
GET /uri
--- response_body
3.14
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]



=== TEST 4: escape_uri (string, escaped)
--- http_config eval: $::HttpConfig
--- config
    location = /uri {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.escape_uri("hello world")
            end
            ngx.say(s)
        ';
    }
--- request
GET /uri
--- response_body
hello%20world
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]



=== TEST 5: escape_uri (string, no escaped)
--- http_config eval: $::HttpConfig
--- config
    location = /uri {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.escape_uri("helloworld")
            end
            ngx.say(s)
        ';
    }
--- request
GET /uri
--- response_body
helloworld
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]



=== TEST 6: escape_uri (nil)
--- http_config eval: $::HttpConfig
--- config
    location = /uri {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.escape_uri(nil)
            end
            ngx.say(s)
        ';
    }
--- request
GET /uri
--- response_body eval: "\n"
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]



=== TEST 7: escape_uri (number)
--- http_config eval: $::HttpConfig
--- config
    location = /uri {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.escape_uri(3.14)
            end
            ngx.say(s)
        ';
    }
--- request
GET /uri
--- response_body
3.14
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]



=== TEST 8: escape_uri (larger than 4k, nothing to be escaped)
--- http_config eval: $::HttpConfig
--- config
    location = /uri {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.escape_uri(string.rep("a", 4097))
            end
            ngx.say(s)
        ';
    }
--- request
GET /uri
--- response_body eval: "a" x 4097 . "\n"
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]



=== TEST 9: escape_uri (a little smaller than 4k, need to be escaped)
--- http_config eval: $::HttpConfig
--- config
    location = /uri {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.escape_uri(string.rep(" ", 1365))
            end
            ngx.say(s)
        ';
    }
--- request
GET /uri
--- response_body eval: "%20" x 1365 . "\n"
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]



=== TEST 10: escape_uri (a little bigger than 4k, need to be escaped)
--- http_config eval: $::HttpConfig
--- config
    location = /uri {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.escape_uri(string.rep(" ", 1366))
            end
            ngx.say(s)
        ';
    }
--- request
GET /uri
--- response_body eval: "%20" x 1366 . "\n"
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]

