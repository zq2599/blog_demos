
#line 1 "src/ngx_http_redis2_reply.rl"
#ifndef DDEBUG
#define DDEBUG 0
#endif
#include "ddebug.h"

#include "ngx_http_redis2_reply.h"
#include "ngx_http_redis2_util.h"
#include <nginx.h>


#line 20 "src/ngx_http_redis2_reply.rl"



#line 18 "src/ngx_http_redis2_reply.c"
static const int reply_start = 1;
static const int reply_first_final = 52;
static const int reply_error = 0;

static const int reply_en_main = 1;


#line 23 "src/ngx_http_redis2_reply.rl"

ngx_int_t
ngx_http_redis2_process_reply(ngx_http_redis2_ctx_t *ctx, ssize_t bytes)
{
    ngx_buf_t                *b;
    ngx_http_upstream_t      *u;
    ngx_str_t                 buf;
    ngx_flag_t                done;
    ngx_chain_t              *cl = NULL;
    ngx_chain_t             **ll = NULL;

    int                       cs;
    signed char              *p;
    signed char              *orig_p;
    ssize_t                   orig_len;
    signed char              *pe;

    u = ctx->request->upstream;
    b = &u->buffer;

    orig_p = (signed char *) b->last;
    orig_len = bytes;

    while (ctx->query_count) {
        done = 0;

        if (ctx->state == NGX_ERROR) {
            dd("init the state machine");

            
#line 57 "src/ngx_http_redis2_reply.c"
	{
	cs = reply_start;
	}

#line 53 "src/ngx_http_redis2_reply.rl"

            ctx->state = cs;

        } else {
            cs = ctx->state;
            dd("resumed the old state %d", cs);
        }

        p  = (signed char *) b->last;
        pe = (signed char *) b->last + bytes;

        dd("response body: %.*s", (int) bytes, p);

        
#line 77 "src/ngx_http_redis2_reply.c"
	{
	short _widec;
	if ( p == pe )
		goto _test_eof;
	switch ( cs )
	{
case 1:
	switch( (*p) ) {
		case 36: goto st2;
		case 42: goto st14;
		case 43: goto st50;
		case 45: goto st50;
		case 58: goto st50;
	}
	goto st0;
st0:
cs = 0;
	goto _out;
st2:
	if ( ++p == pe )
		goto _test_eof2;
case 2:
	switch( (*p) ) {
		case 45: goto st3;
		case 48: goto st6;
	}
	if ( 49 <= (*p) && (*p) <= 57 )
		goto tr6;
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
	if ( (*p) == 13 )
		goto st5;
	if ( 48 <= (*p) && (*p) <= 57 )
		goto st4;
	goto st0;
st5:
	if ( ++p == pe )
		goto _test_eof5;
case 5:
	if ( (*p) == 10 )
		goto tr9;
	goto st0;
tr9:
#line 50 "src/common.rl"
	{
        ctx->chunks_read++;
        dd("have read chunk %d, %.*s", (int) ctx->chunks_read,
            (int) (p - (signed char *) b->last), (signed char *) b->last);
    }
#line 12 "src/common.rl"
	{
        dd("done!");
        done = 1;
    }
	goto st52;
tr23:
#line 30 "src/multi_bulk_reply.rl"
	{
        dd("finalize multi bulks");

        if (ctx->chunks_read == ctx->chunk_count) {
            dd("done multi bunlk reading!");
            done = 1;
        }
    }
	goto st52;
tr62:
#line 12 "src/common.rl"
	{
        dd("done!");
        done = 1;
    }
	goto st52;
st52:
	if ( ++p == pe )
		goto _test_eof52;
case 52:
#line 165 "src/ngx_http_redis2_reply.c"
	goto st0;
st6:
	if ( ++p == pe )
		goto _test_eof6;
case 6:
	switch( (*p) ) {
		case 13: goto st7;
		case 48: goto st6;
	}
	goto st0;
st7:
	if ( ++p == pe )
		goto _test_eof7;
case 7:
	if ( (*p) == 10 )
		goto st8;
	goto st0;
st8:
	if ( ++p == pe )
		goto _test_eof8;
case 8:
	if ( (*p) == 13 )
		goto st5;
	goto st0;
tr6:
#line 23 "src/common.rl"
	{
        dd("start reading chunk size");
        ctx->chunk_bytes_read = 0;
        ctx->chunk_size = 0;
    }
#line 17 "src/common.rl"
	{
        ctx->chunk_size *= 10;
        ctx->chunk_size += *p - '0';
        dd("read chunk size: %d", (int) ctx->chunk_size);
    }
	goto st9;
tr13:
#line 17 "src/common.rl"
	{
        ctx->chunk_size *= 10;
        ctx->chunk_size += *p - '0';
        dd("read chunk size: %d", (int) ctx->chunk_size);
    }
	goto st9;
st9:
	if ( ++p == pe )
		goto _test_eof9;
case 9:
#line 216 "src/ngx_http_redis2_reply.c"
	if ( (*p) == 13 )
		goto st10;
	if ( 48 <= (*p) && (*p) <= 57 )
		goto tr13;
	goto st0;
st10:
	if ( ++p == pe )
		goto _test_eof10;
case 10:
	if ( (*p) == 10 )
		goto st11;
	goto st0;
st11:
	if ( ++p == pe )
		goto _test_eof11;
case 11:
	_widec = (*p);
	_widec = (short)(128 + ((*p) - -128));
	if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
	if ( 384 <= _widec && _widec <= 639 )
		goto st12;
	goto st0;
st12:
	if ( ++p == pe )
		goto _test_eof12;
case 12:
	_widec = (*p);
	if ( (*p) < 13 ) {
		if ( (*p) <= 12 ) {
			_widec = (short)(128 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		}
	} else if ( (*p) > 13 ) {
		if ( 14 <= (*p) )
 {			_widec = (short)(128 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		}
	} else {
		_widec = (short)(1152 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
	}
	switch( _widec ) {
		case 1549: goto st12;
		case 1805: goto st5;
		case 2061: goto st13;
	}
	if ( _widec > 524 ) {
		if ( 526 <= _widec && _widec <= 639 )
			goto st12;
	} else if ( _widec >= 384 )
		goto st12;
	goto st0;
st13:
	if ( ++p == pe )
		goto _test_eof13;
case 13:
	_widec = (*p);
	if ( (*p) < 13 ) {
		if ( (*p) <= 12 ) {
			_widec = (short)(128 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		}
	} else if ( (*p) > 13 ) {
		if ( 14 <= (*p) )
 {			_widec = (short)(128 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		}
	} else {
		_widec = (short)(1152 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
	}
	switch( _widec ) {
		case 266: goto tr9;
		case 522: goto tr17;
		case 1549: goto st12;
		case 1805: goto st5;
		case 2061: goto st13;
	}
	if ( _widec > 524 ) {
		if ( 526 <= _widec && _widec <= 639 )
			goto st12;
	} else if ( _widec >= 384 )
		goto st12;
	goto st0;
tr17:
#line 50 "src/common.rl"
	{
        ctx->chunks_read++;
        dd("have read chunk %d, %.*s", (int) ctx->chunks_read,
            (int) (p - (signed char *) b->last), (signed char *) b->last);
    }
#line 12 "src/common.rl"
	{
        dd("done!");
        done = 1;
    }
	goto st53;
st53:
	if ( ++p == pe )
		goto _test_eof53;
case 53:
#line 394 "src/ngx_http_redis2_reply.c"
	_widec = (*p);
	if ( (*p) < 13 ) {
		if ( (*p) <= 12 ) {
			_widec = (short)(128 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		}
	} else if ( (*p) > 13 ) {
		if ( 14 <= (*p) )
 {			_widec = (short)(128 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		}
	} else {
		_widec = (short)(1152 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
	}
	switch( _widec ) {
		case 1549: goto st12;
		case 1805: goto st5;
		case 2061: goto st13;
	}
	if ( _widec > 524 ) {
		if ( 526 <= _widec && _widec <= 639 )
			goto st12;
	} else if ( _widec >= 384 )
		goto st12;
	goto st0;
st14:
	if ( ++p == pe )
		goto _test_eof14;
case 14:
	switch( (*p) ) {
		case 45: goto st15;
		case 48: goto st18;
	}
	if ( 49 <= (*p) && (*p) <= 57 )
		goto tr20;
	goto st0;
st15:
	if ( ++p == pe )
		goto _test_eof15;
case 15:
	if ( (*p) == 49 )
		goto st16;
	goto st0;
st16:
	if ( ++p == pe )
		goto _test_eof16;
case 16:
	if ( (*p) == 13 )
		goto st17;
	goto st0;
st17:
	if ( ++p == pe )
		goto _test_eof17;
case 17:
	if ( (*p) == 10 )
		goto tr23;
	goto st0;
st18:
	if ( ++p == pe )
		goto _test_eof18;
case 18:
	switch( (*p) ) {
		case 13: goto st17;
		case 48: goto st18;
	}
	goto st0;
tr20:
#line 19 "src/multi_bulk_reply.rl"
	{
        dd("start reading bulk count");
        ctx->chunk_count = 0;
    }
#line 24 "src/multi_bulk_reply.rl"
	{
        ctx->chunk_count *= 10;
        ctx->chunk_count += *p - '0';
        dd("chunk count: %d", (int) ctx->chunk_count);
    }
	goto st19;
tr25:
#line 24 "src/multi_bulk_reply.rl"
	{
        ctx->chunk_count *= 10;
        ctx->chunk_count += *p - '0';
        dd("chunk count: %d", (int) ctx->chunk_count);
    }
	goto st19;
st19:
	if ( ++p == pe )
		goto _test_eof19;
case 19:
#line 521 "src/ngx_http_redis2_reply.c"
	if ( (*p) == 13 )
		goto st20;
	if ( 48 <= (*p) && (*p) <= 57 )
		goto tr25;
	goto st0;
st20:
	if ( ++p == pe )
		goto _test_eof20;
case 20:
	if ( (*p) == 10 )
		goto tr26;
	goto st0;
tr26:
#line 14 "src/multi_bulk_reply.rl"
	{
        dd("start reading bulk");
        ctx->chunks_read = 0;
    }
	goto st21;
st21:
	if ( ++p == pe )
		goto _test_eof21;
case 21:
#line 545 "src/ngx_http_redis2_reply.c"
	_widec = (*p);
	if ( (*p) < 43 ) {
		if ( 36 <= (*p) && (*p) <= 36 ) {
			_widec = (short)(5248 + ((*p) - -128));
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
		}
	} else if ( (*p) > 43 ) {
		if ( (*p) > 45 ) {
			if ( 58 <= (*p) && (*p) <= 58 ) {
				_widec = (short)(5248 + ((*p) - -128));
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
			}
		} else if ( (*p) >= 45 ) {
			_widec = (short)(5248 + ((*p) - -128));
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
		}
	} else {
		_widec = (short)(5248 + ((*p) - -128));
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
	}
	switch( _widec ) {
		case 5668: goto st22;
		case 5675: goto st26;
		case 5677: goto st26;
		case 5690: goto st26;
	}
	goto st0;
st22:
	if ( ++p == pe )
		goto _test_eof22;
case 22:
	_widec = (*p);
	if ( (*p) < 48 ) {
		if ( 45 <= (*p) && (*p) <= 45 ) {
			_widec = (short)(5248 + ((*p) - -128));
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
		}
	} else if ( (*p) > 48 ) {
		if ( 49 <= (*p) && (*p) <= 57 ) {
			_widec = (short)(5248 + ((*p) - -128));
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
		}
	} else {
		_widec = (short)(5248 + ((*p) - -128));
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
	}
	switch( _widec ) {
		case 5677: goto st23;
		case 5680: goto st28;
	}
	if ( 5681 <= _widec && _widec <= 5689 )
		goto tr31;
	goto st0;
st23:
	if ( ++p == pe )
		goto _test_eof23;
case 23:
	_widec = (*p);
	if ( 48 <= (*p) && (*p) <= 57 ) {
		_widec = (short)(5248 + ((*p) - -128));
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
	}
	if ( 5680 <= _widec && _widec <= 5689 )
		goto st24;
	goto st0;
st24:
	if ( ++p == pe )
		goto _test_eof24;
case 24:
	_widec = (*p);
	if ( (*p) > 13 ) {
		if ( 48 <= (*p) && (*p) <= 57 ) {
			_widec = (short)(5248 + ((*p) - -128));
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
		}
	} else if ( (*p) >= 13 ) {
		_widec = (short)(5248 + ((*p) - -128));
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
	}
	if ( _widec == 5645 )
		goto st25;
	if ( 5680 <= _widec && _widec <= 5689 )
		goto st24;
	goto st0;
st25:
	if ( ++p == pe )
		goto _test_eof25;
case 25:
	_widec = (*p);
	if ( 10 <= (*p) && (*p) <= 10 ) {
		_widec = (short)(5248 + ((*p) - -128));
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
	}
	if ( _widec == 5642 )
		goto tr34;
	goto st0;
tr34:
#line 50 "src/common.rl"
	{
        ctx->chunks_read++;
        dd("have read chunk %d, %.*s", (int) ctx->chunks_read,
            (int) (p - (signed char *) b->last), (signed char *) b->last);
    }
#line 30 "src/multi_bulk_reply.rl"
	{
        dd("finalize multi bulks");

        if (ctx->chunks_read == ctx->chunk_count) {
            dd("done multi bunlk reading!");
            done = 1;
        }
    }
	goto st54;
st54:
	if ( ++p == pe )
		goto _test_eof54;
case 54:
#line 751 "src/ngx_http_redis2_reply.c"
	_widec = (*p);
	if ( (*p) < 43 ) {
		if ( 36 <= (*p) && (*p) <= 36 ) {
			_widec = (short)(5248 + ((*p) - -128));
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
		}
	} else if ( (*p) > 43 ) {
		if ( (*p) > 45 ) {
			if ( 58 <= (*p) && (*p) <= 58 ) {
				_widec = (short)(5248 + ((*p) - -128));
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
			}
		} else if ( (*p) >= 45 ) {
			_widec = (short)(5248 + ((*p) - -128));
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
		}
	} else {
		_widec = (short)(5248 + ((*p) - -128));
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
	}
	switch( _widec ) {
		case 5668: goto st22;
		case 5675: goto st26;
		case 5677: goto st26;
		case 5690: goto st26;
	}
	goto st0;
st26:
	if ( ++p == pe )
		goto _test_eof26;
case 26:
	_widec = (*p);
	if ( (*p) < 13 ) {
		if ( (*p) <= 12 ) {
			_widec = (short)(5248 + ((*p) - -128));
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
		}
	} else if ( (*p) > 13 ) {
		if ( 14 <= (*p) )
 {			_widec = (short)(5248 + ((*p) - -128));
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
		}
	} else {
		_widec = (short)(5248 + ((*p) - -128));
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
	}
	if ( _widec == 5645 )
		goto st27;
	if ( 5504 <= _widec && _widec <= 5759 )
		goto st26;
	goto st0;
st27:
	if ( ++p == pe )
		goto _test_eof27;
case 27:
	_widec = (*p);
	if ( (*p) < 11 ) {
		if ( (*p) > 9 ) {
			if ( 10 <= (*p) && (*p) <= 10 ) {
				_widec = (short)(5248 + ((*p) - -128));
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
			}
		} else {
			_widec = (short)(5248 + ((*p) - -128));
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
		}
	} else if ( (*p) > 12 ) {
		if ( (*p) > 13 ) {
			if ( 14 <= (*p) )
 {				_widec = (short)(5248 + ((*p) - -128));
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
			}
		} else if ( (*p) >= 13 ) {
			_widec = (short)(5248 + ((*p) - -128));
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
		}
	} else {
		_widec = (short)(5248 + ((*p) - -128));
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
	}
	switch( _widec ) {
		case 5642: goto tr34;
		case 5645: goto st27;
	}
	if ( 5504 <= _widec && _widec <= 5759 )
		goto st26;
	goto st0;
st28:
	if ( ++p == pe )
		goto _test_eof28;
case 28:
	_widec = (*p);
	if ( (*p) > 13 ) {
		if ( 48 <= (*p) && (*p) <= 48 ) {
			_widec = (short)(5248 + ((*p) - -128));
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
		}
	} else if ( (*p) >= 13 ) {
		_widec = (short)(5248 + ((*p) - -128));
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
	}
	switch( _widec ) {
		case 5645: goto st29;
		case 5680: goto st28;
	}
	goto st0;
st29:
	if ( ++p == pe )
		goto _test_eof29;
case 29:
	_widec = (*p);
	if ( 10 <= (*p) && (*p) <= 10 ) {
		_widec = (short)(5248 + ((*p) - -128));
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
	}
	if ( _widec == 5642 )
		goto st30;
	goto st0;
st30:
	if ( ++p == pe )
		goto _test_eof30;
case 30:
	_widec = (*p);
	if ( 13 <= (*p) && (*p) <= 13 ) {
		_widec = (short)(5248 + ((*p) - -128));
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
	}
	if ( _widec == 5645 )
		goto st25;
	goto st0;
tr31:
#line 23 "src/common.rl"
	{
        dd("start reading chunk size");
        ctx->chunk_bytes_read = 0;
        ctx->chunk_size = 0;
    }
#line 17 "src/common.rl"
	{
        ctx->chunk_size *= 10;
        ctx->chunk_size += *p - '0';
        dd("read chunk size: %d", (int) ctx->chunk_size);
    }
	goto st31;
tr39:
#line 17 "src/common.rl"
	{
        ctx->chunk_size *= 10;
        ctx->chunk_size += *p - '0';
        dd("read chunk size: %d", (int) ctx->chunk_size);
    }
	goto st31;
st31:
	if ( ++p == pe )
		goto _test_eof31;
case 31:
#line 1036 "src/ngx_http_redis2_reply.c"
	_widec = (*p);
	if ( (*p) > 13 ) {
		if ( 48 <= (*p) && (*p) <= 57 ) {
			_widec = (short)(5248 + ((*p) - -128));
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
		}
	} else if ( (*p) >= 13 ) {
		_widec = (short)(5248 + ((*p) - -128));
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
	}
	if ( _widec == 5645 )
		goto st32;
	if ( 5680 <= _widec && _widec <= 5689 )
		goto tr39;
	goto st0;
st32:
	if ( ++p == pe )
		goto _test_eof32;
case 32:
	_widec = (*p);
	if ( 10 <= (*p) && (*p) <= 10 ) {
		_widec = (short)(5248 + ((*p) - -128));
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
	}
	if ( _widec == 5642 )
		goto st33;
	goto st0;
st33:
	if ( ++p == pe )
		goto _test_eof33;
case 33:
	_widec = (*p);
	_widec = (short)(2176 + ((*p) - -128));
	if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
	if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
	if ( 2944 <= _widec && _widec <= 3199 )
		goto st34;
	goto st0;
st34:
	if ( ++p == pe )
		goto _test_eof34;
case 34:
	_widec = (*p);
	if ( (*p) < 13 ) {
		if ( (*p) <= 12 ) {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else if ( (*p) > 13 ) {
		if ( 14 <= (*p) )
 {			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else {
		_widec = (short)(3200 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 1024;
	}
	switch( _widec ) {
		case 4621: goto st34;
		case 4877: goto st25;
		case 5133: goto st35;
	}
	if ( _widec > 3084 ) {
		if ( 3086 <= _widec && _widec <= 3199 )
			goto st34;
	} else if ( _widec >= 2944 )
		goto st34;
	goto st0;
st35:
	if ( ++p == pe )
		goto _test_eof35;
case 35:
	_widec = (*p);
	if ( (*p) < 11 ) {
		if ( (*p) > 9 ) {
			if ( 10 <= (*p) && (*p) <= 10 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else if ( (*p) > 12 ) {
		if ( (*p) > 13 ) {
			if ( 14 <= (*p) )
 {				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) >= 13 ) {
			_widec = (short)(3200 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 1024;
		}
	} else {
		_widec = (short)(2176 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
	}
	switch( _widec ) {
		case 2826: goto tr34;
		case 3082: goto tr43;
		case 4621: goto st34;
		case 4877: goto st25;
		case 5133: goto st35;
	}
	if ( _widec > 3084 ) {
		if ( 3086 <= _widec && _widec <= 3199 )
			goto st34;
	} else if ( _widec >= 2944 )
		goto st34;
	goto st0;
tr43:
#line 50 "src/common.rl"
	{
        ctx->chunks_read++;
        dd("have read chunk %d, %.*s", (int) ctx->chunks_read,
            (int) (p - (signed char *) b->last), (signed char *) b->last);
    }
#line 30 "src/multi_bulk_reply.rl"
	{
        dd("finalize multi bulks");

        if (ctx->chunks_read == ctx->chunk_count) {
            dd("done multi bunlk reading!");
            done = 1;
        }
    }
	goto st55;
st55:
	if ( ++p == pe )
		goto _test_eof55;
case 55:
#line 1364 "src/ngx_http_redis2_reply.c"
	_widec = (*p);
	if ( (*p) < 43 ) {
		if ( (*p) < 14 ) {
			if ( (*p) > 12 ) {
				if ( 13 <= (*p) && (*p) <= 13 ) {
					_widec = (short)(3200 + ((*p) - -128));
					if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
					if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
					if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 1024;
				}
			} else {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) > 35 ) {
			if ( (*p) > 36 ) {
				if ( 37 <= (*p) && (*p) <= 42 ) {
					_widec = (short)(2176 + ((*p) - -128));
					if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
					if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
				}
			} else if ( (*p) >= 36 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else if ( (*p) > 43 ) {
		if ( (*p) < 46 ) {
			if ( (*p) > 44 ) {
				if ( 45 <= (*p) && (*p) <= 45 ) {
					_widec = (short)(2176 + ((*p) - -128));
					if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
					if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
				}
			} else if ( (*p) >= 44 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) > 57 ) {
			if ( (*p) > 58 ) {
				if ( 59 <= (*p) )
 {					_widec = (short)(2176 + ((*p) - -128));
					if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
					if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
				}
			} else if ( (*p) >= 58 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else {
		_widec = (short)(2176 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
	}
	switch( _widec ) {
		case 2852: goto st22;
		case 2859: goto st26;
		case 2861: goto st26;
		case 2874: goto st26;
		case 3108: goto st36;
		case 3115: goto st44;
		case 3117: goto st44;
		case 3130: goto st44;
		case 4621: goto st34;
		case 4877: goto st25;
		case 5133: goto st35;
	}
	if ( _widec > 3084 ) {
		if ( 3086 <= _widec && _widec <= 3199 )
			goto st34;
	} else if ( _widec >= 2944 )
		goto st34;
	goto st0;
st36:
	if ( ++p == pe )
		goto _test_eof36;
case 36:
	_widec = (*p);
	if ( (*p) < 45 ) {
		if ( (*p) < 13 ) {
			if ( (*p) <= 12 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) > 13 ) {
			if ( 14 <= (*p) && (*p) <= 44 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else {
			_widec = (short)(3200 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 1024;
		}
	} else if ( (*p) > 45 ) {
		if ( (*p) < 48 ) {
			if ( 46 <= (*p) && (*p) <= 47 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) > 48 ) {
			if ( (*p) > 57 ) {
				if ( 58 <= (*p) )
 {					_widec = (short)(2176 + ((*p) - -128));
					if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
					if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
				}
			} else if ( (*p) >= 49 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else {
		_widec = (short)(2176 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
	}
	switch( _widec ) {
		case 2861: goto st23;
		case 2864: goto st28;
		case 3117: goto st37;
		case 3120: goto st39;
		case 4621: goto st34;
		case 4877: goto st25;
		case 5133: goto st35;
	}
	if ( _widec < 3086 ) {
		if ( _widec > 2873 ) {
			if ( 2944 <= _widec && _widec <= 3084 )
				goto st34;
		} else if ( _widec >= 2865 )
			goto tr31;
	} else if ( _widec > 3119 ) {
		if ( _widec > 3129 ) {
			if ( 3130 <= _widec && _widec <= 3199 )
				goto st34;
		} else if ( _widec >= 3121 )
			goto tr46;
	} else
		goto st34;
	goto st0;
st37:
	if ( ++p == pe )
		goto _test_eof37;
case 37:
	_widec = (*p);
	if ( (*p) < 14 ) {
		if ( (*p) > 12 ) {
			if ( 13 <= (*p) && (*p) <= 13 ) {
				_widec = (short)(3200 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 1024;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else if ( (*p) > 47 ) {
		if ( (*p) > 57 ) {
			if ( 58 <= (*p) )
 {				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) >= 48 ) {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else {
		_widec = (short)(2176 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
	}
	switch( _widec ) {
		case 4621: goto st34;
		case 4877: goto st25;
		case 5133: goto st35;
	}
	if ( _widec < 3086 ) {
		if ( _widec > 2873 ) {
			if ( 2944 <= _widec && _widec <= 3084 )
				goto st34;
		} else if ( _widec >= 2864 )
			goto st24;
	} else if ( _widec > 3119 ) {
		if ( _widec > 3129 ) {
			if ( 3130 <= _widec && _widec <= 3199 )
				goto st34;
		} else if ( _widec >= 3120 )
			goto st38;
	} else
		goto st34;
	goto st0;
st38:
	if ( ++p == pe )
		goto _test_eof38;
case 38:
	_widec = (*p);
	if ( (*p) < 14 ) {
		if ( (*p) > 12 ) {
			if ( 13 <= (*p) && (*p) <= 13 ) {
				_widec = (short)(3200 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 1024;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else if ( (*p) > 47 ) {
		if ( (*p) > 57 ) {
			if ( 58 <= (*p) )
 {				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) >= 48 ) {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else {
		_widec = (short)(2176 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
	}
	switch( _widec ) {
		case 4365: goto st25;
		case 4621: goto st35;
		case 4877: goto st25;
		case 5133: goto st35;
	}
	if ( _widec < 3086 ) {
		if ( _widec > 2873 ) {
			if ( 2944 <= _widec && _widec <= 3084 )
				goto st34;
		} else if ( _widec >= 2864 )
			goto st24;
	} else if ( _widec > 3119 ) {
		if ( _widec > 3129 ) {
			if ( 3130 <= _widec && _widec <= 3199 )
				goto st34;
		} else if ( _widec >= 3120 )
			goto st38;
	} else
		goto st34;
	goto st0;
st39:
	if ( ++p == pe )
		goto _test_eof39;
case 39:
	_widec = (*p);
	if ( (*p) < 14 ) {
		if ( (*p) > 12 ) {
			if ( 13 <= (*p) && (*p) <= 13 ) {
				_widec = (short)(3200 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 1024;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else if ( (*p) > 47 ) {
		if ( (*p) > 48 ) {
			if ( 49 <= (*p) )
 {				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) >= 48 ) {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else {
		_widec = (short)(2176 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
	}
	switch( _widec ) {
		case 2864: goto st28;
		case 3120: goto st39;
		case 4365: goto st29;
		case 4621: goto st40;
		case 4877: goto st42;
		case 5133: goto st43;
	}
	if ( _widec > 3084 ) {
		if ( 3086 <= _widec && _widec <= 3199 )
			goto st34;
	} else if ( _widec >= 2944 )
		goto st34;
	goto st0;
st40:
	if ( ++p == pe )
		goto _test_eof40;
case 40:
	_widec = (*p);
	if ( (*p) < 11 ) {
		if ( (*p) > 9 ) {
			if ( 10 <= (*p) && (*p) <= 10 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else if ( (*p) > 12 ) {
		if ( (*p) > 13 ) {
			if ( 14 <= (*p) )
 {				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) >= 13 ) {
			_widec = (short)(3200 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 1024;
		}
	} else {
		_widec = (short)(2176 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
	}
	switch( _widec ) {
		case 2826: goto st30;
		case 3082: goto st41;
		case 4621: goto st34;
		case 4877: goto st25;
		case 5133: goto st35;
	}
	if ( _widec > 3084 ) {
		if ( 3086 <= _widec && _widec <= 3199 )
			goto st34;
	} else if ( _widec >= 2944 )
		goto st34;
	goto st0;
st41:
	if ( ++p == pe )
		goto _test_eof41;
case 41:
	_widec = (*p);
	if ( (*p) < 13 ) {
		if ( (*p) <= 12 ) {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else if ( (*p) > 13 ) {
		if ( 14 <= (*p) )
 {			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else {
		_widec = (short)(3200 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 1024;
	}
	switch( _widec ) {
		case 4365: goto st25;
		case 4621: goto st35;
		case 4877: goto st25;
		case 5133: goto st35;
	}
	if ( _widec > 3084 ) {
		if ( 3086 <= _widec && _widec <= 3199 )
			goto st34;
	} else if ( _widec >= 2944 )
		goto st34;
	goto st0;
st42:
	if ( ++p == pe )
		goto _test_eof42;
case 42:
	_widec = (*p);
	if ( 10 <= (*p) && (*p) <= 10 ) {
		_widec = (short)(5248 + ((*p) - -128));
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
	}
	if ( _widec == 5642 )
		goto tr52;
	goto st0;
tr52:
#line 50 "src/common.rl"
	{
        ctx->chunks_read++;
        dd("have read chunk %d, %.*s", (int) ctx->chunks_read,
            (int) (p - (signed char *) b->last), (signed char *) b->last);
    }
#line 30 "src/multi_bulk_reply.rl"
	{
        dd("finalize multi bulks");

        if (ctx->chunks_read == ctx->chunk_count) {
            dd("done multi bunlk reading!");
            done = 1;
        }
    }
	goto st56;
st56:
	if ( ++p == pe )
		goto _test_eof56;
case 56:
#line 2547 "src/ngx_http_redis2_reply.c"
	_widec = (*p);
	if ( (*p) < 43 ) {
		if ( (*p) > 13 ) {
			if ( 36 <= (*p) && (*p) <= 36 ) {
				_widec = (short)(5248 + ((*p) - -128));
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
			}
		} else if ( (*p) >= 13 ) {
			_widec = (short)(5248 + ((*p) - -128));
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
		}
	} else if ( (*p) > 43 ) {
		if ( (*p) > 45 ) {
			if ( 58 <= (*p) && (*p) <= 58 ) {
				_widec = (short)(5248 + ((*p) - -128));
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
			}
		} else if ( (*p) >= 45 ) {
			_widec = (short)(5248 + ((*p) - -128));
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
		}
	} else {
		_widec = (short)(5248 + ((*p) - -128));
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
	}
	switch( _widec ) {
		case 5645: goto st25;
		case 5668: goto st22;
		case 5675: goto st26;
		case 5677: goto st26;
		case 5690: goto st26;
	}
	goto st0;
st43:
	if ( ++p == pe )
		goto _test_eof43;
case 43:
	_widec = (*p);
	if ( (*p) < 11 ) {
		if ( (*p) > 9 ) {
			if ( 10 <= (*p) && (*p) <= 10 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else if ( (*p) > 12 ) {
		if ( (*p) > 13 ) {
			if ( 14 <= (*p) )
 {				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) >= 13 ) {
			_widec = (short)(3200 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 1024;
		}
	} else {
		_widec = (short)(2176 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
	}
	switch( _widec ) {
		case 2826: goto tr52;
		case 3082: goto tr53;
		case 4621: goto st34;
		case 4877: goto st25;
		case 5133: goto st35;
	}
	if ( _widec > 3084 ) {
		if ( 3086 <= _widec && _widec <= 3199 )
			goto st34;
	} else if ( _widec >= 2944 )
		goto st34;
	goto st0;
tr53:
#line 50 "src/common.rl"
	{
        ctx->chunks_read++;
        dd("have read chunk %d, %.*s", (int) ctx->chunks_read,
            (int) (p - (signed char *) b->last), (signed char *) b->last);
    }
#line 30 "src/multi_bulk_reply.rl"
	{
        dd("finalize multi bulks");

        if (ctx->chunks_read == ctx->chunk_count) {
            dd("done multi bunlk reading!");
            done = 1;
        }
    }
	goto st57;
st57:
	if ( ++p == pe )
		goto _test_eof57;
case 57:
#line 2779 "src/ngx_http_redis2_reply.c"
	_widec = (*p);
	if ( (*p) < 43 ) {
		if ( (*p) < 14 ) {
			if ( (*p) > 12 ) {
				if ( 13 <= (*p) && (*p) <= 13 ) {
					_widec = (short)(3200 + ((*p) - -128));
					if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
					if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
					if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 1024;
				}
			} else {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) > 35 ) {
			if ( (*p) > 36 ) {
				if ( 37 <= (*p) && (*p) <= 42 ) {
					_widec = (short)(2176 + ((*p) - -128));
					if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
					if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
				}
			} else if ( (*p) >= 36 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else if ( (*p) > 43 ) {
		if ( (*p) < 46 ) {
			if ( (*p) > 44 ) {
				if ( 45 <= (*p) && (*p) <= 45 ) {
					_widec = (short)(2176 + ((*p) - -128));
					if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
					if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
				}
			} else if ( (*p) >= 44 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) > 57 ) {
			if ( (*p) > 58 ) {
				if ( 59 <= (*p) )
 {					_widec = (short)(2176 + ((*p) - -128));
					if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
					if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
				}
			} else if ( (*p) >= 58 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else {
		_widec = (short)(2176 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
	}
	switch( _widec ) {
		case 2852: goto st22;
		case 2859: goto st26;
		case 2861: goto st26;
		case 2874: goto st26;
		case 3108: goto st36;
		case 3115: goto st44;
		case 3117: goto st44;
		case 3130: goto st44;
		case 4365: goto st25;
		case 4621: goto st35;
		case 4877: goto st25;
		case 5133: goto st35;
	}
	if ( _widec > 3084 ) {
		if ( 3086 <= _widec && _widec <= 3199 )
			goto st34;
	} else if ( _widec >= 2944 )
		goto st34;
	goto st0;
st44:
	if ( ++p == pe )
		goto _test_eof44;
case 44:
	_widec = (*p);
	if ( (*p) < 13 ) {
		if ( (*p) <= 12 ) {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else if ( (*p) > 13 ) {
		if ( 14 <= (*p) )
 {			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else {
		_widec = (short)(3200 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 1024;
	}
	switch( _widec ) {
		case 4365: goto st27;
		case 4621: goto st45;
		case 4877: goto st27;
		case 5133: goto st45;
	}
	if ( _widec < 2830 ) {
		if ( 2688 <= _widec && _widec <= 2828 )
			goto st26;
	} else if ( _widec > 2943 ) {
		if ( _widec > 3084 ) {
			if ( 3086 <= _widec && _widec <= 3199 )
				goto st44;
		} else if ( _widec >= 2944 )
			goto st44;
	} else
		goto st26;
	goto st0;
st45:
	if ( ++p == pe )
		goto _test_eof45;
case 45:
	_widec = (*p);
	if ( (*p) < 11 ) {
		if ( (*p) > 9 ) {
			if ( 10 <= (*p) && (*p) <= 10 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else if ( (*p) > 12 ) {
		if ( (*p) > 13 ) {
			if ( 14 <= (*p) )
 {				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) >= 13 ) {
			_widec = (short)(3200 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 1024;
		}
	} else {
		_widec = (short)(2176 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
	}
	switch( _widec ) {
		case 2826: goto tr34;
		case 3082: goto tr43;
		case 4365: goto st27;
		case 4621: goto st45;
		case 4877: goto st27;
		case 5133: goto st45;
	}
	if ( _widec < 2830 ) {
		if ( 2688 <= _widec && _widec <= 2828 )
			goto st26;
	} else if ( _widec > 2943 ) {
		if ( _widec > 3084 ) {
			if ( 3086 <= _widec && _widec <= 3199 )
				goto st44;
		} else if ( _widec >= 2944 )
			goto st44;
	} else
		goto st26;
	goto st0;
tr46:
#line 23 "src/common.rl"
	{
        dd("start reading chunk size");
        ctx->chunk_bytes_read = 0;
        ctx->chunk_size = 0;
    }
#line 17 "src/common.rl"
	{
        ctx->chunk_size *= 10;
        ctx->chunk_size += *p - '0';
        dd("read chunk size: %d", (int) ctx->chunk_size);
    }
	goto st46;
tr56:
#line 17 "src/common.rl"
	{
        ctx->chunk_size *= 10;
        ctx->chunk_size += *p - '0';
        dd("read chunk size: %d", (int) ctx->chunk_size);
    }
	goto st46;
st46:
	if ( ++p == pe )
		goto _test_eof46;
case 46:
#line 3320 "src/ngx_http_redis2_reply.c"
	_widec = (*p);
	if ( (*p) < 14 ) {
		if ( (*p) > 12 ) {
			if ( 13 <= (*p) && (*p) <= 13 ) {
				_widec = (short)(3200 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 1024;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else if ( (*p) > 47 ) {
		if ( (*p) > 57 ) {
			if ( 58 <= (*p) )
 {				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) >= 48 ) {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else {
		_widec = (short)(2176 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
	}
	switch( _widec ) {
		case 4365: goto st32;
		case 4621: goto st47;
		case 4877: goto st48;
		case 5133: goto st49;
	}
	if ( _widec < 3086 ) {
		if ( _widec > 2873 ) {
			if ( 2944 <= _widec && _widec <= 3084 )
				goto st34;
		} else if ( _widec >= 2864 )
			goto tr39;
	} else if ( _widec > 3119 ) {
		if ( _widec > 3129 ) {
			if ( 3130 <= _widec && _widec <= 3199 )
				goto st34;
		} else if ( _widec >= 3120 )
			goto tr56;
	} else
		goto st34;
	goto st0;
st47:
	if ( ++p == pe )
		goto _test_eof47;
case 47:
	_widec = (*p);
	if ( (*p) < 11 ) {
		if ( (*p) > 9 ) {
			if ( 10 <= (*p) && (*p) <= 10 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else if ( (*p) > 12 ) {
		if ( (*p) > 13 ) {
			if ( 14 <= (*p) )
 {				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) >= 13 ) {
			_widec = (short)(3200 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 1024;
		}
	} else {
		_widec = (short)(2176 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
	}
	switch( _widec ) {
		case 2826: goto st33;
		case 4621: goto st34;
		case 4877: goto st25;
		case 5133: goto st35;
	}
	if ( _widec > 3084 ) {
		if ( 3086 <= _widec && _widec <= 3199 )
			goto st34;
	} else if ( _widec >= 2944 )
		goto st34;
	goto st0;
st48:
	if ( ++p == pe )
		goto _test_eof48;
case 48:
	_widec = (*p);
	if ( 10 <= (*p) && (*p) <= 10 ) {
		_widec = (short)(5248 + ((*p) - -128));
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 256;
	}
	if ( _widec == 5642 )
		goto tr60;
	goto st0;
tr60:
#line 50 "src/common.rl"
	{
        ctx->chunks_read++;
        dd("have read chunk %d, %.*s", (int) ctx->chunks_read,
            (int) (p - (signed char *) b->last), (signed char *) b->last);
    }
#line 30 "src/multi_bulk_reply.rl"
	{
        dd("finalize multi bulks");

        if (ctx->chunks_read == ctx->chunk_count) {
            dd("done multi bunlk reading!");
            done = 1;
        }
    }
	goto st58;
st58:
	if ( ++p == pe )
		goto _test_eof58;
case 58:
#line 3639 "src/ngx_http_redis2_reply.c"
	_widec = (*p);
	if ( (*p) < 44 ) {
		if ( (*p) < 36 ) {
			if ( (*p) <= 35 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) > 36 ) {
			if ( (*p) > 42 ) {
				if ( 43 <= (*p) && (*p) <= 43 ) {
					_widec = (short)(2176 + ((*p) - -128));
					if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
					if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
				}
			} else if ( (*p) >= 37 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else if ( (*p) > 44 ) {
		if ( (*p) < 46 ) {
			if ( 45 <= (*p) && (*p) <= 45 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) > 57 ) {
			if ( (*p) > 58 ) {
				if ( 59 <= (*p) )
 {					_widec = (short)(2176 + ((*p) - -128));
					if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
					if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
				}
			} else if ( (*p) >= 58 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else {
		_widec = (short)(2176 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
	}
	switch( _widec ) {
		case 2852: goto st22;
		case 2859: goto st26;
		case 2861: goto st26;
		case 2874: goto st26;
		case 3108: goto st36;
		case 3115: goto st44;
		case 3117: goto st44;
		case 3130: goto st44;
	}
	if ( 2944 <= _widec && _widec <= 3199 )
		goto st34;
	goto st0;
st49:
	if ( ++p == pe )
		goto _test_eof49;
case 49:
	_widec = (*p);
	if ( (*p) < 11 ) {
		if ( (*p) > 9 ) {
			if ( 10 <= (*p) && (*p) <= 10 ) {
				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else {
			_widec = (short)(2176 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
		}
	} else if ( (*p) > 12 ) {
		if ( (*p) > 13 ) {
			if ( 14 <= (*p) )
 {				_widec = (short)(2176 + ((*p) - -128));
				if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
				if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
			}
		} else if ( (*p) >= 13 ) {
			_widec = (short)(3200 + ((*p) - -128));
			if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
			if ( 
#line 56 "src/common.rl"

#if 0
        fprintf(stderr,
            "check_data_complete: chunk bytes read: %d, chunk size: %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read == ctx->chunk_size + 1
     ) _widec += 512;
			if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 1024;
		}
	} else {
		_widec = (short)(2176 + ((*p) - -128));
		if ( 
#line 34 "src/common.rl"

#if 0
        fprintf(stderr, "test chunk len: %d < %d\n",
            (int) ctx->chunk_bytes_read, (int) ctx->chunk_size),
#endif
        ctx->chunk_bytes_read++ < ctx->chunk_size
     ) _widec += 256;
		if ( 
#line 6 "src/multi_bulk_reply.rl"

#if 0
        fprintf(stderr, "test chunk count: %d < %d\n",
            (int) ctx->chunks_read, (int) ctx->chunk_count),
#endif
        ctx->chunks_read < ctx->chunk_count
     ) _widec += 512;
	}
	switch( _widec ) {
		case 2826: goto tr60;
		case 3082: goto tr43;
		case 4621: goto st34;
		case 4877: goto st25;
		case 5133: goto st35;
	}
	if ( _widec > 3084 ) {
		if ( 3086 <= _widec && _widec <= 3199 )
			goto st34;
	} else if ( _widec >= 2944 )
		goto st34;
	goto st0;
st50:
	if ( ++p == pe )
		goto _test_eof50;
case 50:
	if ( (*p) == 13 )
		goto st51;
	goto st50;
st51:
	if ( ++p == pe )
		goto _test_eof51;
case 51:
	switch( (*p) ) {
		case 10: goto tr62;
		case 13: goto st51;
	}
	goto st50;
	}
	_test_eof2: cs = 2; goto _test_eof; 
	_test_eof3: cs = 3; goto _test_eof; 
	_test_eof4: cs = 4; goto _test_eof; 
	_test_eof5: cs = 5; goto _test_eof; 
	_test_eof52: cs = 52; goto _test_eof; 
	_test_eof6: cs = 6; goto _test_eof; 
	_test_eof7: cs = 7; goto _test_eof; 
	_test_eof8: cs = 8; goto _test_eof; 
	_test_eof9: cs = 9; goto _test_eof; 
	_test_eof10: cs = 10; goto _test_eof; 
	_test_eof11: cs = 11; goto _test_eof; 
	_test_eof12: cs = 12; goto _test_eof; 
	_test_eof13: cs = 13; goto _test_eof; 
	_test_eof53: cs = 53; goto _test_eof; 
	_test_eof14: cs = 14; goto _test_eof; 
	_test_eof15: cs = 15; goto _test_eof; 
	_test_eof16: cs = 16; goto _test_eof; 
	_test_eof17: cs = 17; goto _test_eof; 
	_test_eof18: cs = 18; goto _test_eof; 
	_test_eof19: cs = 19; goto _test_eof; 
	_test_eof20: cs = 20; goto _test_eof; 
	_test_eof21: cs = 21; goto _test_eof; 
	_test_eof22: cs = 22; goto _test_eof; 
	_test_eof23: cs = 23; goto _test_eof; 
	_test_eof24: cs = 24; goto _test_eof; 
	_test_eof25: cs = 25; goto _test_eof; 
	_test_eof54: cs = 54; goto _test_eof; 
	_test_eof26: cs = 26; goto _test_eof; 
	_test_eof27: cs = 27; goto _test_eof; 
	_test_eof28: cs = 28; goto _test_eof; 
	_test_eof29: cs = 29; goto _test_eof; 
	_test_eof30: cs = 30; goto _test_eof; 
	_test_eof31: cs = 31; goto _test_eof; 
	_test_eof32: cs = 32; goto _test_eof; 
	_test_eof33: cs = 33; goto _test_eof; 
	_test_eof34: cs = 34; goto _test_eof; 
	_test_eof35: cs = 35; goto _test_eof; 
	_test_eof55: cs = 55; goto _test_eof; 
	_test_eof36: cs = 36; goto _test_eof; 
	_test_eof37: cs = 37; goto _test_eof; 
	_test_eof38: cs = 38; goto _test_eof; 
	_test_eof39: cs = 39; goto _test_eof; 
	_test_eof40: cs = 40; goto _test_eof; 
	_test_eof41: cs = 41; goto _test_eof; 
	_test_eof42: cs = 42; goto _test_eof; 
	_test_eof56: cs = 56; goto _test_eof; 
	_test_eof43: cs = 43; goto _test_eof; 
	_test_eof57: cs = 57; goto _test_eof; 
	_test_eof44: cs = 44; goto _test_eof; 
	_test_eof45: cs = 45; goto _test_eof; 
	_test_eof46: cs = 46; goto _test_eof; 
	_test_eof47: cs = 47; goto _test_eof; 
	_test_eof48: cs = 48; goto _test_eof; 
	_test_eof58: cs = 58; goto _test_eof; 
	_test_eof49: cs = 49; goto _test_eof; 
	_test_eof50: cs = 50; goto _test_eof; 
	_test_eof51: cs = 51; goto _test_eof; 

	_test_eof: {}
	_out: {}
	}

#line 67 "src/ngx_http_redis2_reply.rl"

        dd("state after exec: %d, done: %d, %.*s", cs, (int) done,
            (int) (bytes - ((u_char *) p - b->last)), p);

        ctx->state = cs;

        if (!done && cs == reply_error) {
            if (cl) {
                cl->buf->last = cl->buf->pos;
                cl = NULL;
                *ll = NULL;
            }

            buf.data = b->pos;
            buf.len = b->last - b->pos + bytes;

            ngx_log_error(NGX_LOG_ERR, ctx->request->connection->log, 0,
                "Redis server returned invalid response near pos %z in "
                "\"%V\"",
                    (ssize_t) ((u_char *) p - b->pos), &buf);

            u->length = 0;

            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }

        if (cl == NULL) {
            for (cl = u->out_bufs, ll = &u->out_bufs; cl; cl = cl->next) {
                ll = &cl->next;
            }

            cl = ngx_chain_get_free_buf(ctx->request->pool, &u->free_bufs);
            if (cl == NULL) {
                u->length = 0;
                return NGX_ERROR;
            }

            cl->buf->flush = 1;
            cl->buf->memory = 1;

            *ll = cl;

            dd("response body: %.*s", (int) bytes, p);

            cl->buf->pos = b->last;
            cl->buf->last = (u_char *) p;
            cl->buf->tag = u->output.tag;

        } else {
            cl->buf->last = (u_char *) p;
        }

        bytes -= (ssize_t) ((u_char *) p - b->last);
        b->last = (u_char *) p;

        if (done) {
            dd("response parser done");

            ctx->query_count--;

            if (ctx->query_count == 0) {
                if (cs == reply_error) {
                    buf.data = (u_char *) p;
                    buf.len = orig_p - p + orig_len;

                    ngx_log_error(NGX_LOG_WARN, ctx->request->connection->log,
                        0, "Redis server returned extra bytes: \"%V\" (len %z)",
                        &buf, buf.len);

#if 0
                    if (cl) {
                        cl->buf->last = cl->buf->pos;
                        cl = NULL;
                        *ll = NULL;
                    }

                    u->length = 0;

                    return NGX_HTTP_INTERNAL_SERVER_ERROR;
#endif

                } else {
#if defined(nginx_version) && nginx_version >= 1001004
                    u->keepalive = 1;
#endif
                }

                u->length = 0;

                break;

            } else {
                ctx->state = NGX_ERROR;
                /* continue */
            }

        } else {
            /* need more data */
            break;
        }
    }

    return NGX_OK;
}
