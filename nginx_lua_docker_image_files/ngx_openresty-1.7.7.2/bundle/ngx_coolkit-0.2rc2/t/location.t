# vi:filetype=perl

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (blocks() * 3);

worker_connections(128);
run_tests();

no_diff();

__DATA__

=== TEST 1: sanity
--- config
    location /echo {
        echo -n  $location;
    }
--- request
GET /echo
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body chop
/echo
--- timeout: 10



=== TEST 2: regex
--- config
    location ~/echo {
        echo -n  $location;
    }
--- request
GET /test/echo/regex
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body chop
/echo
--- timeout: 10
