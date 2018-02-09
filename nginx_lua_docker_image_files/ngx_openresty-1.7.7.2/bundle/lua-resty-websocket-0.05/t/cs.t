# vim:set ft= ts=4 sw=4 et:

use Test::Nginx::Socket::Lua;
use Cwd qw(cwd);
use Protocol::WebSocket::Frame;

repeat_each(2);

plan tests => repeat_each() * (blocks() * 4 + 14);

my $pwd = cwd();

our $HttpConfig = qq{
    lua_package_path "$pwd/lib/?.lua;;";
    lua_package_cpath "/usr/local/openresty-debug/lualib/?.so;/usr/local/openresty/lualib/?.so;;";
};

$ENV{TEST_NGINX_RESOLVER} = '8.8.8.8';
$ENV{TEST_NGINX_REDIS_PORT} ||= 6379;

check_accum_error_log();
no_long_string();
#no_diff();

run_tests();

__DATA__

=== TEST 1: text frame
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 1st frame: ", err)
                return
            end

            ngx.say("1: received: ", data, " (", typ, ")")

            local bytes, err = wb:send_text("copy: " .. data)
            if not bytes then
                ngx.say("failed to send frame: ", err)
                return
            end

            data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 2nd frame: ", err)
                return
            end

            ngx.say("2: received: ", data, " (", typ, ")")
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            local bytes, err = wb:send_text("你好, WebSocket!")
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 1st text: ", err)
                return ngx.exit(444)
            end

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                return ngx.exit(444)
            end

            -- send it back!
            bytes, err = wb:send_text(data)
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 2nd text: ", err)
                return ngx.exit(444)
            end
        ';
    }
--- request
GET /c
--- response_body
1: received: 你好, WebSocket! (text)
2: received: copy: 你好, WebSocket! (text)
--- no_error_log
[error]
[warn]
--- error_log
recv_frame: mask bit: 0
recv_frame: mask bit: 1



=== TEST 2: binary frame
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 1st frame: ", err)
                return
            end

            ngx.say("1: received: ", data, " (", typ, ")")

            local bytes, err = wb:send_binary("copy: " .. data)
            if not bytes then
                ngx.say("failed to send frame: ", err)
                return
            end

            data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 2nd frame: ", err)
                return
            end

            ngx.say("2: received: ", data, " (", typ, ")")
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            local bytes, err = wb:send_binary("你好, WebSocket!")
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 1st text: ", err)
                return ngx.exit(444)
            end

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                return ngx.exit(444)
            end

            -- send it back!
            bytes, err = wb:send_binary(data)
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 2nd text: ", err)
                return ngx.exit(444)
            end
        ';
    }
--- request
GET /c
--- response_body
1: received: 你好, WebSocket! (binary)
2: received: copy: 你好, WebSocket! (binary)
--- no_error_log
[error]
[warn]



=== TEST 3: close frame (without msg body)
--- http_config eval: $::HttpConfig
--- config
    lingering_close always;
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            -- print("c: receiving frame")

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 1st frame: ", err)
                return
            end

            -- print("c: received frame")

            ngx.say("received ", typ, ": ", data, ": ", err)

            local bytes, err = wb:send_close()
            if not bytes then
                ngx.say("failed to send frame: ", err)
                return
            end

            wb:recv_frame()  -- receive the close frame
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            -- print("s: sending close")

            local bytes, err = wb:send_close()
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 1st text: ", err)
                return ngx.exit(444)
            end

            -- print("s: sent close")

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                return ngx.exit(444)
            end

            ngx.log(ngx.WARN, "received: ", typ, ": ", data, ": ", err)

            wb:send_close()
        ';
    }
--- request
GET /c
--- response_body
received close: : nil

--- error_log
received: close: : nil
--- no_error_log
[error]



=== TEST 4: close frame (with msg body)
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            -- print("c: receiving frame")

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 1st frame: ", err)
                return
            end

            -- print("c: received frame")

            ngx.say("received ", typ, ": ", data, ": ", err)

            local bytes, err = wb:send_close(1000, "server, let\'s close!")
            if not bytes then
                ngx.say("failed to send frame: ", err)
                return
            end
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            -- print("s: sending close")

            local bytes, err = wb:send_close(1001, "client, let\'s close!")
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 1st text: ", err)
                return ngx.exit(444)
            end

            -- print("s: sent close")

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                return ngx.exit(444)
            end

            ngx.log(ngx.WARN, "received: ", typ, ": ", data, ": ", err)
        ';
    }
