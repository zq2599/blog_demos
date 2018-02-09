#!/bin/bash

# this file is mostly meant to be used by the author himself.

root=`pwd`
version=$1
home=~
force=$2

         #--with-cc="gcc46" \
ngx-build $force $version \
          --with-ld-opt="-L$PCRE_LIB -Wl,-rpath,$PCRE_LIB:$LUAJIT_LIB:/usr/local/lib" \
          --with-cc-opt='-O0' \
          --add-module=$root/../echo-nginx-module \
          --add-module=$root/../ndk-nginx-module \
          --add-module=$root/../lua-nginx-module \
          --add-module=$root/../set-misc-nginx-module \
          --add-module=$root/../form-input-nginx-module \
          --add-module=$root/../rds-json-nginx-module \
          --add-module=$root $opts \
            --with-debug \
          || exit 1
          #--with-debug || exit 1
          #--add-module=$home/work/ngx_http_auth_request-0.1 #\
          #--with-rtsig_module
          #--with-cc-opt="-g3 -O0"
          #--add-module=$root/../echo-nginx-module \
  #--without-http_ssi_module  # we cannot disable ssi because echo_location_async depends on it (i dunno why?!)

