# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(3);

plan tests => repeat_each() * 2 * blocks();

no_long_string();

run_tests();

#no_diff();

__DATA__

=== TEST 1: base64 encode
--- config
    location /bar {
        set_encode_base64 $out "abcde";
        echo $out;
    }
--- request
    GET /bar
--- response_body
YWJjZGU=



=== TEST 2: base64 decode
--- config
    location /bar {
        set_decode_base64 $out "YWJjZGU=";
        echo $out;
    }
--- request
    GET /bar
--- response_body
abcde

