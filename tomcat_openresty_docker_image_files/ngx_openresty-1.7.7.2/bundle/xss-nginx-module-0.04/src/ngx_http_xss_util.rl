
/*
 * Copyright (C) agentzh
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_xss_util.h"


%% machine javascript;
%% write data;

ngx_int_t ngx_http_xss_test_callback(u_char *data, size_t len)
{
    signed char *p = (signed char *) data;
    signed char *pe;
    int cs;

    pe = p + len;

    %%{
        identifier = [$A-Za-z_] [$A-Za-z0-9_]*;

        index = [0-9]* '.' [0-9]+
              | [0-9]+
              ;

        main := identifier ( '.' identifier )*
                  ('[' index ']')? ;

        write init;
        write exec;
    }%%

    if (cs < %%{ write first_final; }%% || p != pe) {
        return NGX_DECLINED;
    }

    return NGX_OK;
}
