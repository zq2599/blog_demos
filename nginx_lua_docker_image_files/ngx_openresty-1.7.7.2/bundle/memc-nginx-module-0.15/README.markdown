<!---
Don't edit this file manually! Instead you should generate it by using:
    wiki2markdown.pl doc/HttpMemcModule.wiki
-->

Name
====

**ngx_memc** - An extended version of the standard memcached module that supports set, add, delete, and many more memcached commands.

*This module is not distributed with the Nginx source.* See [the installation instructions](#installation).

Table of Contents
=================

* [Version](#version)
* [Synopsis](#synopsis)
* [Description](#description)
    * [Keep-alive connections to memcached servers](#keep-alive-connections-to-memcached-servers)
    * [How it works](#how-it-works)
* [Memcached commands supported](#memcached-commands-supported)
    * [get $memc_key](#get-memc_key)
    * [set $memc_key $memc_flags $memc_exptime $memc_value](#set-memc_key-memc_flags-memc_exptime-memc_value)
    * [add $memc_key $memc_flags $memc_exptime $memc_value](#add-memc_key-memc_flags-memc_exptime-memc_value)
    * [replace $memc_key $memc_flags $memc_exptime $memc_value](#replace-memc_key-memc_flags-memc_exptime-memc_value)
    * [append $memc_key $memc_flags $memc_exptime $memc_value](#append-memc_key-memc_flags-memc_exptime-memc_value)
    * [prepend $memc_key $memc_flags $memc_exptime $memc_value](#prepend-memc_key-memc_flags-memc_exptime-memc_value)
    * [delete $memc_key](#delete-memc_key)
    * [delete $memc_key $memc_exptime](#delete-memc_key-memc_exptime)
    * [incr $memc_key $memc_value](#incr-memc_key-memc_value)
    * [decr $memc_key $memc_value](#decr-memc_key-memc_value)
    * [flush_all](#flush_all)
    * [flush_all $memc_exptime](#flush_all-memc_exptime)
    * [stats](#stats)
    * [version](#version)
* [Directives](#directives)
    * [memc_pass](#memc_pass)
    * [memc_cmds_allowed](#memc_cmds_allowed)
    * [memc_flags_to_last_modified](#memc_flags_to_last_modified)
    * [memc_connect_timeout](#memc_connect_timeout)
    * [memc_send_timeout](#memc_send_timeout)
    * [memc_read_timeout](#memc_read_timeout)
    * [memc_buffer_size](#memc_buffer_size)
    * [memc_ignore_client_abort](#memc_ignore_client_abort)
* [Installation](#installation)
    * [For Developers](#for-developers)
* [Compatibility](#compatibility)
* [Community](#community)
    * [English Mailing List](#english-mailing-list)
    * [Chinese Mailing List](#chinese-mailing-list)
* [Report Bugs](#report-bugs)
* [Source Repository](#source-repository)
* [Changes](#changes)
* [Test Suite](#test-suite)
* [TODO](#todo)
* [Getting involved](#getting-involved)
* [Author](#author)
* [Copyright & License](#copyright--license)
* [See Also](#see-also)

Version
=======

This document describes ngx_memc [v0.15](http://github.com/openresty/memc-nginx-module/tags) released on 8 July 2014.

Synopsis
========

```nginx

# GET /foo?key=dog
#
# POST /foo?key=cat
# Cat's value...
#
# PUT /foo?key=bird
# Bird's value...
#
# DELETE /foo?key=Tiger
location /foo {
    set $memc_key $arg_key;

    # $memc_cmd defaults to get for GET,
    #   add for POST, set for PUT, and
    #   delete for the DELETE request method.

    memc_pass 127.0.0.1:11211;
}
```

```nginx

# GET /bar?cmd=get&key=cat
#
# POST /bar?cmd=set&key=dog
# My value for the "dog" key...
#
# DELETE /bar?cmd=delete&key=dog
# GET /bar?cmd=delete&key=dog
location /bar {
    set $memc_cmd $arg_cmd;
    set $memc_key $arg_key;
    set $memc_flags $arg_flags; # defaults to 0
    set $memc_exptime $arg_exptime; # defaults to 0

    memc_pass 127.0.0.1:11211;
}
```

```nginx

# GET /bar?cmd=get&key=cat
# GET /bar?cmd=set&key=dog&val=animal&flags=1234&exptime=2
# GET /bar?cmd=delete&key=dog
# GET /bar?cmd=flush_all
location /bar {
    set $memc_cmd $arg_cmd;
    set $memc_key $arg_key;
    set $memc_value $arg_val;
    set $memc_flags $arg_flags; # defaults to 0
    set $memc_exptime $arg_exptime; # defaults to 0

    memc_cmds_allowed get set add delete flush_all;

    memc_pass 127.0.0.1:11211;
}
```

```nginx

  http {
    ...
    upstream backend {
       server 127.0.0.1:11984;
       server 127.0.0.1:11985;
    }
    server {
        location /stats {
            set $memc_cmd stats;
            memc_pass backend;
        }
        ...
    }
  }
  ...
```

```nginx

# read the memcached flags into the Last-Modified header
# to respond 304 to conditional GET
location /memc {
    set $memc_key $arg_key;

    memc_pass 127.0.0.1:11984;

    memc_flags_to_last_modified on;
}
```

```nginx

location /memc {
    set $memc_key foo;
    set $memc_cmd get;

    # access the unix domain socket listend by memcached
    memc_pass unix:/tmp/memcached.sock;
}
```

Description
===========

This module extends the standard [memcached module](http://nginx.org/en/docs/http/ngx_http_memcached_module.html) to support almost the whole [memcached ascii protocol](http://code.sixapart.com/svn/memcached/trunk/server/doc/protocol.txt).

It allows you to define a custom [REST](http://en.wikipedia.org/wiki/REST) interface to your memcached servers or access memcached in a very efficient way from within the nginx server by means of subrequests or [independent fake requests](http://github.com/srlindsay/nginx-independent-subrequest).

This module is not supposed to be merged into the Nginx core because I've used [Ragel](http://www.complang.org/ragel/) to generate the memcached response parsers (in C) for joy :)

If you are going to use this module to cache location responses out of the box, try [srcache-nginx-module](http://github.com/openresty/srcache-nginx-module) with this module to achieve that.

When used in conjunction with [lua-nginx-module](http://github.com/openresty/lua-nginx-module), it is recommended to use the [lua-resty-memcached](http://github.com/openresty/lua-resty-memcached) library instead of this module though, because the former is much more flexible and memory-efficient.

[Back to TOC](#table-of-contents)

Keep-alive connections to memcached servers
-------------------------------------------

You need the (now standard) [HttpUpstreamKeepaliveModule](http://wiki.nginx.org/HttpUpstreamKeepaliveModule) together with this module for keep-alive TCP connections to your backend memcached servers.

Here's a sample configuration:

```nginx

  http {
    upstream backend {
      server 127.0.0.1:11211;

      # a pool with at most 1024 connections
      # and do not distinguish the servers:
      keepalive 1024;
    }

    server {
        ...
        location /memc {
            set $memc_cmd get;
            set $memc_key $arg_key;
            memc_pass backend;
        }
    }
  }
```

[Back to TOC](#table-of-contents)

How it works
------------

It implements the memcached TCP protocol all by itself, based upon the `upstream` mechanism. Everything involving I/O is non-blocking.

The module itself does not keep TCP connections to the upstream memcached servers across requests, just like other upstream modules. For a working solution, see section [Keep-alive connections to memcached servers](#keep-alive-connections-to-memcached-servers).

[Back to TOC](#table-of-contents)

Memcached commands supported
============================

The memcached storage commands [set](#set-memc_key-memc_flags-memc_exptime-memc_value), [add](#add-memc_key-memc_flags-memc_exptime-memc_value), [replace](#replace-memc_key-memc_flags-memc_exptime-memc_value), [prepend](#prepend-memc_key-memc_flags-memc_exptime-memc_value), and [append](#append-memc_key-memc_flags-memc_exptime-memc_value) uses the `$memc_key` as the key, `$memc_exptime` as the expiration time (or delay) (defaults to 0), `$memc_flags` as the flags (defaults to 0), to build the corresponding memcached queries.

If `$memc_value` is not defined at all, then the request body will be used as the value of the `$memc_value` except for the [incr](#incr-memc_key-memc_value) and [decr](#decr-memc_key-memc_value) commands. Note that if `$memc_value` is defined as an empty string (`""`), that empty string will still be used as the value as is.

The following memcached commands have been implemented and tested (with their parameters marked by corresponding
nginx variables defined by this module):

[Back to TOC](#table-of-contents)

get $memc_key
-------------

Retrieves the value using a key.

```nginx

  location /foo {
      set $memc_cmd 'get';
      set $memc_key 'my_key';
      
      memc_pass 127.0.0.1:11211;
      
      add_header X-Memc-Flags $memc_flags;
  }
```

Returns `200 OK` with the value put into the response body if the key is found, or `404 Not Found` otherwise. The `flags` number will be set into the `$memc_flags` variable so it's often desired to put that info into the response headers by means of the standard [add_header directive](http://nginx.org/en/docs/http/ngx_http_headers_module.html#add_header).

It returns `502` for `ERROR`, `CLIENT_ERROR`, or `SERVER_ERROR`.

[Back to TOC](#table-of-contents)

set $memc_key $memc_flags $memc_exptime $memc_value
---------------------------------------------------

To use the request body as the memcached value, just avoid setting the `$memc_value` variable:

```nginx

  # POST /foo
  # my value...
  location /foo {
      set $memc_cmd 'set';
      set $memc_key 'my_key';
      set $memc_flags 12345;
      set $memc_exptime 24;
      
      memc_pass 127.0.0.1:11211;
  }
```

Or let the `$memc_value` hold the value:

```nginx

  location /foo {
      set $memc_cmd 'set';
      set $memc_key 'my_key';
      set $memc_flags 12345;
      set $memc_exptime 24;
      set $memc_value 'my_value';

      memc_pass 127.0.0.1:11211;
  }
```

Returns `201 Created` if the upstream memcached server replies `STORED`, `200` for `NOT_STORED`, `404` for `NOT_FOUND`, `502` for `ERROR`, `CLIENT_ERROR`, or `SERVER_ERROR`.

The original memcached responses are returned as the response body except for `404 NOT FOUND`.

[Back to TOC](#table-of-contents)

add $memc_key $memc_flags $memc_exptime $memc_value
---------------------------------------------------

Similar to the [set command](#set-memc_key-memc_flags-memc_exptime-memc_value).

[Back to TOC](#table-of-contents)

replace $memc_key $memc_flags $memc_exptime $memc_value
-------------------------------------------------------

Similar to the [set command](#set-memc_key-memc_flags-memc_exptime-memc_value).

[Back to TOC](#table-of-contents)

append $memc_key $memc_flags $memc_exptime $memc_value
------------------------------------------------------

Similar to the [set command](#set-memc_key-memc_flags-memc_exptime-memc_value).

Note that at least memcached version 1.2.2 does not support the "append" and "prepend" commands. At least 1.2.4 and later versions seem to supports these two commands.

[Back to TOC](#table-of-contents)

prepend $memc_key $memc_flags $memc_exptime $memc_value
-------------------------------------------------------

Similar to the [append command](#append-memc_key-memc_flags-memc_exptime-memc_value).

[Back to TOC](#table-of-contents)

delete $memc_key
----------------

Deletes the memcached entry using a key.

```nginx

  location /foo
      set $memc_cmd delete;
      set $memc_key my_key;
      
      memc_pass 127.0.0.1:11211;
  }
```

Returns `200 OK` if deleted successfully, `404 Not Found` for `NOT_FOUND`, or `502` for `ERROR`, `CLIENT_ERROR`, or `SERVER_ERROR`.

The original memcached responses are returned as the response body except for `404 NOT FOUND`.

[Back to TOC](#table-of-contents)

delete $memc_key $memc_exptime
------------------------------

Similar to the [delete $memc_key](#delete-memc_key) command except it accepts an optional `expiration` time specified by the `$memc_exptime` variable.

This command is no longer available in the latest memcached version 1.4.4.

[Back to TOC](#table-of-contents)

incr $memc_key $memc_value
--------------------------

Increments the existing value of `$memc_key` by the amount specified by `$memc_value`:

```nginx

  location /foo {
      set $memc_key my_key;
      set $memc_value 2;
      memc_pass 127.0.0.1:11211;
  }
```

In the preceding example, every time we access `/foo` will cause the value of `my_key` increments by `2`.

Returns `200 OK` with the new value associated with that key as the response body if successful, or `404 Not Found` if the key is not found.

It returns `502` for `ERROR`, `CLIENT_ERROR`, or `SERVER_ERROR`.

[Back to TOC](#table-of-contents)

decr $memc_key $memc_value
--------------------------

Similar to [incr $memc_key $memc_value](#incr-memc_key-memc_value).

[Back to TOC](#table-of-contents)

flush_all
---------

Mark all the keys on the memcached server as expired:

```nginx

  location /foo {
      set $memc_cmd flush_all;
      memc_pass 127.0.0.1:11211;
  }
```

[Back to TOC](#table-of-contents)

flush_all $memc_exptime
-----------------------

Just like [flush_all](#flush_all) but also accepts an expiration time specified by the `$memc_exptime` variable.

[Back to TOC](#table-of-contents)

stats
-----

Causes the memcached server to output general-purpose statistics and settings

```nginx

  location /foo {
      set $memc_cmd stats;
      memc_pass 127.0.0.1:11211;
  }
```

Returns `200 OK` if the request succeeds, or 502 for `ERROR`, `CLIENT_ERROR`, or `SERVER_ERROR`.

The raw `stats` command output from the upstream memcached server will be put into the response body. 

[Back to TOC](#table-of-contents)

version
-------

Queries the memcached server's version number:

```nginx

  location /foo {
      set $memc_cmd version;
      memc_pass 127.0.0.1:11211;
  }
```

Returns `200 OK` if the request succeeds, or 502 for `ERROR`, `CLIENT_ERROR`, or `SERVER_ERROR`.

The raw `version` command output from the upstream memcached server will be put into the response body.

[Back to TOC](#table-of-contents)

Directives
==========

All the standard [memcached module](http://nginx.org/en/docs/http/ngx_http_memcached_module.html) directives in nginx 0.8.28 are directly inherited, with the `memcached_` prefixes replaced by `memc_`. For example, the `memcached_pass` directive is spelled `memc_pass`.

Here we only document the most important two directives (the latter is a new directive introduced by this module).

[Back to TOC](#table-of-contents)

memc_pass
---------

**syntax:** *memc_pass &lt;memcached server IP address&gt;:&lt;memcached server port&gt;*

**syntax:** *memc_pass &lt;memcached server hostname&gt;:&lt;memcached server port&gt;*

**syntax:** *memc_pass &lt;upstream_backend_name&gt;*

**syntax:** *memc_pass unix:&lt;path_to_unix_domain_socket&gt;*

**default:** *none*

**context:** *http, server, location, if*

**phase:** *content*

Specify the memcached server backend.

[Back to TOC](#table-of-contents)

memc_cmds_allowed
-----------------
**syntax:** *memc_cmds_allowed &lt;cmd&gt;...*

**default:** *none*

**context:** *http, server, location, if*

Lists memcached commands that are allowed to access. By default, all the memcached commands supported by this module are accessible.
An example is

```nginx

   location /foo {
       set $memc_cmd $arg_cmd;
       set $memc_key $arg_key;
       set $memc_value $arg_val;
       
       memc_pass 127.0.0.1:11211;
        
       memc_cmds_allowed get;
   }
```

[Back to TOC](#table-of-contents)

memc_flags_to_last_modified
---------------------------
**syntax:** *memc_flags_to_last_modified on|off*

**default:** *off*

**context:** *http, server, location, if*

Read the memcached flags as epoch seconds and set it as the value of the `Last-Modified` header. For conditional GET, it will signal nginx to return `304 Not Modified` response to save bandwidth.

[Back to TOC](#table-of-contents)

memc_connect_timeout
--------------------
**syntax:** *memc_connect_timeout &lt;time&gt;*

**default:** *60s*

**context:** *http, server, location*

The timeout for connecting to the memcached server, in seconds by default.

It's wise to always explicitly specify the time unit to avoid confusion. Time units supported are "s"(seconds), "ms"(milliseconds), "y"(years), "M"(months), "w"(weeks), "d"(days), "h"(hours), and "m"(minutes).

This time must be less than 597 hours.

[Back to TOC](#table-of-contents)

memc_send_timeout
-----------------
**syntax:** *memc_send_timeout &lt;time&gt;*

**default:** *60s*

**context:** *http, server, location*

The timeout for sending TCP requests to the memcached server, in seconds by default.

It's wise to always explicitly specify the time unit to avoid confusion. Time units supported are "s"(seconds), "ms"(milliseconds), "y"(years), "M"(months), "w"(weeks), "d"(days), "h"(hours), and "m"(minutes).

This time must be less than 597 hours.

[Back to TOC](#table-of-contents)

memc_read_timeout
-----------------
**syntax:** *memc_read_timeout &lt;time&gt;*

**default:** *60s*

**context:** *http, server, location*

The timeout for reading TCP responses from the memcached server, in seconds by default.

It's wise to always explicitly specify the time unit to avoid confusion. Time units supported are "s"(seconds), "ms"(milliseconds), "y"(years), "M"(months), "w"(weeks), "d"(days), "h"(hours), and "m"(minutes).

This time must be less than 597 hours.

[Back to TOC](#table-of-contents)

memc_buffer_size
----------------
**syntax:** *memc_buffer_size &lt;size&gt;*

**default:** *4k/8k*

**context:** *http, server, location*

This buffer size is used for the memory buffer to hold

* the complete response for memcached commands other than `get`,
* the complete response header (i.e., the first line of the response) for the `get` memcached command.

This default size is the page size, may be `4k` or `8k`.

[Back to TOC](#table-of-contents)

memc_ignore_client_abort
------------------------
**syntax:** *memc_ignore_client_abort on|off*

**default:** *off*

**context:** *location*

Determines whether the connection with a memcache server should be closed when a client closes a connection without waiting for a response.

This directive was first added in the `v0.14` release.

[Back to TOC](#table-of-contents)

Installation
============

You're recommended to install this module (as well as the Nginx core and many other goodies) via the [ngx_openresty bundle](http://openresty.org). See the [installation steps](http://openresty.org/#Installation) for `ngx_openresty`.

Alternatively, you can compile this module into the standard Nginx source distribution by hand:

Grab the nginx source code from [nginx.org](http://nginx.org/), for example,
the version 1.7.2 (see [nginx compatibility](#compatibility)), and then build the source with this module:

```bash

wget 'http://nginx.org/download/nginx-1.7.2.tar.gz'
tar -xzvf nginx-1.7.2.tar.gz
cd nginx-1.7.2/

# Here we assume you would install you nginx under /opt/nginx/.
./configure --prefix=/opt/nginx \
    --add-module=/path/to/memc-nginx-module
 
make -j2
make install
```

Download the latest version of the release tarball of this module from [memc-nginx-module file list](http://github.com/openresty/memc-nginx-module/tags).

[Back to TOC](#table-of-contents)

For Developers
--------------

The memached response parsers were generated by [Ragel](http://www.complang.org/ragel/). If you want to
regenerate the parser's C file, i.e., [src/ngx_http_memc_response.c](http://github.com/openresty/memc-nginx-module/blob/master/src/ngx_http_memc_response.c), use the following command from the root of the memc module's source tree:

```bash

$ ragel -G2 src/ngx_http_memc_response.rl
```

[Back to TOC](#table-of-contents)

Compatibility
=============

The following versions of Nginx should work with this module:

* **1.7.x**                       (last tested: 1.7.2)
* **1.5.x**                       (last tested: 1.5.12)
* **1.4.x**                       (last tested: 1.4.4)
* **1.2.x**                       (last tested: 1.2.9)
* **1.1.x**                       (last tested: 1.1.5)
* **1.0.x**                       (last tested: 1.0.10)
* **0.9.x**                       (last tested: 0.9.4)
* **0.8.x**                       (last tested: 0.8.54)
* **0.7.x >= 0.7.46**             (last tested: 0.7.68)

It's worth mentioning that some 0.7.x versions older than 0.7.46 might also work, but I can't easily test them because the test suite makes extensive use of the [echo module](http://github.com/openresty/echo-nginx-module)'s [echo_location directive](http://github.com/openresty/echo-nginx-module#echo_location), which requires at least nginx 0.7.46 :)

Earlier versions of Nginx like 0.6.x and 0.5.x will *not* work.

If you find that any particular version of Nginx above 0.7.46 does not work with this module, please consider [reporting a bug](#report-bugs).

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

Report Bugs
===========

Although a lot of effort has been put into testing and code tuning, there must be some serious bugs lurking somewhere in this module. So whenever you are bitten by any quirks, please don't hesitate to

1. create a ticket on the [issue tracking interface](http://github.com/openresty/memc-nginx-module/issues) provided by GitHub,
1. or send a bug report or even patches to the [nginx mailing list](http://mailman.nginx.org/mailman/listinfo/nginx).

[Back to TOC](#table-of-contents)

Source Repository
=================

Available on github at [openresty/memc-nginx-module](http://github.com/openresty/memc-nginx-module).

[Back to TOC](#table-of-contents)

Changes
=======

The changes of every release of this module can be obtained from the ngx_openresty bundle's change logs:

<http://openresty.org/#Changes>

[Back to TOC](#table-of-contents)

Test Suite
==========

This module comes with a Perl-driven test suite. The [test cases](http://github.com/openresty/memc-nginx-module/tree/master/t/) are
[declarative](http://github.com/openresty/memc-nginx-module/blob/master/t/storage.t) too. Thanks to the [Test::Base](http://search.cpan.org/perldoc?Test::Base) module in the Perl world.

To run it on your side:

```bash

$ PATH=/path/to/your/nginx-with-memc-module:$PATH prove -r t
```

You need to terminate any Nginx processes before running the test suite if you have changed the Nginx server binary.

Either [LWP::UserAgent](http://search.cpan.org/perldoc?LWP::UserAgent) or [IO::Socket](http://search.cpan.org/perldoc?IO::Socket) is used by the [test scaffold](http://github.com/openresty/memc-nginx-module/blob/master/test/lib/Test/Nginx/LWP.pm).

Because a single nginx server (by default, `localhost:1984`) is used across all the test scripts (`.t` files), it's meaningless to run the test suite in parallel by specifying `-jN` when invoking the `prove` utility.

You should also keep a memcached server listening on the `11211` port at localhost before running the test suite.

Some parts of the test suite requires modules [rewrite](http://nginx.org/en/docs/http/ngx_http_rewrite_module.html) and [echo](http://github.com/openresty/echo-nginx-module) to be enabled as well when building Nginx.

[Back to TOC](#table-of-contents)

TODO
====

* add support for the memcached commands `cas`, `gets` and `stats $memc_value`.
* add support for the `noreply` option.

[Back to TOC](#table-of-contents)

Getting involved
================

You'll be very welcomed to submit patches to the [author](#author) or just ask for a commit bit to the [source repository](#source-repository) on GitHub.

[Back to TOC](#table-of-contents)

Author
======

Yichun "agentzh" Zhang (章亦春) *&lt;agentzh@gmail.com&gt;*, CloudFlare Inc.

This wiki page is also maintained by the author himself, and everybody is encouraged to improve this page as well.

[Back to TOC](#table-of-contents)

Copyright & License
===================

The code base is borrowed directly from the standard [memcached module](http://nginx.org/en/docs/http/ngx_http_memcached_module.html) in the Nginx core. This part of code is copyrighted by Igor Sysoev and Nginx Inc.

Copyright (c) 2009-2013, Yichun "agentzh" Zhang (章亦春) <agentzh@gmail.com>, CloudFlare Inc.

This module is licensed under the terms of the BSD license.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

[Back to TOC](#table-of-contents)

See Also
========

* The original announcement email on the nginx mailing list: [ngx_memc: "an extended version of ngx_memcached that supports set, add, delete, and many more commands"](http://forum.nginx.org/read.php?2,28359)
* My slides demonstrating various ngx_memc usage: <http://agentzh.org/misc/slides/nginx-conf-scripting/nginx-conf-scripting.html#34> (use the arrow or pageup/pagedown keys on the keyboard to swith pages)
* The latest [memcached TCP protocol](http://code.sixapart.com/svn/memcached/trunk/server/doc/protocol.txt).
* The [ngx_srcache](http://github.com/openresty/srcache-nginx-module) module
* The [lua-resty-memcached](https://github.com/openresty/lua-resty-memcached) library based on the [lua-nginx-module](http://github.com/openresty/lua-nginx-module) cosocket API.
* The standard [memcached](http://nginx.org/en/docs/http/ngx_http_memcached_module.html) module.
* The [echo module](http://github.com/openresty/echo-nginx-module) for Nginx module's automated testing.
* The standard [headers](http://nginx.org/en/docs/http/ngx_http_headers_module.html) module and the 3rd-parth [headers-more](http://github.com/openresty/headers-more-nginx-module) module.

