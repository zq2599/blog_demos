# vi:filetype=

use Test::Nginx::Socket;

repeat_each(3);

plan tests => repeat_each() * 2 * blocks();

no_long_string();

run_tests();

#no_diff();

__DATA__

=== TEST 1: base32 (5 bytes)
--- config
    set_base32_padding off;
    location /bar {
        set $a 'abcde';
        set_encode_base32 $a;
        set $b $a;
        set_decode_base32 $b;

        echo $a;
        echo $b;
    }
--- request
    GET /bar
--- response_body
c5h66p35
abcde



=== TEST 2: base32 (1 byte)
--- config
    set_base32_padding off;
    location /bar {
        set $a '!';
        set_encode_base32 $a;
        set $b $a;
        set_decode_base32 $b;

        echo $a;
        echo $b;
    }
--- request
    GET /bar
--- response_body
44
!



=== TEST 3: base32 (1 byte) - not in-place editing
--- config
    location /bar {
        set_base32_padding off;
        set $a '!';
        set_encode_base32 $a $a;
        set_decode_base32 $b $a;

        echo $a;
        echo $b;
    }
--- request
    GET /bar
--- response_body
44
!



=== TEST 4: base32 (hello world)
--- config
    set_base32_padding off;
    location /bar {
        set $a '"hello, world!\nhiya"';
        set_encode_base32 $a;
        set $b $a;
        set_decode_base32 $b;

        echo $a;
        echo $b;
    }
--- request
    GET /bar
--- response_body
49k6ar3cdsm20trfe9m6888ad1knio92
"hello, world!
hiya"



