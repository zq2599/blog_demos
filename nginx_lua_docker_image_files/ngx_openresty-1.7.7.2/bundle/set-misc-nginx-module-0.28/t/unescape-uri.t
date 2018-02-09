# vi:filetype=perl

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(3);

plan tests => repeat_each() * 2 * blocks();

no_long_string();

run_tests();

#no_diff();

__DATA__

=== TEST 1: set unescape uri
buggy?
--- config
    location /foo {
        set $foo "hello%20world";
        set_unescape_uri $foo $foo;
        echo $foo;
    }
--- request
GET /foo
--- response_body
hello world



=== TEST 2: set unescape uri (in-place)
buggy?
--- config
    location /foo {
        set $foo "hello%20world";
        set_unescape_uri $foo;
        echo $foo;
    }
--- request
GET /foo
--- response_body
hello world



=== TEST 3: unescape '+' to ' '
--- config
    location /bar {
        set $a 'a+b';
        set_unescape_uri $a;
        echo $a;
    }
--- request
    GET /bar
--- response_body
a b


