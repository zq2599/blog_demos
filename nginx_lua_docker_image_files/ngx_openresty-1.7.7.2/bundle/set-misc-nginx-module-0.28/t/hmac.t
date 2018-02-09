# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(3);

plan tests => repeat_each() * 2 * blocks();

no_long_string();

run_tests();

#no_diff();

__DATA__

=== TEST 1: hmac
--- config
    location /bar {
        set $secret 'thisisverysecretstuff';
        set $string_to_sign 'some string we want to sign';
        set_hmac_sha1 $signature $secret $string_to_sign;
        set_encode_base64 $signature $signature;
        echo $signature;
    }
--- request
    GET /bar
--- response_body
R/pvxzHC4NLtj7S+kXFg/NePTmk=



=== TEST 2: hmac empty vars
--- config
    location /bar {
        set $secret '';
        set $string_to_sign '';
        set_hmac_sha1 $signature $secret $string_to_sign;
        set_encode_base64 $signature $signature;
        echo $signature;
    }
--- request
    GET /bar
--- response_body
+9sdGxiqbAgyS31ktx+3Y3BpDh0=


