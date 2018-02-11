-- Copyright (C) Yichun Zhang (agentzh)


local ffi = require 'ffi'
local ffi_string = ffi.string
local C = ffi.C
local ngx = ngx
local tostring = tostring
local error = error
local base = require "resty.core.base"
local get_string_buf = base.get_string_buf
local get_size_ptr = base.get_size_ptr
local base = require "resty.core.base"
local getfenv = getfenv
local co_yield = coroutine._yield


ffi.cdef[[
    int ngx_http_lua_ffi_exit(ngx_http_request_t *r, int status,
                               unsigned char *err, size_t *errlen);
]]


local ERR_BUF_SIZE = 128
local FFI_DONE = base.FFI_DONE


ngx.exit = function (rc)
    local err = get_string_buf(ERR_BUF_SIZE)
    local errlen = get_size_ptr()
    local r = getfenv(0).__ngx_req
    if r == nil then
        return error("no request found")
    end
    errlen[0] = ERR_BUF_SIZE
    local rc = C.ngx_http_lua_ffi_exit(r, rc, err, errlen)
    if rc == 0 then
        -- print("yielding...")
        return co_yield()
    end
    if rc == FFI_DONE then
        return
    end
    return error(ffi_string(err, errlen[0]))
end


return {
    version = base.version
}
