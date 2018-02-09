# vi:filetype=perl

use Test::Nginx::Socket;

repeat_each(100);

plan tests => repeat_each() * 2 * blocks();

no_long_string();

run_tests();

#no_diff();

__DATA__

=== TEST 1: sanity
--- config
    location /rand {
        set $from 5;
        set $to 7;
        set_random $res $from $to;

        echo $res;
    }
--- request
    GET /rand
--- response_body_like: [5-7]



=== TEST 2: sanity (two digits)
--- config
    location /rand {
        set $from 35;
        set $to 37;
        set_random $res $from $to;

        echo $res;
    }
--- request
    GET /rand
--- response_body_like: 3[5-7]



=== TEST 3: sanity (two digits, from > to)
--- config
    location /rand {
        set $from 37;
        set $to 35;
        set_random $res $from $to;

        echo $res;
    }
--- request
    GET /rand
--- response_body_like: 3[5-7]



=== TEST 4: sanity (two digits, from == to)
--- config
    location /rand {
        set $from 117;
        set $to 117;
        set_random $res $from $to;

        echo $res;
    }
--- request
    GET /rand
--- response_body
117



=== TEST 5: negative number not allowed in from arg
--- config
    location /rand {
        set $from -2;
        set $to 4;
        set_random $res $from $to;

        echo $res;
    }
--- request
    GET /rand
--- response_body_like: 500 Internal Server Error
--- error_code: 500



=== TEST 6: negative number not allowed in to arg
--- config
    location /rand {
        set $from 2;
        set $to -4;
        set_random $res $from $to;

        echo $res;
    }
--- request
    GET /rand
--- response_body_like: 500 Internal Server Error
--- error_code: 500



=== TEST 7: empty string not allowed in from arg
--- config
    location /rand {
        set $from '';
        set $to 4;
        set_random $res $from $to;

        echo $res;
    }
--- request
    GET /rand
--- response_body_like: 500 Internal Server Error
--- error_code: 500



=== TEST 8: empty string not allowed in to arg
--- config
    location /rand {
        set $from 2;
        set $to '';
        set_random $res $from $to;

        echo $res;
    }
--- request
    GET /rand
--- response_body_like: 500 Internal Server Error
--- error_code: 500



=== TEST 9: wrong number of arguments
--- config
    location /rand {
        set $from 2;
        set_random $res $from;

        echo $res;
    }
--- request
    GET /rand
--- response_body_like: 500 Internal Server Error
--- error_code: 500
--- SKIP



=== TEST 10: zero is fine
--- config
    location /rand {
        set_random $res 0 0;

        echo $res;
    }
--- request
    GET /rand
--- response_body
0

