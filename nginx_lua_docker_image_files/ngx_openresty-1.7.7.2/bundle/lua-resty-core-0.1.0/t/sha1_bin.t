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

=== TEST 1: set sha1_bin (string)
--- http_config eval: $::HttpConfig
--- config
    location = /sha1_bin {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.sha1_bin("hello")
            end
            ngx.say(string.len(s))
        ';
    }
--- request
GET /sha1_bin
--- response_body
20
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]



=== TEST 2: set sha1_bin (nil)
--- http_config eval: $::HttpConfig
--- config
    location = /sha1_bin {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.sha1_bin(nil)
            end
            ngx.say(string.len(s))
        ';
    }
--- request
GET /sha1_bin
--- response_body
20
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]



=== TEST 3: set sha1_bin (number)
--- http_config eval: $::HttpConfig
--- config
    location = /sha1_bin {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.sha1_bin(3.14)
            end
            ngx.say(string.len(s))
        ';
    }
--- request
GET /sha1_bin
--- response_body
20
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]



=== TEST 4: set sha1_bin (boolean)
--- http_config eval: $::HttpConfig
--- config
    location = /sha1_bin {
        content_by_lua '
            local s
            for i = 1, 100 do
                s = ngx.sha1_bin(true)
            end
            ngx.say(string.len(s))
        ';
    }
--- request
GET /sha1_bin
--- response_body
20
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]

