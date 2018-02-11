# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(3);

plan tests => repeat_each() * 2 * blocks();

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

no_shuffle();

#no_diff;
#no_long_string();

run_tests();

__DATA__

=== TEST 1: bug in nginx core? (1)
--- http_config
    upstream foo {
        server 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- config
    location /set {
        set $memc_key foo;
        set $memc_cmd set;
        set $memc_value '[["mafiaclans.eu", 12], ["picfu.net", 5], ["www.test.com", 0], ["www.ayom.com", 0], ["www.21dezember2012.org", 0], ["the-indie.ch", 0], ["spiele-check.de", 0], ["online-right-now.net", 0], ["google.com", 0]]';
        memc_pass foo;
    }
--- request
GET /set
--- response_body eval
"STORED\r\n"
--- error_code: 201



=== TEST 2: bug in nginx core? (2)
--- http_config
    upstream foo {
        server 127.0.0.1:$TEST_NGINX_MEMCACHED_PORT;
    }
--- config
    location /get {
        set $memcached_key foo;
        memcached_pass foo;
    }
--- request
GET /get
--- response_body eval
qq{[["mafiaclans.eu", 12], ["picfu.net", 5], ["www.test.com", 0], ["www.ayom.com", 0], ["www.21dezember2012.org", 0], ["the-indie.ch", 0], ["spiele-check.de", 0], ["online-right-now.net", 0], ["google.com", 0]]}

