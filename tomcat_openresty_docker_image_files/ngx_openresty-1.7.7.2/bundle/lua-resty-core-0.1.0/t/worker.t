# vim:set ft= ts=4 sw=4 et fdm=marker:
use lib 'lib';
use Test::Nginx::Socket::Lua;
use Cwd qw(cwd);

#worker_connections(1014);
#master_process_enabled(1);
#log_level('warn');

repeat_each(2);

plan tests => repeat_each() * (blocks() * 6);

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

=== TEST 1: ngx.worker.exiting
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        content_by_lua '
            local v
            local exiting = ngx.worker.exiting
            for i = 1, 400 do
                v = exiting()
            end
            ngx.say(v)
        ';
    }
--- request
GET /t
--- response_body
false
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):4 loop\]/
--- no_error_log
[error]
 -- NYI:
 stitch



=== TEST 2: ngx.worker.pid
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        content_by_lua '
            local v
            local pid = ngx.worker.pid
            for i = 1, 400 do
                v = pid()
            end
            ngx.say(v == tonumber(ngx.var.pid))
            ngx.say(v)
        ';
    }
--- request
GET /t
--- response_body_like chop
^true
\d+$
--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):4 loop\]/
--- no_error_log
[error]
 -- NYI:
 stitch

