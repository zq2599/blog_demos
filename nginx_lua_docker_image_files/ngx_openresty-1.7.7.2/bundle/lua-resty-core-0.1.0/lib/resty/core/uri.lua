-- Copyright (C) Yichun Zhang (agentzh)


local ffi = require 'ffi'
local ffi_string = ffi.string
local ffi_new = ffi.new
local C = ffi.C
local setmetatable = setmetatable
local ngx = ngx
local type = type
local tostring = tostring
local error = error
local base = require "resty.core.base"
local get_string_buf = base.get_string_buf
local get_size_ptr = base.get_size_ptr
local print = print
local tonumber = tonumber


ffi.cdef[[
    size_t ngx_http_lua_ffi_uri_escaped_length(const unsigned char *src,
                                               size_t len);

    void ngx_http_lua_ffi_escape_uri(const unsigned char *src, size_t len,
                                     unsigned char *dst);

    size_t ngx_http_lua_ffi_unescape_uri(const unsigned char *src,
                                         size_t len, unsigned char *dst);
]]


ngx.escape_uri = function (s)
    if type(s) ~= 'string' then
        if not s then
            s = ''
        else
            s = tostring(s)
        end
    end
    local slen = #s
    local dlen = C.ngx_http_lua_ffi_uri_escaped_length(s, slen)
    -- print("dlen: ", tonumber(dlen))
    if dlen == slen then
        return s
    end
    local dst = get_string_buf(dlen)
    C.ngx_http_lua_ffi_escape_uri(s, slen, dst)
    return ffi_string(dst, dlen)
end


ngx.unescape_uri = function (s)
    if type(s) ~= 'string' then
        if not s then
            s = ''
        else
            s = tostring(s)
        end
    end
    local slen = #s
    local dlen = slen
    local dst = get_string_buf(dlen)
    dlen = C.ngx_http_lua_ffi_unescape_uri(s, slen, dst)
    return ffi_string(dst, dlen)
end


return {
    version = base.version,
}
