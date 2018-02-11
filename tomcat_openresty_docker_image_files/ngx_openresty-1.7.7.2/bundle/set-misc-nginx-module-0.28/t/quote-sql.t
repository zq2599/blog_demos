# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(3);

plan tests => repeat_each() * 2 * blocks();

no_long_string();

run_tests();

#no_diff();

__DATA__

=== TEST 1: set quote sql value
--- config
    location /foo {
        set $foo "hello\n\r'\"\\";
        set_quote_sql_str $foo $foo;
        echo $foo;
    }
--- request
GET /foo
--- response_body
'hello\n\r\'\"\\'



=== TEST 2: set quote sql value (in place)
--- config
    location /foo {
        set $foo "hello\n\r'\"\\";
        set_quote_sql_str $foo;
        echo $foo;
    }
--- request
GET /foo
--- response_body
'hello\n\r\'\"\\'



=== TEST 3: set quote empty sql value
--- config
    location /foo {
        set $foo "";
        set_quote_sql_str $foo;
        echo $foo;
    }
--- request
GET /foo
--- response_body
''



=== TEST 4: set quote null sql value
--- config
    location /foo {
        set_quote_sql_str $foo;
        echo $foo;
    }
--- request
GET /foo
--- response_body
''



=== TEST 5: set quote null pgsql value
--- config
    location /foo {
        set_quote_pgsql_str $foo;
        echo $foo;
    }
--- request
GET /foo
--- response_body
''



=== TEST 6: set quote pgsql value
--- config
    location /foo {
        set $foo "hello\n\r'\"\\";
        set_quote_pgsql_str $foo;
        echo $foo;
    }
--- request
GET /foo
--- response_body
E'hello\n\r\'\"\\'



=== TEST 7: set quote pgsql valid utf8 value
--- config
    location /foo {
        set $foo "你好";
        set_quote_pgsql_str $foo;
        echo $foo;
    }
--- request
GET /foo
--- response_body
E'你好'



=== TEST 8: set quote pgsql invalid utf8 value
--- config
    location /foo {
        set $foo "你好";
        set_iconv $foo $foo from=utf-8 to=gbk;
        set_quote_pgsql_str $foo;
        echo $foo;
    }
--- request
GET /foo
--- response_body
E'\\304\\343\\272\\303'



=== TEST 9: \0 for mysql
--- config
    location /foo {
        set_unescape_uri $foo $arg_a;
        set_quote_sql_str $foo $foo;
        echo $foo;
    }
--- request
GET /foo?a=a%00b%00
--- response_body
'a\0b\0'



=== TEST 10: \b for mysql
--- config
    location /foo {
        set_unescape_uri $foo $arg_a;
        set_quote_sql_str $foo $foo;
        echo $foo;
    }
--- request
GET /foo?a=a%08b%08
--- response_body
'a\bb\b'



=== TEST 11: \t for mysql
--- config
    location /foo {
        set_unescape_uri $foo $arg_a;
        set_quote_sql_str $foo $foo;
        echo $foo;
    }
--- request
GET /foo?a=a%09b%09
--- response_body
'a\tb\t'



=== TEST 12: \Z for mysql
--- config
    location /foo {
        set_unescape_uri $foo $arg_a;
        set_quote_sql_str $foo $foo;
        echo $foo;
    }
--- request
GET /foo?a=a%1ab%1a
--- response_body
'a\Zb\Z'

