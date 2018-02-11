# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(1);

plan tests => repeat_each() * 2 * blocks();

#$Test::Nginx::LWP::LogLevel = 'debug';

run_tests();

__DATA__

=== TEST 1: sanity
--- config
    location /gzip {
      gzip             on;
      gzip_min_length  10;
      gzip_types       text/plain;

      echo_duplicate   1000 hello;
    }
--- request
    GET /gzip
--- more_headers
Accept-Encoding: gzip
--- response_headers
Content-Encoding: gzip
--- timeout: 20
