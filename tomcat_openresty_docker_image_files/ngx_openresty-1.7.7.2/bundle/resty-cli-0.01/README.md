Name
====

resty-cli - Fancy command-line utilities for OpenResty.

Table of Contents
=================

* [Name](#name)
* [Description](#description)
* [Synopsis](#synopsis)
* [Installation](#installation)
* [Authors](#authors)
* [Copyright and License](#copyright-and-license)

Description
===========

The `resty` command-line utility can be used to run OpenResty's Lua scripts directly off the command-line just like the `lua` or `luajit` command-line utilities. It can be used to create various command-line utilities using OpenResty Lua.

This tool works by creating a head-less `nginx` insance,
disabling [daemon](http://nginx.org/en/docs/ngx_core_module.html#daemon), [master_process](http://nginx.org/en/docs/ngx_core_module.html#master_process), [access_log](http://nginx.org/en/docs/http/ngx_http_log_module.html#access_log), and other things it does
not need. No `server {}` is configured hence *no* listening sockets
are involved at all.

The Lua code is initiated by the [init_worker_by_lua](https://github.com/openresty/lua-nginx-module#init_worker_by_lua)
directive and run in the context of [ngx.timer](https://github.com/openresty/lua-nginx-module#ngxtimerat) callback. So all of
[ngx_lua](https://github.com/openresty/lua-nginx-module#readme)'s Lua APIs available in the [ngx.timer](https://github.com/openresty/lua-nginx-module#ngxtimerat) callback context are
also available in the `resty` utility. We may remove some of the
remaining limitations in the future though.

Synopsis
========

    $ export PATH=/usr/local/openresty/bin:$PATH

    $ which resty
    /usr/local/openresty/bin/resty

    $ resty -h
    resty [options] [lua-file [args]]

    Options:
        -c num      set maximal connection count (default: 64).
        -e prog     run the inlined Lua code in "prog".
        --help      print this help.
        -I dir      Add dir to the search paths for Lua libraries.
        --nginx     specify the nginx path (this option might be removed in the future).
        -V          print version numbers and nginx configurations.
        --valgrind  use valgrind to run the underyling nginx

        --valgrind-opts     pass extra options to valgrind

    For bug reporting instructions, please see:
    <http://openresty.org/#Community>

    $ resty -e 'print("hello")'
    hello

    $ time resty -e 'ngx.sleep(3) print("done\n")'
    done

    real 0m3.085s
    user 0m0.071s
    sys 0m0.010s

    $ resty -e 'ngx.say(ngx.md5("hello"))'
    5d41402abc4b2a76b9719d911017c592

    $ resty -e 'io.stderr:write("hello world\n")' > /dev/null
    hello world

To run the code example in [lua-resty-mysql](https://github.com/openresty/lua-resty-mysql)'s
"Synopsis" documentation section:

    $ resty mysql-example.lua
    connected to mysql.
    table cats created.
    3 rows inserted into table cats (last insert id: 1)
    result: [{"name":"Bob","id":"1"},{"name":"","id":"2"},{"name":null,"id":"3"}]

where `mysql-example.lua` is from [lua-resty-mysql](https://github.com/openresty/lua-resty-mysql)'s docs.

Stdin also works (in addition to stdout and stderr):

    $ resty -e 'print("got: ", io.stdin:read("*l"))'
    hiya
    got: hiya

where the first "hiya" line was entered from the keyboard.

"Light theads" also work:

    $ time resty -e 'local ths = {}
                     for i = 1, 3 do
                         ths[i] = ngx.thread.spawn(function ()
                                      ngx.sleep(3) ngx.say("done ", i)
                                  end)
                     end
                     for i = 1, #ths do ngx.thread.wait(ths[i]) end'
    done 1
    done 2
    done 3

    real 0m3.073s
    user 0m0.053s
    sys 0m0.015s

User command-line arguments are also passed:

    $ resty -e 'print(arg[1], ", ", arg[2])' hello world
    hello, world

To check version numbers:

    $ resty -V
    resty 0.01
    nginx version: openresty/1.7.7.2
    built by gcc 4.8.3 20140911 (Red Hat 4.8.3-7) (GCC)
    TLS SNI support enabled
    configure arguments: --prefix=/usr/local/openresty/nginx ...

Installation
============

The `resty` command-line utility is bundled in OpenResty 1.7.7.2+ and is
installed under `<openresty-prefix>/bin/` by default.
If the OpenResty prefix is the default value (`/usr/local/openresty`),
then you can just add `/usr/local/openresty/bin` to your PATH environment:

    export PATH=/usr/local/openresty/bin:$PATH

[Back to TOC](#table-of-contents)

Authors
=======

* Yichun Zhang (agentzh) <agentzh@gmail.com>, CloudFlare Inc.

* Guanlan Dai <guanlan@cloudflare.com>, CloudFlare Inc.

[Back to TOC](#table-of-contents)

Copyright and License
=====================

This module is licensed under the BSD license.

Copyright (C) 2014, by Yichun "agentzh" Zhang (章亦春) <agentzh@gmail.com>, CloudFlare Inc.
Copyright (C) 2014, by Guanlan Dai <guanlan@cloudflare.com>, CloudFlare Inc.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

[Back to TOC](#table-of-contents)

