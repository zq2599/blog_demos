# vim:set ft= ts=4 sw=4 et:

use Test::Nginx::Socket::Lua;
use Cwd qw(cwd);

repeat_each(2);

plan tests => repeat_each() * (3 * blocks());

my $pwd = cwd();

our $HttpConfig = qq{
    resolver \$TEST_NGINX_RESOLVER;
    lua_package_path "$pwd/lib/?.lua;$pwd/t/lib/?.lua;;";
    lua_package_cpath "/usr/local/openresty-debug/lualib/?.so;/usr/local/openresty/lualib/?.so;;";
};

$ENV{TEST_NGINX_RESOLVER} = '8.8.8.8';
$ENV{TEST_NGINX_MYSQL_PORT} ||= 3306;
$ENV{TEST_NGINX_MYSQL_HOST} ||= '127.0.0.1';
$ENV{TEST_NGINX_MYSQL_PATH} ||= '/var/run/mysql/mysql.sock';

#log_level 'warn';

no_long_string();
no_shuffle();
check_accum_error_log();

run_tests();

__DATA__

=== TEST 1: send query w/o result set
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local mysql = require "resty.mysql"
            local db = mysql:new()

            db:set_timeout(4000) -- 4 sec

            local ok, err, errno, sqlstate = db:connect({
                host = "$TEST_NGINX_MYSQL_HOST",
                port = $TEST_NGINX_MYSQL_PORT,
                database = "ngx_test",
                user = "ngx_test",
                password = "ngx_test",
                ssl = true,
            })

            if not ok then
                ngx.say("failed to connect: ", err, ": ", errno, " ", sqlstate)
                return
            end

            ngx.say("connected to mysql ", db:server_ver(), ".")

            local bytes, err = db:send_query("drop table if exists cats")
            if not bytes then
                ngx.say("failed to send query: ", err)
            end

            ngx.say("sent ", bytes, " bytes.")

            local res, err, errno, sqlstate = db:read_result()
            if not res then
                ngx.say("bad result: ", err, ": ", errno, ": ", sqlstate, ".")
            end

            local ljson = require "ljson"
            ngx.say("result: ", ljson.encode(res))

            local ok, err = db:close()
            if not ok then
                ngx.say("failed to close: ", err)
                return
            end
        ';
    }
--- request
GET /t
--- response_body_like chop
^connected to mysql \d\.\S+\.
sent 30 bytes\.
result: \{"affected_rows":0,"insert_id":0,"server_status":2,"warning_count":[01]\}$
--- no_error_log
[error]
--- timeout: 5



=== TEST 2: send query w/o result set (verify)
--- http_config eval: $::HttpConfig
--- config
    lua_ssl_trusted_certificate /etc/test.crt;  # assuming used by the MySQL server
    location /t {
        content_by_lua '
            local mysql = require "resty.mysql"
            local db = mysql:new()

            db:set_timeout(4000) -- 4 sec

            local ok, err, errno, sqlstate = db:connect({
                host = "$TEST_NGINX_MYSQL_HOST",
                port = $TEST_NGINX_MYSQL_PORT,
                database = "ngx_test",
                user = "ngx_test",
                password = "ngx_test",
                ssl = true,
                ssl_verify = true,
            })

            if not ok then
                ngx.say("failed to connect: ", err, ": ", errno, " ", sqlstate)
                return
            end

            ngx.say("connected to mysql ", db:server_ver(), ".")

            local bytes, err = db:send_query("drop table if exists cats")
            if not bytes then
                ngx.say("failed to send query: ", err)
            end

            ngx.say("sent ", bytes, " bytes.")

            local res, err, errno, sqlstate = db:read_result()
            if not res then
                ngx.say("bad result: ", err, ": ", errno, ": ", sqlstate, ".")
            end

            local ljson = require "ljson"
            ngx.say("result: ", ljson.encode(res))

            local ok, err = db:close()
            if not ok then
                ngx.say("failed to close: ", err)
                return
            end
        ';
    }
--- request
GET /t
--- response_body_like chop
^connected to mysql \d\.\S+\.
sent 30 bytes\.
result: \{"affected_rows":0,"insert_id":0,"server_status":2,"warning_count":[01]\}$
--- no_error_log
[error]
--- timeout: 5



=== TEST 3: send query w/o result set (verify, failed)
--- http_config eval: $::HttpConfig
--- config
    location /t {
        content_by_lua '
            local mysql = require "resty.mysql"
            local db = mysql:new()

            db:set_timeout(4000) -- 4 sec

            local ok, err, errno, sqlstate = db:connect({
                host = "$TEST_NGINX_MYSQL_HOST",
                port = $TEST_NGINX_MYSQL_PORT,
                database = "ngx_test",
                user = "ngx_test",
                password = "ngx_test",
                ssl = true,
                ssl_verify = true,
            })

            if not ok then
                ngx.say("failed to connect: ", err, ": ", errno, " ", sqlstate)
                return
            end

            ngx.say("connected to mysql ", db:server_ver(), ".")

            local bytes, err = db:send_query("drop table if exists cats")
            if not bytes then
                ngx.say("failed to send query: ", err)
            end

            ngx.say("sent ", bytes, " bytes.")

            local res, err, errno, sqlstate = db:read_result()
            if not res then
                ngx.say("bad result: ", err, ": ", errno, ": ", sqlstate, ".")
            end

            local ljson = require "ljson"
            ngx.say("result: ", ljson.encode(res))

            local ok, err = db:close()
            if not ok then
                ngx.say("failed to close: ", err)
                return
            end
        ';
    }
--- request
GET /t
--- response_body
failed to connect: failed to do ssl handshake: 18: self signed certificate: nil nil
--- error_log
lua ssl certificate verify error: (18: self signed certificate)
--- timeout: 5

