Name
====

lua-resty-memcached - Lua memcached client driver for the ngx_lua based on the cosocket API

Table of Contents
=================

* [Name](#name)
* [Status](#status)
* [Description](#description)
* [Synopsis](#synopsis)
* [Methods](#methods)
    * [new](#new)
    * [connect](#connect)
    * [set](#set)
    * [set_timeout](#set_timeout)
    * [set_keepalive](#set_keepalive)
    * [get_reused_times](#get_reused_times)
    * [close](#close)
    * [add](#add)
    * [replace](#replace)
    * [append](#append)
    * [prepend](#prepend)
    * [get](#get)
    * [gets](#gets)
    * [cas](#cas)
    * [touch](#touch)
    * [flush_all](#flush_all)
    * [delete](#delete)
    * [incr](#incr)
    * [decr](#decr)
    * [stats](#stats)
    * [version](#version)
    * [quit](#quit)
    * [verbosity](#verbosity)
* [Automatic Error Logging](#automatic-error-logging)
* [Limitations](#limitations)
* [TODO](#todo)
* [Author](#author)
* [Copyright and License](#copyright-and-license)
* [See Also](#see-also)

Status
======

This library is considered production ready.

Description
===========

This Lua library is a memcached client driver for the ngx_lua nginx module:

http://wiki.nginx.org/HttpLuaModule

This Lua library takes advantage of ngx_lua's cosocket API, which ensures
100% nonblocking behavior.

Note that at least [ngx_lua 0.5.0rc29](https://github.com/chaoslawful/lua-nginx-module/tags) or [ngx_openresty 1.0.15.7](http://openresty.org/#Download) is required.

Synopsis
========

```lua
    lua_package_path "/path/to/lua-resty-memcached/lib/?.lua;;";

    server {
        location /test {
            content_by_lua '
                local memcached = require "resty.memcached"
                local memc, err = memcached:new()
                if not memc then
                    ngx.say("failed to instantiate memc: ", err)
                    return
                end

                memc:set_timeout(1000) -- 1 sec

                -- or connect to a unix domain socket file listened
                -- by a memcached server:
                --     local ok, err = memc:connect("unix:/path/to/memc.sock")

                local ok, err = memc:connect("127.0.0.1", 11211)
                if not ok then
                    ngx.say("failed to connect: ", err)
                    return
                end

                local ok, err = memc:flush_all()
                if not ok then
                    ngx.say("failed to flush all: ", err)
                    return
                end

                local ok, err = memc:set("dog", 32)
                if not ok then
                    ngx.say("failed to set dog: ", err)
                    return
                end

                local res, flags, err = memc:get("dog")
                if err then
                    ngx.say("failed to get dog: ", err)
                    return
                end

                if not res then
                    ngx.say("dog not found")
                    return
                end

                ngx.say("dog: ", res)

                -- put it into the connection pool of size 100,
                -- with 10 seconds max idle timeout
                local ok, err = memc:set_keepalive(10000, 100)
                if not ok then
                    ngx.say("cannot set keepalive: ", err)
                    return
                end

                -- or just close the connection right away:
                -- local ok, err = memc:close()
                -- if not ok then
                --     ngx.say("failed to close: ", err)
                --     return
                -- end
            ';
        }
    }
```

[Back to TOC](#table-of-contents)

Methods
=======

The `key` argument provided in the following methods will be automatically escaped according to the URI escaping rules before sending to the memcached server.

[Back to TOC](#table-of-contents)

new
---
`syntax: memc, err = memcached:new(opts?)`

Creates a memcached object. In case of failures, returns `nil` and a string describing the error.

It accepts an optional `opts` table argument. The following options are supported:

* `key_transform`

    an array table containing two functions for escaping and unescaping the
    memcached keys, respectively. By default,
    the memcached keys will be escaped and unescaped as URI components, that is

```lua
    memached:new{
        key_transform = { ngx.escape_uri, ngx.unescape_uri }
    }
```

[Back to TOC](#table-of-contents)

connect
-------
`syntax: ok, err = memc:connect(host, port)`

`syntax: ok, err = memc:connect("unix:/path/to/unix.sock")`

Attempts to connect to the remote host and port that the memcached server is listening to or a local unix domain socket file listened by the memcached server.

Before actually resolving the host name and connecting to the remote backend, this method will always look up the connection pool for matched idle connections created by previous calls of this method.

[Back to TOC](#table-of-contents)

set
---
`syntax: ok, err = memc:set(key, value, exptime, flags)`

Inserts an entry into memcached unconditionally. If the key already exists, overrides it.

The `value` argument could also be a Lua table holding multiple Lua
strings that are supposed to be concatenated as a whole
(without any delimiters). For example,

```lua
    memc:set("dog", {"a ", {"kind of"}, " animal"})
```

is functionally equivalent to

```lua
    memc:set("dog", "a kind of animal")
```

The `exptime` parameter is optional, defaults to `0`.

The `flags` parameter is optional, defaults to `0`.

[Back to TOC](#table-of-contents)

set_timeout
----------
`syntax: memc:set_timeout(time)`

Sets the timeout (in ms) protection for subsequent operations, including the `connect` method.

[Back to TOC](#table-of-contents)

set_keepalive
------------
`syntax: ok, err = memc:set_keepalive(max_idle_timeout, pool_size)`

Puts the current memcached connection immediately into the ngx_lua cosocket connection pool.

You can specify the max idle timeout (in ms) when the connection is in the pool and the maximal size of the pool every nginx worker process.

In case of success, returns `1`. In case of errors, returns `nil` with a string describing the error.

Only call this method in the place you would have called the `close` method instead. Calling this method will immediately turn the current memcached object into the `closed` state. Any subsequent operations other than `connect()` on the current objet will return the `closed` error.

[Back to TOC](#table-of-contents)

get_reused_times
----------------
`syntax: times, err = memc:get_reused_times()`

This method returns the (successfully) reused times for the current connection. In case of error, it returns `nil` and a string describing the error.

If the current connection does not come from the built-in connection pool, then this method always returns `0`, that is, the connection has never been reused (yet). If the connection comes from the connection pool, then the return value is always non-zero. So this method can also be used to determine if the current connection comes from the pool.

[Back to TOC](#table-of-contents)

close
-----
`syntax: ok, err = memc:close()`

Closes the current memcached connection and returns the status.

In case of success, returns `1`. In case of errors, returns `nil` with a string describing the error.


[Back to TOC](#table-of-contents)

add
---
`syntax: ok, err = memc:add(key, value, exptime, flags)`

Inserts an entry into memcached if and only if the key does not exist.

The `value` argument could also be a Lua table holding multiple Lua
strings that are supposed to be concatenated as a whole
(without any delimiters). For example,

```lua
    memc:add("dog", {"a ", {"kind of"}, " animal"})
```

is functionally equivalent to

```lua
    memc:add("dog", "a kind of animal")
```

The `exptime` parameter is optional, defaults to `0`.

The `flags` parameter is optional, defaults to `0`.

In case of success, returns `1`. In case of errors, returns `nil` with a string describing the error.

[Back to TOC](#table-of-contents)

replace
-------
`syntax: ok, err = memc:replace(key, value, exptime, flags)`

Inserts an entry into memcached if and only if the key does exist.

The `value` argument could also be a Lua table holding multiple Lua
strings that are supposed to be concatenated as a whole
(without any delimiters). For example,

```lua
    memc:replace("dog", {"a ", {"kind of"}, " animal"})
```

is functionally equivalent to

```lua
    memc:replace("dog", "a kind of animal")
```

The `exptime` parameter is optional, defaults to `0`.

The `flags` parameter is optional, defaults to `0`.

In case of success, returns `1`. In case of errors, returns `nil` with a string describing the error.

[Back to TOC](#table-of-contents)

append
------
`syntax: ok, err = memc:append(key, value, exptime, flags)`

Appends the value to an entry with the same key that already exists in memcached.

The `value` argument could also be a Lua table holding multiple Lua
strings that are supposed to be concatenated as a whole
(without any delimiters). For example,

```lua
    memc:append("dog", {"a ", {"kind of"}, " animal"})
```

is functionally equivalent to

```lua
    memc:append("dog", "a kind of animal")
```

The `exptime` parameter is optional, defaults to `0`.

The `flags` parameter is optional, defaults to `0`.

In case of success, returns `1`. In case of errors, returns `nil` with a string describing the error.

[Back to TOC](#table-of-contents)

prepend
-------
`syntax: ok, err = memc:prepend(key, value, exptime, flags)`

Prepends the value to an entry with the same key that already exists in memcached.

The `value` argument could also be a Lua table holding multiple Lua
strings that are supposed to be concatenated as a whole
(without any delimiters). For example,

```lua
    memc:prepend("dog", {"a ", {"kind of"}, " animal"})
```

is functionally equivalent to

```lua
    memc:prepend("dog", "a kind of animal")
```

The `exptime` parameter is optional, defaults to `0`.

The `flags` parameter is optional, defaults to `0`.

In case of success, returns `1`. In case of errors, returns `nil` with a string describing the error.

[Back to TOC](#table-of-contents)

get
---
`syntax: value, flags, err = memc:get(key)`
`syntax: results, err = memc:get(keys)`

Get a single entry or multiple entries in the memcached server via a single key or a talbe of keys.

Let us first discuss the case When the key is a single string.

The key's value and associated flags value will be returned if the entry is found and no error happens.

In case of errors, `nil` values will be turned for `value` and `flags` and a 3rd (string) value will also be returned for describing the error.

If the entry is not found, then three `nil` values will be returned.

Then let us discuss the case when the a Lua table of multiple keys are provided.

In this case, a Lua table holding the key-result pairs will be always returned in case of success. Each value corresponding each key in the table is also a table holding two values, the key's value and the key's flags. If a key does not exist, then there is no responding entries in the `results` table.

In case of errors, `nil` will be returned, and the second return value will be a string describing the error.

[Back to TOC](#table-of-contents)

gets
----
`syntax: value, flags, cas_unique, err = memc:gets(key)`

`syntax: results, err = memc:gets(keys)`

Just like the `get` method, but will also return the CAS unique value associated with the entry in addition to the key's value and flags.

This method is usually used together with the `cas` method.

[Back to TOC](#table-of-contents)

cas
---
`syntax: ok, err = memc:cas(key, value, cas_unique, exptime?, flags?)`

Just like the `set` method but does a check and set operation, which means "store this data but
  only if no one else has updated since I last fetched it."

The `cas_unique` argument can be obtained from the `gets` method.

[Back to TOC](#table-of-contents)

touch
---
`syntax: ok, err = memc:touch(key, exptime)`

Update the expiration time of an existing key.

Returns `1` for success or `nil` with a string describing the error otherwise.

This method was first introduced in the `v0.11` release.

[Back to TOC](#table-of-contents)

flush_all
---------
`syntax: ok, err = memc:flush_all(time?)`

Flushes (or invalidates) all the existing entries in the memcached server immediately (by default) or after the expiration
specified by the `time` argument (in seconds).

In case of success, returns `1`. In case of errors, returns `nil` with a string describing the error.

[Back to TOC](#table-of-contents)

delete
------
`syntax: ok, err = memc:delete(key)`

Deletes the key from memcached immediately.

The key to be deleted must already exist in memcached.

In case of success, returns `1`. In case of errors, returns `nil` with a string describing the error.

[Back to TOC](#table-of-contents)

incr
----
`syntax: new_value, err = memc:incr(key, delta)`

Increments the value of the specified key by the integer value specified in the `delta` argument.

Returns the new value after incrementation in success, and `nil` with a string describing the error in case of failures.

[Back to TOC](#table-of-contents)

decr
----
`syntax: new_value, err = memc:decr(key, value)`

Decrements the value of the specified key by the integer value specified in the `delta` argument.

Returns the new value after decrementation in success, and `nil` with a string describing the error in case of failures.

[Back to TOC](#table-of-contents)

stats
-----
`syntax: lines, err = memc:stats(args?)`

Returns memcached server statistics information with an optional `args` argument.

In case of success, this method returns a lua table holding all of the lines of the output; in case of failures, it returns `nil` with a string describing the error.

If the `args` argument is omitted, general server statistics is returned. Possible `args` argument values are `items`, `sizes`, `slabs`, among others.

[Back to TOC](#table-of-contents)

version
-------
`syntax: version, err = memc:version(args?)`

Returns the server version number, like `1.2.8`.

In case of error, it returns `nil` with a string describing the error.

[Back to TOC](#table-of-contents)

quit
----
`syntax: ok, err = memc:quit()`

Tells the server to close the current memcached connection.

Returns `1` in case of success and `nil` other wise. In case of failures, another string value will also be returned to describe the error.

Generally you can just directly call the `close` method to achieve the same effect.

[Back to TOC](#table-of-contents)

verbosity
---------
`syntax: ok, err = memc:verbosity(level)`

Sets the verbosity level used by the memcached server. The `level` argument should be given integers only.

Returns `1` in case of success and `nil` other wise. In case of failures, another string value will also be returned to describe the error.

[Back to TOC](#table-of-contents)

Automatic Error Logging
=======================

By default the underlying [ngx_lua](http://wiki.nginx.org/HttpLuaModule) module
does error logging when socket errors happen. If you are already doing proper error
handling in your own Lua code, then you are recommended to disable this automatic error logging by turning off [ngx_lua](http://wiki.nginx.org/HttpLuaModule)'s [lua_socket_log_errors](http://wiki.nginx.org/HttpLuaModule#lua_socket_log_errors) directive, that is,

```nginx
    lua_socket_log_errors off;
```

[Back to TOC](#table-of-contents)

Limitations
===========

* This library cannot be used in code contexts like set_by_lua*, log_by_lua*, and
header_filter_by_lua* where the ngx_lua cosocket API is not available.
* The `resty.memcached` object instance cannot be stored in a Lua variable at the Lua module level,
because it will then be shared by all the concurrent requests handled by the same nginx
 worker process (see
http://wiki.nginx.org/HttpLuaModule#Data_Sharing_within_an_Nginx_Worker ) and
result in bad race conditions when concurrent requests are trying to use the same `resty.memcached` instance.
You should always initiate `resty.memcached` objects in function local
variables or in the `ngx.ctx` table. These places all have their own data copies for
each request.

[Back to TOC](#table-of-contents)

TODO
====

* implement the memcached pipelining API.
* implement the UDP part of the memcached ascii protocol.

[Back to TOC](#table-of-contents)

Author
======

Yichun "agentzh" Zhang (章亦春) <agentzh@gmail.com>, CloudFlare Inc.

[Back to TOC](#table-of-contents)

Copyright and License
=====================

This module is licensed under the BSD license.

Copyright (C) 2012-2013, by Yichun "agentzh" Zhang (章亦春) <agentzh@gmail.com>, CloudFlare Inc.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

[Back to TOC](#table-of-contents)

See Also
========
* the ngx_lua module: http://wiki.nginx.org/HttpLuaModule
* the memcached wired protocol specification: http://code.sixapart.com/svn/memcached/trunk/server/doc/protocol.txt
* the [lua-resty-redis](https://github.com/agentzh/lua-resty-redis) library.
* the [lua-resty-mysql](https://github.com/agentzh/lua-resty-mysql) library.

[Back to TOC](#table-of-contents)

