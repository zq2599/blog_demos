-- Copyright (C) Yichun Zhang (agentzh)


local ffi = require 'ffi'
local ffi_new = ffi.new
local error = error
local setmetatable = setmetatable
local floor = math.floor
local ceil = math.ceil


local str_buf_size = 4096
local str_buf
local size_ptr
local FREE_LIST_REF = 0


if not ngx.config
   or not ngx.config.ngx_lua_version
   or ngx.config.ngx_lua_version < 9011
then
    error("ngx_lua 0.9.11+ required")
end


if string.find(jit.version, " 2.0") then
    ngx.log(ngx.WARN, "use of lua-resty-core with LuaJIT 2.0 is "
            .. "not recommended; use LuaJIT 2.1+ instead")
end


local ok, new_tab = pcall(require, "table.new")
if not ok then
    new_tab = function (narr, nrec) return {} end
end


local ok, clear_tab = pcall(require, "table.clear")
if not ok then
    clear_tab = function (tab)
                    for k, _ in pairs(tab) do
                        tab[k] = nil
                    end
                end
end


-- XXX for now LuaJIT 2.1 cannot compile require()
-- so we make the fast code path Lua only in our own
-- wrapper so that most of the require() calls in hot
-- Lua code paths can be JIT compiled.
do
    local orig_require = require
    local pkg_loaded = package.loaded
    local function my_require(name)
        local mod = pkg_loaded[name]
        if mod then
            return mod
        end
        return orig_require(name)
    end
    getfenv(0).require = my_require
end


if not pcall(ffi.typeof, "ngx_str_t") then
    ffi.cdef[[
        typedef struct {
            size_t                 len;
            const unsigned char   *data;
        } ngx_str_t;
    ]]
end


if not pcall(ffi.typeof, "ngx_http_request_t") then
    ffi.cdef[[
        struct ngx_http_request_s;
        typedef struct ngx_http_request_s  ngx_http_request_t;
    ]]
end


if not pcall(ffi.typeof, "ngx_http_lua_ffi_str_t") then
    ffi.cdef[[
        typedef struct {
            int                       len;
            const unsigned char      *data;
        } ngx_http_lua_ffi_str_t;
    ]]
end


local c_buf_type = ffi.typeof("char[?]")


local _M = new_tab(0, 16)


_M.version = "0.1.0"
_M.new_tab = new_tab
_M.clear_tab = clear_tab


local errmsg


function _M.get_errmsg_ptr()
    if not errmsg then
        errmsg = ffi_new("char *[1]")
    end
    return errmsg
end


if not ngx then
    return error("no existing ngx. table found")
end


function _M.set_string_buf_size(size)
    if size <= 0 then
        return
    end
    if str_buf then
        str_buf = nil
    end
    str_buf_size = ceil(size)
end


function _M.get_string_buf_size()
    return str_buf_size
end


function _M.get_size_ptr()
    if not size_ptr then
        size_ptr = ffi_new("size_t[1]")
    end

    return size_ptr
end


function _M.get_string_buf(size, must_alloc)
    -- ngx.log(ngx.ERR, "str buf size: ", str_buf_size)
    if size > str_buf_size or must_alloc then
        return ffi_new(c_buf_type, size)
    end

    if not str_buf then
        str_buf = ffi_new(c_buf_type, str_buf_size)
    end

    return str_buf
end


function _M.ref_in_table(tb, key)
    if key == nil then
        return -1
    end
    local ref = tb[FREE_LIST_REF]
    if ref and ref ~= 0 then
         tb[FREE_LIST_REF] = tb[ref]

    else
        ref = #tb + 1
    end
    tb[ref] = key

    -- print("ref key_id returned ", ref)
    return ref
end


_M.FFI_OK = 0
_M.FFI_NO_REQ_CTX = -100
_M.FFI_BAD_CONTEXT = -101
_M.FFI_ERROR = -1
_M.FFI_BUSY = -3
_M.FFI_DONE = -4
_M.FFI_DECLINED = -5


return _M
