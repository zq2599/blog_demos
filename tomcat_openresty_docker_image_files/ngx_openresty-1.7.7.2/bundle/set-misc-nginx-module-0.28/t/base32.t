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
44======
!



=== TEST 3: base32 (1 byte) - not in-place editing
--- config
    location /bar {
        set $a '!';
        set_encode_base32 $a $a;
        set_decode_base32 $b $a;

        echo $a;
        echo $b;
    }
--- request
    GET /bar
--- response_body
44======
!



=== TEST 4: base32 (hello world)
--- config
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
    set_base32_padding on;
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
    set_base32_padding on;
    location /bar {
        set $a '"hello, world!"a';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
49k6ar3cdsm20trfe9m68892c4======



=== TEST 7: base32 (4 bytes left)
--- config
    set_base32_padding on;
    location /bar {
        set $a '"hello, world!"ab';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
49k6ar3cdsm20trfe9m68892c5h0====



=== TEST 8: base32 (3 bytes left)
--- config
    set_base32_padding on;
    location /bar {
        set $a '"hello, world!"abc';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
49k6ar3cdsm20trfe9m68892c5h66===



=== TEST 9: base32 (1 bytes left)
--- config
    set_base32_padding on;
    location /bar {
        set $a '"hello, world!"abcd';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
49k6ar3cdsm20trfe9m68892c5h66p0=



=== TEST 10: base32 standard alphabet (5 bytes)
--- config
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
EE======
!



=== TEST 12: base32 standard alphabet (1 byte) - not in-place editing
--- config
    set_base32_alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    location /bar {
        set $a '!';
        set_encode_base32 $a $a;
        set_decode_base32 $b $a;

        echo $a;
        echo $b;
    }
--- request
    GET /bar
--- response_body
EE======
!



=== TEST 13: base32 standard alphabet (hello world)
--- config
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
    set_base32_padding on;
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
    set_base32_padding on;
    set_base32_alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    location /bar {
        set $a '"hello, world!"a';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
EJUGK3DMN4WCA53POJWGIIJCME======



=== TEST 16: base32 standard alphabet (4 bytes left)
--- config
    set_base32_padding on;
    set_base32_alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    location /bar {
        set $a '"hello, world!"ab';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
EJUGK3DMN4WCA53POJWGIIJCMFRA====



=== TEST 17: base32 standard alphabet (3 bytes left)
--- config
    set_base32_padding on;
    set_base32_alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    location /bar {
        set $a '"hello, world!"abc';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
EJUGK3DMN4WCA53POJWGIIJCMFRGG===



=== TEST 18: base32 standard alphabet (1 bytes left)
--- config
    set_base32_padding on;
    set_base32_alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    location /bar {
        set $a '"hello, world!"abcd';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
EJUGK3DMN4WCA53POJWGIIJCMFRGGZA=



=== TEST 19: base32 custom alphabet (5 bytes)
--- config
    set_base32_alphabet "efghijklmnopqrstuvwxyz0123456789";
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
qjvkk3hj
abcde



=== TEST 20: base32 custom alphabet (1 byte)
--- config
    set_base32_alphabet "efghijklmnopqrstuvwxyz0123456789";
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
ii======
!



=== TEST 21: base32 custom alphabet (1 byte) - not in-place editing
--- config
    set_base32_alphabet "efghijklmnopqrstuvwxyz0123456789";
    location /bar {
        set $a '!';
        set_encode_base32 $a $a;
        set_decode_base32 $b $a;

        echo $a;
        echo $b;
    }
--- request
    GET /bar
--- response_body
ii======
!



=== TEST 22: base32 custom alphabet (hello world)
--- config
    set_base32_alphabet "efghijklmnopqrstuvwxyz0123456789";
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
inyko5hqr60ge75tsn0kmmmorfy1w2ng
"hello, world!
hiya"



=== TEST 23: base32 custom alphabet (0 bytes left)
--- config
    set_base32_padding on;
    set_base32_alphabet "efghijklmnopqrstuvwxyz0123456789";
    location /bar {
        set $a '"hello, world!"';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
inyko5hqr60ge75tsn0kmmng



=== TEST 24: base32 custom alphabet (6 bytes padded)
--- config
    set_base32_padding on;
    set_base32_alphabet "efghijklmnopqrstuvwxyz0123456789";
    location /bar {
        set $a '"hello, world!"a';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
inyko5hqr60ge75tsn0kmmngqi======



=== TEST 25: base32 custom alphabet (4 bytes left)
--- config
    set_base32_padding on;
    set_base32_alphabet "efghijklmnopqrstuvwxyz0123456789";
    location /bar {
        set $a '"hello, world!"ab';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
inyko5hqr60ge75tsn0kmmngqjve====



=== TEST 26: base32 custom alphabet (3 bytes left)
--- config
    set_base32_padding on;
    set_base32_alphabet "efghijklmnopqrstuvwxyz0123456789";
    location /bar {
        set $a '"hello, world!"abc';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
inyko5hqr60ge75tsn0kmmngqjvkk===



=== TEST 27: base32 custom alphabet (1 bytes left)
--- config
    set_base32_padding on;
    set_base32_alphabet "efghijklmnopqrstuvwxyz0123456789";
    location /bar {
        set $a '"hello, world!"abcd';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
inyko5hqr60ge75tsn0kmmngqjvkk3e=



=== TEST 28: use set_base32_alphabet in location
--- config
    set_base32_padding on;
    location /bar {
        set_base32_alphabet "efghijklmnopqrstuvwxyz0123456789";
        set $a '"hello, world!"abcd';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
inyko5hqr60ge75tsn0kmmngqjvkk3e=



=== TEST 29: one byte less in set_base32_alphabet
--- config
    set_base32_padding on;
    location /bar {
        set_base32_alphabet "efghijklmnopqrstuvwxyz012345678";
        set $a '"hello, world!"abcd?\/.;';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
inyko5hqr60ge75tsn0kmmngqjvkk3e=
--- must_die
--- error_log eval
qr/\[emerg\] .*? "set_base32_alphabet" directive takes an alphabet of 31 bytes but 32 expected/



=== TEST 30: one byte more in set_base32_alphabet
--- config
    set_base32_padding on;
    location /bar {
        set_base32_alphabet "efghijklmnopqrstuvwxyz0123456789A";
        set $a '"hello, world!"abcd?\/.;';
        set_encode_base32 $a;

        echo $a;
    }
--- request
    GET /bar
--- response_body
inyko5hqr60ge75tsn0kmmngqjvkk3e=
--- must_die
--- error_log eval
qr/\[emerg\] .*? "set_base32_alphabet" directive takes an alphabet of 33 bytes but 32 expected/

