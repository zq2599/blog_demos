# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (2 * blocks() + 5);

#$Test::Nginx::LWP::LogLevel = 'debug';

run_tests();

__DATA__

=== TEST 1: sanity
--- config
    location /echo {
        echo hello;
    }
--- request
    GET /echo
--- response_body
hello



=== TEST 2: multiple args
--- config
    location /echo {
        echo say hello world;
    }
--- request
    GET /echo
--- response_body
say hello world



=== TEST 3: multiple directive instances
--- config
    location /echo {
        echo say that;
        echo hello;
        echo world !;
    }
--- request
    GET /echo
--- response_body
say that
hello
world !



=== TEST 4: echo without arguments
--- config
    location /echo {
        echo;
        echo;
    }
--- request
    GET /echo
--- response_body eval
"\n\n"



=== TEST 5: escaped newline
--- config
    location /echo {
        echo "hello\nworld";
    }
--- request
    GET /echo
--- response_body
hello
world



=== TEST 6: escaped tabs and \r and " wihtin "..."
--- config
    location /echo {
        echo "i say \"hello\tworld\"\r";
    }
--- request
    GET /echo
--- response_body eval: "i say \"hello\tworld\"\r\n"



=== TEST 7: escaped tabs and \r and " in single quotes
--- config
    location /echo {
        echo 'i say \"hello\tworld\"\r';
    }
--- request
    GET /echo
--- response_body eval: "i say \"hello\tworld\"\r\n"



=== TEST 8: escaped tabs and \r and " w/o any quotes
--- config
    location /echo {
        echo i say \"hello\tworld\"\r;
    }
--- request
    GET /echo
--- response_body eval: "i say \"hello\tworld\"\r\n"



=== TEST 9: escaping $
As of Nginx 0.8.20, there's still no way to escape the '$' character.
--- config
    location /echo {
        echo \$;
    }
--- request
    GET /echo
--- response_body
$
--- SKIP



=== TEST 10: XSS
--- config
    location /blah {
        echo_duplicate 1 "$arg_callback(";
        echo_location_async "/data?$uri";
        echo_duplicate 1 ")";
    }
    location /data {
        echo_duplicate 1 '{"dog":"$query_string"}';
    }
--- request
    GET /blah/9999999.json?callback=ding1111111
--- response_body chomp
ding1111111({"dog":"/blah/9999999.json"})



=== TEST 11: XSS - filter version
--- config
    location /blah {
        echo_before_body "$arg_callback(";

        echo_duplicate 1 '{"dog":"$uri"}';

        echo_after_body ")";
    }
--- request
    GET /blah/9999999.json?callback=ding1111111
--- response_body
ding1111111(
{"dog":"/blah/9999999.json"})



=== TEST 12: if
--- config
location /first {
 echo "before";
 echo_location_async /second $request_uri;
 echo "after";
}

location = /second {
 if ($query_string ~ '([^?]+)') {
     set $memcached_key $1;  # needing this to be keyed on the request_path, not the entire uri
     echo $memcached_key;
 }
}
--- request
    GET /first/9999999.json?callback=ding1111111
--- response_body
before
/first/9999999.json
after



=== TEST 13: echo -n
--- config
    location /echo {
        echo -n hello;
        echo -n world;
    }
--- request
    GET /echo
--- response_body chop
helloworld



=== TEST 14: echo a -n
--- config
    location /echo {
        echo a -n hello;
        echo b -n world;
    }
--- request
    GET /echo
--- response_body
a -n hello
b -n world



=== TEST 15: -n in a var
--- config
    location /echo {
        set $opt -n;
        echo $opt hello;
        echo $opt world;
    }
--- request
    GET /echo
--- response_body
-n hello
-n world



=== TEST 16: -n only
--- config
    location /echo {
        echo -n;
        echo -n;
    }
--- request
    GET /echo
--- response_body chop



=== TEST 17: -n with an empty string
--- config
    location /echo {
        echo -n "";
        set $empty "";
        echo -n $empty;
    }
--- request
    GET /echo
--- response_body chop



=== TEST 18: -- -n
--- config
    location /echo {
        echo -- -n hello;
        echo -- -n world;
    }
--- request
    GET /echo
--- response_body
-n hello
-n world



=== TEST 19: -n -n
--- config
    location /echo {
        echo -n -n hello;
        echo -n -n world;
    }
--- request
    GET /echo
--- response_body chop
helloworld



=== TEST 20: -n -- -n
--- config
    location /echo {
        echo -n -- -n hello;
        echo -n -- -n world;
    }
--- request
    GET /echo
--- response_body chop
-n hello-n world



=== TEST 21: proxy
--- config
    location /main {
        proxy_pass http://127.0.0.1:$server_port/echo;
    }
    location /echo {
        echo hello;
        echo world;
    }
--- request
    GET /main
--- response_headers
!Content-Length
--- response_body
hello
world



=== TEST 22: if is evil
--- config
    location /test {
        set $a 3;
        set_by_lua $a '
            if ngx.var.a == "3" then
                return 4
            end
        ';
        echo $a;
    }
--- request
    GET /test
--- response_body
4
--- SKIP



=== TEST 23: HEAD
--- config
    location /echo {
        echo hello;
        echo world;
    }
--- request
    HEAD /echo
--- response_body



=== TEST 24: POST
--- config
    location /echo {
        echo hello;
        echo world;
    }
--- pipelined_requests eval
["POST /echo
blah blah", "POST /echo
foo bar baz"]
--- response_body eval
["hello\nworld\n","hello\nworld\n"]



=== TEST 25: POST
--- config
    location /echo {
        echo_sleep 0.001;
        echo hello;
        echo world;
    }
--- pipelined_requests eval
["POST /echo
blah blah", "POST /echo
foo bar baz"]
--- response_body eval
["hello\nworld\n","hello\nworld\n"]



=== TEST 26: empty arg after -n (github issue #33)
--- config
    location = /t {
        set $empty "";
        echo -n $empty hello world;
    }
--- request
    GET /t
--- response_body chop
 hello world

