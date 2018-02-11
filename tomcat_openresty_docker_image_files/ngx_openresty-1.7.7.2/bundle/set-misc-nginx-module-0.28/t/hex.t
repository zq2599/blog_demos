# vi:filetype=perl

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(3);

plan tests => repeat_each() * 2 * blocks();

no_long_string();

run_tests();

#no_diff();

__DATA__

=== TEST 1: hex encode
--- config
    location /bar {
        set_encode_hex $out "abcde";
        echo $out;
    }
--- request
    GET /bar
--- response_body
6162636465



=== TEST 2: hex decode
--- config
    location /bar {
        set_decode_hex $out "6162636465";
        echo $out;
    }
--- request
    GET /bar
--- response_body
abcde



=== TEST 3: hex encode (chinese)
--- config
    location /bar {
        set $raw "章亦春";
        set_encode_hex $digest $raw;
        set_decode_hex $hex $digest;
        echo $digest;
        echo $hex;
    }
--- request
    GET /bar
--- response_body
e7aba0e4baa6e698a5
章亦春

