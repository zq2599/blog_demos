# vi:filetype=perl

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(1);

plan tests => repeat_each() * (blocks() * 4 + 6 * 1);

our $http_config = <<'_EOC_';
    proxy_cache_path  /tmp/ngx_cache_purge_cache keys_zone=test_cache:10m;
    proxy_temp_path   /tmp/ngx_cache_purge_temp 1 2;
_EOC_

our $config = <<'_EOC_';
    proxy_cache_purge  on;

    location /proxy {
        proxy_pass         $scheme://127.0.0.1:$server_port/etc/passwd;
        proxy_cache        test_cache;
        proxy_cache_key    $uri$is_args$args;
        proxy_cache_valid  3m;
        add_header         X-Cache-Status $upstream_cache_status;

        if ($uri)          { }
    }

    location = /etc/passwd {
        root               /;
    }
_EOC_

our $config_allowed = <<'_EOC_';
    proxy_cache_purge  PURGE from 1.0.0.0/8 127.0.0.0/8 3.0.0.0/8;

    location /proxy {
        proxy_pass         $scheme://127.0.0.1:$server_port/etc/passwd;
        proxy_cache        test_cache;
        proxy_cache_key    $uri$is_args$args;
        proxy_cache_valid  3m;
        add_header         X-Cache-Status $upstream_cache_status;
    }

    location = /etc/passwd {
        root               /;
    }
_EOC_

our $config_forbidden = <<'_EOC_';
    proxy_cache_purge  PURGE from 1.0.0.0/8;

    location /proxy {
        proxy_pass         $scheme://127.0.0.1:$server_port/etc/passwd;
        proxy_cache        test_cache;
        proxy_cache_key    $uri$is_args$args;
        proxy_cache_valid  3m;
        add_header         X-Cache-Status $upstream_cache_status;
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
--- skip_nginx2: 4: < 0.8.3 or < 0.7.62



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
--- skip_nginx2: 5: < 0.8.3 or < 0.7.62



=== TEST 3: purge from cache
--- http_config eval: $::http_config
--- config eval: $::config
--- request
PURGE /proxy/passwd
--- error_code: 200
--- response_headers
Content-Type: text/html
--- response_body_like: Successful purge
--- timeout: 10
--- no_error_log eval
qr/\[(warn|error|crit|alert|emerg)\]/
--- skip_nginx2: 4: < 0.8.3 or < 0.7.62



=== TEST 4: purge from empty cache
--- http_config eval: $::http_config
--- config eval: $::config
--- request
PURGE /proxy/passwd
--- error_code: 404
--- response_headers
Content-Type: text/html
--- response_body_like: 404 Not Found
--- timeout: 10
--- no_error_log eval
qr/\[(warn|error|crit|alert|emerg)\]/
--- skip_nginx2: 4: < 0.8.3 or < 0.7.62



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
--- skip_nginx2: 5: < 0.8.3 or < 0.7.62



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
--- skip_nginx2: 5: < 0.8.3 or < 0.7.62



=== TEST 7: purge from cache (PURGE allowed)
--- http_config eval: $::http_config
--- config eval: $::config_allowed
--- request
PURGE /proxy/passwd
--- error_code: 200
--- response_headers
Content-Type: text/html
--- response_body_like: Successful purge
--- timeout: 10
--- no_error_log eval
qr/\[(warn|error|crit|alert|emerg)\]/
--- skip_nginx2: 4: < 0.8.3 or < 0.7.62



=== TEST 8: purge from empty cache (PURGE allowed)
--- http_config eval: $::http_config
--- config eval: $::config_allowed
--- request
PURGE /proxy/passwd
--- error_code: 404
--- response_headers
Content-Type: text/html
--- response_body_like: 404 Not Found
--- timeout: 10
--- no_error_log eval
qr/\[(warn|error|crit|alert|emerg)\]/
--- skip_nginx2: 4: < 0.8.3 or < 0.7.62



=== TEST 9: get from source (PURGE allowed)
--- http_config eval: $::http_config
--- config eval: $::config_allowed
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
--- skip_nginx2: 5: < 0.8.3 or < 0.7.62



=== TEST 10: get from cache (PURGE allowed)
--- http_config eval: $::http_config
--- config eval: $::config_allowed
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
--- skip_nginx2: 5: < 0.8.3 or < 0.7.62



=== TEST 11: purge from cache (PURGE not allowed)
--- http_config eval: $::http_config
--- config eval: $::config_forbidden
--- request
PURGE /proxy/passwd
--- error_code: 403
--- response_headers
Content-Type: text/html
--- response_body_like: 403 Forbidden
--- timeout: 10
--- no_error_log eval
qr/\[(warn|error|crit|alert|emerg)\]/
--- skip_nginx2: 4: < 0.8.3 or < 0.7.62



=== TEST 12: get from cache (PURGE not allowed)
--- http_config eval: $::http_config
--- config eval: $::config_forbidden
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
--- skip_nginx2: 5: < 0.8.3 or < 0.7.62



=== TEST 13: no cache (PURGE allowed)
--- http_config eval: $::http_config
--- config
    proxy_cache_purge  PURGE from 1.0.0.0/8 127.0.0.0/8 3.0.0.0/8;

    location /proxy {
        proxy_pass         $scheme://127.0.0.1:$server_port/etc/passwd;
    }

    location = /etc/passwd {
        root               /;
    }
--- request
PURGE /proxy/passwd
--- error_code: 404
--- response_headers
Content-Type: text/html
--- response_body_like: 404 Not Found
--- timeout: 10
--- no_error_log eval
qr/\[(warn|error|crit|alert|emerg)\]/
--- skip_nginx2: 4: < 0.8.3 or < 0.7.62



=== TEST 14: no cache (PURGE not allowed)
--- http_config eval: $::http_config
--- config
    proxy_cache_purge  PURGE from 1.0.0.0/8;

    location /proxy {
        proxy_pass         $scheme://127.0.0.1:$server_port/etc/passwd;
    }

    location = /etc/passwd {
        root               /;
    }
--- request
PURGE /proxy/passwd
--- error_code: 403
--- response_headers
Content-Type: text/html
--- response_body_like: 403 Forbidden
--- timeout: 10
--- no_error_log eval
qr/\[(warn|error|crit|alert|emerg)\]/
--- skip_nginx2: 4: < 0.8.3 or < 0.7.62



=== TEST 15: multiple cache purge directives
--- http_config eval: $::http_config
--- config
    fastcgi_cache_purge  on;
    proxy_cache_purge    on;

    location /proxy {
        proxy_pass         $scheme://127.0.0.1:$server_port/etc/passwd;
        proxy_cache        test_cache;
        proxy_cache_key    $uri$is_args$args;
        proxy_cache_valid  3m;
        add_header         X-Cache-Status $upstream_cache_status;

        if ($uri)          { }
    }

    location = /etc/passwd {
        root               /;
    }
--- request
PURGE /proxy/passwd
--- error_code: 200
--- response_headers
Content-Type: text/html
--- response_body_like: Successful purge
--- timeout: 10
--- no_error_log eval
qr/\[(warn|error|crit|alert|emerg)\]/
--- skip_nginx2: 4: < 0.8.3 or < 0.7.62
