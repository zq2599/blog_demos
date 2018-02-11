# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

plan tests => repeat_each() * (4 * blocks());

$ENV{TEST_NGINX_MEMCACHED_PORT} ||= 11211;

#no_diff;

run_tests();

__DATA__

=== TEST 1: module not used
--- config
    location /t {
        content_by_lua '
            local v = ngx.var
            ngx.say(v.memc_cmd, v.memc_exptime, v.memc_value, v.memc_key, v.memc_flags)
        ';
    }
--- request
    GET /t
--- stap
F(ngx_http_memc_add_variable) {
    printf("memc add variable \"%s\"\n", user_string_n($name->data, $name->len))
}
--- stap_out
--- response_body
nilnilnilnilnil
--- no_error_log
[error]



=== TEST 2: module used
--- config
    location /t {
        content_by_lua '
            local v = ngx.var
            ngx.say(v.memc_cmd, v.memc_exptime, v.memc_value, v.memc_key, v.memc_flags)
        ';
    }
    location /bah {
        set $memc_key foo;
        memc_pass 127.0.0.1:11211;
    }
--- request
    GET /t
--- stap
F(ngx_http_memc_add_variable) {
    printf("memc add variable \"%s\"\n", user_string_n($name->data, $name->len))
}
--- stap_out
memc add variable "memc_key"
memc add variable "memc_cmd"
memc add variable "memc_flags"
memc add variable "memc_exptime"
memc add variable "memc_value"

--- response_body
nilnilnilnilnil
--- no_error_log
[error]

