# vi:set ft= ts=4 sw=4 et fdm=marker:

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(3);

plan tests => repeat_each() * (3 * blocks() + 3);

no_long_string();

run_tests();

__DATA__

=== TEST 1: charset postfix
--- config
    location /bar2 {
        set_form_input $foo bar;
        echo $foo;
    }
--- more_headers
Content-Type: application/x-www-form-urlencoded; charset=UTF-8
--- request
POST /bar2
bar=32
--- response_body
32
--- timeout: 3
--- no_error_log
[error]



=== TEST 2: test case sensitivity
--- config
    location /bar2 {
        set_form_input $foo bar;
        echo $foo;
    }
--- more_headers
Content-Type: application/x-www-Form-UrlencodeD
--- request
POST /bar2
bar=32
--- response_body
32
--- timeout: 3
--- no_error_log
[error]



=== TEST 3: Internal server error when using array_map_op [calio/form-input-nginx-module GH-1]
http://github.com/calio/form-input-nginx-module/issues#issue/1
--- config
   location ~ ^/(\w+)/(\d+)/?$ {
       set_form_input    $columns;
       set_form_input    $values;
       array_split       ',' $values to=$array;
       array_map_op      set_quote_sql_str $array;
       array_join        ',' $array to=$sql_args;

       echo $sql_args;
   }
--- request
POST /users/1
columns=username,password&values=joe,secret
--- more_headers
Content-Type: application/x-www-form-urlencoded
--- response_body
'joe','secret'
--- no_error_log
[error]



=== TEST 4: 100-Continue
--- config
    location /t {
        set_form_input $foo bar;
        echo $foo;
    }
--- more_headers
Content-Type: application/x-www-form-urlencoded; charset=UTF-8
Expect: 100-Continue
--- request
POST /t
bar=32
--- ignore_response
--- no_error_log
[alert]
[error]



=== TEST 5: set_form_input_multi + missing Content-Type
--- config
location /modtest {
        set_form_input_multi $val;
        echo $val;
}
--- request
POST /modtest
val=foo&val=bar&val=baz
--- response_body eval: "\n"
--- no_error_log
[alert]
[error]



=== TEST 6: bad set_form_input + missing Content-Type
--- config
location /modtest {
        set_form_input $val foo;
        echo $val;
}
--- request
POST /modtest
val=foo&val=bar&val=baz
--- response_body eval: "\n"
--- no_error_log
[alert]
[error]



=== TEST 7: working with if and regex
--- config
location /modtest {
        set_form_input $foo foo;
        if ($foo ~* ^ab+c$) {
            echo $foo;
        }
        echo $foo;
        echo done;
}
--- request
POST /modtest
val=foo&val=bar&val=baz
--- stap2
probe process("$LIBPCRE_PATH").function("pcre_exec") {
    printf("subj: %p\n", $subject);
    printf("len: %d\n", $length);
    #print_ubacktrace()
}
--- stap_out2
--- more_headers
Content-Type: application/x-www-form-urlencoded
--- response_body eval: "\ndone\n"
--- no_error_log
[alert]
[error]



=== TEST 8: content type is not application/x-www-form-urlencoded
--- config
location /modtest {
        set_form_input $foo foo;
        if ($foo ~* ^ab+c$) {
            echo $foo;
        }
        echo $foo;
        echo done;
}
--- request
POST /modtest
val=foo&val=bar&val=baz
--- more_headers
Content-Type: multipart/form-data
--- response_body eval: "\ndone\n"
--- no_error_log
[alert]
[error]

