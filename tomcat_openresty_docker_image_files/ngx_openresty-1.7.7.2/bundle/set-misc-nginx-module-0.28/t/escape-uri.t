# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(3);

plan tests => repeat_each() * 2 * blocks();

no_long_string();
#no_diff();

run_tests();

__DATA__

=== TEST 1: set escape uri
--- config
	location /foo {
		set $foo "hello world";
		set_escape_uri $foo $foo;
		echo $foo;
	}
--- request
GET /foo
--- response_body
hello%20world



=== TEST 2: set escape uri(in-place)
--- config
	location /foo {
		set $foo "hello world";
		set_escape_uri $foo;
		echo $foo;
	}
--- request
GET /foo
--- response_body
hello%20world



=== TEST 3: blank string
--- config
	location /foo {
		set $foo "";
		set_escape_uri $foo;
		echo $foo;
	}
--- request
GET /foo
--- response_body eval
"\n"



=== TEST 4: blank string(in place)
--- config
	location /foo {
		set $foo "";
		set_escape_uri $foo;
		echo $foo;
	}
--- request
GET /foo
--- response_body eval
"\n"



=== TEST 5: eacape chinese character
--- config
    location /foo {
        set $foo "你好";
        set_escape_uri $foo;
        echo $foo;
    }
--- request
GET /foo
--- response_body
%E4%BD%A0%E5%A5%BD



=== TEST 6: escape long string
--- config
    location /foo {
        set $foo "法规及饿哦物权法家哦低价非结果哦我二期界  附件饿哦武器  积分饿哦为契机佛i 该软件哦气氛  份额叫我起 国无二君哦气氛为界非ieowq结果哦而完全附件  份额叫我iqfjeowiqgjeriowqfjpdjfosadijfoiasdjf 附件饿哦武器界 份额叫我起界份额叫我起哦ifjefejwioq附件饿哦武器界非风格及去哦根据份额叫我起哦界份额为契机哦乳房阿基完全哦igqtewqo个人就去哦ieorjwrewqoi日哦额外起今天诶哦我亲热为特务前日哦我而哥特完全哦iijrtewmkdf 服务鄂潜江哦irewq";
        set_escape_uri $foo;
        echo $foo;
    }
--- request
GET /foo
--- response_body
%E6%B3%95%E8%A7%84%E5%8F%8A%E9%A5%BF%E5%93%A6%E7%89%A9%E6%9D%83%E6%B3%95%E5%AE%B6%E5%93%A6%E4%BD%8E%E4%BB%B7%E9%9D%9E%E7%BB%93%E6%9E%9C%E5%93%A6%E6%88%91%E4%BA%8C%E6%9C%9F%E7%95%8C%20%20%E9%99%84%E4%BB%B6%E9%A5%BF%E5%93%A6%E6%AD%A6%E5%99%A8%20%20%E7%A7%AF%E5%88%86%E9%A5%BF%E5%93%A6%E4%B8%BA%E5%A5%91%E6%9C%BA%E4%BD%9Bi%20%E8%AF%A5%E8%BD%AF%E4%BB%B6%E5%93%A6%E6%B0%94%E6%B0%9B%20%20%E4%BB%BD%E9%A2%9D%E5%8F%AB%E6%88%91%E8%B5%B7%20%E5%9B%BD%E6%97%A0%E4%BA%8C%E5%90%9B%E5%93%A6%E6%B0%94%E6%B0%9B%E4%B8%BA%E7%95%8C%E9%9D%9Eieowq%E7%BB%93%E6%9E%9C%E5%93%A6%E8%80%8C%E5%AE%8C%E5%85%A8%E9%99%84%E4%BB%B6%20%20%E4%BB%BD%E9%A2%9D%E5%8F%AB%E6%88%91iqfjeowiqgjeriowqfjpdjfosadijfoiasdjf%20%E9%99%84%E4%BB%B6%E9%A5%BF%E5%93%A6%E6%AD%A6%E5%99%A8%E7%95%8C%20%E4%BB%BD%E9%A2%9D%E5%8F%AB%E6%88%91%E8%B5%B7%E7%95%8C%E4%BB%BD%E9%A2%9D%E5%8F%AB%E6%88%91%E8%B5%B7%E5%93%A6ifjefejwioq%E9%99%84%E4%BB%B6%E9%A5%BF%E5%93%A6%E6%AD%A6%E5%99%A8%E7%95%8C%E9%9D%9E%E9%A3%8E%E6%A0%BC%E5%8F%8A%E5%8E%BB%E5%93%A6%E6%A0%B9%E6%8D%AE%E4%BB%BD%E9%A2%9D%E5%8F%AB%E6%88%91%E8%B5%B7%E5%93%A6%E7%95%8C%E4%BB%BD%E9%A2%9D%E4%B8%BA%E5%A5%91%E6%9C%BA%E5%93%A6%E4%B9%B3%E6%88%BF%E9%98%BF%E5%9F%BA%E5%AE%8C%E5%85%A8%E5%93%A6igqtewqo%E4%B8%AA%E4%BA%BA%E5%B0%B1%E5%8E%BB%E5%93%A6ieorjwrewqoi%E6%97%A5%E5%93%A6%E9%A2%9D%E5%A4%96%E8%B5%B7%E4%BB%8A%E5%A4%A9%E8%AF%B6%E5%93%A6%E6%88%91%E4%BA%B2%E7%83%AD%E4%B8%BA%E7%89%B9%E5%8A%A1%E5%89%8D%E6%97%A5%E5%93%A6%E6%88%91%E8%80%8C%E5%93%A5%E7%89%B9%E5%AE%8C%E5%85%A8%E5%93%A6iijrtewmkdf%20%E6%9C%8D%E5%8A%A1%E9%84%82%E6%BD%9C%E6%B1%9F%E5%93%A6irewq



=== TEST 7: no need to escape
--- config
    location /foo {
        set $foo 'welcometotheworldofnginx';
        set_escape_uri $foo;
        echo $foo;
    }
--- request
GET /foo
--- response_body
welcometotheworldofnginx



=== TEST 8: fixed ngx_escape_uri issues: + and / should also be escaped
--- config
    location /foo {
        set $foo '+/=';
        set_escape_uri $foo;
        echo $foo;
    }
--- request
GET /foo
--- response_body
%2B%2F%3D



=== TEST 9: fixed ngx_escape_uri issues: / {} : & [] and more
--- config
    location /foo {
        set $foo '"a/b={}:<>;&[]\\^';
        set_escape_uri $foo;
        echo $foo;
    }
--- request
GET /foo
--- response_body
%22a%2Fb%3D%7B%7D%3A%3C%3E%3B%26%5B%5D%5C%5E

