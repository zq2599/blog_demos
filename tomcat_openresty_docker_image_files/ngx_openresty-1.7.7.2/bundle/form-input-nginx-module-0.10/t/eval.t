# vi:set ft= ts=4 sw=4 et fdm=marker:

use lib 'lib';
use Test::Nginx::Socket skip_all => 'not working at all';

#repeat_each(3);

plan tests => repeat_each() * 2 * blocks();

no_long_string();

run_tests();

#no_diff();

__DATA__

=== TEST 1: blank body
--- config
    location /bar1 {
        eval_subrequest_in_memory off;
        eval_override_content_type text/plain;
        eval $res {
            default_type text/plain;
            set_form_input $foo bar;
            echo $foo;
        }
        echo [$res];
    }
--- more_headers
Content-Type: application/x-www-form-urlencoded
--- request
POST /bar1
bar=3
--- response_body
[3]

