# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(1);

plan tests => repeat_each() * 3 * blocks();

no_long_string();

run_tests();

#no_diff();

__DATA__

=== TEST 1: skipped: no callback arg given
--- config
    location /foo {
        default_type 'application/json';
        xss_get on;
        xss_callback_arg foo;
        echo '[]';
    }
--- request
GET /foo
--- response_headers_like
Content-Type: application/json
--- response_body
[]



=== TEST 2: sanity
--- config
    location /foo {
        default_type 'application/json';
        xss_get on;
        xss_callback_arg a;
        echo '[]';
    }
--- request
GET /foo?foo=blar&a=bar
--- response_headers_like
Content-Type: application/x-javascript
--- response_body chop
bar([]
);



=== TEST 3: bug
--- config
    location /foo {
        default_type 'application/json';
        xss_get on;
        xss_callback_arg foo;
        echo '[]';
    }
--- request
GET /foo?foo=bar
--- response_headers_like
Content-Type: application/x-javascript
--- response_body chop
bar([]
);



=== TEST 4: uri escaped
--- config
    location /foo {
        default_type 'application/json';
        xss_get on;
        xss_callback_arg _callback;
        echo '[]';
    }
--- request
GET /foo?_callback=OpenResty.callbackMap%5b32%5D
--- response_headers_like
Content-Type: application/x-javascript
--- response_body chop
OpenResty.callbackMap[32]([]
);



=== TEST 5: test invalid callback
--- config
    location /foo {
        default_type 'application/json';
        xss_get on;
        xss_callback_arg _callback;
        echo '[]';
    }
--- request
GET /foo?_callback=a();b
--- response_headers_like
Content-Type: application/json
--- response_body
[]
--- error_code: 200



=== TEST 6: input type mismatch
--- config
    location /foo {
        default_type 'text/plain';
        xss_get on;
        xss_callback_arg foo;
        echo '[]';
    }
--- request
GET /foo?foo=bar
--- response_headers_like
Content-Type: text/plain
--- response_body
[]



=== TEST 7: input type match by setting xss_input_types
--- config
    location /foo {
        default_type 'text/plain';
        xss_get on;
        xss_callback_arg foo;
        xss_input_types text/plain text/css;
        echo '[]';
    }
--- request
GET /foo?foo=bar
--- response_headers_like
Content-Type: application/x-javascript
--- response_body chop
bar([]
);



=== TEST 8: set a different output type
--- config
    location /foo {
        default_type 'text/plain';
        xss_get on;
        xss_callback_arg foo;
        xss_input_types text/plain text/css;
        xss_output_type text/html;
        echo '[]';
    }
--- request
GET /foo?foo=bar
--- response_headers_like
Content-Type: text/html
--- response_body chop
bar([]
);



=== TEST 9: xss_get is on while no xss_callback_arg
--- config
    location /foo {
        default_type 'application/json';
        xss_get on;
        #xss_callback_arg foo;
        echo '[]';
    }
--- request
GET /foo?foo=bar
--- response_headers_like
Content-Type: application/json
--- response_body
[]



=== TEST 10: xss_get is on while no xss_callback_arg
--- config
    location /foo {
        default_type "application/json";
        echo '{"errcode":400,"errstr":"Bad Request"}';

        xss_get on; # enable cross-site GET support
        xss_callback_arg callback; # use $arg_callback
    }
--- request
    GET /foo?callback=blah
--- response_headers
Content-Type: application/x-javascript
--- response_body chop
blah({"errcode":400,"errstr":"Bad Request"}
);



=== TEST 11: xss_get is on while no xss_callback_arg & xss_output_type
--- config
    location /foo {
        default_type "application/json";
        echo '{"errcode":400,"errstr":"Bad Request"}';

        xss_get on; # enable cross-site GET support
        xss_callback_arg callback; # use $arg_callback
        xss_output_type text/javascript;
    }
--- request
    GET /foo?callback=blah
--- response_headers
Content-Type: text/javascript
--- response_body chop
blah({"errcode":400,"errstr":"Bad Request"}
);



=== TEST 12: check status and override status
--- config
    location /foo {
        xss_get on;
        xss_callback_arg c;
        xss_check_status off;
        default_type application/json;

        content_by_lua '
            ngx.status = 404
            ngx.print("hello")
        ';
    }
--- request
    GET /foo?c=blah
--- response_body chop
blah(hello);
--- error_code: 200
--- response_headers
Content-Type: application/x-javascript



=== TEST 13: check status and NO override status
--- config
    location /foo {
        xss_get on;
        xss_callback_arg c;
        xss_override_status off;
        xss_check_status off;
        default_type application/json;

        content_by_lua '
            ngx.status = 404
            ngx.print("hello")
        ';
    }
--- request
    GET /foo?c=blah
--- response_body chop
blah(hello);
--- error_code: 404
--- response_headers
Content-Type: application/x-javascript



=== TEST 14: bug: keys started by underscore
--- config
    location /foo {
        default_type 'application/json';
        xss_get on;
        xss_callback_arg _callback;
        echo '[]';
    }
--- request
GET /foo?_callback=foo._bar
--- response_headers_like
Content-Type: application/x-javascript
--- response_body chop
foo._bar([]
);



=== TEST 15: exec
--- config
    location /foo {
        default_type "application/json";
        echo -n hello world;
        xss_get on;
        xss_callback_arg _c;
    }
    location /lua {
        echo_exec /foo $args;
    }
--- request
    GET /lua?_c=bah
--- response_headers
Content-Type: application/x-javascript
--- response_body chop
bah(hello world);

