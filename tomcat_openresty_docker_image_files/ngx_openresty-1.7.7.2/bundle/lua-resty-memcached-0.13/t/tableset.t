# vim:set ft= ts=4 sw=4 et:

use Test::Nginx::Socket::Lua;
use Cwd qw(cwd);

repeat_each(2);

plan tests => repeat_each() * (3 * blocks());

my $pwd = cwd();

our $HttpConfig = qq{
    lua_package_path "$pwd/lib/?.lua;;";
};

$ENV{TEST_NGINX_RESOLVER} = '8.8.8.8';
$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

no_long_string();

run_tests();

__DATA__

=== TEST 1: set with table
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local memcached = require "resty.memcached"
            local memc = memcached:new()

            memc:set_timeout(1000) -- 1 sec

            local ok, err = memc:connect("127.0.0.1", $TEST_NGINX_MEMCACHED_PORT)
            if not ok then
                ngx.say("failed to connect: ", err)
                return
            end

            local ok, err = memc:flush_all()
            if not ok then
                ngx.say("failed to flush all: ", err)
                return
            end

            local ok, err = memc:set("dog", { "c", "a", "t" })
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            for i = 1, 2 do
                local res, flags, err = memc:get("dog")
                if err then
                    ngx.say("failed to get dog: ", err)
                    return
                end

                if not res then
                    ngx.say("dog not found")
                    return
                end

                ngx.say("dog: ", res, " (flags: ", flags, ")")
            end

            memc:close()
        ';
    }
--- request
GET /t
--- response_body
dog: cat (flags: 0)
dog: cat (flags: 0)
--- no_error_log
[error]



=== TEST 2: set with nested table
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local memcached = require "resty.memcached"
            local memc = memcached:new()

            memc:set_timeout(1000) -- 1 sec

            local ok, err = memc:connect("127.0.0.1", $TEST_NGINX_MEMCACHED_PORT)
            if not ok then
                ngx.say("failed to connect: ", err)
                return
            end

            local ok, err = memc:flush_all()
            if not ok then
                ngx.say("failed to flush all: ", err)
                return
            end

            local ok, err = memc:set("dog", { "c", "a", { "t"} })
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            for i = 1, 2 do
                local res, flags, err = memc:get("dog")
                if err then
                    ngx.say("failed to get dog: ", err)
                    return
                end

                if not res then
                    ngx.say("dog not found")
                    return
                end

                ngx.say("dog: ", res, " (flags: ", flags, ")")
            end

            memc:close()
        ';
    }
--- request
GET /t
--- response_body
dog: cat (flags: 0)
dog: cat (flags: 0)
--- no_error_log
[error]

