-- Copyright (C) Yichun Zhang (agentzh)


local bit = require "bit"
local wbproto = require "resty.websocket.protocol"

local new_tab = wbproto.new_tab
local _recv_frame = wbproto.recv_frame
local _send_frame = wbproto.send_frame
local http_ver = ngx.req.http_version
local req_sock = ngx.req.socket
local ngx_header = ngx.header
local req_headers = ngx.req.get_headers
local str_lower = string.lower
local char = string.char
local str_find = string.find
local sha1_bin = ngx.sha1_bin
local base64 = ngx.encode_base64
local ngx = ngx
local read_body = ngx.req.read_body
local band = bit.band
local rshift = bit.rshift
local type = type
local setmetatable = setmetatable
-- local print = print


local _M = new_tab(0, 10)
_M._VERSION = '0.05'

local mt = { __index = _M }


function _M.new(self, opts)
    if ngx.headers_sent then
        return nil, "response header already sent"
    end

    read_body()

    if http_ver() ~= 1.1 then
        return nil, "bad http version"
    end

    local headers = req_headers()

    local val = headers.upgrade
    if type(val) == "table" then
        val = val[1]
    end
    if not val or str_lower(val) ~= "websocket" then
        return nil, "bad \"upgrade\" request header"
    end

    val = headers.connection
    if type(val) == "table" then
        val = val[1]
    end
    if not val or not str_find(str_lower(val), "upgrade", 1, true) then
        return nil, "bad \"connection\" request header"
    end

    local key = headers["sec-websocket-key"]
    if type(key) == "table" then
        key = key[1]
    end
    if not key then
        return nil, "bad \"sec-websocket-key\" request header"
    end

    local ver = headers["sec-websocket-version"]
    if type(ver) == "table" then
        ver = ver[1]
    end
    if not ver or ver ~= "13" then
        return nil, "bad \"sec-websocket-version\" request header"
    end

    local protocols = headers["sec-websocket-protocol"]
    if type(protocols) == "table" then
        protocols = protocols[1]
    end

    if protocols then
        ngx_header["Sec-WebSocket-Protocol"] = protocols
    end
    ngx_header["Upgrade"] = "websocket"

    local sha1 = sha1_bin(key .. "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
    ngx_header["Sec-WebSocket-Accept"] = base64(sha1)

    ngx_header["Content-Type"] = nil

    ngx.status = 101
    local ok, err = ngx.send_headers()
    if not ok then
        return nil, "failed to send response header: " .. (err or "unknonw")
    end
    ok, err = ngx.flush(true)
    if not ok then
        return nil, "failed to flush response header: " .. (err or "unknown")
    end

    local sock
    sock, err = req_sock(true)
    if not sock then
        return nil, err
    end

    local max_payload_len, send_masked, timeout
    if opts then
        max_payload_len = opts.max_payload_len
        send_masked = opts.send_masked
        timeout = opts.timeout

        if timeout then
            sock:settimeout(timeout)
        end
    end

    return setmetatable({
        sock = sock,
        max_payload_len = max_payload_len or 65535,
        send_masked = send_masked,
    }, mt)
end


function _M.set_timeout(self, time)
    local sock = self.sock
    if not sock then
        return nil, nil, "not initialized yet"
    end

    return sock:settimeout(time)
end


function _M.recv_frame(self)
    if self.fatal then
        return nil, nil, "fatal error already happened"
    end

    local sock = self.sock
    if not sock then
        return nil, nil, "not initialized yet"
    end

    local data, typ, err =  _recv_frame(sock, self.max_payload_len, true)
    if not data and not str_find(err, ": timeout", 1, true) then
        self.fatal = true
    end
    return data, typ, err
end


local function send_frame(self, fin, opcode, payload)
    if self.fatal then
        return nil, "fatal error already happened"
    end

    local sock = self.sock
    if not sock then
        return nil, "not initialized yet"
    end

    local bytes, err = _send_frame(sock, fin, opcode, payload,
                                   self.max_payload_len, self.send_masked)
    if not bytes then
        self.fatal = true
    end
    return bytes, err
end
_M.send_frame = send_frame


function _M.send_text(self, data)
    return send_frame(self, true, 0x1, data)
end


function _M.send_binary(self, data)
    return send_frame(self, true, 0x2, data)
end


function _M.send_close(self, code, msg)
    local payload
    if code then
        if type(code) ~= "number" or code > 0x7fff then
        end
        payload = char(band(rshift(code, 8), 0xff), band(code, 0xff))
                        .. (msg or "")
    end
    return send_frame(self, true, 0x8, payload)
end


function _M.send_ping(self, data)
    return send_frame(self, true, 0x9, data)
end


function _M.send_pong(self, data)
    return send_frame(self, true, 0xa, data)
end


return _M
