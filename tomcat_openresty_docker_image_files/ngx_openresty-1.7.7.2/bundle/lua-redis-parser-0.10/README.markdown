NAME
====

lua-redis-parser - Redis reply parser and request constructor library for Lua

Version
=======

This document describes lua-redis-parser [v0.09rc5](https://github.com/agentzh/lua-redis-parser/tags) released on 28 August 2011.

Description
===========

This lua library implements a thin and fast redis raw response parser
that constructs corresponding lua data strucutres, as well as a
function that constructs redis raw requests.

To maximize speed, this module is implemented in pure C.

This library is usually used by Lua code running atop [HttpLuaModule](http://wiki.nginx.org/HttpLuaModule) to access
redis backends though [HttpRedis2Module](http://wiki.nginx.org/HttpRedis2Module).

Functions
=========

The `parser` variable used below is referring to the variable holding the return value of `require "redis.parser"`. In other words, we assume you have written the following code first:


    local parser = require "redis.parser"


parse_reply
-----------
**syntax:** *res, typ = parser.parse_reply(raw_reply)*

Parses the single (or the first) raw redis reply from the `raw_reply` string and returns the Lua data structure `res`, as well as the reply type `typ`.

Here is an example:


    local parser = require 'redis.parser'

    -- assuming the reply variable holds the (single) redis response
    --  to be parsed:
    local res, typ = parser.parse_reply(reply)

    if typ == parser.BAD_REPLY then
        -- res is the textual error message from the parser
    elseif typ == parser.INTEGER_REPLY then
        -- res is an integer, like 3, returned from the redis server
    elseif typ == parser.ERROR_REPLY then
        -- res is the error message from the redis2 server
    elseif typ == parser.STATUS_REPLY then
        -- res is the textual message from the redis server
    elseif typ == parser.BULK_REPLY then
        --- res is a string for the bulk data
    elseif typ == parser.MULTI_BULK_REPLY then
        -- res is a lua (array) table that holds the individual bulks
    end


parse_replies
-------------
**syntax:** *results = parser.parse_replies(raw_replies)*

Similar to the [parse_reply](http://wiki.nginx.org/LuaRedisParser#parse_reply) method, but parse multiple pipelined redis replies in the `raw_replies` string argument. Returns a table of all the parsed results where each result is an array-like table consists of two elements, `res` and `typ`, which have exactly the same meaning as the return values of the [parse_reply](http://wiki.nginx.org/LuaRedisParser#parse_reply) function.

For instance,


    local parser = require "redis.parser"

    -- assuming the replies variable holds n redis responses
    --  to be parsed:
    local results = parser.parse_replies(replies, n)
    for i, result in ipairs(results) do
        local res = result[1]
        local typ = result[2]

        -- res and typ have exactly the same meaning as in
        --  the parse_reply method documented above
    end


typename
--------
**syntax:** *str = parser.typename(typ)*

Returns the textual representation of the reply type values returned by the [parse_reply](http://wiki.nginx.org/LuaRedisParser#parse_reply) and [parse_replies](http://wiki.nginx.org/LuaRedisParser#parse_replies) functions. Here's the correspondence:


    parser.typename(parser.BAD_REPLY)        == "bad reply"
    parser.typename(parser.INTEGER_REPLY)    == "integer reply"
    parser.typename(parser.ERROR_REPLY)      == "error reply"
    parser.typename(parser.STATUS_REPLY)     == "status reply"
    parser.typename(parser.BULK_REPLY)       == "bulk reply"
    parser.typename(parser.MULTI_BULK_REPLY) == "multi-bulk reply"


build_query
-----------
**syntax:** *raw_request = parser.build_query(args)*

Constructs a raw Redis request from simple Lua values. It simply accepts a Lua array-like table, a list of parameters including
the command name.

Please check out the complete list of redis 2.0 commands,

<http://redis.io/commands>

The first command in that list, "APPEND   key value", for example, we can just use


    local parser = require "redis.parser"

    local req = parser.build_query({'APPEND', 'some-key', 'some-value'})


to construct a binary request in the return value. Because the Redis command is case insensitive, I usually just use 'append', the lower case form, as the first element of that list, as in


    local parser = require "redis.parser"

    local req = parser.build_query({'set', 'foo', 'some value'})
        -- req is the raw TCP request ready to send to the remote redis server
        -- over the TCP connection


Null values should be specified by `parser.null` rather than Lua's `nil` value.

Boolean values will be converted to `1` or `0`, for `true` and `false`, respectively.

Constants
=========

BAD_REPLY
---------
**syntax:** *typ = parser.BAD_REPLY*

INTEGER_REPLY
-------------
**syntax:** *typ = parser.INTEGER_REPLY*

ERROR_REPLY
-----------
**syntax:** *typ = parser.ERROR_REPLY*

STATUS_REPLY
------------
**syntax:** *typ = parser.STATUS_REPLY*

BULK_REPLY
----------
**syntax:** *typ = parser.BULK_REPLY*

MULTI_BULK_REPLY
----------------
**syntax:** *typ = parser.MULTI_BULK_REPLY*

null
----
**syntax:** *val = parser.null*

Background
==========

This module is originally written for [HttpLuaModule](http://wiki.nginx.org/HttpLuaModule) and [HttpRedis2Module](http://wiki.nginx.org/HttpRedis2Module):

Report Bugs
===========

Although a lot of effort has been put into testing and code tuning, there must be some serious bugs lurking somewhere in this module. So whenever you are bitten by any quirks, please don't hesitate to

1. create a ticket on the [issue tracking interface](http://github.com/agentzh/lua-redis-parser/issues) provided by GitHub,
1. or send a bug report or even patches to `agentzh@gmail.com`.

Source Repository
=================

Available on GitHub at [agentzh/lua-redis-parser](http://github.com/agentzh/lua-redis-parser).

Installation
============

Build requirements
------------------

* Lua (http://www.lua.org/)
* or LuaJIT (http://www.luajit.org/)
* Latest source tarball of this library downloaded from <https://github.com/agentzh/lua-redis-parser/tags>

Gnu make and gcc is required to build this module.

Linux/BSD/Solaris
-----------------


    gmake CC=gcc
    gmake install CC=gcc


Mac OS X
--------


    make LDFLAGS='-bundle -undefined dynamic_lookup' CC=gcc
    make install


If your Lua or LuaJIT is not installed into the system, specify its include directory like this:


    make LUA_INCLUDE_DIR=/opt/luajit/include/luajit-2.0


You can specify a custom path for the installation target:


    make install LUA_LIB_DIR=/opt/lualib


The `DESTDIR` variable is also supported, to ease RPM packaging.

This library is included and enabled by default in the [ngx_openresty bundle](http://openresty.org).

Author
======

* Zhang "agentzh" Yichun (章亦春) <agentzh@gmail.com>

Copyright & License
===================

This module is licenced under the BSD license.

Copyright (C) 2009, 2010, 2011, Taobao Inc., Alibaba Group ( <http://www.taobao.com> ).

Copyright (C) 2009, 2010, 2011, by Zhang "agentzh" Yichun (章亦春) <agentzh@gmail.com>.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

SEE ALSO
========
* Use case: [Dynamic Routing Based On Redis](http://openresty.org/#DynamicRoutingBasedOnRedis)
* [HttpLuaModule](http://wiki.nginx.org/HttpLuaModule)
* [HttpRedis2Module](http://wiki.nginx.org/HttpRedis2Module)
* [Redis official site](http://redis.io/)

