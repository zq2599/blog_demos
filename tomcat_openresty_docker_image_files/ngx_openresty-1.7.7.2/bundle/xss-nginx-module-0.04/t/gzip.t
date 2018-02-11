# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (3 * blocks() + 1);

#no_long_string();
log_level 'warn';

run_tests();

#no_diff();

__DATA__

=== TEST 1: xss_get is on while no xss_callback_arg & xss_output_type
--- config
    xss_get on; # enable cross-site GET support
    xss_callback_arg c; # use $arg_callback
    gzip on;
    gzip_types application/json application/x-javascript;

    location /foo {
        default_type "application/json";
        echo -n '{"cat":32}';
    }
--- more_headers
Accept-Encoding: gzip
--- request
    GET /foo?c=blah
--- response_headers
Content-Type: application/x-javascript
Content-Encoding: gzip
--- response_body_like eval
"\x{00}\x{00}\x{00}"

