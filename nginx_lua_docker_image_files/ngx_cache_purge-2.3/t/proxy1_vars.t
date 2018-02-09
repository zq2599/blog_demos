# vi:filetype=perl

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(1);

plan tests => repeat_each() * (blocks() * 4 + 3 * 1);

our $http_config = <<'_EOC_';
    proxy_cache_path  /tmp/ngx_cache_purge_cache keys_zone=test_cache:10m;
    proxy_temp_path   /tmp/ngx_cache_purge_temp 1 2;
_EOC_

our $config = <<'_EOC_';
    set $cache  test_cache;

    location /proxy {
        proxy_pass         $scheme://127.0.0.1:$server_port/etc/passwd;
        proxy_cache        $cache;
        proxy_cache_key    $uri$is_args$args;
        proxy_cache_valid  3m;
        add_header         X-Cache-Status $upstream_cache_status;
    }

    location ~ /purge(/.*) {
        proxy_cache_purge  $cache $1$is_args$args;
    }

    location = /etc/passwd {
        root               /;
    }
_EOC_

worker_connections(128);
no_shuffle();
run_tests();

no_diff();

__DATA__

=== TEST 1: prepare
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /proxy/passwd
--- error_code: 200
--- response_headers
Content-Type: text/plain
--- response_body_like: root
--- timeout: 10
--- no_error_log eval
qr/\[(warn|error|crit|alert|emerg)\]/
--- skip_nginx: 4: < 1.7.9



=== TEST 2: get from cache
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /proxy/passwd
--- error_code: 200
--- response_headers
Content-Type: text/plain
X-Cache-Status: HIT
--- response_body_like: root
--- timeout: 10
--- no_error_log eval
qr/\[(warn|error|crit|alert|emerg)\]/
--- skip_nginx: 5: < 1.7.9



=== TEST 3: purge from cache
--- http_config eval: $::http_config
--- config eval: $::config
--- request
PURGE /purge/proxy/passwd
--- error_code: 200
--- response_headers
Content-Type: text/html
--- response_body_like: Successful purge
--- timeout: 10
--- no_error_log eval
qr/\[(warn|error|crit|alert|emerg)\]/
--- skip_nginx: 4: < 1.7.9



=== TEST 4: purge from empty cache
--- http_config eval: $::http_config
--- config eval: $::config
--- request
PURGE /purge/proxy/passwd
--- error_code: 404
--- response_headers
Content-Type: text/html
--- response_body_like: 404 Not Found
--- timeout: 10
--- no_error_log eval
qr/\[(warn|error|crit|alert|emerg)\]/
--- skip_nginx: 4: < 1.7.9



=== TEST 5: get from source
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /proxy/passwd
--- error_code: 200
--- response_headers
Content-Type: text/plain
X-Cache-Status: MISS
--- response_body_like: root
--- timeout: 10
--- no_error_log eval
qr/\[(warn|error|crit|alert|emerg)\]/
--- skip_nginx: 5: < 1.7.9



=== TEST 6: get from cache
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /proxy/passwd
--- error_code: 200
--- response_headers
Content-Type: text/plain
X-Cache-Status: HIT
--- response_body_like: root
--- timeout: 10
--- no_error_log eval
qr/\[(warn|error|crit|alert|emerg)\]/
--- skip_nginx: 5: < 1.7.9