--- request
GET /c
--- response_body
received close: client, let's close!: 1001

--- error_log
received: close: server, let's close!: 1000
--- no_error_log
[error]



=== TEST 5: ping frame (without msg body)
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            -- print("c: receiving frame")

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 1st frame: ", err)
                return
            end

            -- print("c: received frame")

            ngx.say("received ", typ, ": ", data, ": ", err)

            local bytes, err = wb:send_ping()
            if not bytes then
                ngx.say("failed to send frame: ", err)
                return
            end
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            -- print("s: sending close")

            local bytes, err = wb:send_ping()
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 1st text: ", err)
                return ngx.exit(444)
            end

            -- print("s: sent close")

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                return ngx.exit(444)
            end

            ngx.log(ngx.WARN, "received: ", typ, ": ", data, ": ", err)
        ';
    }
--- request
GET /c
--- response_body
received ping: : nil

--- error_log
received: ping: : nil
--- no_error_log
[error]



=== TEST 6: ping frame (with msg body)
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            -- print("c: receiving frame")

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 1st frame: ", err)
                return
            end

            -- print("c: received frame")

            ngx.say("received ", typ, ": ", data, ": ", err)

            local bytes, err = wb:send_ping("hey, server?")
            if not bytes then
                ngx.say("failed to send frame: ", err)
                return
            end
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            -- print("s: sending close")

            local bytes, err = wb:send_ping("hey, client?")
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 1st text: ", err)
                return ngx.exit(444)
            end

            -- print("s: sent close")

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                return ngx.exit(444)
            end

            ngx.log(ngx.WARN, "received: ", typ, ": ", data, ": ", err)
        ';
    }
--- request
GET /c
--- response_body
received ping: hey, client?: nil

--- error_log
received: ping: hey, server?: nil
--- no_error_log
[error]



=== TEST 7: pong frame (without msg body)
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            -- print("c: receiving frame")

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 1st frame: ", err)
                return
            end

            -- print("c: received frame")

            ngx.say("received ", typ, ": ", data, ": ", err)

            local bytes, err = wb:send_pong()
            if not bytes then
                ngx.say("failed to send frame: ", err)
                return
            end

            wb:recv_frame()  -- receive the close frame
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            -- print("s: sending close")

            local bytes, err = wb:send_pong()
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 1st text: ", err)
                return ngx.exit(444)
            end

            -- print("s: sent close")

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                return ngx.exit(444)
            end

            ngx.log(ngx.WARN, "received: ", typ, ": ", data, ": ", err)

            wb:send_close()
        ';
    }
--- request
GET /c
--- response_body
received pong: : nil

--- error_log
received: pong: : nil
--- no_error_log
[error]



=== TEST 8: pong frame (with msg body)
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            -- print("c: receiving frame")

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 1st frame: ", err)
                return
            end

            -- print("c: received frame")

            ngx.say("received ", typ, ": ", data, ": ", err)

            local bytes, err = wb:send_pong("halo, server!")
            if not bytes then
                ngx.say("failed to send frame: ", err)
                return
            end
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            -- print("s: sending close")

            local bytes, err = wb:send_pong("halo, client!")
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 1st text: ", err)
                return ngx.exit(444)
            end

            -- print("s: sent close")

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                return ngx.exit(444)
            end

            ngx.log(ngx.WARN, "received: ", typ, ": ", data, ": ", err)
        ';
    }
--- request
GET /c
--- response_body
received pong: halo, client!: nil

--- error_log
received: pong: halo, server!: nil
--- no_error_log
[error]



=== TEST 9: client recv timeout (set_timeout)
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            wb:set_timeout(1)

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 1st frame: ", err)
            else
                ngx.say("1: received: ", data, " (", typ, ")")
            end

            wb:set_timeout(1000)

            data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 2nd frame: ", err)
            else
                ngx.say("2: received: ", data, " (", typ, ")")
            end
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            ngx.sleep(0.2)

            local bytes, err = wb:send_text("你好, WebSocket!")
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 1st text: ", err)
                return ngx.exit(444)
            end
        ';
    }
