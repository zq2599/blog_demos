# vim:set ft= ts=4 sw=4 et fdm=marker:
use lib 'lib';
use Test::Nginx::Socket::Lua;
use Cwd qw(cwd);

#worker_connections(1014);
#master_process_enabled(1);
log_level('warn');

#repeat_each(120);
repeat_each(2);

plan tests => repeat_each() * (blocks() * 6);

my $pwd = cwd();

our $HttpConfig = <<_EOC_;
    lua_package_path "$pwd/lib/?.lua;\$prefix/html/?.lua;../lua-resty-lrucache/lib/?.lua;;";
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

=== TEST 1: get ngx.ctx
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        content_by_lua '
            for i = 1, 100 do
                ngx.ctx.foo = i
            end
            ngx.say("ctx.foo = ", ngx.ctx.foo)
        ';
    }
--- request
GET /t
--- response_body
ctx.foo = 100
--- no_error_log
[error]
 -- NYI:
 bad argument
--- error_log eval
qr/\[TRACE\s+\d+\s+content_by_lua\(nginx\.conf:\d+\):2 loop\]/



=== TEST 2: set ngx.ctx
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        content_by_lua '
            for i = 1, 100 do
                ngx.ctx = {foo = i}
            end
            ngx.say("ctx.foo = ", ngx.ctx.foo)
        ';
    }
--- request
GET /t
--- response_body
ctx.foo = 100
--- no_error_log
[error]
 -- NYI:
 bad argument
--- error_log eval
qr/\[TRACE\s+\d+\s+content_by_lua\(nginx\.conf:\d+\):2 loop\]/

