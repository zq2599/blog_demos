-- Copyright (C) Yichun Zhang (agentzh)


local ffi = require 'ffi'
local base = require "resty.core.base"

local ffi_new = ffi.new
local ffi_str = ffi.string
local C = ffi.C
local get_string_buf = base.get_string_buf
local get_string_buf_size = base.get_string_buf_size
local get_size_ptr = base.get_size_ptr
local tonumber = tonumber
local tostring = tostring
local next = next
local type = type
local error = error
local ngx_shared = ngx.shared
local getmetatable = getmetatable


ffi.cdef[[
    int ngx_http_lua_ffi_shdict_get(void *zone, const unsigned char *key,
        size_t key_len, int *value_type, unsigned char **str_value_buf,
        size_t *str_value_len, double *num_value, int *user_flags,
        int get_stale, int *is_stale);

    int ngx_http_lua_ffi_shdict_incr(void *zone, const unsigned char *key,
        size_t key_len, double *value, char **err);

    int ngx_http_lua_ffi_shdict_store(void *zone, int op,
        const unsigned char *key, size_t key_len, int value_type,
        const unsigned char *str_value_buf, size_t str_value_len,
        double num_value, int exptime, int user_flags, char **errmsg,
        int *forcible);

    int ngx_http_lua_ffi_shdict_flush_all(void *zone);
]]


if not pcall(function () return C.free end) then
    ffi.cdef[[
        void free(void *ptr);
    ]]
end


local value_type = ffi_new("int[1]")
local user_flags = ffi_new("int[1]")
local num_value = ffi_new("double[1]")
local is_stale = ffi_new("int[1]")
local forcible = ffi_new("int[1]")
local str_value_buf = ffi_new("unsigned char *[1]")
local errmsg = base.get_errmsg_ptr()


local function shdict_store(zone, op, key, value, exptime, flags)
    if not zone or type(zone) ~= "userdata" then
        return error('bad "zone" argument')
    end

    if not exptime then
        exptime = 0
    end

    if not flags then
        flags = 0
    end

    if key == nil then
        return nil, "nil key"
    end

    if type(key) ~= "string" then
        key = tostring(key)
    end

    local key_len = #key
    if key_len == 0 then
        return nil, "empty key"
    end
    if key_len > 65535 then
        return nil, "key too long"
    end

    local str_value_buf
    local str_value_len = 0
    local num_value = 0
    local valtyp = type(value)

    -- print("value type: ", valtyp)
    -- print("exptime: ", exptime)

    if valtyp == "string" then
        valtyp = 4  -- LUA_TSTRING
        str_value_buf = value
        str_value_len = #value

    elseif valtyp == "number" then
        valtyp = 3  -- LUA_TNUMBER
        num_value = value

    elseif value == nil then
        valtyp = 0  -- LUA_TNIL

    elseif valtyp == "boolean" then
        valtyp = 1  -- LUA_TBOOLEAN
        num_value = value and 1 or 0

    else
        return nil, "bad value type"
    end

    local rc = C.ngx_http_lua_ffi_shdict_store(zone, op, key, key_len,
                                               valtyp, str_value_buf,
                                               str_value_len, num_value,
                                               exptime * 1000, flags, errmsg,
                                               forcible)

    -- print("rc == ", rc)

    if rc == 0 then  -- NGX_OK
        return true, nil, forcible[0] == 1
    end

    -- NGX_DECLINED or NGX_ERROR
    return false, ffi_str(errmsg[0]), forcible[0] == 1
end


local function shdict_set(zone, key, value, exptime, flags)
    return shdict_store(zone, 0, key, value, exptime, flags)
end


local function shdict_safe_set(zone, key, value, exptime, flags)
    return shdict_store(zone, 0x0004, key, value, exptime, flags)
end


local function shdict_add(zone, key, value, exptime, flags)
    return shdict_store(zone, 0x0001, key, value, exptime, flags)
end


local function shdict_safe_add(zone, key, value, exptime, flags)
    return shdict_store(zone, 0x0005, key, value, exptime, flags)
end


local function shdict_replace(zone, key, value, exptime, flags)
    return shdict_store(zone, 0x0002, key, value, exptime, flags)
end


local function shdict_delete(zone, key)
    return shdict_set(zone, key, nil)
end