--- request
GET /c
--- response_body
failed to receive 1st frame: failed to receive the first 2 bytes: timeout
2: received: 你好, WebSocket! (text)
--- error_log
lua tcp socket read timed out
--- no_error_log
[warn]



=== TEST 10: client recv timeout (timeout option)
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new{ timeout = 100 }
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 1st frame: ", err)
            else
                ngx.say("1: received: ", data, " (", typ, ")")
            end

            wb:set_timeout(1000)

            data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 2nd frame: ", err)
            else
                ngx.say("2: received: ", data, " (", typ, ")")
            end
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            ngx.sleep(0.2)

            local bytes, err = wb:send_text("你好, WebSocket!")
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 1st text: ", err)
                return ngx.exit(444)
            end
        ';
    }
--- request
GET /c
--- response_body
failed to receive 1st frame: failed to receive the first 2 bytes: timeout
2: received: 你好, WebSocket! (text)
--- error_log
lua tcp socket read timed out
--- no_error_log
[warn]



=== TEST 11: server recv timeout (set_timeout)
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            ngx.sleep(0.2)

            local bytes, err = wb:send_text("你好, WebSocket!")
            if not bytes then
                ngx.say("failed to send the 1st text: ", err)
                return ngx.exit(444)
            end

            ngx.say("ok")
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            wb:set_timeout(1)

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.log(ngx.ERR, "failed to receive 1st frame: ", err)
            else
                ngx.log(ngx.WARN, "1: received: ", data, " (", typ, ")")
            end

            wb:set_timeout(1000)

            data, typ, err = wb:recv_frame()
            if not data then
                ngx.log(ngx.ERR, "failed to receive 2nd frame: ", err)
            else
                ngx.log(ngx.WARN, "2: received: ", data, " (", typ, ")")
            end
        ';
    }
--- request
GET /c
--- response_body
ok
--- error_log
failed to receive 1st frame: failed to receive the first 2 bytes: timeout
2: received: 你好, WebSocket! (text)
lua tcp socket read timed out



=== TEST 12: server recv timeout (in constructor)
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            ngx.sleep(0.2)

            local bytes, err = wb:send_text("你好, WebSocket!")
            if not bytes then
                ngx.say("failed to send the 1st text: ", err)
                return ngx.exit(444)
            end

            ngx.say("ok")
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new{ timeout = 1 }
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.log(ngx.ERR, "failed to receive 1st frame: ", err)
            else
                ngx.log(ngx.WARN, "1: received: ", data, " (", typ, ")")
            end

            wb:set_timeout(1000)

            data, typ, err = wb:recv_frame()
            if not data then
                ngx.log(ngx.ERR, "failed to receive 2nd frame: ", err)
            else
                ngx.log(ngx.WARN, "2: received: ", data, " (", typ, ")")
            end
        ';
    }
--- request
GET /c
--- response_body
ok
--- error_log
failed to receive 1st frame: failed to receive the first 2 bytes: timeout
2: received: 你好, WebSocket! (text)
lua tcp socket read timed out



=== TEST 13: reused upstream websocket connections (set_keepalive)
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()

            for i = 1, 3 do
                local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
                -- ngx.say("uri: ", uri)
                local ok, err = wb:connect(uri)
                if not ok then
                    ngx.say("failed to connect: " .. err)
                    return
                end

                local data = "hello " .. i
                local bytes, err = wb:send_text(data)
                if not bytes then
                    ngx.say("failed to send frame: ", err)
                    return
                end

                data, typ, err = wb:recv_frame()
                if not data then
                    ngx.say("failed to receive 2nd frame: ", err)
                    return
                end

                ngx.say("received: ", data, " (", typ, ")")

                local ok, err = wb:set_keepalive()
                if not ok then
                    ngx.say("failed to recycle conn: ", err)
                    return
                end
            end
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            while true do
                local data, typ, err = wb:recv_frame()
                if not data then
                    -- ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                    return ngx.exit(444)
                end

                -- send it back!
                bytes, err = wb:send_text(data)
                if not bytes then
                    ngx.log(ngx.ERR, "failed to send the 2nd text: ", err)
                    return ngx.exit(444)
                end
            end
        ';
    }
--- request
GET /c
--- response_body
received: hello 1 (text)
received: hello 2 (text)
received: hello 3 (text)

--- no_error_log
[error]
[warn]
--- error_log
recv_frame: mask bit: 0
recv_frame: mask bit: 1



