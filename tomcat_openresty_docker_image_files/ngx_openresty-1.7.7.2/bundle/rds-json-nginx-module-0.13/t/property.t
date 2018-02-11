# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * 2 * blocks();

$ENV{TEST_NGINX_MYSQL_HOST} ||= '127.0.0.1';
$ENV{TEST_NGINX_MYSQL_PORT} ||= 3306;

our $http_config = <<'_EOC_';
    upstream backend {
        drizzle_server $TEST_NGINX_MYSQL_HOST:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
    }
_EOC_

no_diff();
no_long_string();

run_tests();

__DATA__

=== TEST 1: sanity
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_query '
            select * from cats order by id asc
        ';
        drizzle_pass backend;
        rds_json on;
        rds_json_root rows;
    }
--- request
GET /mysql
--- response_body chomp
{"rows":[{"id":2,"name":null},{"id":3,"name":"bob"}]}



=== TEST 2: update (root)
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        #drizzle_dbname $dbname;
        drizzle_query "update cats set name='bob' where name='bob'";
        rds_json on;
        rds_json_root data;
    }
--- request
GET /mysql
--- response_body chop
{"errcode":0,"errstr":"Rows matched: 1  Changed: 0  Warnings: 0"}



=== TEST 3: select empty result
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        drizzle_query "select * from cats where name='tom'";
        rds_json on;
        rds_json_root data;
    }
--- request
GET /mysql
--- response_body chop
{"data":[]}



=== TEST 4: sanity + compact
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_query '
            select * from cats order by id asc
        ';
        drizzle_pass backend;
        rds_json on;
        rds_json_root rows;
        rds_json_format compact;
    }
--- request
GET /mysql
--- response_body chomp
{"rows":[["id","name"],[2,null],[3,"bob"]]}



=== TEST 5: select empty result + compact
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        drizzle_query "select * from cats where name='tom'";
        rds_json on;
        rds_json_root data;
        rds_json_format compact;
    }
--- request
GET /mysql
--- response_body chop
{"data":[["id","name"]]}



=== TEST 6: select empty result + compact + escaping
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        drizzle_query "select * from cats where name='tom'";
        rds_json on;
        rds_json_root "'\"\\:";
        rds_json_format compact;
    }
--- request
GET /mysql
--- response_body chop
{"'\"\\:":[["id","name"]]}



=== TEST 7: success property (inherited)
--- http_config eval: $::http_config
--- config
    rds_json_success_property "suc";
    location /mysql {
        drizzle_pass backend;
        drizzle_query "select * from cats where name='tom'";
        rds_json on;
        rds_json_root "rows";
        rds_json_format compact;
    }
--- request
GET /mysql
--- response_body chop
{"suc":true,"rows":[["id","name"]]}



=== TEST 8: success property (root inherited)
--- http_config eval: $::http_config
--- config
    rds_json_root "rows";
    location /mysql {
        drizzle_pass backend;
        drizzle_query "select * from cats where name='tom'";
        rds_json on;
        rds_json_success_property "suc";
        rds_json_format compact;
    }
--- request
GET /mysql
--- response_body chop
{"suc":true,"rows":[["id","name"]]}



=== TEST 9: success property
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        drizzle_query "select * from cats where name='tom'";
        rds_json on;
        #rds_json_root "data";
        rds_json_success_property "suc";
        rds_json_format compact;
    }
--- request
GET /mysql
--- response_body chop
{"suc":true,"data":[["id","name"]]}



=== TEST 10: success property with an odd key
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        drizzle_query "select * from cats where name='tom'";
        rds_json on;
        rds_json_root "data";
        rds_json_success_property "a\"'\\:";
        rds_json_format compact;
    }
--- request
GET /mysql
--- response_body chop
{"a\"'\\:":true,"data":[["id","name"]]}



=== TEST 11: update (root + success prop)
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        #drizzle_dbname $dbname;
        drizzle_query "update cats set name='bob' where name='bob'";
        rds_json on;
        rds_json_root rows;
        rds_json_success_property success;
    }
