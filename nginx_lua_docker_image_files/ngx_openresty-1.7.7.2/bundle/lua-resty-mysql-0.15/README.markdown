Name
====

lua-resty-mysql - Lua MySQL client driver for ngx_lua based on the cosocket API

Table of Contents
=================

* [Name](#name)
* [Status](#status)
* [Description](#description)
* [Synopsis](#synopsis)
* [Methods](#methods)
    * [new](#new)
    * [connect](#connect)
    * [set_timeout](#set_timeout)
    * [set_keepalive](#set_keepalive)
    * [get_reused_times](#get_reused_times)
    * [close](#close)
    * [send_query](#send_query)
    * [read_result](#read_result)
    * [query](#query)
    * [server_ver](#server_ver)
    * [set_compact_arrays](#set_compact_arrays)
* [SQL Literal Quoting](#sql-literal-quoting)
* [Multi-Resultset Support](#multi-resultset-support)
* [Debugging](#debugging)
* [Automatic Error Logging](#automatic-error-logging)
* [Limitations](#limitations)
* [Installation](#installation)
* [Community](#community)
    * [English Mailing List](#english-mailing-list)
    * [Chinese Mailing List](#chinese-mailing-list)
* [Bugs and Patches](#bugs-and-patches)
* [TODO](#todo)
* [Author](#author)
* [Copyright and License](#copyright-and-license)
* [See Also](#see-also)

Status
======

This library is considered production ready.

Description
===========

This Lua library is a MySQL client driver for the ngx_lua nginx module:

http://wiki.nginx.org/HttpLuaModule

This Lua library takes advantage of ngx_lua's cosocket API, which ensures
100% nonblocking behavior.

Note that at least [ngx_lua 0.9.11](https://github.com/chaoslawful/lua-nginx-module/tags) or [ngx_openresty 1.7.4.1](http://openresty.org/#Download) is required.

Also, the [bit library](http://bitop.luajit.org/) is also required. If you're using LuaJIT 2.0 with ngx_lua, then the `bit` library is already available by default.

Synopsis
========

```lua

    # you do not need the following line if you are using
    # the ngx_openresty bundle:
    lua_package_path "/path/to/lua-resty-mysql/lib/?.lua;;";

    server {
        location /test {
            content_by_lua '
                local mysql = require "resty.mysql"
                local db, err = mysql:new()
                if not db then
                    ngx.say("failed to instantiate mysql: ", err)
                    return
                end

                db:set_timeout(1000) -- 1 sec

                -- or connect to a unix domain socket file listened
                -- by a mysql server:
                --     local ok, err, errno, sqlstate =
                --           db:connect{
                --              path = "/path/to/mysql.sock",
                --              database = "ngx_test",
                --              user = "ngx_test",
                --              password = "ngx_test" }

                local ok, err, errno, sqlstate = db:connect{
                    host = "127.0.0.1",
                    port = 3306,
                    database = "ngx_test",
                    user = "ngx_test",
                    password = "ngx_test",
                    max_packet_size = 1024 * 1024 }

                if not ok then
                    ngx.say("failed to connect: ", err, ": ", errno, " ", sqlstate)
                    return
                end

                ngx.say("connected to mysql.")

                local res, err, errno, sqlstate =
                    db:query("drop table if exists cats")
                if not res then
                    ngx.say("bad result: ", err, ": ", errno, ": ", sqlstate, ".")
                    return
                end

                res, err, errno, sqlstate =
                    db:query("create table cats "
                             .. "(id serial primary key, "
                             .. "name varchar(5))")
                if not res then
                    ngx.say("bad result: ", err, ": ", errno, ": ", sqlstate, ".")
                    return
                end

                ngx.say("table cats created.")

                res, err, errno, sqlstate =
                    db:query("insert into cats (name) "
                             .. "values (\'Bob\'),(\'\'),(null)")
                if not res then
                    ngx.say("bad result: ", err, ": ", errno, ": ", sqlstate, ".")
                    return
                end

                ngx.say(res.affected_rows, " rows inserted into table cats ",
                        "(last insert id: ", res.insert_id, ")")

                -- run a select query, expected about 10 rows in
                -- the result set:
                res, err, errno, sqlstate =
                    db:query("select * from cats order by id asc", 10)
                if not res then
                    ngx.say("bad result: ", err, ": ", errno, ": ", sqlstate, ".")
                    return
                end

                local cjson = require "cjson"
                ngx.say("result: ", cjson.encode(res))

                -- put it into the connection pool of size 100,
                -- with 10 seconds max idle timeout
                local ok, err = db:set_keepalive(10000, 100)
                if not ok then
                    ngx.say("failed to set keepalive: ", err)
                    return
                end

                -- or just close the connection right away:
                -- local ok, err = db:close()
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

[Back to TOC](#table-of-contents)

new
---
`syntax: db, err = mysql:new()`

Creates a MySQL connection object. In case of failures, returns `nil` and a string describing the error.

[Back to TOC](#table-of-contents)

connect
-------
`syntax: ok, err = db:connect(options)`

Attempts to connect to the remote MySQL server.

The `options` argument is a Lua table holding the following keys:

* `host`

    the host name for the MySQL server.
* `port`

    the port that the MySQL server is listening on. Default to 3306.
* `path`

    the path of the unix socket file listened by the MySQL server.
* `database`

    the MySQL database name.
* `user`

    MySQL account name for login.
* `password`

    MySQL account password for login (in clear text).
* `max_packet_size`

    the upper limit for the reply packets sent from the MySQL server (default to 1MB).
* `ssl`

    If set to `true`, then uses SSL to connect to MySQL (default to `false`). If the MySQL
    server does not have SSL support
    (or just disabled), the error string "ssl disabled on server" will be returned.
* `ssl_verify`

    If set to `true`, then verifies the validity of the server SSL certificate (default to `false`).
    Note that you need to configure the [lua_ssl_trusted_certificate](https://github.com/openresty/lua-nginx-module#lua_ssl_trusted_certificate)
    to specify the CA (or server) certificate used by your MySQL server. You may also
    need to configure [lua_ssl_verify_depth](https://github.com/openresty/lua-nginx-module#lua_ssl_verify_depth)
    accordingly.
* `pool`

    the name for the MySQL connection pool. if omitted, an ambiguous pool name will be generated automatically with the string template `user:database:host:port` or `user:database:path`. (this option was first introduced in `v0.08`.)
* `compact_arrays`

    when this option is set to true, then the [query](#query) and [read_result](#read_result) methods will return the array-of-arrays structure for the resultset, rather than the default array-of-hashes structure.

Before actually resolving the host name and connecting to the remote backend, this method will always look up the connection pool for matched idle connections created by previous calls of this method.

[Back to TOC](#table-of-contents)

set_timeout
----------
`syntax: db:set_timeout(time)`

Sets the timeout (in ms) protection for subsequent operations, including the `connect` method.

[Back to TOC](#table-of-contents)

set_keepalive
------------
`syntax: ok, err = db:set_keepalive(max_idle_timeout, pool_size)`

Puts the current MySQL connection immediately into the ngx_lua cosocket connection pool.

You can specify the max idle timeout (in ms) when the connection is in the pool and the maximal size of the pool every nginx worker process.

In case of success, returns `1`. In case of errors, returns `nil` with a string describing the error.

Only call this method in the place you would have called the `close` method instead. Calling this method will immediately turn the current `resty.mysql` object into the `closed` state. Any subsequent operations other than `connect()` on the current objet will return the `closed` error.

[Back to TOC](#table-of-contents)

get_reused_times
----------------
`syntax: times, err = db:get_reused_times()`

This method returns the (successfully) reused times for the current connection. In case of error, it returns `nil` and a string describing the error.

If the current connection does not come from the built-in connection pool, then this method always returns `0`, that is, the connection has never been reused (yet). If the connection comes from the connection pool, then the return value is always non-zero. So this method can also be used to determine if the current connection comes from the pool.

[Back to TOC](#table-of-contents)

close
-----
`syntax: ok, err = db:close()`

Closes the current mysql connection and returns the status.

In case of success, returns `1`. In case of errors, returns `nil` with a string describing the error.

[Back to TOC](#table-of-contents)

send_query
----------
`syntax: bytes, err = db:send_query(query)`

Sends the query to the remote MySQL server without waiting for its replies.

Returns the bytes successfully sent out in success and otherwise returns `nil` and a string describing the error.

You should use the [read_result](#read_result) method to read the MySQL replies afterwards.

[Back to TOC](#table-of-contents)

read_result
-----------
`syntax: res, err, errno, sqlstate = db:read_result()`

`syntax: res, err, errno, sqlstate = db:read_result(nrows)`

Reads in one result returned from the MySQL server.

It returns a Lua table (`res`) describing the MySQL `OK packet` or `result set packet` for the query result.

For queries corresponding to a result set, it returns an array holding all the rows. Each row holds key-value apirs for each data fields. For instance,

```lua
    {
        { name = "Bob", age = 32, phone = ngx.null },
        { name = "Marry", age = 18, phone = "10666372"}
    }
```

For queries that do not correspond to a result set, it returns a Lua table like this:

```lua
    {
        insert_id = 0,
        server_status = 2,
        warning_count = 1,
        affected_rows = 32,
        message = nil
    }
```

If more results are following the current result, a second `err` return value will be given the string `again`. One should always check this (second) return value and if it is `again`, then she should call this method again to retrieve more results. This usually happens when the original query contains multiple statements (separated by semicolon in the same query string) or calling a MySQL procedure. See also [Multi-Resultset Support](#multi-resultset-support).

In case of errors, this method returns at most 4 values: `nil`, `err`, `errcode`, and `sqlstate`. The `err` return value contains a string describing the error, the `errcode` return value holds the MySQL error code (a numerical value), and finally, the `sqlstate` return value contains the standard SQL error code that consists of 5 characters. Note that, the `errcode` and `sqlstate` might be `nil` if MySQL does not return them.

The optional argument `nrows` can be used to specify an approximate number of rows for the result set. This value can be used
to pre-allocate space in the resulting Lua table for the result set. By default, it takes the value 4.

[Back to TOC](#table-of-contents)

query
-----
`syntax: res, err, errcode, sqlstate = db:query(query)`

`syntax: res, err, errcode, sqlstate = db:query(query, nrows)`

This is a shortcut for combining the [send_query](#send_query) call and the first [read_result](#read_result) call.

You should always check if the `err` return value  is `again` in case of success because this method will only call [read_result](#read_result) only once for you. See also [Multi-Resultset Support](#multi-resultset-support).

[Back to TOC](#table-of-contents)

server_ver
----------
`syntax: str = db:server_ver()`

Returns the MySQL server version string, like `"5.1.64"`.

You should only call this method after successfully connecting to a MySQL server, otherwise `nil` will be returned.

[Back to TOC](#table-of-contents)

set_compact_arrays
------------------
`syntax: db:set_compact_arrays(boolean)`

Sets whether to use the "compact-arrays" structure for the resultsets returned by subsequent queries. See the `compact_arrays` option for the `connect` method for more details.

This method was first introduced in the `v0.09` release.

[Back to TOC](#table-of-contents)

SQL Literal Quoting
===================

It is always important to quote SQL literals properly to prevent SQL injection attacks. You can use the
[ngx.quote_sql_str](http://wiki.nginx.org/HttpLuaModule#ngx.quote_sql_str) function provided by ngx_lua to quote values.
Here is an example:

```lua
    local name = ngx.unescape_uri(ngx.var.arg_name)
    local quoted_name = ngx.quote_sql_str(name)
    local sql = "select * from users where name = " .. quoted_name
```

[Back to TOC](#table-of-contents)

Multi-Resultset Support
=======================

For a SQL query that produces multiple result-sets, it is always your duty to check the "again" error message returned by the [query](#query) or [read_result](#read_result) method calls, and keep pulling more result sets by calling the [read_result](#read_result) method until no "again" error message returned (or some other errors happen).

Below is a trivial example for this:

```lua
    local cjson = require "cjson"
    local mysql = require "resty.mysql"

    local db = mysql:new()
    local ok, err, errno, sqlstate = db:connect({
        host = "127.0.0.1",
        port = 3306,
        database = "world",
        user = "monty",
        password = "pass"})

    if not ok then
        ngx.log(ngx.ERR, "failed to connect: ", err, ": ", errno, " ", sqlstate)
        return ngx.exit(500)
    end

    res, err, errno, sqlstate = db:query("select 1; select 2; select 3;")
    if not res then
        ngx.log(ngx.ERR, "bad result #1: ", err, ": ", errno, ": ", sqlstate, ".")
        return ngx.exit(500)
    end

    ngx.say("result #1: ", cjson.encode(res))

    local i = 2
    while err == "again" do
        res, err, errno, sqlstate = db:read_result()
        if not res then
            ngx.log(ngx.ERR, "bad result #2: ", err, ": ", errno, ": ", sqlstate, ".")
            return ngx.exit(500)
        end

        ngx.say("result #", i, ": ", cjson.encode(res))
        i = i + 1
    end

    local ok, err = db:set_keepalive(10000, 50)
    if not ok then
        ngx.log(ngx.ERR, "failed to set keepalive: ", err)
        ngx.exit(500)
    end
```

This code snippet will produce the following response body data:

    result #1: [{"1":"1"}]
    result #2: [{"2":"2"}]
    result #3: [{"3":"3"}]

[Back to TOC](#table-of-contents)

Debugging
=========

It is usually convenient to use the [lua-cjson](http://www.kyne.com.au/~mark/software/lua-cjson.php) library to encode the return values of the MySQL query methods to JSON. For example,

```lua
    local cjson = require "cjson"
    ...
    local res, err, errcode, sqlstate = db:query("select * from cats")
    if res then
        print("res: ", cjson.encode(res))
    end
```

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

* This library cannot be used in code contexts like init_by_lua*, set_by_lua*, log_by_lua*, and
header_filter_by_lua* where the ngx_lua cosocket API is not available.
* The `resty.mysql` object instance cannot be stored in a Lua variable at the Lua module level,
because it will then be shared by all the concurrent requests handled by the same nginx
 worker process (see
http://wiki.nginx.org/HttpLuaModule#Data_Sharing_within_an_Nginx_Worker ) and
result in bad race conditions when concurrent requests are trying to use the same `resty.mysql` instance.
You should always initiate `resty.mysql` objects in function local
variables or in the `ngx.ctx` table. These places all have their own data copies for
each request.

[Back to TOC](#table-of-contents)

Installation
============

If you are using the ngx_openresty bundle (http://openresty.org ), then
you do not need to do anything because it already includes and enables
lua-resty-mysql by default. And you can just use it in your Lua code,
as in

```lua
    local mysql = require "resty.mysql"
    ...
```

If you are using your own nginx + ngx_lua build, then you need to configure
the lua_package_path directive to add the path of your lua-resty-mysql source
tree to ngx_lua's LUA_PATH search path, as in

```nginx
    # nginx.conf
    http {
        lua_package_path "/path/to/lua-resty-mysql/lib/?.lua;;";
        ...
    }
```

Ensure that the system account running your Nginx ''worker'' proceses have
enough permission to read the `.lua` file.

[Back to TOC](#table-of-contents)

Community
=========

[Back to TOC](#table-of-contents)

English Mailing List
--------------------

The [openresty-en](https://groups.google.com/group/openresty-en) mailing list is for English speakers.

[Back to TOC](#table-of-contents)

Chinese Mailing List
--------------------

The [openresty](https://groups.google.com/group/openresty) mailing list is for Chinese speakers.

[Back to TOC](#table-of-contents)

Bugs and Patches
================

Please submit bug reports, wishlists, or patches by

1. creating a ticket on the [GitHub Issue Tracker](http://github.com/agentzh/lua-resty-mysql/issues),
1. or posting to the [OpenResty community](http://wiki.nginx.org/HttpLuaModule#Community).

[Back to TOC](#table-of-contents)

TODO
====

* improve the MySQL connection pool support.
* implement the MySQL binary row data packets.
* implement MySQL's old pre-4.0 authentication method.
* implement MySQL server prepare and execute packets.
* implement the data compression support in the protocol.

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
* the MySQL wired protocol specification: http://forge.mysql.com/wiki/MySQL_Internals_ClientServer_Protocol
* the [lua-resty-memcached](https://github.com/agentzh/lua-resty-memcached) library
* the [lua-resty-redis](https://github.com/agentzh/lua-resty-redis) library
* the ngx_drizzle module: http://wiki.nginx.org/HttpDrizzleModule

[Back to TOC](#table-of-contents)

