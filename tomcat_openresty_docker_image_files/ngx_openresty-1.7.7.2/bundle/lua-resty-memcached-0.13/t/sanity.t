# vim:set ft= ts=4 sw=4 et:

use Test::Nginx::Socket::Lua;
use Cwd qw(cwd);

repeat_each(2);

plan tests => repeat_each() * (3 * blocks() - 1);

my $pwd = cwd();

our $HttpConfig = qq{
    lua_package_path "$pwd/lib/?.lua;;";
};

$ENV{TEST_NGINX_RESOLVER} = '8.8.8.8';
$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

no_long_string();

run_tests();

__DATA__

=== TEST 1: basic
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

            local ok, err = memc:set("dog", 32)
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
dog: 32 (flags: 0)
dog: 32 (flags: 0)
--- no_error_log
[error]



=== TEST 2: add an exsitent key
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local ok, err = memc:add("dog", 56)
            if not ok then
                ngx.say("failed to add dog: ", err)
            end

            local res, flags, err = memc:get("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            if not res then
                ngx.say("dog not found")
                return
            end

            ngx.say("dog: ", res)
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
failed to add dog: NOT_STORED
dog: 32
--- no_error_log
[error]



=== TEST 3: add a nonexistent key
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

            local ok, err = memc:add("dog", 56)
            if not ok then
                ngx.say("failed to add dog: ", err)
            end

            local res, flags, err = memc:get("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            if not res then
                ngx.say("dog not found")
                return
            end

            ngx.say("dog: ", res)
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
dog: 56
--- no_error_log
[error]



=== TEST 4: set an exsistent key
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local ok, err = memc:set("dog", 56)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local res, flags, err = memc:get("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            if not res then
                ngx.say("dog not found")
                return
            end

            ngx.say("dog: ", res)
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
dog: 56
--- no_error_log
[error]



=== TEST 5: replace an exsistent key
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local ok, err = memc:replace("dog", 56)
            if not ok then
                ngx.say("failed to replace dog: ", err)
                return
            end

            local res, flags, err = memc:get("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            if not res then
                ngx.say("dog not found")
                return
            end

            ngx.say("dog: ", res)
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
dog: 56
--- no_error_log
[error]



=== TEST 6: replace a nonexsistent key
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

            local ok, err = memc:replace("dog", 56)
            if not ok then
                ngx.say("failed to replace dog: ", err)
            end

            local res, flags, err = memc:get("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            if not res then
                ngx.say("dog not found")
                return
            end

            ngx.say("dog: ", res)
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
failed to replace dog: NOT_STORED
dog not found
--- no_error_log
[error]



=== TEST 7: prepend to a nonexsistent key
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

            local ok, err = memc:prepend("dog", 56)
            if not ok then
                ngx.say("failed to prepend to dog: ", err)
            end

            local res, flags, err = memc:get("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            if not res then
                ngx.say("dog not found")
                return
            end

            ngx.say("dog: ", res)
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
failed to prepend to dog: NOT_STORED
dog not found
--- no_error_log
[error]



=== TEST 8: prepend to an exsistent key
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
            end

            local ok, err = memc:prepend("dog", 56)
            if not ok then
                ngx.say("failed to prepend to dog: ", err)
            end

            local res, flags, err = memc:get("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            if not res then
                ngx.say("dog not found")
                return
            end

            ngx.say("dog: ", res)
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
dog: 5632
--- no_error_log
[error]



=== TEST 9: append to a nonexsistent key
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

            local ok, err = memc:append("dog", 56)
            if not ok then
                ngx.say("failed to append to dog: ", err)
            end

            local res, flags, err = memc:get("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            if not res then
                ngx.say("dog not found")
                return
            end

            ngx.say("dog: ", res)
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
failed to append to dog: NOT_STORED
dog not found
--- no_error_log
[error]



=== TEST 10: append to an exsistent key
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
            end

            local ok, err = memc:append("dog", 56)
            if not ok then
                ngx.say("failed to append to dog: ", err)
            end

            local res, flags, err = memc:get("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            if not res then
                ngx.say("dog not found")
                return
            end

            ngx.say("dog: ", res)
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
dog: 3256
--- no_error_log
[error]



=== TEST 11: delete an exsistent key
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
            end

            local ok, err = memc:delete("dog")
            if not ok then
                ngx.say("failed to delete dog: ", err)
            end

            local res, flags, err = memc:get("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            if not res then
                ngx.say("dog not found")
                return
            end

            ngx.say("dog: ", res)

            local res, flags, err = memc:add("dog", 772)
            if err then
                ngx.say("failed to add dog: ", err)
                return
            end

            memc:close()
        ';
    }
--- request
GET /t
--- response_body
dog not found
--- no_error_log
[error]



=== TEST 12: delete a nonexsistent key
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

            local ok, err = memc:delete("dog")
            if not ok then
                ngx.say("failed to delete dog: ", err)
            end

            local res, flags, err = memc:get("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            if not res then
                ngx.say("dog not found")
                return
            end

            ngx.say("dog: ", res)
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
failed to delete dog: NOT_FOUND
dog not found
--- no_error_log
[error]



=== TEST 13: delete an exsistent key with delay
--- SKIP
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local ok, err = memc:delete("dog", 1)
            if not ok then
                ngx.say("failed to delete dog: ", err)
            end

            local ok, err = memc:add("dog", 76)
            if not ok then
                ngx.say("failed to add dog: ", err)
            end

            local ok, err = memc:replace("dog", 53)
            if not ok then
                ngx.say("failed to replace dog: ", err)
            end

            local res, flags, err = memc:get("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            if not res then
                ngx.say("dog not found")
                return
            end

            ngx.say("dog: ", res)
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
failed to add dog: NOT_STORED
failed to replace dog: NOT_STORED
dog not found
--- no_error_log
[error]



=== TEST 14: flags
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

            local ok, err = memc:set("dog", 32, 0, 526)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

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
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
dog: 32 (flags: 526)
--- no_error_log
[error]



=== TEST 15: set with exptime
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

            local res, flags, err = memc:get("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            ngx.location.capture("/sleep");

            local ok, err = memc:set("dog", 32, 1, 526)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            if not res then
                ngx.say("dog not found")
                return
            end

            ngx.say("dog: ", res, " (flags: ", flags, ")")
            memc:close()
        ';
    }

    location /sleep {
        echo_sleep 1.1;
    }
--- request
GET /t
--- response_body
dog not found
--- no_error_log
[error]



=== TEST 16: flush with a delay
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local ok, err = memc:flush_all(3)
            if not ok then
                ngx.say("failed to flush all: ", err)
                return
            end

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
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
dog: 32 (flags: 0)
--- no_error_log
[error]



=== TEST 17: incr an existent key
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local value, err = memc:incr("dog", 2)
            if not value then
                ngx.say("failed to incr dog: ", err)
                return
            end

            ngx.say("dog is now: ", value)

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
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
dog is now: 34
dog: 34 (flags: 0)
--- no_error_log
[error]



=== TEST 18: incr a nonexistent key
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

            local value, err = memc:incr("dog", 2)
            if not value then
                ngx.say("failed to incr dog: ", err)
                return
            end

            ngx.say("dog is now: ", value)

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
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
failed to incr dog: NOT_FOUND
--- no_error_log
[error]



=== TEST 19: decr an existent key
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local value, err = memc:decr("dog", 3)
            if not value then
                ngx.say("failed to decr dog: ", err)
                return
            end

            ngx.say("dog is now: ", value)

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
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
dog is now: 29
dog: 29 (flags: 0)
--- no_error_log
[error]



=== TEST 20: decr a nonexistent key
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

            local value, err = memc:decr("dog", 2)
            if not value then
                ngx.say("failed to decr dog: ", err)
                return
            end

            ngx.say("dog is now: ", value)

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
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
failed to decr dog: NOT_FOUND
--- no_error_log
[error]



=== TEST 21: general stats
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local lines, err = memc:stats()
            if not lines then
                ngx.say("failed to stats: ", err)
                return
            end

            ngx.say("stats:\\n", table.concat(lines, "\\n"))

            memc:close()
        ';
    }
--- request
GET /t
--- response_body_like chop
^stats:
STAT pid \d+
(?:STAT [^\n]+\n)*$
--- no_error_log
[error]



=== TEST 22: stats items
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local lines, err = memc:stats("items")
            if not lines then
                ngx.say("failed to stats items: ", err)
                return
            end

            ngx.say("stats:\\n", table.concat(lines, "\\n"))

            memc:close()
        ';
    }
--- request
GET /t
--- response_body_like chop
^stats:
(?:STAT items:[^\n]+\n)*$
--- no_error_log
[error]



=== TEST 23: stats sizes
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local lines, err = memc:stats("sizes")
            if not lines then
                ngx.say("failed to stats sizes: ", err)
                return
            end

            ngx.say("stats:\\n", table.concat(lines, "\\n"))

            memc:close()
        ';
    }
--- request
GET /t
--- response_body_like chop
^stats:
(?:STAT \d+ \d+\n)*$
--- no_error_log
[error]



=== TEST 24: version
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

            local ver, err = memc:version()
            if not ver then
                ngx.say("failed to get version: ", err)
                return
            end

            ngx.say("version: ", ver)

            memc:close()
        ';
    }
--- request
GET /t
--- response_body_like chop
^version: \d+(?:\.\d+)*$
--- no_error_log
[error]



=== TEST 25: quit
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

            local ok, err = memc:quit()
            if not ok then
                ngx.say("failed to quit: ", err)
                return
            end

            local ver, err = memc:version()
            if not ver then
                ngx.say("failed to get version: ", err)
                return
            end

            local ok, err = memc:close()
            if not ok then
                ngx.say("failed to close: ", err)
                return
            end

            ngx.say("closed successfully")
        ';
    }
--- request
GET /t
--- response_body_like chop
^failed to get version: (closed|timeout|broken pipe|connection reset by peer)$



=== TEST 26: verbosity
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

            local ok, err = memc:verbosity(2)
            if not ok then
                ngx.say("failed to quit: ", err)
                return
            end

            ngx.say("successfully set verbosity to level 2")

            local ver, err = memc:version()
            if not ver then
                ngx.say("failed to get version: ", err)
                return
            end

            local ok, err = memc:close()
            if not ok then
                ngx.say("failed to close: ", err)
                return
            end
        ';
    }
--- request
GET /t
--- response_body
successfully set verbosity to level 2
--- no_error_log
[error]



=== TEST 27: multi get
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local ok, err = memc:set("cat", "hello\\nworld\\n")
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            for i = 1, 2 do
                local results, err = memc:get({"dog", "blah", "cat"})
                if err then
                    ngx.say("failed to get keys: ", err)
                    return
                end

                if not results then
                    ngx.say("results empty")
                    return
                end

                ngx.say("dog: ", results.dog and table.concat(results.dog, " ") or "not found")
                ngx.say("cat: ", results.cat and table.concat(results.cat, " ") or "not found")
                ngx.say("blah: ", results.blah and table.concat(results.blah, " ") or "not found")
            end

            local ok, err = memc:close()
            if not ok then
                ngx.say("failed to close: ", err)
                return
            end
        ';
    }
--- request
GET /t
--- response_body
dog: 32 0
cat: hello
world
 0
blah: not found
dog: 32 0
cat: hello
world
 0
blah: not found
--- no_error_log
[error]



=== TEST 28: multi get (special chars in keys)
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

            local ok, err = memc:set("dog A", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local ok, err = memc:set("cat B", "hello\\nworld\\n")
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local results, err = memc:get({"dog A", "blah", "cat B"})
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            if not results then
                ngx.say("results empty")
                return
            end

            ngx.say("dog A: ", results["dog A"] and table.concat(results["dog A"], " ") or "not found")
            ngx.say("cat B: ", results["cat B"] and table.concat(results["cat B"], " ") or "not found")
            ngx.say("blah: ", results.blah and table.concat(results.blah, " ") or "not found")

            local ok, err = memc:close()
            if not ok then
                ngx.say("failed to close: ", err)
                return
            end
        ';
    }
--- request
GET /t
--- response_body
dog A: 32 0
cat B: hello
world
 0
blah: not found
--- no_error_log
[error]



=== TEST 29: connect timeout
--- http_config eval: $::HttpConfig
--- config
    resolver $TEST_NGINX_RESOLVER;
    location /t {
        content_by_lua '
            local memcached = require "resty.memcached"
            local memc = memcached:new()

            memc:set_timeout(100) -- 100 ms

            local ok, err = memc:connect("www.taobao.com", $TEST_NGINX_MEMCACHED_PORT)
            if not ok then
                ngx.say("failed to connect: ", err)
                return
            end

            local ok, err = memc:flush_all()
            if not ok then
                ngx.say("failed to flush all: ", err)
                return
            end

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

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
            memc:close()
        ';
    }
--- request
GET /t
--- response_body
failed to connect: timeout
--- error_log
lua tcp socket connect timed out



=== TEST 30: set keepalive and get reused times
--- http_config eval: $::HttpConfig
--- config
    resolver $TEST_NGINX_RESOLVER;
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

            local times = memc:get_reused_times()
            ngx.say("reused times: ", times)

            local ok, err = memc:set_keepalive()
            if not ok then
                ngx.say("failed to set keepalive: ", err)
                return
            end

            ok, err = memc:connect("127.0.0.1", $TEST_NGINX_MEMCACHED_PORT)
            if not ok then
                ngx.say("failed to connect: ", err)
                return
            end

            times = memc:get_reused_times()
            ngx.say("reused times: ", times)
        ';
    }
--- request
GET /t
--- response_body
reused times: 0
reused times: 1
--- no_error_log
[error]



=== TEST 31: gets (single key, found)
--- http_config eval: $::HttpConfig
--- config
    resolver $TEST_NGINX_RESOLVER;
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local res, flags, cas_uniq, err = memc:gets("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            if not res then
                ngx.say("dog not found")
                return
            end

            ngx.say("dog: ", res, " (flags: ", flags, ", cas_uniq: ", cas_uniq, ")")
        ';
    }
--- request
GET /t
--- response_body_like chop
^dog: 32 \(flags: 0, cas_uniq: \d+\)$
--- no_error_log
[error]



=== TEST 32: gets (single key, not found)
--- http_config eval: $::HttpConfig
--- config
    resolver $TEST_NGINX_RESOLVER;
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

            local res, flags, cas_uniq, err = memc:gets("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            if not res then
                ngx.say("dog not found")
                return
            end

            ngx.say("dog: ", res, " (flags: ", flags, ", cas_uniq: ", cas_uniq, ")")
        ';
    }
--- request
GET /t
--- response_body_like chop
dog not found
--- no_error_log
[error]



=== TEST 33: gets (multiple key)
--- http_config eval: $::HttpConfig
--- config
    resolver $TEST_NGINX_RESOLVER;
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local ok, err = memc:set("cat", "hello\\nworld\\n")
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local results, err = memc:gets({"dog", "blah", "cat"})
            if err then
                ngx.say("failed to get keys: ", err)
                return
            end

            if not results then
                ngx.say("results empty")
                return
            end

            if results.dog then
                ngx.say("dog: ", table.concat(results.dog, " "))
            else
                ngx.say("dog not found")
            end

            if results.blah then
                ngx.say("blah: ", table.concat(results.blah, " "))
            else
                ngx.say("blah not found")
            end

            if results.cat then
                ngx.say("cat: ", table.concat(results.cat, " "))
            else
                ngx.say("cat not found")
            end

            local ok, err = memc:close()
            if not ok then
                ngx.say("failed to close: ", err)
                return
            end
        ';
    }
--- request
GET /t
--- response_body_like chop
^dog: 32 0 \d+
blah not found
cat: hello
world
 0 \d+$
--- no_error_log
[error]



=== TEST 34: gets (single key) + cas
--- http_config eval: $::HttpConfig
--- config
    resolver $TEST_NGINX_RESOLVER;
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local value, flags, cas_uniq, err = memc:gets("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            ngx.say("dog: ", value, " (flags: ", flags, ", cas_uniq: ", cas_uniq, ")")

            local ok, err = memc:cas("dog", "hello world", cas_uniq, 0, 78)
            if not ok then
                ngx.say("failed to cas: ", err)
                return
            end

            ngx.say("cas succeeded")

            local value, flags, err = memc:get("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            ngx.say("dog: ", value, " (flags: ", flags, ")")
        ';
    }
--- request
GET /t
--- response_body_like chop
^dog: 32 \(flags: 0, cas_uniq: \d+\)
cas succeeded
dog: hello world \(flags: 78\)$
--- no_error_log
[error]



=== TEST 35: gets (multi key) + cas
--- http_config eval: $::HttpConfig
--- config
    resolver $TEST_NGINX_RESOLVER;
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local results, err = memc:gets({"dog"})
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            local value, flags, cas_uniq, err = unpack(results.dog)

            ngx.say("dog: ", value, " (flags: ", flags, ", cas_uniq: ", cas_uniq, ")")

            local ok, err = memc:cas("dog", "hello world", cas_uniq, 0, 78)
            if not ok then
                ngx.say("failed to cas: ", err)
                return
            end

            ngx.say("cas succeeded")

            local value, flags, err = memc:get("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            ngx.say("dog: ", value, " (flags: ", flags, ")")
        ';
    }
--- request
GET /t
--- response_body_like chop
^dog: 32 \(flags: 0, cas_uniq: \d+\)
cas succeeded
dog: hello world \(flags: 78\)$
--- no_error_log
[error]



=== TEST 36: gets (single key) + cas
--- http_config eval: $::HttpConfig
--- config
    resolver $TEST_NGINX_RESOLVER;
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

            local ok, err = memc:set("dog", 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local value, flags, cas_uniq, err = memc:gets("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            ok, err = memc:set("dog", 117)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            ngx.say("dog: ", value, " (flags: ", flags, ", cas_uniq: ", cas_uniq, ")")

            local ok, err = memc:cas("dog", "hello world", cas_uniq, 0, 78)
            if not ok then
                ngx.say("failed to cas: ", err)
                return
            end

            ngx.say("cas succeeded")

            local value, flags, err = memc:get("dog")
            if err then
                ngx.say("failed to get dog: ", err)
                return
            end

            ngx.say("dog: ", value, " (flags: ", flags, ")")
        ';
    }
--- request
GET /t
--- response_body_like chop
^dog: 32 \(flags: 0, cas_uniq: \d+\)
failed to cas: EXISTS$
--- no_error_log
[error]



=== TEST 37: change escape method
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '

            function identity(str)
                return str
            end

            local memcached = require "resty.memcached"
            local memc = memcached:new{ key_transform = { identity, identity }}
            local key = "dog&cat"

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

            local ok, err = memc:set(key, 32)
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            for i = 1, 2 do
                local res, flags, err = memc:get(key)
                if err then
                    ngx.say("failed to get", key, ": ", err)
                    return
                end

                if not res then
                    ngx.say(key, " not found")
                    return
                end

                ngx.say(key, ": ", res, " (flags: ", flags, ")")
            end

            memc:close()
        ';
    }
--- request
GET /t
--- response_body
dog&cat: 32 (flags: 0)
dog&cat: 32 (flags: 0)
--- no_error_log
[error]



=== TEST 38: gets (multiple key) + change only unescape key
--- http_config eval: $::HttpConfig
--- config
    resolver $TEST_NGINX_RESOLVER;
    location /t {
        content_by_lua '
            function identity(str)
                return str
            end

            local memcached = require "resty.memcached"
            local memc = memcached:new{key_transform = {ngx.escape_uri, identity}}

            local key = "dog&cat"

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

            local ok, err = memc:set(key, 32)
            if not ok then
                ngx.say("failed to set ", key, ": ", err)
                return
            end

            local ok, err = memc:set("cat", "hello\\nworld\\n")
            if not ok then
                ngx.say("failed to set dog: ", err)
                return
            end

            local results, err = memc:gets({key, "blah", "cat"})
            if err then
                ngx.say("failed to get keys: ", err)
                return
            end

            if not results then
                ngx.say("results empty")
                return
            end

            if results[key] then
                ngx.say(key, ": ", table.concat(results[key], " "))
            else
                ngx.say(key, " not found")
            end

            -- encode key for second run
            key = ngx.escape_uri(key)

            if results[key] then
                ngx.say(key, ": ", table.concat(results[key], " "))
            else
                ngx.say(key, " not found")
            end


            if results.blah then
                ngx.say("blah: ", table.concat(results.blah, " "))
            else
                ngx.say("blah not found")
            end

            if results.cat then
                ngx.say("cat: ", table.concat(results.cat, " "))
            else
                ngx.say("cat not found")
            end

            local ok, err = memc:close()
            if not ok then
                ngx.say("failed to close: ", err)
                return
            end
        ';
    }
--- request
GET /t
--- response_body_like chop
^dog&cat not found
dog%26cat: 32 0 \d+
blah not found
cat: hello
world
 0 \d+$
--- no_error_log
[error]

