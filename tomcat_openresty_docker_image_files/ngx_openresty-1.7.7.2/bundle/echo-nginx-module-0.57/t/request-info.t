# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * (3 * blocks() + 15);

run_tests();

__DATA__

=== TEST 1: standalone directive
--- config
    location /echo {
        echo -n $echo_client_request_headers;
    }
--- request
    GET /echo
--- response_body eval
"GET /echo HTTP/1.1\r
Host: localhost\r
Connection: close\r
\r
"
--- no_error_log
[error]



=== TEST 2: multiple instances
--- config
    location /echo {
        echo -n $echo_client_request_headers;
        echo -n $echo_client_request_headers;
    }
--- request
    GET /echo
--- response_body eval
"GET /echo HTTP/1.1\r
Host: localhost\r
Connection: close\r
\r
GET /echo HTTP/1.1\r
Host: localhost\r
Connection: close\r
\r
"
--- no_error_log
[error]



=== TEST 3: does not explicitly request_body
--- config
    location /echo {
        echo [$echo_request_body];
    }
--- request
POST /echo
body here
heh
--- response_body
[]
--- no_error_log
[error]



=== TEST 4: let proxy read request_body
--- config
    location /echo {
        echo_before_body [$echo_request_body];
        proxy_pass $scheme://127.0.0.1:$server_port/blah;
    }
    location /blah { echo_duplicate 0 ''; }
--- request
POST /echo
body here
heh
--- response_body
[body here
heh]
--- no_error_log
[error]



=== TEST 5: use echo_read_request_body to read it!
--- config
    location /echo {
        echo_read_request_body;
        echo [$echo_request_body];
    }
--- request
POST /echo
body here
heh
--- response_body
[body here
heh]
--- no_error_log
[error]



=== TEST 6: how about sleep after that?
--- config
    location /echo {
        echo_read_request_body;
        echo_sleep 0.002;
        echo [$echo_request_body];
    }
--- request
POST /echo
body here
heh
--- response_body
[body here
heh]
--- no_error_log
[error]



=== TEST 7: echo back the whole client request
--- config
  # echo back the client request
  location /echoback {
    echo -n $echo_client_request_headers;
    echo_read_request_body;
    echo $echo_request_body;
  }
--- request
POST /echoback
body here
haha
--- response_body eval
"POST /echoback HTTP/1.1\r
Host: localhost\r
Connection: close\r
Content-Length: 14\r
\r
body here
haha
"
--- no_error_log
[error]



=== TEST 8: echo_request_body
--- config
    location /body {
      client_body_buffer_size    5;
      echo_read_request_body;
      echo "[$echo_request_body]";
      echo_request_body;
    }
--- request eval
"POST /body
" . ('a' x 2048) . "b"
--- response_body eval
"[]\n" .
('a' x 2048) . "b"
--- no_error_log
[error]



=== TEST 9: $echo_response_status in content handler
--- config
    location /status {
        echo "status: $echo_response_status";
    }
--- request
    GET /status
--- response_body
status: 
--- no_error_log
[error]



=== TEST 10: echo_request_body (empty body)
--- config
    location /body {
      echo_read_request_body;
      echo_request_body;
    }
    location /main {
        proxy_pass http://127.0.0.1:$server_port/body;
    }
--- request eval
"POST /main"
--- response_body eval
""
--- no_error_log
[error]



=== TEST 11: small header
--- config
    location /t {
        echo -n $echo_client_request_headers;
    }
--- request
GET /t
--- response_body eval
qq{GET /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
\r
}
--- no_error_log
[error]
--- no_error_log
[error]



=== TEST 12: large header
--- config
    client_header_buffer_size 10;
    large_client_header_buffers 30 561;
    location /t {
        echo -n $echo_client_request_headers;
    }
--- request
GET /t
--- more_headers eval
CORE::join "\n", map { "Header$_: value-$_" } 1..512

--- response_body eval
qq{GET /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
}
.(CORE::join "\r\n", map { "Header$_: value-$_" } 1..512) . "\r\n\r\n"

