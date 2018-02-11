# vim:set ft= ts=4 sw=4 et fdm=marker:
use lib 'lib';
use Test::Nginx::Socket::Lua;
use Cwd qw(cwd);

#worker_connections(1014);
#master_process_enabled(1);
log_level('warn');

repeat_each(120);
#repeat_each(2);

plan tests => repeat_each() * (blocks() * 5);

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
run_tests();

__DATA__

=== TEST 1: sanity
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        content_by_lua '
            ngx.exit(403)
        ';
    }
--- request
GET /t
--- response_body_like: 403 Forbidden
--- error_code: 403
--- no_error_log eval
["[error]",
" -- NYI: ",
" bad argument"]



=== TEST 2: call ngx.exit() from a custom lua module
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        content_by_lua '
            local foo = require "foo"
            foo.go()
        ';
    }
--- user_files
>>> foo.lua
local exit = ngx.exit

local function go()
    exit(403)
    return
end

return { go = go }
--- request
GET /t
--- response_body_like: 403 Forbidden
--- error_code: 403
--- no_error_log eval
["[error]",
" -- NYI: ",
" bad argument"]

