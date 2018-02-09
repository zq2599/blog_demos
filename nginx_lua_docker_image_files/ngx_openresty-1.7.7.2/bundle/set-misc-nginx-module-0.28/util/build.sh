#!/bin/bash

# this file is mostly meant to be used by the author himself.

root=`pwd`
home=~
version=$1
force=$2

    #--with-cc="gcc46" \
    #--with-ld-opt="-rdynamic" \
    #--with-mail \
    #--with-mail_ssl_module \

ngx-build $force $version \
    --with-http_ssl_module \
    --without-mail_pop3_module \
    --without-mail_imap_module \
    --without-mail_smtp_module \
    --without-http_upstream_ip_hash_module \
    --without-http_empty_gif_module \
    --without-http_memcached_module \
    --without-http_referer_module \
    --without-http_autoindex_module \
    --without-http_auth_basic_module \
    --without-http_userid_module \
    --add-module=$root/../echo-nginx-module \
    --add-module=$root/../ndk-nginx-module \
    --add-module=$root/../iconv-nginx-module \
    --add-module=$root $opts \
    --with-debug \
  || exit 1

