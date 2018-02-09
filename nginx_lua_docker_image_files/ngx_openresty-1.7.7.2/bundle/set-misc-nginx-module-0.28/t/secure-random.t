# vi:filetype=

use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * 2 * blocks();

no_long_string();

run_tests();

#no_diff();

__DATA__

=== TEST 1: a 32-character alphanum
--- config
    location /alphanum {
        set_secure_random_alphanum $res 32;

        echo $res;
    }
--- request
    GET /alphanum
--- response_body_like: ^[a-zA-Z0-9]{32}$



=== TEST 2: a 16-character alphanum
--- config
    location /alphanum {
        set_secure_random_alphanum $res 16;

        echo $res;
    }
--- request
    GET /alphanum
--- response_body_like: ^[a-zA-Z0-9]{16}$



=== TEST 3: a 1-character alphanum
--- config
    location /alphanum {
        set_secure_random_alphanum $res 1;

        echo $res;
    }
--- request
    GET /alphanum
--- response_body_like: ^[a-zA-Z0-9]{1}$



=== TEST 4: length less than <= 0 should fail
--- config
    location /alphanum {
        set_secure_random_alphanum $res 0;

        echo $res;
    }
--- request
    GET /alphanum
--- response_body_like: 500 Internal Server Error
--- error_code: 500



=== TEST 5: length less than <= 0 should fail
--- config
    location /alphanum {
        set_secure_random_alphanum $res -4;

        echo $res;
    }
--- request
    GET /alphanum
--- response_body_like: 500 Internal Server Error
--- error_code: 500



=== TEST 6: non-numeric length should fail
--- config
    location /alphanum {
        set_secure_random_alphanum $res bob;

        echo $res;
    }
--- request
    GET /alphanum
--- response_body_like: 500 Internal Server Error
--- error_code: 500



=== TEST 7: a 16-character lcalpha
--- config
    location /lcalpha {
        set_secure_random_lcalpha $res 16;

        echo $res;
    }
--- request
    GET /lcalpha
--- response_body_like: ^[a-z]{16}$

