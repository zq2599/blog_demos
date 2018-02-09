# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

plan tests => 2 * blocks();

#$Test::Nginx::LWP::LogLevel = 'debug';

run_tests();

__DATA__

=== TEST 1: sanity
--- config
    location /main {
        echo "main pre: $echo_incr";
        echo_location_async /sub;
        echo_location_async /sub;
        echo "main post: $echo_incr";
    }
    location /sub {
        echo "sub: $echo_incr";
    }
--- request
    GET /main
--- response_body
main pre: 1
sub: 3
sub: 4
main post: 2

