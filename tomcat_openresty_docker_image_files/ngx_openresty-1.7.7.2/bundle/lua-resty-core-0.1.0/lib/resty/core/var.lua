-- Copyright (C) Yichun Zhang (agentzh)


local ffi = require 'ffi'
local base = require "resty.core.base"

local ffi_new = ffi.new
local ffi_str = ffi.string
local C = ffi.C
local type = type
local getfenv = getfenv
local get_string_buf = base.get_string_buf
local get_size_ptr = base.get_size_ptr
local error = error
local tostring = tostring
local ngx_var = ngx.var
local getmetatable = getmetatable


ffi.cdef[[
    int ngx_http_lua_ffi_var_get(ngx_http_request_t *r,
        const char *name_data, size_t name_len, char *lowcase_buf,
        int capture_id, char **value, size_t *value_len, char **err);

    int ngx_http_lua_ffi_var_set(ngx_http_request_t *r,
        const unsigned char *name_data, size_t name_len,
        unsigned char *lowcase_buf, const unsigned char *value,
        size_t value_len, unsigned char *errbuf, size_t errlen);
]]


local value_ptr = ffi_new("unsigned char *[1]")
local errmsg = base.get_errmsg_ptr()


local function var_get(self, name)
    local r = getfenv(0).__ngx_req
    if not r then
        return error("no request found")
    end

    local value_len = get_size_ptr()
    local rc
    if type(name) == "number" then
        rc = C.ngx_http_lua_ffi_var_get(r, nil, 0, nil, name, value_ptr,
                                        value_len, errmsg)

    else
        if type(name) ~= "string" then
            return error("bad variable name")
        end

        local name_len = #name
        local lowcase_buf = get_string_buf(name_len)

        rc = C.ngx_http_lua_ffi_var_get(r, name, name_len, lowcase_buf, 0,
                                        value_ptr, value_len, errmsg)
    end

    -- ngx.log(ngx.WARN, "rc = ", rc)

    if rc == 0 then -- NGX_OK
        return ffi_str(value_ptr[0], value_len[0])
    end

    if rc == -5 then  -- NGX_DECLINED
        return nil
    end

    if rc == -1 then  -- NGX_ERROR
        return error(ffi_str(errmsg[0]))
    end
end


local function var_set(self, name, value)
    local r = getfenv(0).__ngx_req
    if not r then
        return error("no request found")
    end

    if type(name) ~= "string" then
        return error("bad variable name")
    end
    local name_len = #name

    local errlen = 256
    local lowcase_buf = get_string_buf(name_len + errlen)

    local value_len
    if value == nil then
        value_len = 0
    else
        if type(value) ~= 'string' then
            value = tostring(value)
        end
        value_len = #value
    end

    local errbuf = lowcase_buf + name_len
    local rc = C.ngx_http_lua_ffi_var_set(r, name, name_len, lowcase_buf,
                                          value, value_len, errbuf, errlen)

    -- ngx.log(ngx.WARN, "rc = ", rc)

    if rc == 0 then -- NGX_OK
        return
    end

    if rc == -1 then  -- NGX_ERROR
        return error(ffi_str(errbuf, errlen))
    end
end


if ngx_var then
    local mt = getmetatable(ngx_var)
    if mt then
        mt.__index = var_get
        mt.__newindex = var_set
    end
end


return {
    version = base.version
}
