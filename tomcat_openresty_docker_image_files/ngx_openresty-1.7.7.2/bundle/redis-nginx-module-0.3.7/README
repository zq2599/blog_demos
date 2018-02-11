nginx HTTP redis module
--

Description:
--

The nginx HTTP redis module for caching with redis,
http://code.google.com/p/redis/.

The redis protocol
(http://code.google.com/p/redis/wiki/ProtocolSpecification)
not yet fully implemented, but GET and SELECT commands only.



Installation:
--

You'll need to re-compile Nginx from source to include this module.
Modify your compile of Nginx by adding the following directive
(modified to suit your path of course):

./configure --add-module=/absolute/path/to/ngx_http_redis
make
make install



Usage:
--

Example 1.


http
{
 ...
        server {
                location / {
                        set $redis_key  "$uri?$args";
                        redis_pass      127.0.0.1:6379;
                        error_page      404 502 504 = @fallback;
                }

                location @fallback {
                        proxy_pass      backed;
                }
        }
}


Example 2.

Capture User-Agent from an HTTP header, query to redis database
for lookup appropriate backend.

Eval module (http://www.grid.net.ru/nginx/eval.en.html) required.

http
{
 ...
    upstream redis {
        server  127.0.0.1:6379;
    }

    server {
     ...
        location / {

	    eval_escalate on;

	    eval $answer {
		set $redis_key	"$http_user_agent";
		redis_pass	redis;
	    }

            proxy_pass $answer;
        }
        ...
     }
}


Example 3.

Compile nginx with ngx_http_redis and ngx_http_gunzip_filter modules
(http://mdounin.ru/hg/ngx_http_gunzip_filter_module/).
Gzip content and put it into redis database.

% cat index.html
<html><body>Hello from redis!</body></html>
% gzip index.html
% cat index.html.gz | redis-cli -x set /index.html
OK
% cat index.html.gz | redis-cli -x set /
OK
%

Using following configuration nginx get data from redis database and
gunzip it.

http
{
 ...
    upstream redis {
        server  127.0.0.1:6379;
    }

    server {
     ...
        location / {
                gunzip on;
                redis_gzip_flag 1;
                set $redis_key "$uri";
                redis_pass redis;
    }
}



Thanks to:
--

Maxim Dounin
Vsevolod Stakhov
Ezra Zygmuntowicz



Special thanks to:
--
Evan Miller for his "Guide To Nginx Module Development" and "Advanced Topics
In Nginx Module Development"
Valery Kholodkov for his "Nginx modules development"
