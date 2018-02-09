-- Copyright (C) Yichun Zhang (agentzh)


local ffi = require 'ffi'
local debug = require 'debug'
local base = require "resty.core.base"
local misc = require "resty.core.misc"


local register_getter = misc.register_ngx_magic_key_getter
local register_setter = misc.register_ngx_magic_key_setter
local registry = debug.getregistry()
local new_tab = base.new_tab
local ref_in_table = base.ref_in_table
local getfenv = getfenv
local C = ffi.C
local FFI_NO_REQ_CTX = base.FFI_NO_REQ_CTX
local FFI_OK = base.FFI_OK


ffi.cdef[[
int ngx_http_lua_ffi_get_ctx_ref(ngx_http_request_t *r);
int ngx_http_lua_ffi_set_ctx_ref(ngx_http_request_t *r, int ref);
]]


local _M = {
    _VERSION = base.version
}


local function get_ctx_table()
    local r = getfenv(0).__ngx_req

    if not r then
        return error("no request found")
    end

    local ctx_ref = C.ngx_http_lua_ffi_get_ctx_ref(r)
    if ctx_ref == FFI_NO_REQ_CTX then
        return error("no request ctx found")
    end

    local ctxs = registry.ngx_lua_ctx_tables
    if ctx_ref < 0 then
        local ctx = new_tab(0, 4)
        ctx_ref = ref_in_table(ctxs, ctx)
        if C.ngx_http_lua_ffi_set_ctx_ref(r, ctx_ref) ~= FFI_OK then
            return nil
        end
        return ctx
    end
    return ctxs[ctx_ref]
end
register_getter("ctx", get_ctx_table)


local function set_ctx_table(ctx)
    local r = getfenv(0).__ngx_req

    if not r then
        return error("no request found")
    end

    local ctx_ref = C.ngx_http_lua_ffi_get_ctx_ref(r)
    if ctx_ref == FFI_NO_REQ_CTX then
        return error("no request ctx found")
    end

    local ctxs = registry.ngx_lua_ctx_tables
    if ctx_ref < 0 then
        ctx_ref = ref_in_table(ctxs, ctx)
        C.ngx_http_lua_ffi_set_ctx_ref(r, ctx_ref)
        return
    end
    ctxs[ctx_ref] = ctx
end
register_setter("ctx", set_ctx_table)


return _M
