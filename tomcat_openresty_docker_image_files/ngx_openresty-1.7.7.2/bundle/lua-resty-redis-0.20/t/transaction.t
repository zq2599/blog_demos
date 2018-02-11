# vim:set ft= ts=4 sw=4 et:

use Test::Nginx::Socket::Lua;
use Cwd qw(cwd);

repeat_each(2);

plan tests => repeat_each() * (3 * blocks());

my $pwd = cwd();

our $HttpConfig = qq{
    lua_package_path "$pwd/lib/?.lua;;";
    lua_package_cpath "/usr/local/openresty-debug/lualib/?.so;/usr/local/openresty/lualib/?.so;;";
};

$ENV{TEST_NGINX_RESOLVER} = '8.8.8.8';
$ENV{TEST_NGINX_REDIS_PORT} ||= 6379;

no_long_string();
#no_diff();

run_tests();

__DATA__

=== TEST 1: sanity
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local cjson = require "cjson"
            local redis = require "resty.redis"
            local red = redis:new()

            red:set_timeout(1000) -- 1 sec

            local ok, err = red:connect("127.0.0.1", $TEST_NGINX_REDIS_PORT)
            if not ok then
                ngx.say("failed to connect: ", err)
                return
            end

            local redis_key = "foo"

            local ok, err = red:multi()
            if not ok then
                ngx.say("failed to run multi: ", err)
                return
            end

            ngx.say("multi ans: ", cjson.encode(ok))

            local ans, err = red:sort("log", "by", redis_key .. ":*->timestamp")
            if not ans then
                ngx.say("failed to run sort: ", err)
                return
            end

            ngx.say("sort ans: ", cjson.encode(ans))

            ans, err = red:exec()

            ngx.say("exec ans: ", cjson.encode(ans))

            local ok, err = red:set_keepalive(0, 1024)
            if not ok then
                ngx.say("failed to put the current redis connection into pool: ", err)
                return
            end
        ';
    }
--- request
GET /t
--- response_body
multi ans: "OK"
sort ans: "QUEUED"
exec ans: [{}]
--- no_error_log
[error]



=== TEST 2: redis cmd reference sample: redis does not halt on errors
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local cjson = require "cjson"
            local redis = require "resty.redis"
            local red = redis:new()

            red:set_timeout(1000) -- 1 sec

            local ok, err = red:connect("127.0.0.1", $TEST_NGINX_REDIS_PORT)
            if not ok then
                ngx.say("failed to connect: ", err)
                return
            end

            local ok, err = red:multi()
            if not ok then
                ngx.say("failed to run multi: ", err)
                return
            end

            ngx.say("multi ans: ", cjson.encode(ok))

            local ans, err = red:set("a", "abc")
            if not ans then
                ngx.say("failed to run sort: ", err)
                return
            end

            ngx.say("set ans: ", cjson.encode(ans))

            local ans, err = red:lpop("a")
            if not ans then
                ngx.say("failed to run sort: ", err)
                return
            end

            ngx.say("set ans: ", cjson.encode(ans))

            ans, err = red:exec()

            ngx.say("exec ans: ", cjson.encode(ans))

            red:close()
        ';
    }
--- request
GET /t
--- response_body
multi ans: "OK"
set ans: "QUEUED"
set ans: "QUEUED"
exec ans: ["OK",[false,"ERR Operation against a key holding the wrong kind of value"]]
--- no_error_log
[error]

