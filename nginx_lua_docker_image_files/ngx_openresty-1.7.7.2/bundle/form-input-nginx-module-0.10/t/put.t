# vi:set ft= ts=4 sw=4 et fdm=marker:

use lib 'lib';
use Test::Nginx::Socket;# skip_all => 'not working now';

plan tests => repeat_each() * 2 * blocks();

no_long_string();

run_tests();

#no_diff();

__DATA__

=== TEST 1: basic
--- config
    location /foo {
        set_form_input $foo name;
        echo $foo;
    }
--- more_headers
Content-Type: application/x-www-form-urlencoded
--- request
POST /foo
name=calio
--- response_body
calio



=== TEST 2: basic
--- config
    location /foo {
        set_form_input $foo name;
        set_form_input_multi $bar name;
        array_join ' ' $bar;
        echo $foo;
        echo $bar;
    }
--- more_headers
Content-Type: application/x-www-form-urlencoded
--- request
PUT /foo
name=calio&name=agentzh
--- response_body
calio
calio agentzh


