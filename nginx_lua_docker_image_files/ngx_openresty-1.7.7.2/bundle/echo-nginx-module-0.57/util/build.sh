#!/usr/bin/env bash

# this file is mostly meant to be used by the author himself.

root=`pwd`
version=$1
force=$2
home=~

  #--without-http_ssi_module \
            #--with-cc=gcc46 \

ngx-build $force $version \
            --with-ld-opt="-L$PCRE_LIB -Wl,-rpath,$PCRE_LIB:$LIBDRIZZLE_LIB:/usr/local/lib" \
            --with-cc-opt="-DDEBUG_MALLOC" \
            --with-http_stub_status_module \
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
          --with-http_addition_module \
          --add-module=$root/../ndk-nginx-module \
          --add-module=$root/../set-misc-nginx-module \
          --add-module=$root/../eval-nginx-module \
          --add-module=$root/../xss-nginx-module \
          --add-module=$root/../rds-json-nginx-module \
          --add-module=$root/../headers-more-nginx-module \
          --add-module=$root/../lua-nginx-module \
          --add-module=$root $opts \
          --with-select_module \
          --with-poll_module \
          --with-rtsig_module \
          --with-debug || exit 1
          #--add-module=$root/../lz-session-nginx-module \
          #--add-module=$home/work/ndk \
          #--add-module=$home/work/ndk/examples/http/set_var \
          #--add-module=$root/../eval-nginx-module \
          #--add-module=/home/agentz/work/nginx_eval_module-1.0.1 \

