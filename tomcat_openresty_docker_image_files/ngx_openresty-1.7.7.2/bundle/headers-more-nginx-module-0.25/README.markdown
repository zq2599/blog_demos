<!---
Don't edit this file manually! Instead you should generate it by using:
    wiki2markdown.pl doc/HttpHeadersMoreModule.wiki
-->

Name
====

**ngx_headers_more** - Set and clear input and output headers...more than "add"!

*This module is not distributed with the Nginx source.* See [the installation instructions](#installation).

Table of Contents
=================

* [Version](#version)
* [Synopsis](#synopsis)
* [Description](#description)
* [Directives](#directives)
    * [more_set_headers](#more_set_headers)
    * [more_clear_headers](#more_clear_headers)
    * [more_set_input_headers](#more_set_input_headers)
    * [more_clear_input_headers](#more_clear_input_headers)
* [Limitations](#limitations)
* [Installation](#installation)
* [Compatibility](#compatibility)
* [Community](#community)
    * [English Mailing List](#english-mailing-list)
    * [Chinese Mailing List](#chinese-mailing-list)
* [Bugs and Patches](#bugs-and-patches)
* [Source Repository](#source-repository)
* [Changes](#changes)
* [Test Suite](#test-suite)
* [TODO](#todo)
* [Getting involved](#getting-involved)
* [Authors](#authors)
* [Copyright & License](#copyright--license)
* [See Also](#see-also)

Version
=======

This document describes headers-more-nginx-module [v0.25](http://github.com/agentzh/headers-more-nginx-module/tags) released on 10 January 2014.

Synopsis
========

```nginx

# set the Server output header
more_set_headers 'Server: my-server';

# set and clear output headers
location /bar {
    more_set_headers 'X-MyHeader: blah' 'X-MyHeader2: foo';
    more_set_headers -t 'text/plain text/css' 'Content-Type: text/foo';
    more_set_headers -s '400 404 500 503' -s 413 'Foo: Bar';
    more_clear_headers 'Content-Type';
    
    # your proxy_pass/memcached_pass/or any other config goes here...
}

# set output headers
location /type {
    more_set_headers 'Content-Type: text/plain';
    # ...
}

# set input headers
location /foo {
    set $my_host 'my dog';
    more_set_input_headers 'Host: $my_host';
    more_set_input_headers -t 'text/plain' 'X-Foo: bah';
   
    # now $host and $http_host have their new values...
    # ...
}

# replace input header X-Foo *only* if it already exists
more_set_input_headers -r 'X-Foo: howdy';
```

Description
===========

This module allows you to add, set, or clear any output
or input header that you specify.

This is an enhanced version of the standard
[headers](http://nginx.org/en/docs/http/ngx_http_headers_module.html) module because it provides more utilities like
resetting or clearing "builtin headers" like `Content-Type`,
`Content-Length`, and `Server`.

It also allows you to specify an optional HTTP status code
criteria using the `-s` option and an optional content
type criteria using the `-t` option while modifying the
output headers with the [more_set_headers](#more_set_headers) and
[more_clear_headers](#more_clear_headers) directives. For example,

```nginx

more_set_headers -s 404 -t 'text/html' 'X-Foo: Bar';
```

Input headers can be modified as well. For example

```nginx

location /foo {
    more_set_input_headers 'Host: foo' 'User-Agent: faked';
    # now $host, $http_host, $user_agent, and
    #   $http_user_agent all have their new values.
}
```

The option `-t` is also available in the
[more_set_input_headers](#more_set_input_headers) and
[more_clear_input_headers](#more_clear_input_headers) directives (for request header filtering) while the `-s` option
is not allowed.

Unlike the standard [headers](http://nginx.org/en/docs/http/ngx_http_headers_module.html) module, this module's directives will by
default apply to all the status codes, including `4xx` and `5xx`.

[Back to TOC](#table-of-contents)

Directives
==========

[Back to TOC](#table-of-contents)

more_set_headers
----------------
**syntax:** *more_set_headers [-t &lt;content-type list&gt;]... [-s &lt;status-code list&gt;]... &lt;new-header&gt;...*

**default:** *no*

**context:** *http, server, location, location if*

**phase:** *output-header-filter*

Adds or replaces the specified output headers when the response status code matches the codes specified by the `-s` option *AND* the response content type matches the types specified by the `-t` option.

If either `-s` or `-t` is not specified or has an empty list value, then no match is required. Therefore, the following directive set the `Server` output header to the custom value for *any* status code and *any* content type:

```nginx

  more_set_headers    "Server: my_server";
```

A single directive can set/add multiple output headers. For example

```nginx

  more_set_headers 'Foo: bar' 'Baz: bah';
```

Multiple occurrences of the options are allowed in a single directive. Their values will be merged together. For instance

```nginx

  more_set_headers -s 404 -s '500 503' 'Foo: bar';
```

is equivalent to

```nginx

  more_set_headers -s '404 500 503' 'Foo: bar';
```

The new header should be the one of the forms:

1. `Name: Value`
1. `Name: `
1. `Name`

The last two effectively clear the value of the header `Name`.

Nginx variables are allowed in header values. For example:

```nginx

   set $my_var "dog";
   more_set_headers "Server: $my_var";
```

But variables won't work in header keys due to performance considerations.

Multiple set/clear header directives are allowed in a single location, and they're executed sequentially.

Directives inherited from an upper level scope (say, http block or server blocks) are executed before the directives in the location block.

Note that although `more_set_headers` is allowed in *location* if blocks, it is *not* allowed in the *server* if blocks, as in

```nginx

  ?  # This is NOT allowed!
  ?  server {
  ?      if ($args ~ 'download') {
  ?          more_set_headers 'Foo: Bar';
  ?      }
  ?      ...
  ?  }
```

Behind the scene, use of this directive and its friend [more_clear_headers](#more_clear_headers) will (lazily) register an ouput header filter that modifies `r->headers_out` the way you specify.

[Back to TOC](#table-of-contents)

more_clear_headers
------------------
**syntax:** *more_clear_headers [-t &lt;content-type list&gt;]... [-s &lt;status-code list&gt;]... &lt;new-header&gt;...*

**default:** *no*

**context:** *http, server, location, location if*

**phase:** *output-header-filter*

Clears the specified output headers.

In fact,

```nginx

   more_clear_headers -s 404 -t 'text/plain' Foo Baz;
```

is exactly equivalent to

```nginx

   more_set_headers -s 404 -t 'text/plain' "Foo: " "Baz: ";
```

or

```nginx

   more_set_headers -s 404 -t 'text/plain' Foo Baz
```

See [more_set_headers](#more_set_headers) for more details.

Wildcard `*` can also be used to specify a header name pattern. For example, the following directive effectively clears *any* output headers starting by "`X-Hidden-`":

```nginx

more_clear_headers 'X-Hidden-*';
```

The `*` wildcard support was first introduced in [v0.09](#v009).

[Back to TOC](#table-of-contents)

more_set_input_headers
----------------------
**syntax:** *more_set_input_headers [-r] [-t &lt;content-type list&gt;]... &lt;new-header&gt;...*

**default:** *no*

**context:** *http, server, location, location if*

**phase:** *rewrite tail*

Very much like [more_set_headers](#more_set_headers) except that it operates on input headers (or request headers) and it only supports the `-t` option.

Note that using the `-t` option in this directive means filtering by the `Content-Type` *request* header, rather than the response header.

Behind the scene, use of this directive and its friend [more_clear_input_headers](#more_clear_input_headers) will (lazily) register a `rewrite phase` handler that modifies `r->headers_in` the way you specify. Note that it always run at the *end* of the `rewrite` so that it runs *after* the standard [rewrite module](http://nginx.org/en/docs/http/ngx_http_rewrite_module.html) and works in subrequests as well.

If the `-r` option is specified, then the headers will be replaced to the new values *only if* they already exist.

[Back to TOC](#table-of-contents)

more_clear_input_headers
------------------------
**syntax:** *more_clear_input_headers [-t &lt;content-type list&gt;]... &lt;new-header&gt;...*

**default:** *no*

**context:** *http, server, location, location if*

**phase:** *rewrite tail*

Clears the specified input headers.

In fact,

```nginx

   more_clear_input_headers -s 404 -t 'text/plain' Foo Baz;
```

is exactly equivalent to

```nginx

   more_set_input_headers -s 404 -t 'text/plain' "Foo: " "Baz: ";
```

or

```nginx

   more_set_input_headers -s 404 -t 'text/plain' Foo Baz
```

See [more_set_input_headers](#more_set_input_headers) for more details.

[Back to TOC](#table-of-contents)

Limitations
===========

* Unlike the standard [headers](http://nginx.org/en/docs/http/ngx_http_headers_module.html) module, this module does not automatically take care of the constraint among the `Expires`, `Cache-Control`, and `Last-Modified` headers. You have to get them right yourself or use the [headers](http://nginx.org/en/docs/http/ngx_http_headers_module.html) module together with this module.
* You cannot remove the `Connection` response header using this module because the `Connection` response header is generated by the standard `ngx_http_header_filter_module` in the Nginx core, whose output header filter runs always *after* the filter of this module. The only way to actually remove the `Connection` header is to patch the Nginx core, that is, editing the C function `ngx_http_header_filter` in the `src/http/ngx_http_header_filter_module.c` file.

[Back to TOC](#table-of-contents)

Installation
============

Grab the nginx source code from [nginx.org](http://nginx.org/), for example,
the version 1.5.8 (see [nginx compatibility](#compatibility)), and then build the source with this module:

```bash

wget 'http://nginx.org/download/nginx-1.5.8.tar.gz'
tar -xzvf nginx-1.5.8.tar.gz
cd nginx-1.5.8/

# Here we assume you would install you nginx under /opt/nginx/.
./configure --prefix=/opt/nginx \
    --add-module=/path/to/headers-more-nginx-module
 
make
make install
```

Download the latest version of the release tarball of this module from [headers-more-nginx-module file list](http://github.com/agentzh/headers-more-nginx-module/tags).

Also, this module is included and enabled by default in the [ngx_openresty bundle](http://openresty.org).

[Back to TOC](#table-of-contents)

Compatibility
=============

The following versions of Nginx should work with this module:

* **1.5.x**                       (last tested: 1.5.8)
* **1.4.x**                       (last tested: 1.4.4)
* **1.3.x**                       (last tested: 1.3.7)
* **1.2.x**                       (last tested: 1.2.9)
* **1.1.x**                       (last tested: 1.1.5)
* **1.0.x**                       (last tested: 1.0.11)
* **0.9.x**                       (last tested: 0.9.4)
* **0.8.x**                       (last tested: 0.8.54)
* **0.7.x >= 0.7.44**             (last tested: 0.7.68)

Earlier versions of Nginx like 0.6.x and 0.5.x will *not* work.

If you find that any particular version of Nginx above 0.7.44 does not work with this module, please consider [reporting a bug](#report-bugs).

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

1. creating a ticket on the [GitHub Issue Tracker](http://github.com/chaoslawful/lua-nginx-module/issues),
1. or posting to the [OpenResty community](#community).

[Back to TOC](#table-of-contents)

Source Repository
=================

Available on github at [agentzh/headers-more-nginx-module](http://github.com/agentzh/headers-more-nginx-module).

[Back to TOC](#table-of-contents)

Changes
=======

The changes of every release of this module can be obtained from the ngx_openresty bundle's change logs:

<http://openresty.org/#Changes>

[Back to TOC](#table-of-contents)

Test Suite
==========

This module comes with a Perl-driven test suite. The [test cases](http://github.com/agentzh/headers-more-nginx-module/tree/master/t/) are
[declarative](http://github.com/agentzh/headers-more-nginx-module/blob/master/t/sanity.t) too. Thanks to the [Test::Nginx](http://search.cpan.org/perldoc?Test::Nginx) module in the Perl world.

To run it on your side:

```bash

$ PATH=/path/to/your/nginx-with-headers-more-module:$PATH prove -r t
```

To run the test suite with valgrind's memcheck, use the following commands:

```bash

$ export PATH=/path/to/your/nginx-with-headers-more-module:$PATH
$ TEST_NGINX_USE_VALGRIND=1 prove -r t
```

You need to terminate any Nginx processes before running the test suite if you have changed the Nginx server binary.

Because a single nginx server (by default, `localhost:1984`) is used across all the test scripts (`.t` files), it's meaningless to run the test suite in parallel by specifying `-jN` when invoking the `prove` utility.

Some parts of the test suite requires modules [proxy](http://nginx.org/en/docs/http/ngx_http_proxy_module.html), [rewrite](http://nginx.org/en/docs/http/ngx_http_rewrite_module.html), and [echo](http://github.com/agentzh/echo-nginx-module) to be enabled as well when building Nginx.

[Back to TOC](#table-of-contents)

TODO
====

* Support variables in new headers' keys.

[Back to TOC](#table-of-contents)

Getting involved
================

You'll be very welcomed to submit patches to the [author](#author) or just ask for a commit bit to the [source repository](#source-repository) on GitHub.

[Back to TOC](#table-of-contents)

Authors
=======

* Yichun "agentzh" Zhang (章亦春) *&lt;agentzh@gmail.com&gt;*, CloudFlare Inc.
* Bernd Dorn ( <http://www.lovelysystems.com/> )

This wiki page is also maintained by the author himself, and everybody is encouraged to improve this page as well.

[Back to TOC](#table-of-contents)

Copyright & License
===================

The code base is borrowed directly from the standard [headers](http://nginx.org/en/docs/http/ngx_http_headers_module.html) module in Nginx 0.8.24. This part of code is copyrighted by Igor Sysoev.

Copyright (c) 2009-2014, Yichun "agentzh" Zhang (章亦春) <agentzh@gmail.com>, CloudFlare Inc.

Copyright (c) 2010-2013, Bernd Dorn.

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

* The original thread on the Nginx mailing list that inspires this module's development: ["A question about add_header replication"](http://forum.nginx.org/read.php?2,11206,11738).
* The orginal announcement thread on the Nginx mailing list: ["The "headers_more" module: Set and clear output headers...more than 'add'!"](http://forum.nginx.org/read.php?2,23460).
* The original [blog post](http://agentzh.blogspot.com/2009/11/headers-more-module-scripting-input-and.html) about this module's initial development.
* The [echo module](http://github.com/agentzh/echo-nginx-module) for Nginx module's automated testing.
* The standard [headers](http://nginx.org/en/docs/http/ngx_http_headers_module.html) module.