=== TEST 14: pool option
--- http_config eval: $::HttpConfig
--- config
    lua_socket_log_errors off;
    location = /c2 {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s2"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri, { pool = "my_conn_pool" })
            if not ok then
                ok, err = wb:connect(uri)
                if not ok then
                    ngx.say("failed to connect: " .. err)
                    return
                end
            end

            data, typ, err = wb:send_text("hello websocket")
            if not data then
                ngx.say("failed to receive 2nd frame: ", err)
                return
            end

            data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 2nd frame: ", err)
                return
            end

            ngx.say("received: ", data, " (", typ, ")")

            local ok, err = wb:set_keepalive()
            if not ok then
                ngx.say("failed to set keepalive: ", err)
                return
            end
        ';
    }

    location = /s2 {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            while true do
                local data, err = wb:recv_frame()
                if not data then
                    -- ngx.log(ngx.ERR, "failed to recv text: ", err)
                    return ngx.exit(444)
                end

                local bytes, err = wb:send_text(data)
                if not bytes then
                    ngx.log(ngx.ERR, "failed to send the 1st text: ", err)
                    return ngx.exit(444)
                end
            end
        ';
    }
--- request
GET /c2
--- response_body
received: hello websocket (text)
--- stap
F(ngx_http_lua_socket_tcp_setkeepalive) {
    println("socket tcp set keepalive")
}
--- stap_out
socket tcp set keepalive
--- error_log
lua tcp socket keepalive create connection pool for key "my_conn_pool"
--- no_error_log
[error]
[warn]



=== TEST 15: text frame (send masked frames on the server side)
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 1st frame: ", err)
                return
            end

            ngx.say("1: received: ", data, " (", typ, ")")

            local bytes, err = wb:send_text("copy: " .. data)
            if not bytes then
                ngx.say("failed to send frame: ", err)
                return
            end

            data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 2nd frame: ", err)
                return
            end

            ngx.say("2: received: ", data, " (", typ, ")")
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new{ send_masked = true }
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            local bytes, err = wb:send_text("你好, WebSocket!")
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 1st text: ", err)
                return ngx.exit(444)
            end

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                return ngx.exit(444)
            end

            -- send it back!
            bytes, err = wb:send_text(data)
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 2nd text: ", err)
                return ngx.exit(444)
            end

            ngx.sleep(0.001)  -- lingering close
        ';
    }
--- request
GET /c
--- response_body
1: received: 你好, WebSocket! (text)
2: received: copy: 你好, WebSocket! (text)
--- no_error_log
[error]
[warn]
recv_frame: mask bit: 0
--- error_log
recv_frame: mask bit: 1



=== TEST 16: text frame (send unmasked frames on the client side)
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new{ send_unmasked = true }
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 1st frame: ", err)
                return
            end

            ngx.say("1: received: ", data, " (", typ, ")")

            local bytes, err = wb:send_text("copy: " .. data)
            if not bytes then
                ngx.say("failed to send frame: ", err)
                return
            end

            data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 2nd frame: ", err)
                return
            end

            ngx.say("2: received: ", data, " (", typ, ")")
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            local bytes, err = wb:send_text("你好, WebSocket!")
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 1st text: ", err)
                return ngx.exit(444)
            end

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                return ngx.exit(444)
            end

            -- send it back!
            bytes, err = wb:send_text(data)
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 2nd text: ", err)
                return ngx.exit(444)
            end

            ngx.sleep(0.001)  -- lingering close
        ';
    }
--- request
GET /c
--- response_body_like eval
qr/^1: received: 你好, WebSocket! \(text\)
(?:failed to receive 2nd frame: failed to receive the first 2 bytes: (?:closed|connection reset by peer)
|failed to send frame: failed to send frame: broken pipe)$/
--- no_error_log
[warn]
--- error_log
recv_frame: mask bit: 0
failed to receive a frame: frame unmasked



=== TEST 17: close frame (without msg body) + close()
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            -- print("c: receiving frame")

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 1st frame: ", err)
                return
            end

            -- print("c: received frame")

            ngx.say("received ", typ, ": ", data, ": ", err)

            local bytes, err = wb:send_close()
            if not bytes then
                ngx.say("failed to send frame: ", err)
                return
            end

            local ok, err = wb:close()
            if not ok then
                ngx.say("failed to close: ", err)
                return
            end

            ngx.say("successfully closed the TCP connection")
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            -- print("s: sending close")

            local bytes, err = wb:send_close()
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 1st text: ", err)
                return ngx.exit(444)
            end

            -- print("s: sent close")

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                return ngx.exit(444)
            end

            ngx.log(ngx.WARN, "received: ", typ, ": ", data, ": ", err)
        ';
    }