=== TEST 5: base32 (0 bytes left)
--- config
    set_base32_padding off;
    location /bar {
        set $a '"hello, world!"';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
49k6ar3cdsm20trfe9m68892



=== TEST 6: base32 (6 bytes padded)
--- config
    set_base32_padding off;
    location /bar {
        set $a '"hello, world!"a';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
49k6ar3cdsm20trfe9m68892c4



=== TEST 7: base32 (4 bytes left)
--- config
    set_base32_padding off;
    location /bar {
        set $a '"hello, world!"ab';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
49k6ar3cdsm20trfe9m68892c5h0



=== TEST 8: base32 (3 bytes left)
--- config
    set_base32_padding off;
    location /bar {
        set $a '"hello, world!"abc';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
49k6ar3cdsm20trfe9m68892c5h66



=== TEST 9: base32 (1 bytes left)
--- config
    set_base32_padding off;
    location /bar {
        set $a '"hello, world!"abcd';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
49k6ar3cdsm20trfe9m68892c5h66p0



=== TEST 10: base32 standard alphabet (5 bytes)
--- config
    set_base32_padding off;
    set_base32_alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    location /bar {
        set $a 'abcde';
        set_encode_base32 $a;
        set $b $a;
        set_decode_base32 $b;

        echo $a;
        echo $b;
    }
--- request
    GET /bar
--- response_body
MFRGGZDF
abcde



=== TEST 11: base32 standard alphabet (1 byte)
--- config
    set_base32_padding off;
    set_base32_alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    location /bar {
        set $a '!';
        set_encode_base32 $a;
        set $b $a;
        set_decode_base32 $b;

        echo $a;
        echo $b;
    }
--- request
    GET /bar
--- response_body
EE
!



=== TEST 12: base32 standard alphabet (1 byte) - not in-place editing
--- config
    location /bar {
        set_base32_padding off;
        set_base32_alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        set $a '!';
        set_encode_base32 $a $a;
        set_decode_base32 $b $a;

        echo $a;
        echo $b;
    }
--- request
    GET /bar
--- response_body
EE
!



=== TEST 13: base32 standard alphabet (hello world)
--- config
    set_base32_padding off;
    set_base32_alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    location /bar {
        set $a '"hello, world!\nhiya"';
        set_encode_base32 $a;
        set $b $a;
        set_decode_base32 $b;

        echo $a;
        echo $b;
    }
--- request
    GET /bar
--- response_body
EJUGK3DMN4WCA53POJWGIIIKNBUXSYJC
"hello, world!
hiya"



=== TEST 14: base32 standard alphabet (0 bytes left)
--- config
    set_base32_padding off;
    set_base32_alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    location /bar {
        set $a '"hello, world!"';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
EJUGK3DMN4WCA53POJWGIIJC



=== TEST 15: base32 standard alphabet (6 bytes padded)
--- config
    set_base32_padding off;
    set_base32_alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    location /bar {
        set $a '"hello, world!"a';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
EJUGK3DMN4WCA53POJWGIIJCME



=== TEST 16: base32 standard alphabet (4 bytes left)
--- config
    set_base32_padding off;
    set_base32_alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    location /bar {
        set $a '"hello, world!"ab';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
EJUGK3DMN4WCA53POJWGIIJCMFRA



=== TEST 17: base32 standard alphabet (3 bytes left)
--- config
    set_base32_padding off;
    set_base32_alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    location /bar {
        set $a '"hello, world!"abc';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
EJUGK3DMN4WCA53POJWGIIJCMFRGG



=== TEST 18: base32 standard alphabet (1 bytes left)
--- config
    set_base32_padding off;
    set_base32_alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    location /bar {
        set $a '"hello, world!"abcd';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
EJUGK3DMN4WCA53POJWGIIJCMFRGGZA



=== TEST 19: base32 custom alphabet (5 bytes)
--- config
    set_base32_padding off;
    set_base32_alphabet "cdefghijklmnopqrstuvwxyz12345678";
    location /bar {
        set $a 'abcde';
        set_encode_base32 $a;
        set $b $a;
        set_decode_base32 $b;

        echo $a;
        echo $b;
    }
--- request
    GET /bar
--- response_body
ohtii2fh
abcde



=== TEST 20: base32 custom alphabet (1 byte)
--- config
    set_base32_padding off;
    set_base32_alphabet "cdefghijklmnopqrstuvwxyz12345678";
    location /bar {
        set $a '!';
        set_encode_base32 $a;
        set $b $a;
        set_decode_base32 $b;

        echo $a;
        echo $b;
    }
--- request
    GET /bar
--- response_body
gg
!



=== TEST 21: base32 custom alphabet (1 byte) - not in-place editing
--- config
    location /bar {
        set_base32_padding off;
        set_base32_alphabet "cdefghijklmnopqrstuvwxyz12345678";
        set $a '!';
        set_encode_base32 $a $a;
        set_decode_base32 $b $a;

        echo $a;
        echo $b;
    }
--- request
    GET /bar
--- response_body
gg
!



=== TEST 22: base32 custom alphabet (hello world)
--- config
    set_base32_padding off;
    set_base32_alphabet "cdefghijklmnopqrstuvwxyz12345678";
    location /bar {
        set $a '"hello, world!\nhiya"';
        set_encode_base32 $a;
        set $b $a;
        set_decode_base32 $b;

        echo $a;
        echo $b;
    }
--- request
    GET /bar
--- response_body
glwim4fop5yec64rqlyikkkmpdwzu1le
"hello, world!
hiya"



=== TEST 23: base32 custom alphabet (0 bytes left)
--- config
    set_base32_padding off;
    set_base32_alphabet "cdefghijklmnopqrstuvwxyz12345678";
    location /bar {
        set $a '"hello, world!"';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
glwim4fop5yec64rqlyikkle



=== TEST 24: base32 custom alphabet (6 bytes padded)
--- config
    set_base32_padding off;
    set_base32_alphabet "cdefghijklmnopqrstuvwxyz12345678";
    location /bar {
        set $a '"hello, world!"a';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
glwim4fop5yec64rqlyikkleog



=== TEST 25: base32 custom alphabet (4 bytes left)
--- config
    set_base32_padding off;
    set_base32_alphabet "cdefghijklmnopqrstuvwxyz12345678";
    location /bar {
        set $a '"hello, world!"ab';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
glwim4fop5yec64rqlyikkleohtc



=== TEST 26: base32 custom alphabet (3 bytes left)
--- config
    set_base32_padding off;
    set_base32_alphabet "cdefghijklmnopqrstuvwxyz12345678";
    location /bar {
        set $a '"hello, world!"abc';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
glwim4fop5yec64rqlyikkleohtii



=== TEST 27: base32 custom alphabet (1 bytes left)
--- config
    set_base32_padding off;
    set_base32_alphabet "cdefghijklmnopqrstuvwxyz12345678";
    location /bar {
        set $a '"hello, world!"abcd';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
glwim4fop5yec64rqlyikkleohtii2c



=== TEST 28: deprecated set_misc_base32_padding
--- config
    set_misc_base32_padding off;
    location /bar {
        set $a 'abcde';
        set_encode_base32 $a;
        set $b $a;
        set_decode_base32 $b;

        echo $a;
        echo $b;
    }
--- request
    GET /bar
--- response_body
c5h66p35
abcde

