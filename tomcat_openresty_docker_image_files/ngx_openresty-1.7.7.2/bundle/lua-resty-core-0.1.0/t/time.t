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

=== TEST 1: ngx.now()
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        access_log off;
        content_by_lua '
            local t
            for i = 1, 500 do
                t = ngx.now()
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



=== TEST 2: ngx.time()
--- http_config eval: $::HttpConfig
--- config
    location = /t {
        access_log off;
        content_by_lua '
            local t
            for i = 1, 500 do
                t = ngx.time()
            end
            ngx.say(t > 1400960598)
            local diff = os.time() - t
            ngx.say(diff <= 1)
        ';
    }
--- request
GET /t
--- response_body
true
true

--- error_log eval
qr/\[TRACE   \d+ content_by_lua\(nginx\.conf:\d+\):3 loop\]/
--- no_error_log
[error]
bad argument type
stitch