--- request
GET /c
--- response_body
received close: : nil
successfully closed the TCP connection

--- error_log
received: close: : nil
sending the close frame
--- no_error_log
[error]



=== TEST 18: directly calling close() without sending the close frame ourselves
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:" .. ngx.var.server_port .. "/s"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            -- print("c: receiving frame")

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 1st frame: ", err)
                return
            end

            -- print("c: received frame")

            ngx.say("received ", typ, ": ", data, ": ", err)

            local ok, err = wb:close()
            if not ok then
                ngx.say("failed to close: ", err)
                return
            end

            ngx.say("successfully closed the TCP connection")
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            -- print("s: sending close")

            local bytes, err = wb:send_close()
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 1st text: ", err)
                return ngx.exit(444)
            end

            -- print("s: sent close")

            local data, typ, err = wb:recv_frame()
            if not data then
                ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                return ngx.exit(444)
            end

            ngx.log(ngx.WARN, "received: ", typ, ": ", data, ": ", err)
        ';
    }
--- request
GET /c
--- response_body
received close: : nil
successfully closed the TCP connection

--- error_log
received: close: : nil
sending the close frame
--- no_error_log
[error]



=== TEST 19: client handshake (scalar protocols)
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            math.randomseed(0)
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:7986/"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri, { protocols = "json" })
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end
        ';
    }

--- request
GET /c
--- tcp_listen: 7986
--- tcp_query eval
"GET / HTTP/1.1\r
Upgrade: websocket\r
Host: 127.0.0.1:7986\r
Sec-WebSocket-Key: y7KXwBSpVrxtkR0O+bQt+Q==\r
Sec-WebSocket-Protocol: json\r
Sec-WebSocket-Version: 13\r
Connection: Upgrade\r
\r
"
--- tcp_reply: blah
--- response_body
failed to connect: failed to receive response header: closed
--- no_error_log
[error]
[warn]



=== TEST 20: client handshake (table protocols)
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            math.randomseed(0)
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:7986/"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri, { protocols = {"xml", "json"} })
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end
        ';
    }

--- request
GET /c
--- tcp_listen: 7986
--- tcp_query eval
"GET / HTTP/1.1\r
Upgrade: websocket\r
Host: 127.0.0.1:7986\r
Sec-WebSocket-Key: y7KXwBSpVrxtkR0O+bQt+Q==\r
Sec-WebSocket-Protocol: xml,json\r
Sec-WebSocket-Version: 13\r
Connection: Upgrade\r
\r
"
--- tcp_reply: blah
--- response_body
failed to connect: failed to receive response header: closed
--- no_error_log
[error]
[warn]



=== TEST 21: client handshake (origin)
--- http_config eval: $::HttpConfig
--- config
    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            math.randomseed(0)
            local wb, err = client:new()
            local uri = "ws://127.0.0.1:7986/"
            -- ngx.say("uri: ", uri)
            local ok, err = wb:connect(uri, { origin = "test.com" })
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end
        ';
    }

--- request
GET /c
--- tcp_listen: 7986
--- tcp_query eval
"GET / HTTP/1.1\r
Upgrade: websocket\r
Host: 127.0.0.1:7986\r
Sec-WebSocket-Key: y7KXwBSpVrxtkR0O+bQt+Q==\r
Sec-WebSocket-Version: 13\r
Origin: test.com\r
Connection: Upgrade\r
\r
"
--- tcp_reply: blah
--- response_body
failed to connect: failed to receive response header: closed
--- no_error_log
[error]
[warn]



