-- Copyright (C) Yichun Zhang (agentzh)


local base = require "resty.core.base"
local ffi = require "ffi"


local FFI_NO_REQ_CTX = base.FFI_NO_REQ_CTX
local FFI_BAD_CONTEXT = base.FFI_BAD_CONTEXT
local new_tab = base.new_tab
local C = ffi.C
local getmetatable = getmetatable
local ngx_magic_key_getters = new_tab(0, 4)
local ngx_magic_key_setters = new_tab(0, 2)
local ngx = ngx
local getfenv = getfenv
local type = type


local _M = new_tab(0, 3)
_M._VERSION = base.version


local function register_getter(key, func)
    ngx_magic_key_getters[key] = func
end
_M.register_ngx_magic_key_getter = register_getter


local function register_setter(key, func)
    ngx_magic_key_setters[key] = func
end
_M.register_ngx_magic_key_setter = register_setter


local mt = getmetatable(ngx)


local old_index = mt.__index
mt.__index = function (tb, key)
    local f = ngx_magic_key_getters[key]
    if f then
        return f()
    end
    return old_index(tb, key)
end


local old_newindex = mt.__newindex
mt.__newindex = function (tb, key, ctx)
    local f = ngx_magic_key_setters[key]
    if f then
        return f(ctx)
    end
    return old_newindex(tb, key, ctx)
end


ffi.cdef[[
    int ngx_http_lua_ffi_get_resp_status(ngx_http_request_t *r);
    int ngx_http_lua_ffi_set_resp_status(ngx_http_request_t *r, int r);
    int ngx_http_lua_ffi_is_subrequest(ngx_http_request_t *r);
    int ngx_http_lua_ffi_headers_sent(ngx_http_request_t *r);
]]


-- ngx.status

local function get_status()
    local r = getfenv(0).__ngx_req

    if not r then
        return error("no request found")
    end

    local rc = C.ngx_http_lua_ffi_get_resp_status(r)

    if rc == FFI_BAD_CONTEXT then
        return error("API disabled in the current context")
    end

    return rc
end
register_getter("status", get_status)


local function set_status(status)
    local r = getfenv(0).__ngx_req

    if not r then
        return error("no request found")
    end

    if type(status) ~= 'number' then
        status = tonumber(status)
    end

    local rc = C.ngx_http_lua_ffi_set_resp_status(r, status)

    if rc == FFI_BAD_CONTEXT then
        return error("API disabled in the current context")
    end

    return
end
register_setter("status", set_status)


-- ngx.is_subrequest

local function is_subreq()
    local r = getfenv(0).__ngx_req

    if not r then
        return error("no request found")
    end

    local rc = C.ngx_http_lua_ffi_is_subrequest(r)

    if rc == FFI_BAD_CONTEXT then
        return error("API disabled in the current context")
    end

    return rc == 1 and true or false
end
register_getter("is_subrequest", is_subreq)


-- ngx.headers_sent

local function headers_sent()
    local r = getfenv(0).__ngx_req

    if not r then
        return error("no request found")
    end

    local rc = C.ngx_http_lua_ffi_headers_sent(r)

    if rc == FFI_NO_REQ_CTX then
        return error("no request ctx found")
    end

    if rc == FFI_BAD_CONTEXT then
        return error("API disabled in the current context")
    end

    return rc == 1
end
register_getter("headers_sent", headers_sent)


return _M
