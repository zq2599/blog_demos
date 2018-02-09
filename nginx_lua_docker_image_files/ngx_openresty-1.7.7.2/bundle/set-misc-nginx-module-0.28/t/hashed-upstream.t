# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(3);

plan tests => repeat_each() * 2 * blocks();

no_long_string();

#master_on();
#log_level("warn");

run_tests();

#no_diff();

__DATA__

=== TEST 1: set hashed upstream
buggy?
--- config
    upstream_list universe moon sun earth;
    location /foo {
        set_hashed_upstream $backend universe $arg_id;
        echo $backend;
    }
    location /main {
        echo_location_async /foo;
        echo_location_async /foo?id=hello;
        echo_location_async /foo?id=world;
        echo_location_async /foo?id=larry;
        echo_location_async /foo?id=audreyt;
    }
--- request
GET /main
--- response_body
moon
sun
moon
earth
earth



=== TEST 2: set hashed upstream (use var for upstream_list name)
buggy?
--- config
    upstream_list universe moon sun earth;
    location /foo {
        set $list_name universe;
        set_hashed_upstream $backend $list_name $arg_id;
        echo $backend;
    }
    location /main {
        echo_location_async /foo;
        echo_location_async /foo?id=hello;
        echo_location_async /foo?id=world;
        echo_location_async /foo?id=larry;
        echo_location_async /foo?id=audreyt;
    }
--- request
GET /main
--- response_body
moon
sun
moon
earth
earth

