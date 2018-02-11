# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

#repeat_each(3);
repeat_each(1);

plan tests => repeat_each() * 2 * blocks();

no_long_string();
#no_shuffle();

run_tests();

#no_diff();

__DATA__

=== TEST 1: array split/join
--- config
    location /foo {
        array_split ',' $arg_names to=$names;
        array_join '+' $names;
        echo $names;
    }
--- request
GET /foo?names=Bob,Marry,John
--- response_body
Bob+Marry+John



=== TEST 2: array split/join (non-empty sep with a limit)
--- config
    location /foo {
        array_split ',' $arg_names 2 to=$names;
        array_join '+' $names;
        echo $names;
    }
--- request
GET /foo?names=Bob,Marry,John
--- response_body
Bob+Marry,John



=== TEST 3: array split/join (non-empty sep with a ZERO limit)
--- config
    location /foo {
        array_split ',' $arg_names 0 to=$names;
        array_join '+' $names;
        echo $names;
    }
--- request
GET /foo?names=Bob,Marry,John
--- response_body
Bob+Marry+John



=== TEST 4: array split/join (emtpy sep with a limit)
--- config
    location /foo {
        array_split '' $arg_names 2 to=$names;
        array_join '+' $names;
        echo $names;
    }
--- request
GET /foo?names=Bob
--- response_body
B+ob



=== TEST 5: array split/join (emtpy sep with a ZERO limit)
--- config
    location /foo {
        array_split '' $arg_names 0 to=$names;
        array_join '+' $names;
        echo $names;
    }
--- request
GET /foo?names=Bob
--- response_body
B+o+b



=== TEST 6: array split (empty split sep)
--- config
    location /foo {
        array_split '' $arg_names to=$names;
        array_join '+' $names;
        echo $names;
    }
--- request
GET /foo?names=Bob
--- response_body
B+o+b



=== TEST 7: array split (empty split/join sep)
--- config
    location /foo {
        array_split '' $arg_names to=$names;
        array_join '' $names;
        echo [$names];
    }
--- request
GET /foo?names=Bob
--- response_body
[Bob]



=== TEST 8: array split (empty split + empty input)
--- config
    location /foo {
        array_split '' $arg_names to=$names;
        array_join '+' $names;
        echo [$names];
    }
--- request
GET /foo?
--- response_body
[]



=== TEST 9: array split/join (single item)
--- config
    location /foo {
        array_split ',' $arg_names to=$names;
        array_join '+' $names;
        echo $names;
    }
--- request
GET /foo?names=nomas
--- response_body
nomas



=== TEST 10: array split/join (empty array)
--- config
    location /foo {
        array_split ',' $arg_names to=$names;
        array_join '+' $names;
        echo "[$names]";
    }
--- request
GET /foo?
--- response_body
[]



=== TEST 11: array split/join (multi-char sep)
--- config
    location /foo {
        array_split '->' $arg_names to=$names;
        array_join '(+)' $names;
        echo "$names";
    }
--- request
GET /foo?names=a->b->c
--- response_body
a(+)b(+)c



=== TEST 12: array split/join (list of empty values)
--- config
    location /foo {
        array_split ',' $arg_names to=$names;
        array_join '+' $names;
        echo "[$names]";
    }
--- request
GET /foo?names=,,,
--- response_body
[+++]



=== TEST 13: array map
--- config
    location /foo {
        array_split ',' $arg_names to=$names;
        array_map 'hi' $names;
        array_join '+' $names;
        echo "[$names]";
    }
--- request
GET /foo?names=,,,
--- response_body
[hi+hi+hi+hi]



=== TEST 14: array map (in-place)
--- config
    location /foo {
        array_split ',' $arg_names to=$names;
        array_map '[$array_it]' $names;
        array_join '+' $names;
        echo "$names";
    }
--- request
GET /foo?names=bob,marry,nomas
--- response_body
[bob]+[marry]+[nomas]



=== TEST 15: array map (copy)
--- config
    location /foo {
        array_split ',' $arg_names to=$names;
        array_map '[$array_it]' $names to=$names2;
        array_join '+' $names;
        array_join '+' $names2;
        echo "$names";
        echo "$names2";
    }
--- request
GET /foo?names=bob,marry,nomas
--- response_body
bob+marry+nomas
[bob]+[marry]+[nomas]



=== TEST 16: array map (empty values)
--- config
    location /foo {
        array_split ',' $arg_names to=$names;
        array_map '[$array_it]' $names;
        array_join '+' $names;
        echo "$names";
    }
--- request
GET /foo?names=,marry,nomas
--- response_body
[]+[marry]+[nomas]



=== TEST 17: non-in-place join
--- config
    location /foo {
        array_split ',' $arg_names to=$names;
        array_join '+' $names to=$res;
        array_join '-' $names to=$res2;
        echo $res;
        echo $res2;
    }
--- request
GET /foo?names=bob,marry,nomas
--- response_body
bob+marry+nomas
bob-marry-nomas



=== TEST 18: non-in-place join
--- config
    location /foo {
        array_split ',' $arg_names to=$names;
        array_join '+' $names to=$res;
        array_join '-' $names to=$res2;
        echo $res;
        echo $res2;
    }
--- request
GET /foo?names=bob,marry,nomas
--- response_body
bob+marry+nomas
bob-marry-nomas



=== TEST 19: map op (in-place)
--- config
    location /foo {
        array_split ',' $arg_names to=$names;
        array_map_op set_quote_sql_str $names;
        array_join '+' $names to=$res;
        echo $res;
    }
--- request
GET /foo?names=bob,marry,nomas
--- response_body
'bob'+'marry'+'nomas'



=== TEST 20: map op (copy)
--- config
    location /foo {
        array_split ',' $arg_names to=$names;
        array_map_op set_quote_sql_str $names to=$list;
        array_join '+' $list to=$res;
        echo $res;
    }
--- request
GET /foo?names=bob,marry,nomas
--- response_body
'bob'+'marry'+'nomas'



=== TEST 21: map op (quote special chars)
--- config
    location /foo {
        array_split ',' $arg_names to=$names;
        array_map_op set_quote_sql_str $names;
        array_join '+' $names to=$res;
        echo $res;
    }
--- request
GET /foo?names=',\
--- response_body
'\''+'\\'



=== TEST 22: $array_it gets cleared after array map
--- config
    location /foo {
        array_split ',' $arg_names to=$names;
        array_map '[$array_it]' $names;
        echo "[$array_it]";
    }
--- request
GET /foo?names=bob,marry,nomas
--- response_body
[]



=== TEST 23: map op (copy) on set_quote_pgsql_str
--- config
    location /foo {
        array_split ',' $arg_names to=$names;
        array_map_op set_quote_pgsql_str $names to=$list;
        array_join '+' $list to=$res;
        echo $res;
    }
--- request
GET /foo?names=bob,marry,nomas
--- response_body
E'bob'+E'marry'+E'nomas'



=== TEST 24: map op (copy) on set_quote_json_str
--- config
    location /foo {
        array_split ',' $arg_names to=$names;
        array_map_op set_quote_json_str $names to=$list;
        array_join '+' $list to=$res;
        echo $res;
    }
--- request
GET /foo?names=bob,marry,nomas
--- response_body
"bob"+"marry"+"nomas"