--- no_error_log
[error]



=== TEST 13: small header, with leading CRLF
--- config
    location /t {
        echo -n $echo_client_request_headers;
    }
--- raw_request eval
"\r\nGET /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
\r
"
--- response_body eval
qq{GET /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
\r
}
--- no_error_log
[error]



=== TEST 14: large header, with leading CRLF
--- config
    client_header_buffer_size 10;
    large_client_header_buffers 30 561;
    location /t {
        echo -n $echo_client_request_headers;
    }

--- raw_request eval
"\r\nGET /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
".
(CORE::join "\r\n", map { "Header$_: value-$_" } 1..512) . "\r\n\r\n"

--- response_body eval
qq{GET /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
}
.(CORE::join "\r\n", map { "Header$_: value-$_" } 1..512) . "\r\n\r\n"

--- no_error_log
[error]



=== TEST 15: small header, pipelined
--- config
    location /t {
        echo -n $echo_client_request_headers;
    }
--- pipelined_requests eval
["GET /t", "GET /th"]

--- more_headers
Foo: bar

--- response_body eval
[qq{GET /t HTTP/1.1\r
Host: localhost\r
Connection: keep-alive\r
Foo: bar\r
\r
}, qq{GET /th HTTP/1.1\r
Host: localhost\r
Connection: close\r
Foo: bar\r
\r
}]
--- no_error_log
[error]



=== TEST 16: large header, pipelined
--- config
    client_header_buffer_size 10;
    large_client_header_buffers 30 561;
    location /t {
        echo -n $echo_client_request_headers;
    }
--- pipelined_requests eval
["GET /t", "GET /t"]

--- more_headers eval
CORE::join "\n", map { "Header$_: value-$_" } 1..512

--- response_body eval
my $headers = (CORE::join "\r\n", map { "Header$_: value-$_" } 1..512) . "\r\n\r\n";

[qq{GET /t HTTP/1.1\r
Host: localhost\r
Connection: keep-alive\r
$headers},
qq{GET /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
$headers}]

--- no_error_log
[error]



=== TEST 17: small header, multi-line header
--- config
    location /t {
        echo -n $echo_client_request_headers;
    }
--- raw_request eval
"GET /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
Foo: bar baz\r
  blah\r
\r
"
--- response_body eval
qq{GET /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
Foo: bar baz\r
  blah\r
\r
}
--- no_error_log
[error]



=== TEST 18: large header, multi-line header
--- config
    client_header_buffer_size 10;
    large_client_header_buffers 50 567;
    location /t {
        echo -n $echo_client_request_headers;
    }

--- raw_request eval
my $headers = (CORE::join "\r\n", map { "Header$_: value-$_\r\n hello $_ world blah blah" } 1..512) . "\r\n\r\n";

qq{GET /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
$headers}

--- response_body eval
qq{GET /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
}
.(CORE::join "\r\n", map { "Header$_: value-$_\r\n hello $_ world blah blah" } 1..512) . "\r\n\r\n"

--- no_error_log
[error]



=== TEST 19: small header (POST body)
--- config
    location /t {
        echo_read_request_body;
        echo -n $echo_client_request_headers;
    }
--- request
POST /t
hello
--- response_body eval
qq{POST /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
Content-Length: 5\r
\r
}
--- no_error_log
[error]



=== TEST 20: small header (POST body) - in subrequests (location)
--- config
    location /t {
        echo -n $echo_client_request_headers;
    }
    location /main {
        echo_location /t;
    }

--- request
POST /main
hello
--- response_body eval
qq{POST /main HTTP/1.1\r
Host: localhost\r
Connection: close\r
Content-Length: 5\r
\r
}
--- no_error_log
[error]



=== TEST 21: large header (POST body)
--- config
    client_header_buffer_size 10;
    large_client_header_buffers 30 561;
    location /t {
        echo_read_request_body;
        echo -n $echo_client_request_headers;
    }
--- request
POST /t
hello

--- more_headers eval
CORE::join"\n", map { "Header$_: value-$_" } 1..512