=== TEST 22: SSL with keepalive
--- no_check_leak
--- http_config eval: $::HttpConfig
--- config
    listen 12345 ssl;
    server_name test.com;
    ssl_certificate ../../cert/test.crt;
    ssl_certificate_key ../../cert/test.key;
    server_tokens off;

    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()

            for i = 1, 3 do
                local uri = "wss://127.0.0.1:12345/s"
                local ok, err = wb:connect(uri)
                if not ok then
                    ngx.say("failed to connect: " .. err)
                    return
                end

                local data = "hello " .. i
                local bytes, err = wb:send_text(data)
                if not bytes then
                    ngx.say("failed to send frame: ", err)
                    return
                end

                data, typ, err = wb:recv_frame()
                if not data then
                    ngx.say("failed to receive 2nd frame: ", err)
                    return
                end

                ngx.say("received: ", data, " (", typ, ")")

                local ok, err = wb:set_keepalive()
                if not ok then
                    ngx.say("failed to recycle conn: ", err)
                    return
                end
            end
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            while true do
                local data, typ, err = wb:recv_frame()
                if not data then
                    -- ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                    return ngx.exit(444)
                end

                -- send it back!
                bytes, err = wb:send_text(data)
                if not bytes then
                    ngx.log(ngx.ERR, "failed to send the 2nd text: ", err)
                    return ngx.exit(444)
                end
            end
        ';
    }
--- request
GET /c
--- response_body
received: hello 1 (text)
received: hello 2 (text)
received: hello 3 (text)

--- no_error_log
[error]
[warn]

--- timeout: 10



=== TEST 23: SSL without keepalive
--- no_check_leak
--- http_config eval: $::HttpConfig
--- config
    listen 12345 ssl;
    server_name test.com;
    ssl_certificate ../../cert/test.crt;
    ssl_certificate_key ../../cert/test.key;
    server_tokens off;

    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()

            local uri = "wss://127.0.0.1:12345/s"
            local ok, err = wb:connect(uri)
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            local data = "hello"
            local bytes, err = wb:send_text(data)
            if not bytes then
                ngx.say("failed to send frame: ", err)
                return
            end

            data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 2nd frame: ", err)
                return
            end

            ngx.say("received: ", data, " (", typ, ")")

            local ok, err = wb:close()
            if not ok then
                ngx.say("failed to close conn: ", err)
                return
            end
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            local data, typ, err = wb:recv_frame()
            if not data then
                -- ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                return ngx.exit(444)
            end

            -- send it back!
            bytes, err = wb:send_text(data)
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 2nd text: ", err)
                return ngx.exit(444)
            end
        ';
    }
--- request
GET /c
--- response_body
received: hello (text)

--- no_error_log
[error]
[warn]

--- timeout: 10



=== TEST 24: SSL with ssl_verify
--- no_check_leak
--- http_config eval: $::HttpConfig
--- config
    listen 12345 ssl;
    server_name test.com;
    ssl_certificate ../../cert/test.crt;
    ssl_certificate_key ../../cert/test.key;
    server_tokens off;

    resolver 127.0.0.1:1953 ipv6=off;
    resolver_timeout 1s;

    lua_ssl_trusted_certificate ../../cert/test.crt;
    lua_ssl_verify_depth 1;

    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()

            local uri = "wss://test.com:12345/s"
            local ok, err = wb:connect(uri, {ssl_verify = true})
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end

            local data = "hello"
            local bytes, err = wb:send_text(data)
            if not bytes then
                ngx.say("failed to send frame: ", err)
                return
            end

            data, typ, err = wb:recv_frame()
            if not data then
                ngx.say("failed to receive 2nd frame: ", err)
                return
            end

            ngx.say("received: ", data, " (", typ, ")")

            local ok, err = wb:close()
            if not ok then
                ngx.say("failed to close conn: ", err)
                return
            end
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            local data, typ, err = wb:recv_frame()
            if not data then
                -- ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                return ngx.exit(444)
            end

            -- send it back!
            bytes, err = wb:send_text(data)
            if not bytes then
                ngx.log(ngx.ERR, "failed to send the 2nd text: ", err)
                return ngx.exit(444)
            end
        ';
    }
--- udp_listen: 1953
--- udp_reply eval
sub {
    # Get DNS request ID from passed UDP datagram
    my $dns_id = unpack("n", shift);
    # Set name and encode it
    my $name = "test.com";
    $name =~ s/([^.]+)\.?/chr(length($1)) . $1/ge;
    $name .= "\0";
    my $s = '';
    $s .= pack("n", $dns_id);
    # DNS response flags, hardcoded
    my $flags = (1 << 15) + (0 << 11) + (0 << 10) + (0 << 9) + (1 << 8) + (1 << 7) + 0;
    $flags = pack("n", $flags);
    $s .= $flags;
    $s .= pack("nnnn", 1, 1, 0, 0);
    $s .= $name;
    $s .= pack("nn", 1, 1);
    # Set response address and pack it
    my @addr = split /\./, "127.0.0.1";
    my $data = pack("CCCC", @addr);
    $s .= $name. pack("nnNn", 1, 1, 1, 4) . $data;
    return $s;
}