--- request
GET /mysql
--- response_body chop
{"success":true,"errcode":0,"errstr":"Rows matched: 1  Changed: 0  Warnings: 0"}



=== TEST 12: update (user property)
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        #drizzle_dbname $dbname;
        drizzle_query "update cats set name='bob' where name='bob'";
        rds_json on;

        set $name 'Jimmy';
        rds_json_user_property name $name;
    }
--- request
GET /mysql
--- response_body chop
{"name":"Jimmy","errcode":0,"errstr":"Rows matched: 1  Changed: 0  Warnings: 0"}



=== TEST 13: update (multi user property)
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        #drizzle_dbname $dbname;
        drizzle_query "update cats set name='bob' where name='bob'";
        rds_json on;

        set $name 'Jimmy"';
        set $age 32;
        rds_json_user_property name $name;
        rds_json_user_property age $age;
    }
--- request
GET /mysql
--- response_body chop
{"name":"Jimmy\"","age":"32","errcode":0,"errstr":"Rows matched: 1  Changed: 0  Warnings: 0"}



=== TEST 14: compact + user property
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_query '
            select * from cats order by id asc
        ';
        drizzle_pass backend;
        rds_json on;
        rds_json_root rows;
        set $val 'bar"';
        rds_json_user_property foo $val;
        rds_json_format compact;
    }
--- request
GET /mysql
--- response_body chomp
{"foo":"bar\"","rows":[["id","name"],[2,null],[3,"bob"]]}



=== TEST 15: compact + user property + success
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_query '
            select * from cats order by id asc
        ';
        drizzle_pass backend;

        rds_json on;
        rds_json_root rows;

        rds_json_success_property suc;

        set $val 'bar"';
        rds_json_user_property foo $val;

        set $baz '"baz"\\';
        rds_json_user_property '\\bar' $baz;
        rds_json_format compact;
    }
--- request
GET /mysql
--- response_body chomp
{"suc":true,"foo":"bar\"","\\bar":"\"baz\"\\","rows":[["id","name"],[2,null],[3,"bob"]]}



=== TEST 16: rds_json_ret with success property
--- http_config eval: $::http_config
--- config
    location /ret {
        rds_json_success_property ret;

        rds_json_ret 400 'Non zero ret';
    }
--- request
GET /ret
--- response_body chomp
{"errcode":400,"errstr":"Non zero ret","ret":false}



=== TEST 17: rds_json_ret with user property
--- http_config eval: $::http_config
--- config
    location /ret {
        set $city 'beijing';
        rds_json_user_property city $city;
        rds_json_ret 400 'Non zero ret';
    }
--- request
GET /ret
--- response_body chomp
{"errcode":400,"errstr":"Non zero ret","city":"beijing"}



=== TEST 18: rds_json_ret with property
--- http_config eval: $::http_config
--- config
    location /ret {
        rds_json_success_property ret;

        set $city 'beijing';
        rds_json_user_property city $city;

        rds_json_ret 400 'Non zero ret';
    }
--- request
GET /ret
--- response_body chomp
{"errcode":400,"errstr":"Non zero ret","ret":false,"city":"beijing"}



=== TEST 19: rds_json_ret when errcode equal 0
--- http_config eval: $::http_config
--- config
    location /ret {
        rds_json_success_property ret;

        set $city 'beijing';
        rds_json_user_property city $city;

        rds_json_ret 0 'Zero errcode';
    }
--- request
GET /ret
--- response_body chomp
{"errcode":0,"errstr":"Zero errcode","ret":true,"city":"beijing"}



=== TEST 20: rds_json_ret with multiple user properties
--- http_config eval: $::http_config
--- config
    location /ret {
        set $city 'beijing';
        rds_json_user_property city $city;
        rds_json_user_property '"hi"' '"hello\n"';
        rds_json_ret 400 'Non zero ret';
    }
--- request
GET /ret
--- response_body chomp
{"errcode":400,"errstr":"Non zero ret","city":"beijing","\"hi\"":"\"hello\n\""}



