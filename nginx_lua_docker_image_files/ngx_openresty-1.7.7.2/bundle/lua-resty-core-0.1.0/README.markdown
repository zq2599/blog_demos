Name
====

lua-resty-core - New FFI-based Lua API for the ngx_lua module

Table of Contents
=================

* [Name](#name)
* [Status](#status)
* [Synopsis](#synopsis)
* [Description](#description)
* [Prerequisites](#prerequisites)
* [API Implemented](#api-implemented)
    * [resty.core.hash](#restycorehash)
    * [resty.core.base64](#restycorebase64)
    * [resty.core.uri](#restycoreuri)
    * [resty.core.regex](#restycoreregex)
    * [resty.core.exit](#restycoreexit)
    * [resty.core.shdict](#restycoreshdict)
    * [resty.core.var](#restycorevar)
    * [resty.core.ctx](#restycorectx)
    * [resty.core.request](#restycorerequest)
    * [resty.core.response](#restycoreresponse)
    * [resty.core.misc](#restycoremisc)
    * [resty.core.time](#restycoretime)
    * [resty.core.worker](#restycoreworker)
* [Caveat](#caveat)
* [TODO](#todo)
* [Author](#author)
* [Copyright and License](#copyright-and-license)
* [See Also](#see-also)

Status
======

This library is production ready and under active development.

Synopsis
========

```nginx
    # nginx.conf

    http {
        # you do NOT need to configure the following line when you
        # are using the ngx_openresty bundle 1.4.3.9+.
        lua_package_path "/path/to/lua-resty-core/lib/?.lua;;";

        init_by_lua '
            require "resty.core"
        ';

        ...
    }
```

Description
===========

This pure Lua library reimplements part of the ngx_lua's
[Nginx API for Lua](https://github.com/chaoslawful/lua-nginx-module#nginx-api-for-lua)
with LuaJIT FFI and installs the new FFI-based Lua API into the ngx.* and ndk.* namespaces
used by the ngx_lua module.

The FFI-based Lua API can work with LuaJIT's JIT compiler. ngx_lua's default API is based on the standard Lua C API, which will never be JIT compiled and the user Lua code is always interpreted (slowly).

[Back to TOC](#table-of-contents)

Prerequisites
=============

* LuaJIT 2.1 (for now, it is the v2.1 git branch in the official luajit-2.0 git repository: http://luajit.org/download.html )
* [ngx_lua](https://github.com/chaoslawful/lua-nginx-module) v0.9.11 or later.
* [lua-resty-lrucache](https://github.com/openresty/lua-resty-lrucache)

Note that if you are using the ngx_openresty bundle version 1.4.3.9, then just build it with `--with-luajit` and you have everything including this library itself. Since ngx_openresty 1.5.8.1, the `--with-luajit` option is now the default and you no longer need to explicitly specify the `--with-luajit` option.

[Back to TOC](#table-of-contents)

API Implemented
===============

[Back to TOC](#table-of-contents)

## resty.core.hash

* [ngx.md5](https://github.com/chaoslawful/lua-nginx-module#ngxmd5)
* [ngx.md5_bin](https://github.com/chaoslawful/lua-nginx-module#ngxmd5_bin)
* [ngx.sha1_bin](https://github.com/chaoslawful/lua-nginx-module#ngxsha1_bin)

[Back to TOC](#table-of-contents)

## resty.core.base64

* [ngx.encode_base64](https://github.com/chaoslawful/lua-nginx-module#ngxencode_base64)
* [ngx.decode_base64](https://github.com/chaoslawful/lua-nginx-module#ngxdecode_base64)

[Back to TOC](#table-of-contents)

## resty.core.uri

* [ngx.escape_uri](https://github.com/chaoslawful/lua-nginx-module#ngxescape_uri)
* [ngx.unescape_uri](https://github.com/chaoslawful/lua-nginx-module#ngxunescape_uri)

[Back to TOC](#table-of-contents)

## resty.core.regex

* [ngx.re.match](https://github.com/chaoslawful/lua-nginx-module#ngxrematch)
* [ngx.re.find](https://github.com/chaoslawful/lua-nginx-module#ngxrefind)
* [ngx.re.sub](https://github.com/chaoslawful/lua-nginx-module#ngxresub)
* [ngx.re.gsub](https://github.com/chaoslawful/lua-nginx-module#ngxregsub)

[Back to TOC](#table-of-contents)

## resty.core.exit

* [ngx.exit](https://github.com/chaoslawful/lua-nginx-module#ngxexit)

[Back to TOC](#table-of-contents)

## resty.core.shdict

* [ngx.shared.DICT.get](https://github.com/chaoslawful/lua-nginx-module#ngxshareddictget)
* [ngx.shared.DICT.get_stale](https://github.com/chaoslawful/lua-nginx-module#ngxshareddictget_stale)
* [ngx.shared.DICT.incr](https://github.com/chaoslawful/lua-nginx-module#ngxshareddictincr)
* [ngx.shared.DICT.set](https://github.com/chaoslawful/lua-nginx-module#ngxshareddictset)
* [ngx.shared.DICT.safe_set](https://github.com/chaoslawful/lua-nginx-module#ngxshareddictsafe_set)
* [ngx.shared.DICT.add](https://github.com/chaoslawful/lua-nginx-module#ngxshareddictadd)
* [ngx.shared.DICT.safe_add](https://github.com/chaoslawful/lua-nginx-module#ngxshareddictsafe_add)
* [ngx.shared.DICT.replace](https://github.com/chaoslawful/lua-nginx-module#ngxshareddictreplace)
* [ngx.shared.DICT.delete](https://github.com/chaoslawful/lua-nginx-module#ngxshareddictdelete)
* [ngx.shared.DICT.flush_all](https://github.com/chaoslawful/lua-nginx-module#ngxshareddictflush_all)

[Back to TOC](#table-of-contents)

## resty.core.var

* [ngx.var.VARIABLE](https://github.com/chaoslawful/lua-nginx-module#ngxvarvariable)

[Back to TOC](#table-of-contents)

## resty.core.ctx

* [ngx.ctx](https://github.com/chaoslawful/lua-nginx-module#ngxctx)

[Back to TOC](#table-of-contents)

## resty.core.request

* [ngx.req.get_headers](https://github.com/chaoslawful/lua-nginx-module#ngxreqget_headers)
* [ngx.req.get_uri_args](https://github.com/chaoslawful/lua-nginx-module#ngxreqget_uri_args)
* [ngx.req.start_time](https://github.com/openresty/lua-nginx-module#ngxreqstart_time)
* [ngx.req.get_method](https://github.com/openresty/lua-nginx-module#ngxreqget_method)
* [ngx.req.set_method](https://github.com/openresty/lua-nginx-module#ngxreqset_method)
* [ngx.req.set_header](https://github.com/openresty/lua-nginx-module#ngxreqset_header)  (partial: table-typed header values not supported yet)
* [ngx.req.clear_header](https://github.com/openresty/lua-nginx-module#ngxreqclear_header)

[Back to TOC](#table-of-contents)

## resty.core.response

* [ngx.header.HEADER](https://github.com/chaoslawful/lua-nginx-module#ngxheaderheader)

[Back to TOC](#table-of-contents)

## resty.core.misc

* [ngx.status](https://github.com/chaoslawful/lua-nginx-module#ngxstatus)
* [ngx.is_subrequest](https://github.com/chaoslawful/lua-nginx-module#ngxis_subrequest)
* [ngx.headers_sent](https://github.com/chaoslawful/lua-nginx-module#ngxheaders_sent)

[Back to TOC](#table-of-contents)

## resty.core.time

* [ngx.time](https://github.com/openresty/lua-nginx-module#ngxtime)
* [ngx.now](https://github.com/openresty/lua-nginx-module#ngxnow)

[Back to TOC](#table-of-contents)

## resty.core.worker

* [ngx.worker.exiting](https://github.com/openresty/lua-nginx-module#ngxworkerexiting)
* [ngx.worker.pid](https://github.com/openresty/lua-nginx-module#ngxworkerpid)

[Back to TOC](#table-of-contents)

Caveat
======

If the user Lua code is not JIT compiled, then use of this library may
lead to performance drop in interpreted mode. You will only observe
speedup when you get a good part of your user Lua code JIT compiled.

[Back to TOC](#table-of-contents)

TODO
====

* Re-implement `ngx_lua`'s cosocket API with FFI.
* Re-implement `ngx_lua`'s `ngx.get_phase` API function with FFI.
* Re-implement `ngx_lua`'s `ngx.eof` and `ngx.flush` API functions with FFI.

[Back to TOC](#table-of-contents)

Author
======

Yichun "agentzh" Zhang (章亦春) <agentzh@gmail.com>, CloudFlare Inc.

[Back to TOC](#table-of-contents)

Copyright and License
=====================

This module is licensed under the BSD license.

Copyright (C) 2013-2015, by Yichun "agentzh" Zhang, CloudFlare Inc.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

[Back to TOC](#table-of-contents)

See Also
========
* the ngx_lua module: http://github.com/chaoslawful/lua-nginx-module#readme
* LuaJIT FFI: http://luajit.org/ext_ffi.html

[Back to TOC](#table-of-contents)

