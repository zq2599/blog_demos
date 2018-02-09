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

=== TEST 1: test an old bug in table.new() on i386 in luajit v2.1
--- http_config eval: $::HttpConfig
--- config
    location /t {
        access_log off;
        content_by_lua '
            -- jit.off()
            local mysql = require "resty.mysql"
            local db = mysql:new()

            local ok, err, errno, sqlstate = db:connect({
                host = "$TEST_NGINX_MYSQL_HOST",
                port = $TEST_NGINX_MYSQL_PORT,
                database = "world",
                user = "ngx_test",
                password = "ngx_test"})

            local res, err, errno, sqlstate
            for j = 1, 10 do
                res, err, errno, sqlstate = db:query("select * from City order by ID limit 50", 50)
                if not res then
                    ngx.log(ngx.ERR, "bad result #1: ", err, ": ", errno, ": ", sqlstate, ".")
                    return ngx.exit(500)
                end
            end

            for _, row in ipairs(res) do
                local ncols = 0
                for k, v in pairs(row) do
                    ncols = ncols + 1
                end
                ngx.say("ncols: ", ncols)
            end

            local ok, err = db:set_keepalive(10000, 50)
            if not ok then
                ngx.log(ngx.ERR, "failed to set keepalive: ", err)
                ngx.exit(500)
            end
        ';
    }
--- request
GET /t
--- response_body eval
"ncols: 5\n" x 50
--- no_error_log
[error]

