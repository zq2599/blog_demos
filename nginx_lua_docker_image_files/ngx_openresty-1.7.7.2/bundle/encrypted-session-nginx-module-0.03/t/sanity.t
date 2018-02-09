# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * 2 * blocks();

no_long_string();

no_shuffle();

run_tests();

#no_diff();

__DATA__

=== TEST 1: key with default iv
--- config
    encrypted_session_key "abcdefghijklmnopqrstuvwxyz123456";
    encrypted_session_expires 0;

    location /encode {
        set $a 'abc';

        set_encrypt_session $res $a;

        set_encode_base32 $ppres $res;

        echo "res = $ppres";

        set_decrypt_session $b $res;
        echo "b = $b";
    }
--- request
    GET /encode
--- response_body
res = ktrp3n437q42laejppc9d4bg0jpv0ejie106ooo65od9lf5huhs0====
b = abc



=== TEST 2: key with custom iv
--- config
    encrypted_session_key "abcdefghijklmnopqrstuvwxyz123456";
    encrypted_session_iv "12345678";
    encrypted_session_expires 0;

    location /encode {
        set $a 'abc';

        set_encrypt_session $res $a;

        set_encode_base32 $ppres $res;

        echo "res = $ppres";

        set_decrypt_session $b $res;
        echo "b = $b";
    }
--- request
    GET /encode
--- response_body
res = ktrp3n437q42laejppc9d4bg0hul4pmqhc8tn2laae40aakqfoh0====
b = abc



=== TEST 3: key with custom iv
--- config
    encrypted_session_key "abcdefghijklmnopqrstuvwxyz123456";
    #encrypted_session_key "abcdefghijklmnopqrstuvwx";
    encrypted_session_iv "12345678";
    encrypted_session_expires 3;

    location /encode {
        set $a 'abc';

        set_encrypt_session $res $a;

        set_encode_base32 $ppres $res;

        echo "res = $ppres";

        set_decrypt_session $b $res;
        echo "b = $b";
    }
--- request
    GET /encode
--- response_body_like
^res = [0-9a-v=]{30,}
b = abc$



=== TEST 4: key with custom iv
--- config
    encrypted_session_key "abcdefghijklmnopqrstuvwxyz123456";
    encrypted_session_iv "12345678";
    encrypted_session_expires 1d;

    location /encode {
        set_encrypt_session $res '1234';
        set_encode_base32 $res;

        echo "res = $res";
    }
--- request
    GET /encode
--- response_body_like
^res = [0-9a-v=]{30,}$



=== TEST 5: key with custom iv
--- config
    encrypted_session_key "abcdefghijklmnopqrstuvwxyz123456";
    encrypted_session_iv "12345678";
    encrypted_session_expires 1d;

    location /foo {
        set $uid 1315;
        set_encrypt_session $session $uid;
        set_encode_base32 $session;

        #echo $session;
        echo_exec /bar _s=$session;
    }

    location /bar {
        encrypted_session_expires 30d;
        set_unescape_uri $session $arg__s;
        set_decode_base32 $session;
        set_decrypt_session $uid $session;
        echo $uid;
    }
--- request
    GET /foo
--- response_body
1315



=== TEST 6: decoder (bad md5 checksum)
valid: ktrp3n437q42laejppc9d4bg0j0i6np4tdpovhgdum09l7a0rg10====
--- config
    encrypted_session_key "abcdefghijklmnopqrstuvwxyz123456";
    encrypted_session_iv "12345678";
    encrypted_session_expires 1d;

    location /decode {
        set_unescape_uri $session $arg__s;
        set_decode_base32 $session;
        set_decrypt_session $uid $session;
        echo '[$uid]';
    }
--- request
GET /decode?_s=3trp3n437q42laejppc9d4bg0j0i6np4tdpovhgdum09l7a0rg10====
--- response_body
[]



=== TEST 7: decoder (bad md5 checksum)
valid: ktrp3n437q42laejppc9d4bg0j0i6np4tdpovhgdum09l7a0rg10====
--- config
    encrypted_session_key "abcdefghijklmnopqrstuvwxyz123456";
    encrypted_session_iv "12345678";
    encrypted_session_expires 1d;

    location /decode {
        set_unescape_uri $session $arg__s;
        set_decode_base32 $session;
        set_decrypt_session $uid $session;
        echo '[$uid]';
    }
--- request
GET /decode?_s=ktrp3n437q42laejppc9d4bg0j0i6np4tdpovhgdum09laa0rg10====
--- response_body
[]

