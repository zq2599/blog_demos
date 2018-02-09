# vi:filetype=perl

use lib 'lib';
use Test::Nginx::Socket;

my ($sec, $min, $hour, $mday, $mon, $year) = localtime;

our $str = sprintf("%04d-%02d-%02d\n", $year + 1900, $mon + 1, $mday);
#repeat_each(3);

plan tests => repeat_each() * 2 * blocks();

#no_long_string();

run_tests();

#no_diff();

__DATA__

=== TEST 1: sanity
--- config
    location /foo {
        set_local_today $today;
        echo $today;
    }
--- request
GET /foo
--- response_body eval: $main::str

