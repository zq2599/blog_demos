# vi:filetype=perl

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (blocks() * 3);

run_tests();

__DATA__

=== TEST 1: '
--- config
    location /test {
        set                 $test "he'llo";
        postgres_escape     $escaped $test;
        echo                $escaped;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
'he''llo'
--- timeout: 10



=== TEST 2: \
--- config
    location /test {
        set                 $test "he\\llo";
        postgres_escape     $escaped $test;
        echo                $escaped;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
'he\\llo'
--- timeout: 10



=== TEST 3: \'
--- config
    location /test {
        set                 $test "he\\'llo";
        postgres_escape     $escaped $test;
        echo                $escaped;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
'he\\''llo'
--- timeout: 10



=== TEST 4: NULL
--- config
    location /test {
        postgres_escape     $escaped $remote_user;
        echo                $escaped;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
NULL
--- timeout: 10



=== TEST 5: empty string
--- config
    location /test {
        set $empty          "";
        postgres_escape     $escaped $empty;
        echo                $escaped;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
NULL
--- timeout: 10



=== TEST 6: UTF-8
--- config
    location /test {
        set $utf8           "你好";
        postgres_escape     $escaped $utf8;
        echo                $escaped;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
'你好'
--- timeout: 10



=== TEST 7: user arg
--- config
    location /test {
        postgres_escape     $escaped $arg_say;
        echo                $escaped;
    }
--- request
GET /test?say=he'llo!
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
'he''llo!'
--- timeout: 10



=== TEST 8: NULL (empty)
--- config
    location /test {
        postgres_escape     $escaped =$remote_user;
        echo                $escaped;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
''
--- timeout: 10



=== TEST 9: empty string (empty)
--- config
    location /test {
        set $empty          "";
        postgres_escape     $escaped =$empty;
        echo                $escaped;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
''
--- timeout: 10



=== TEST 10: in-place escape
--- config
    location /test {
        set                 $test "t'\\est";
        postgres_escape     $test;
        echo                $test;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
't''\\est'
--- timeout: 10



=== TEST 11: re-useable variable name (test1)
--- config
    location /test1 {
        set                 $a "a";
        postgres_escape     $escaped $a;
        echo                $escaped;
    }
    location /test2 {
        set                 $b "b";
        postgres_escape     $escaped $b;
        echo                $escaped;
    }
--- request
GET /test1
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
'a'
--- timeout: 10



=== TEST 12: re-useable variable name (test2)
--- config
    location /test1 {
        set                 $a "a";
        postgres_escape     $escaped $a;
        echo                $escaped;
    }
    location /test2 {
        set                 $b "b";
        postgres_escape     $escaped $b;
        echo                $escaped;
    }
--- request
GET /test2
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
'b'
--- timeout: 10



=== TEST 13: concatenate multiple sources
--- config
    location /test {
        set                 $test "t'\\est";
        set                 $hello " he'llo";
        postgres_escape     $escaped "$test$hello world!";
        echo                $escaped;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
't''\\est he''llo world!'
--- timeout: 10



=== TEST 14: concatenate multiple empty sources
--- config
    location /test {
        set                 $a "";
        set                 $b "";
        postgres_escape     $escaped "$a$b";
        echo                $escaped;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
NULL
--- timeout: 10



=== TEST 15: concatenate multiple empty sources (empty)
--- config
    location /test {
        set                 $a "";
        set                 $b "";
        postgres_escape     $escaped "=$a$b";
        echo                $escaped;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
''
--- timeout: 10



=== TEST 16: in-place escape on empty string
--- config
    location /test {
        set                 $test "";
        postgres_escape     $test;
        echo                $test;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
NULL
--- timeout: 10



=== TEST 17: in-place escape on empty string (empty)
--- config
    location /test {
        set                 $test "";
        postgres_escape     =$test;
        echo                $test;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
''
--- timeout: 10



=== TEST 18: escape anonymous regex capture
--- config
    location ~ /(.*) {
        postgres_escape     $escaped $1;
        echo                $escaped;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
'test'
--- timeout: 10



=== TEST 19: escape named regex capture
--- config
    location ~ /(?<test>.*) {
        postgres_escape     $escaped $test;
        echo                $escaped;
    }
--- request
GET /test
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body
'test'
--- timeout: 10
--- skip_nginx: 3: < 0.8.25
