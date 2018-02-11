# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

plan tests => 2 * blocks();

#$Test::Nginx::LWP::LogLevel = 'debug';

run_tests();

__DATA__

=== TEST 1: sanity
--- config
    location /dup {
        echo_duplicate 3 a;
    }
--- request
    GET /dup
--- response_body: aaa



=== TEST 2: abc abc
--- config
    location /dup {
        echo_duplicate 2 abc;
    }
--- request
    GET /dup
--- response_body: abcabc



=== TEST 3: big size with underscores
--- config
    location /dup {
        echo_duplicate 10_000 A;
    }
--- request
    GET /dup
--- response_body eval
'A' x 10_000



=== TEST 4: 0 duplicate 0 empty strings
--- config
    location /dup {
        echo_duplicate 0 "";
    }
--- request
    GET /dup
--- response_body



=== TEST 5: 0 duplicate non-empty strings
--- config
    location /dup {
        echo_duplicate 0 "abc";
    }
--- request
    GET /dup
--- response_body



=== TEST 6: duplication of empty strings
--- config
    location /dup {
        echo_duplicate 2 "";
    }
--- request
    GET /dup
--- response_body



=== TEST 7: sanity (HEAD)
--- config
    location /dup {
        echo_duplicate 3 a;
    }
--- request
    HEAD /dup
--- response_body

