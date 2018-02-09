# vi:ft=

use strict;
use warnings;

use t::RedisParser;
plan tests => 1 * blocks();

run_tests();

__DATA__

=== TEST 1: no crlf in status reply
--- lua
parser = require("redis.parser")
print(parser._VERSION)
--- out
0.10

