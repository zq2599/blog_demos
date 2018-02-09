
#line 1 "src/ngx_http_xss_util.rl"

/*
 * Copyright (C) agentzh
 */


#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_xss_util.h"



#line 16 "src/ngx_http_xss_util.rl"

#line 21 "src/ngx_http_xss_util.c"
static const int javascript_start = 1;
static const int javascript_first_final = 6;
static const int javascript_error = 0;

static const int javascript_en_main = 1;


#line 17 "src/ngx_http_xss_util.rl"

ngx_int_t ngx_http_xss_test_callback(u_char *data, size_t len)
{
    signed char *p = (signed char *) data;
    signed char *pe;
    int cs;

    pe = p + len;

    
#line 40 "src/ngx_http_xss_util.c"
	{
	cs = javascript_start;
	}

#line 45 "src/ngx_http_xss_util.c"
	{
	if ( p == pe )
		goto _test_eof;
	switch ( cs )
	{
st1:
	if ( ++p == pe )
		goto _test_eof1;
case 1:
	switch( (*p) ) {
		case 36: goto st6;
		case 95: goto st6;
	}
	if ( (*p) > 90 ) {
		if ( 97 <= (*p) && (*p) <= 122 )
			goto st6;
	} else if ( (*p) >= 65 )
		goto st6;
	goto st0;
st0:
cs = 0;
	goto _out;
st6:
	if ( ++p == pe )
		goto _test_eof6;
case 6:
	switch( (*p) ) {
		case 36: goto st6;
		case 46: goto st1;
		case 91: goto st2;
		case 95: goto st6;
	}
	if ( (*p) < 65 ) {
		if ( 48 <= (*p) && (*p) <= 57 )
			goto st6;
	} else if ( (*p) > 90 ) {
		if ( 97 <= (*p) && (*p) <= 122 )
			goto st6;
	} else
		goto st6;
	goto st0;
st2:
	if ( ++p == pe )
		goto _test_eof2;
case 2:
	if ( (*p) == 46 )
		goto st3;
	if ( 48 <= (*p) && (*p) <= 57 )
		goto st5;
	goto st0;
st3:
	if ( ++p == pe )
		goto _test_eof3;
case 3:
	if ( 48 <= (*p) && (*p) <= 57 )
		goto st4;
	goto st0;
st4:
	if ( ++p == pe )
		goto _test_eof4;
case 4:
	if ( (*p) == 93 )
		goto st7;
	if ( 48 <= (*p) && (*p) <= 57 )
		goto st4;
	goto st0;
st7:
	if ( ++p == pe )
		goto _test_eof7;
case 7:
	goto st0;
st5:
	if ( ++p == pe )
		goto _test_eof5;
case 5:
	switch( (*p) ) {
		case 46: goto st3;
		case 93: goto st7;
	}
	if ( 48 <= (*p) && (*p) <= 57 )
		goto st5;
	goto st0;
	}
	_test_eof1: cs = 1; goto _test_eof; 
	_test_eof6: cs = 6; goto _test_eof; 
	_test_eof2: cs = 2; goto _test_eof; 
	_test_eof3: cs = 3; goto _test_eof; 
	_test_eof4: cs = 4; goto _test_eof; 
	_test_eof7: cs = 7; goto _test_eof; 
	_test_eof5: cs = 5; goto _test_eof; 

	_test_eof: {}
	_out: {}
	}

#line 38 "src/ngx_http_xss_util.rl"


    if (cs < 
#line 145 "src/ngx_http_xss_util.c"
6
#line 40 "src/ngx_http_xss_util.rl"
 || p != pe) {
        return NGX_DECLINED;
    }

    return NGX_OK;
}