--- request
GET /c
--- response_body
received: hello (text)

--- no_error_log
[error]
[warn]

--- timeout: 10



=== TEST 25: SSL with ssl_verify (handshake failed)
--- no_check_leak
--- http_config eval: $::HttpConfig
--- config
    listen 12345 ssl;
    server_name test.com;
    ssl_certificate ../../cert/test.crt;
    ssl_certificate_key ../../cert/test.key;
    server_tokens off;

    resolver 127.0.0.1:1953 ipv6=off;
    resolver_timeout 1s;

    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()

            local uri = "wss://test.com:12345/s"
            local ok, err = wb:connect(uri, {ssl_verify = true})
            if not ok then
                ngx.say("failed to connect: " .. err)
                return
            end
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end
        ';
    }
--- udp_listen: 1953
--- udp_reply eval
sub {
    # Get DNS request ID from passed UDP datagram
    my $dns_id = unpack("n", shift);
    # Set name and encode it
    my $name = "test.com";
    $name =~ s/([^.]+)\.?/chr(length($1)) . $1/ge;
    $name .= "\0";
    my $s = '';
    $s .= pack("n", $dns_id);
    # DNS response flags, hardcoded
    my $flags = (1 << 15) + (0 << 11) + (0 << 10) + (0 << 9) + (1 << 8) + (1 << 7) + 0;
    $flags = pack("n", $flags);
    $s .= $flags;
    $s .= pack("nnnn", 1, 1, 0, 0);
    $s .= $name;
    $s .= pack("nn", 1, 1);
    # Set response address and pack it
    my @addr = split /\./, "127.0.0.1";
    my $data = pack("CCCC", @addr);
    $s .= $name. pack("nnNn", 1, 1, 1, 4) . $data;
    return $s;
}

--- request
GET /c

--- error_log
lua ssl certificate verify error: (18: self signed certificate)

--- timeout: 10



=== TEST 26: SSL without ssl_verify
--- no_check_leak
--- http_config eval: $::HttpConfig
--- config
    listen 12345 ssl;
    server_name test.com;
    ssl_certificate ../../cert/test.crt;
    ssl_certificate_key ../../cert/test.key;
    server_tokens off;

    location = /c {
        content_by_lua '
            local client = require "resty.websocket.client"
            local wb, err = client:new()

            for i = 1, 3 do
                local uri = "wss://127.0.0.1:12345/s"
                local ok, err = wb:connect(uri, {ssl_verify = false})
                if not ok then
                    ngx.say("failed to connect: " .. err)
                    return
                end

                local data = "hello " .. i
                local bytes, err = wb:send_text(data)
                if not bytes then
                    ngx.say("failed to send frame: ", err)
                    return
                end

                data, typ, err = wb:recv_frame()
                if not data then
                    ngx.say("failed to receive 2nd frame: ", err)
                    return
                end

                ngx.say("received: ", data, " (", typ, ")")

                local ok, err = wb:set_keepalive()
                if not ok then
                    ngx.say("failed to close conn: ", err)
                    return
                end
            end
        ';
    }

    location = /s {
        content_by_lua '
            local server = require "resty.websocket.server"
            local wb, err = server:new()
            if not wb then
                ngx.log(ngx.ERR, "failed to new websocket: ", err)
                return ngx.exit(444)
            end

            while true do
                local data, typ, err = wb:recv_frame()
                if not data then
                    -- ngx.log(ngx.ERR, "failed to receive a frame: ", err)
                    return ngx.exit(444)
                end

                -- send it back!
                bytes, err = wb:send_text(data)
                if not bytes then
                    ngx.log(ngx.ERR, "failed to send the 2nd text: ", err)
                    return ngx.exit(444)
                end
            end
        ';
    }
--- request
GET /c
--- response_body
received: hello 1 (text)
received: hello 2 (text)
received: hello 3 (text)

--- no_error_log
[error]
[warn]

--- timeout: 10