local function shdict_get(zone, key)
    if not zone or type(zone) ~= "userdata" then
        return error('bad "zone" argument')
    end

    if key == nil then
        return nil, "nil key"
    end

    if type(key) ~= "string" then
        key = tostring(key)
    end

    local key_len = #key
    if key_len == 0 then
        return nil, "empty key"
    end
    if key_len > 65535 then
        return nil, "key too long"
    end

    local size = get_string_buf_size()
    local buf = get_string_buf(size)
    str_value_buf[0] = buf
    local value_len = get_size_ptr()
    value_len[0] = size

    local rc = C.ngx_http_lua_ffi_shdict_get(zone, key, key_len, value_type,
                                             str_value_buf, value_len,
                                             num_value, user_flags, 0,
                                             is_stale)
    if rc ~= 0 then
        return error("failed to get the key")
    end

    local typ = value_type[0]

    if typ == 0 then -- LUA_TNIL
        return nil
    end

    local flags = tonumber(user_flags[0])

    local val

    if typ == 4 then -- LUA_TSTRING
        if str_value_buf[0] ~= buf then
            -- ngx.say("len: ", tonumber(value_len[0]))
            buf = str_value_buf[0]
            val = ffi_str(buf, value_len[0])
            C.free(buf)
        else
            val = ffi_str(buf, value_len[0])
        end

    elseif typ == 3 then -- LUA_TNUMBER
        val = tonumber(num_value[0])

    elseif typ == 1 then -- LUA_TBOOLEAN
        val = (tonumber(buf[0]) ~= 0)

    else
        return error("unknown value type: " .. typ)
    end

    if flags ~= 0 then
        return val, flags
    end

    return val
end


local function shdict_get_stale(zone, key)
    if not zone or type(zone) ~= "userdata" then
        return error("bad \"zone\" argument")
    end

    if key == nil then
        return nil, "nil key"
    end

    if type(key) ~= "string" then
        key = tostring(key)
    end

    local key_len = #key
    if key_len == 0 then
        return nil, "empty key"
    end
    if key_len > 65535 then
        return nil, "key too long"
    end

    local size = get_string_buf_size()
    local buf = get_string_buf(size)
    str_value_buf[0] = buf
    local value_len = get_size_ptr()
    value_len[0] = size

    local rc = C.ngx_http_lua_ffi_shdict_get(zone, key, key_len, value_type,
                                             str_value_buf, value_len,
                                             num_value, user_flags, 1,
                                             is_stale)
    if rc ~= 0 then
        return error("failed to get the key")
    end

    local typ = value_type[0]

    if typ == 0 then -- LUA_TNIL
        return nil
    end

    local flags = tonumber(user_flags[0])
    local val

    if typ == 4 then -- LUA_TSTRING
        if str_value_buf[0] ~= buf then
            -- ngx.say("len: ", tonumber(value_len[0]))
            buf = str_value_buf[0]
            val = ffi_str(buf, value_len[0])
            C.free(buf)
        else
            val = ffi_str(buf, value_len[0])
        end

    elseif typ == 3 then -- LUA_TNUMBER
        val = tonumber(num_value[0])

    elseif typ == 1 then -- LUA_TBOOLEAN
        val = (tonumber(buf[0]) ~= 0)

    else
        return error("unknown value type: " .. typ)
    end

    if flags ~= 0 then
        return val, flags, is_stale[0] == 1
    end

    return val, nil, is_stale[0] == 1
end


local function shdict_incr(zone, key, value)
    if not zone or type(zone) ~= "userdata" then
        return error("bad \"zone\" argument")
    end

    if key == nil then
        return nil, "nil key"
    end

    if type(key) ~= "string" then
        key = tostring(key)
    end

    local key_len = #key
    if key_len == 0 then
        return nil, "empty key"
    end
    if key_len > 65535 then
        return nil, "key too long"
    end

    if type(value) ~= "number" then
        value = tonumber(value)
    end
    num_value[0] = value

    local rc = C.ngx_http_lua_ffi_shdict_incr(zone, key, key_len, num_value,
                                             errmsg)
    if rc ~= 0 then  -- ~= NGX_OK
        return nil, ffi_str(errmsg[0])
    end

    return tonumber(num_value[0])
end


local function shdict_flush_all(zone)
    if not zone or type(zone) ~= "userdata" then
        return error("bad \"zone\" argument")
    end

    C.ngx_http_lua_ffi_shdict_flush_all(zone)
end


if ngx_shared then
    local name, dict = next(ngx_shared, nil)
    if dict then
        local mt = getmetatable(dict)
        if mt then
            mt = mt.__index
            if mt then
                mt.get = shdict_get
                mt.get_stale = shdict_get_stale
                mt.incr = shdict_incr
                mt.set = shdict_set
                mt.safe_set = shdict_safe_set
                mt.add = shdict_add
                mt.safe_add = shdict_safe_add
                mt.replace = shdict_replace
                mt.delete = shdict_delete
                mt.flush_all = shdict_flush_all
            end
        end
    end
end


return {
    version = base.version
}
