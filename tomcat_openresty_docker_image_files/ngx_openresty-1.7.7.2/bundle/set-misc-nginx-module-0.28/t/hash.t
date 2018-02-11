# vi:filetype=perl

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(3);

plan tests => repeat_each() * 2 * blocks();

no_long_string();

run_tests();

#no_diff();

__DATA__

=== TEST 1: sha1 hello (copy)
--- config
    location /sha1 {
        set_sha1 $a hello;
        echo $a;
    }
--- request
GET /sha1
--- response_body
aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d



=== TEST 2: sha1 hello (in-place)
--- config
    location /sha1 {
        set $a hello;
        set_sha1 $a;
        echo $a;
    }
--- request
GET /sha1
--- response_body
aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d



=== TEST 3: sha1 (empty)
--- config
    location /sha1 {
        set_sha1 $a "";
        echo $a;
    }
--- request
GET /sha1
--- response_body
da39a3ee5e6b4b0d3255bfef95601890afd80709



=== TEST 4: md5 hello (copy)
--- config
    location /md5 {
        set_md5 $a hello;
        echo $a;
    }
--- request
GET /md5
--- response_body
5d41402abc4b2a76b9719d911017c592



=== TEST 5: md5 hello (in-place)
--- config
    location /md5 {
        set $a hello;
        set_md5 $a;
        echo $a;
    }
--- request
GET /md5
--- response_body
5d41402abc4b2a76b9719d911017c592



=== TEST 6: md5 (empty)
--- config
    location /md5 {
        set_md5 $a "";
        echo $a;
    }
--- request
GET /md5
--- response_body
d41d8cd98f00b204e9800998ecf8427e