=== TEST 21: location internal rewrite
--- config
    location @err403 {
        rds_json_success_property ret;
        rds_json_ret 403 "Forbidden";
    }

    location /foo {
        error_page 403 = @err403;
        return 403;
    }
--- request
    GET /foo
--- response_body chop
{"errcode":403,"errstr":"Forbidden","ret":false}



=== TEST 22: rds_json_errcode_key
--- config
    location /foo {
        rds_json_errcode_key "ecode";
        rds_json_success_property ret;
        rds_json_ret 403 "Forbidden";
    }
--- request
    GET /foo
--- response_body chop
{"ecode":403,"errstr":"Forbidden","ret":false}



=== TEST 23: rds_json_errcode_key
--- config
    rds_json_errcode_key "ecoderoot";

    location /foo {
        rds_json_success_property ret;
        rds_json_ret 403 "Forbidden";
    }
--- request
    GET /foo
--- response_body chop
{"ecoderoot":403,"errstr":"Forbidden","ret":false}



=== TEST 24: rds_json_errcode_key
--- config
    rds_json_errcode_key "ecoderoot";

    location /foo {
        rds_json_errcode_key "ecode";
        rds_json_success_property ret;
        rds_json_ret 403 "Forbidden";
    }
--- request
    GET /foo
--- response_body chop
{"ecode":403,"errstr":"Forbidden","ret":false}



=== TEST 25: rds_json_errstr_key
--- config
    rds_json_errstr_key "msg";

    location /foo {
        rds_json_success_property ret;
        rds_json_ret 403 "Forbidden";
    }
--- request
    GET /foo
--- response_body chop
{"errcode":403,"msg":"Forbidden","ret":false}



=== TEST 26: rds_json_errstr_key
--- config
    rds_json_errstr_key "msg";

    location /foo {
        rds_json_errstr_key "msg2";
        rds_json_success_property ret;
        rds_json_ret 403 "Forbidden";
    }
--- request
    GET /foo
--- response_body chop
{"errcode":403,"msg2":"Forbidden","ret":false}



=== TEST 27: rds_json_errstr_key & rds_json_root
--- config
    rds_json_errstr_key "msg2";
    rds_json_root rows;

    location /foo {
        rds_json_success_property ret;
        rds_json_ret 403 "Forbidden";
    }
--- request
    GET /foo
--- response_body chop
{"errcode":403,"msg2":"Forbidden","ret":false}



=== TEST 28: rds_json_errcode_key & rds_json_root
--- config
    rds_json_root rows;
    rds_json_errcode_key "code";

    location /foo {
        rds_json_success_property ret;
        rds_json_ret 403 "Forbidden";
    }
--- request
    GET /foo
--- response_body chop
{"code":403,"errstr":"Forbidden","ret":false}



=== TEST 29: update - custom errstr_key
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        #drizzle_dbname $dbname;
        drizzle_query "update cats set name='bob' where name='bob'";
        rds_json on;
        rds_json_errcode_key "\"code\"";
        rds_json_errstr_key "\"str\"";

        set $name 'Jimmy"';
        set $age 32;
        rds_json_user_property name $name;
        rds_json_user_property age $age;
    }
--- request
GET /mysql
--- response_body chop
{"name":"Jimmy\"","age":"32","\"code\"":0,"\"str\"":"Rows matched: 1  Changed: 0  Warnings: 0"}



=== TEST 30: select - custom errstr_key
--- http_config eval: $::http_config
--- config
    location /mysql {
        drizzle_pass backend;
        drizzle_query "select 'aaa' as a, 'bbb' as b";
        rds_json on;
        rds_json_errcode_key "\"code\"";
        rds_json_errstr_key "\"str\"";

        rds_json_root data;

        set $name 'Jimmy"';
        set $age 32;
        rds_json_user_property name $name;
        rds_json_user_property age $age;
    }
--- request
GET /mysql
--- response_body chop
{"name":"Jimmy\"","age":"32","data":[{"a":"aaa","b":"bbb"}]}