--- response_body eval
qq{POST /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
}
.(CORE::join "\r\n", map { "Header$_: value-$_" } 1..512) . "\r\nContent-Length: 5\r\n\r\n"

--- no_error_log
[error]
--- timeout: 5



=== TEST 22: large header (POST body) - in subrequests
--- config
    client_header_buffer_size 10;
    large_client_header_buffers 30 561;
    location /t {
        echo_read_request_body;
        echo -n $echo_client_request_headers;
    }

    location /main {
        echo_location /t;
    }
--- request
POST /main
hello
--- more_headers eval
CORE::join"\n", map { "Header$_: value-$_" } 1..512

--- response_body eval
qq{POST /main HTTP/1.1\r
Host: localhost\r
Connection: close\r
}
.(CORE::join "\r\n", map { "Header$_: value-$_" } 1..512) . "\r\nContent-Length: 5\r\n\r\n"

--- no_error_log
[error]
--- timeout: 5



=== TEST 23: raw headers - the default header buffer can hold the request line, but not the header entries
--- config
    location /t {
        echo_read_request_body;
        echo -n $echo_client_request_headers;
    }
--- request
GET /t
--- more_headers eval
my $s = "User-Agent: curl\nBah: bah\n";
$s .= "Accept: */*\n";
$s .= "Cookie: " . "C" x 1200 . "\n";
$s
--- response_body eval
"GET /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
User-Agent: curl\r
Bah: bah\r
Accept: */*\r
Cookie: " . ("C" x 1200) . "\r\n\r\n"
--- no_error_log
[error]



=== TEST 24: small header (POST body) - in subrequests (location_async)
--- config
    location /t {
        echo -n $echo_client_request_headers;
    }
    location /main {
        echo_location_async /t;
    }

--- request
POST /main
hello
--- response_body eval
qq{POST /main HTTP/1.1\r
Host: localhost\r
Connection: close\r
Content-Length: 5\r
\r
}
--- no_error_log
[error]



=== TEST 25: small header (POST body) - in subrequests (subrequest)
--- config
    location /t {
        echo -n $echo_client_request_headers;
    }
    location /main {
        echo_subrequest GET /t;
    }

--- request
POST /main
hello
--- response_body eval
qq{POST /main HTTP/1.1\r
Host: localhost\r
Connection: close\r
Content-Length: 5\r
\r
}
--- no_error_log
[error]



=== TEST 26: small header (POST body) - in subrequests (subrequest_async)
--- config
    location /t {
        echo -n $echo_client_request_headers;
    }
    location /main {
        echo_subrequest_async GET /t;
    }

--- request
POST /main
hello
--- response_body eval
qq{POST /main HTTP/1.1\r
Host: localhost\r
Connection: close\r
Content-Length: 5\r
\r
}
--- no_error_log
[error]



=== TEST 27: ngx_proxy/ngx_fastcgi/etc change r->header_end to point to their own buffers
--- config
    location = /t {
        proxy_buffering off;
        proxy_pass http://127.0.0.1:$server_port/bad;
        proxy_intercept_errors on;
        error_page 500 = /500;
    }

    location = /bad {
        return 500;
    }

    location = /500 {
        echo -n $echo_client_request_headers;
    }
--- request
GET /t
--- response_body eval
"GET /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
\r
"
--- no_error_log
[error]



=== TEST 28: ngx_proxy/ngx_fastcgi/etc change r->header_end to point to their own buffers (exclusive LF in the request data)
--- config
    location = /t {
        proxy_buffering off;
        proxy_pass http://127.0.0.1:$server_port/bad;
        proxy_intercept_errors on;
        error_page 500 = /500;
    }

    location = /bad {
        return 500;
    }

    location = /500 {
        internal;
        echo -n $echo_client_request_headers;
    }
--- raw_request eval
"GET /t HTTP/1.1
Host: localhost
Connection: close
Content-Length: 5

hello"
--- response_body eval
"GET /t HTTP/1.1
Host: localhost
Connection: close
Content-Length: 5

"
--- no_error_log
[error]



=== TEST 29: ngx_proxy/ngx_fastcgi/etc change r->header_end to point to their own buffers (mixed LF and CRLF in the request data)
--- config
    location = /t {
        proxy_buffering off;
        proxy_pass http://127.0.0.1:$server_port/bad;
        proxy_intercept_errors on;
        error_page 500 = /500;
    }

    location = /bad {
        return 500;
    }

    location = /500 {
        internal;
        echo -n $echo_client_request_headers;
    }
--- raw_request eval
"GET /t HTTP/1.1\r
Host: localhost
Connection: close\r
Content-Length: 5\r

hello"
--- response_body eval
"GET /t HTTP/1.1\r
Host: localhost
Connection: close\r
Content-Length: 5\r

"
--- no_error_log
[error]



=== TEST 30: ngx_proxy/ngx_fastcgi/etc change r->header_end to point to their own buffers (another way of mixing LF and CRLF in the request data)
--- config
    location = /t {
        proxy_buffering off;
        proxy_pass http://127.0.0.1:$server_port/bad;
        proxy_intercept_errors on;
        error_page 500 = /500;
    }

    location = /bad {
        return 500;
    }

    location = /500 {
        internal;
        echo -n $echo_client_request_headers;
    }
--- raw_request eval
"GET /t HTTP/1.1\r
Host: localhost
Connection: close\r
Content-Length: 5
\r
hello"
--- response_body eval
"GET /t HTTP/1.1\r
Host: localhost
Connection: close\r
Content-Length: 5
\r
"
--- no_error_log
[error]



=== TEST 31: two pipelined requests with large headers
--- config
    client_header_buffer_size 10;
    large_client_header_buffers 3 5610;
    location /t {
        echo -n $echo_client_request_headers;
    }
--- pipelined_requests eval
["GET /t", "GET /t"]
--- more_headers eval
CORE::join "\n", map { "Header$_: value-$_" } 1..585

--- response_body eval
[qq{GET /t HTTP/1.1\r
Host: localhost\r
Connection: keep-alive\r
}
.(CORE::join "\r\n", map { "Header$_: value-$_" } 1..585) . "\r\n\r\n",
qq{GET /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
}
.(CORE::join "\r\n", map { "Header$_: value-$_" } 1..585) . "\r\n\r\n",
,
]

--- no_error_log
[error]
--- timeout: 5



=== TEST 32: a request with large header and a smaller pipelined request following
--- config
    client_header_buffer_size 10;
    large_client_header_buffers 2 1921;
    location /t {
        echo -n $echo_client_request_headers;
    }
--- pipelined_requests eval
["GET /t", "GET /t"]
--- more_headers eval
[CORE::join("\n", map { "Header$_: value-$_" } 1..170), "Foo: bar\n"]

--- response_body eval
[qq{GET /t HTTP/1.1\r
Host: localhost\r
Connection: keep-alive\r
}
.(CORE::join "\r\n", map { "Header$_: value-$_" } 1..170) . "\r\n\r\n",
qq{GET /t HTTP/1.1\r
Host: localhost\r
Connection: close\r
Foo: bar\r
\r
},
]

--- no_error_log
[error]
--- timeout: 5



=== TEST 33: a request with large header and a smaller pipelined request following
--- config
    client_header_buffer_size 10;
    large_client_header_buffers 2 1921;
    location /t {
        echo -n $echo_client_request_headers;
    }
--- pipelined_requests eval
["GET /t", "GET /t" . ("a" x 512)]
--- more_headers eval
[CORE::join("\n", map { "Header$_: value-$_" } 1..170), "Foo: bar\n"]

--- response_body eval
[qq{GET /t HTTP/1.1\r
Host: localhost\r
Connection: keep-alive\r
}
.(CORE::join "\r\n", map { "Header$_: value-$_" } 1..170) . "\r\n\r\n",
qq{GET /t} . ("a" x 512) . qq{ HTTP/1.1\r
Host: localhost\r
Connection: close\r
Foo: bar\r
\r
},
]

--- no_error_log
[error]
--- timeout: 5

