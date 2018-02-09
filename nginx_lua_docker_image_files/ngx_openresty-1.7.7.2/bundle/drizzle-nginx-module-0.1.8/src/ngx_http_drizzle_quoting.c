
/*
 * Copyright (C) Yichun Zhang (agentzh)
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"


#include "ngx_http_drizzle_quoting.h"
#include "ngx_http_drizzle_checker.h"
#include <ngx_core.h>
#include <ngx_http.h>


/* static */ ngx_http_drizzle_var_type_t  ngx_http_drizzle_builtin_types[] = {
    { ngx_string("string"),
      NULL,
      NULL,
      quotes_type_single
    },
    { ngx_string("int"),
      ngx_http_drizzle_check_int,
      NULL,
      quotes_type_none
    },
    { ngx_string("bool"),
      ngx_http_drizzle_check_bool,
      NULL,
      quotes_type_none
    },
    { ngx_string("float"),
      ngx_http_drizzle_check_float,
      NULL,
      quotes_type_none
    },
    { ngx_string("column"),
      ngx_http_drizzle_check_col,
      NULL,
      quotes_type_none
    },
    { ngx_string("table"),
      ngx_http_drizzle_check_table,
      NULL,
      quotes_type_none
    },
    { ngx_string("keyword"),
      ngx_http_drizzle_check_keyword,
      NULL,
      quotes_type_none
    }
};
