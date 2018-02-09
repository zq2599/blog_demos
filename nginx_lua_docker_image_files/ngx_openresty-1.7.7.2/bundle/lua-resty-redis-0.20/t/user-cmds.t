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

=== TEST 1: single channel
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local cjson = require "cjson"
            local redis = require "resty.redis"

            redis.add_commands("foo", "bar")

            local red = redis:new()

            red:set_timeout(1000) -- 1 sec

            local ok, err = red:connect("127.0.0.1", $TEST_NGINX_REDIS_PORT)
            if not ok then
                ngx.say("failed to connect: ", err)
                return
            end

            local res, err = red:foo("a")
            if not res then
                ngx.say("failed to foo: ", err)
            end

            res, err = red:bar()
            if not res then
                ngx.say("failed to bar: ", err)
            end
        ';
    }
--- request
GET /t
--- response_body
failed to foo: ERR unknown command 'foo'
failed to bar: ERR unknown command 'bar'
--- no_error_log
[error]

