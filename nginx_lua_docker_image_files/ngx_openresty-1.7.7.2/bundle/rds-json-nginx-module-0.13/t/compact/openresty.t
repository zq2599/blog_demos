# vi:filetype=perl

use lib 'lib';
use Test::Nginx::Socket skip_all => 'not working at all (missing data)';

#repeat_each(100);
repeat_each(2);

worker_connections(128);
workers(1);
#master_on;
log_level('warn');

plan tests => repeat_each() * 3 * blocks();

$ENV{TEST_NGINX_MYSQL_HOST} ||= '127.0.0.1';
$ENV{TEST_NGINX_MYSQL_PORT} ||= 3306;

our $http_config = <<'_EOC_';
    upstream backend {
        drizzle_server $TEST_NGINX_MYSQL_HOST:$TEST_NGINX_MYSQL_PORT protocol=mysql
                       dbname=ngx_test user=ngx_test password=ngx_test;
    }
_EOC_

our $config = <<'_EOC_';
    xss_get on;
    xss_callback_arg _callback;
    rds_json on;
    rds_json_format compact;

   location = /auth {
        #internal;
        default_type 'application/json';

        #internal;
        eval_subrequest_in_memory off;
        eval $res {
            set_quote_sql_str $user $arg_user;
            set $sql 'select count(*) res from users where name=$user';
            drizzle_query $sql;
            drizzle_pass backend;
            #echo $sql;
            #echo hi;
            rds_json on;
            rds_json_content_type application/octet-stream;
        }
        #echo $res;
        if ($res ~ '"res":1') {
            echo pass;
            break;
        }
        if ($res !~ '"res":1') {
            return 403;
            break;
        }
    }

    # XXX we should implement these in the ngx_xss module
    location @err500 { rds_json_ret 500 "Internal Server Error"; }
    location @err404 { rds_json_ret 404 "Not Found"; }
    location @err400 { rds_json_ret 400 "Bad Request"; }
    location @err403 { rds_json_ret 403 "Forbidden"; }
    location @err502 { rds_json_ret 502 "Bad Gateway"; }
    location @err503 { rds_json_ret 503 "Service Unavailable"; }

    error_page 500 = @err500;
    error_page 404 = @err404;
    error_page 403 = @err403;
    error_page 400 = @err400;
    error_page 502 = @err502;
    error_page 503 = @err503;
    error_page 504 507 = @err500;

    location = '/=/view/PostsByMonth/~/~' {
        if ($arg_year !~ '^\d{4}$') {
            rds_json_ret 400 'Bad "year" argument';
        }
        if ($arg_month !~ '^\d{1,2}$') {
            rds_json_ret 400 'Bad "month" argument';
        }

        drizzle_query
"select id, title, day(created) as day
from posts
where year(created) = $arg_year and month(created) = $arg_month
order by created asc";

        drizzle_pass backend;
    }

    location = '/=/view/RecentComments/~/~' {
        set $offset $arg_offset;
        set_if_empty $offset 0;

        set $limit $arg_limit;
        set_if_empty $limit 10;

        if ($offset !~ '^\d+$') {
            rds_json_ret 400 'Bad "offset" argument';
        }
        if ($limit !~ '^\d{1,2}$') {
            rds_json_ret 400 'Bad "limit" argument';
        }

        drizzle_query
"select comments.id as id, post, sender, title
from posts, comments
where post = posts.id
order by comments.id desc
limit $offset, $limit";

        drizzle_pass backend;
    }

    location = '/=/view/RecentPosts/~/~' {
        set $offset $arg_offset;
        set_if_empty $offset 0;

        set $limit $arg_limit;
        set_if_empty $limit 10;

        if ($offset !~ '^\d+$') {
            rds_json_ret 400 'Bad "offset" argument';
        }
        if ($limit !~ '^\d{1,2}$') {
            rds_json_ret 400 'Bad "limit" argument';
        }

        drizzle_query
"select id, title
from posts
order by id desc
limit $offset, $limit";

        drizzle_pass backend;
    }

    location = '/=/view/PrevNextPost/~/~' {
        if ($arg_current !~ '^\d+$') {
            rds_json_ret 400 'Bad "current" argument';
        }

        drizzle_query
"(select id, title
  from posts
  where id < $arg_current
  order by id desc
  limit 1)
  union
 (select id, title
  from posts
  where id > $arg_current
  order by id asc
  limit 1)";

        drizzle_pass backend;
    }

    location = '/=/view/RowCount/~/~' {
        if ($arg_model = 'Post') {
            drizzle_query "select count(*) as count from posts";
            drizzle_pass backend;
        }
        if ($arg_model = 'Comment') {
            drizzle_query "select count(*) as count from comments";
            drizzle_pass backend;
        }

        rds_json_ret 400 'Bad "model" argument';
    }

    location = '/=/view/PostCountByMonths/~/~' {
        set $offset $arg_offset;
        set_if_empty $offset 0;

        set $limit $arg_limit;
        set_if_empty $limit 10;

        if ($offset !~ '^\d+$') {
            rds_json_ret 400 'Bad "offset" argument';
        }
        if ($limit !~ '^\d{1,2}$') {
            rds_json_ret 400 'Bad "limit" argument';
        }

        drizzle_query
"select date_format(created, '%Y-%m-01') `year_month`, count(*) count
from posts
group by `year_month`
order by `year_month` desc
limit $offset, $limit";
        drizzle_pass backend;
    }

    location = '/=/view/FullPostsByMonth/~/~' {
        set $count $arg_count;
        set_if_empty $count 40;

        if ($arg_year !~ '^(?:19|20)\d{2}$') {
            rds_json_ret 400 'Bad "year" argument';
        }
        if ($arg_month !~ '^\d{1,2}$') {
            rds_json_ret 400 'Bad "month" argument';
        }
        if ($arg_count !~ '^\d+$') {
            rds_json_ret 400 'Bad "count" argument';
        }

        drizzle_query
"select * from posts
where year(created) = $arg_year and month(created) = $arg_month
order by id desc
limit $count";

        drizzle_pass backend;
    }

    location = '/=/view/PrevNextArchive/~/~' {
        if ($arg_now !~ '^\d{4}-\d{1,2}(?:-\d{1,2})?$') {
            rds_json_ret 400 'Bad "now" argument';
        }
        if ($arg_month !~ '^\d{1,2}$') {
            rds_json_ret 400 'Bad "month" argument';
        }

        drizzle_query
"(select 'next' as id, month(created) as month, year(created) as year
  from posts
  where created > $arg_now and month(created) <> $arg_month
  order by created asc
  limit 1)
  union
 (select 'prev' as id, month(created) as month, year(created) as year
  from posts
  where created < $arg_now and month(created) <> $arg_month
  order by created desc
  limit 1)";

        drizzle_pass backend;
    }

    location = '/=/batch/GetSidebar/~/~' {
        if ($arg_year !~ '^(?:19|20)\d{2}$') {
            rds_json_ret 400 'Bad "year" argument';
        }
        if ($arg_month !~ '^\d{1,2}$') {
            rds_json_ret 400 'Bad "month" argument';
        }

        default_type 'application/json';
        echo '[';
        echo_location_async '/=/view/PostsByMonth/~/~' "year=$arg_year&month=$arg_month";
        echo ',';
        echo_location_async '/=/view/RecentPosts/~/~' "offset=0&limit=6";
        echo ',';
        echo_location_async '/=/view/RecentComments/~/~' "offset=0&limit=6";
        echo ',';
        echo_location_async '/=/view/PostCountByMonths/~/~' "offset=0&limit=12";
        echo ']';
    }

    location = '/=/batch/GetFullPost/~/~' {
        if ($arg_id !~ '^\d+$') {
            rds_json_ret 400 'Bad "id" argument';
        }

        default_type 'application/json';
        echo '[';
        echo_location_async "/=/model/Post/id/$arg_id";
        echo ',';
        echo_location_async "/=/view/PrevNextPost/~/~" "current=$arg_id";
        echo ',';
        echo_location_async "/=/model/Comment/post/$arg_id" "_order_by=id:desc";
        echo ']';
    }

    location ~* '^/=/model/Post/id/(.*)$' {
        set $id $1;
        if ($id !~ '^\d+$') {
            rds_json_ret 400 'Bad "id" value';
        }

        drizzle_query "select * from posts where id = $id";
        drizzle_pass backend;
    }

    location ~* '^/=/model/Comment/post/(.*)$' {
        set $post $1;
        if ($post !~ '^\d+$') {
            rds_json_ret 400 'Bad "post" value';
        }

        drizzle_query "select * from comments where post = $post";
        drizzle_pass backend;
    }

    location = '/=/model/Post/~/~' {
        if ($arg__offset !~ '^\d+$') {
            rds_json_ret 400 'Bad "_offset" argument';
        }
        if ($arg__limit !~ '^\d{1,2}$') {
            rds_json_ret 400 'Bad "_limit" argument';
        }
        if ($arg__order_by !~ '^([A-Za-z]\w*)%3A(desc|asc)$') {
            rds_json_ret 400 'Bad "_order_by" argument';
        }
        set $col $1;
        set $order $2;
        drizzle_query
"select *
from posts
order by `$col` $order
limit $arg__offset, $arg__limit";

        drizzle_pass backend;
    }

    location = '/=/batch/NewComment/~/~' {
        default_type 'application/json';

        set_unescape_uri $sender $arg_sender;

        set_unescape_uri $email $arg_email;

        set_unescape_uri $url $arg_url;

        set_unescape_uri $body $arg_body;

        set_unescape_uri $post_id $arg_post_id;

        if ($sender !~ '\S') {
            rds_json_ret 400 "Bad \"sender\" argument";
        }
        if ($email !~ '^[-A-Za-z0-9_.]+@[-A-Za-z0-9_.]+$') {
            rds_json_ret 400 "Bad \"email\" argument";
        }
        if ($url !~ '^(?:\s*|https?://\S+)$') {
            rds_json_ret 400 "Bad \"url\" argument: $url";
        }
        if ($body ~ '^\s*$') {
            rds_json_ret 400 "Bad \"body\" argument";
        }
        if ($post_id !~ '^[1-9]\d*$') {
            rds_json_ret 400 "Bad \"post_id\" argument";
        }

        set_quote_sql_str $sender;
        set_quote_sql_str $email;
        set_quote_sql_str $url;
        set_quote_sql_str $body;

        # XXX these operations should be put into a
        # single transaction
        echo '[';

        echo_location '/=/action/RunSQL/~/~'
"insert into comments (sender, email, url, body, post)
values($sender, $email, $url, $body, $post_id)";

        echo ',';

        echo_location '/=/action/RunSQL/~/~'
"update posts
set comments = comments + 1
where id = $post_id";

        echo ']';
    }

    location = '/=/action/RunSQL/~/~' {
        internal;
        drizzle_query $query_string;
        drizzle_pass backend;
    }
_EOC_

no_long_string();

run_tests();

#no_diff();

__DATA__

=== TEST 1: PostsByMonth view (no month arg)
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /=/view/PostsByMonth/~/~?_callback=foo
--- response_headers
Content-Type: application/x-javascript
--- response_body chop
foo({"errcode":400,"errstr":"Bad \"month\" argument"});



=== TEST 2: PostsByMonth view (bad month)
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /=/view/PostsByMonth/~/~?month=1234&_callback=foo
--- response_headers
Content-Type: application/x-javascript
--- response_body chop
foo({"errcode":400,"errstr":"Bad \"month\" argument"});



=== TEST 3: PostsByMonth view (emtpy result)
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /=/view/PostsByMonth/~/~?year=1984&month=2&_callback=bar
--- response_headers
Content-Type: application/x-javascript
--- response_body chop
bar([]);



=== TEST 4: PostsByMonth view (non-emtpy result)
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /=/view/PostsByMonth/~/~?year=2009&month=10&_callback=foo
--- response_headers
Content-Type: application/x-javascript
--- response_body chop
foo([{"id":114,"title":"Hacking on the Nginx echo module","day":15}]);



=== TEST 5: PostsByMonth view (non-emtpy result)
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /=/view/PostsByMonth/~/~?year=2009&month=12&_callback=foo
--- response_headers
Content-Type: application/x-javascript
--- response_body chop
foo([{"id":117,"title":"Major updates to ngx_chunkin: lots of bug fixes and beginning of keep-alive support","day":4},{"id":118,"title":"ngx_memc: an extended version of ngx_memcached that supports set, add, delete, and many more commands","day":6},{"id":119,"title":"Test::Nginx::LWP and Test::Nginx::Socket are now on CPAN","day":8}]);
--- timeout: 90



=== TEST 6: GetSideBar
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /=/batch/GetSidebar/~/~?year=2009&month=10&_callback=foo
--- response_headers
Content-Type: application/x-javascript
--- response_body chop
foo([
[{"id":114,"title":"Hacking on the Nginx echo module","day":15}],
[{"id":119,"title":"Test::Nginx::LWP and Test::Nginx::Socket are now on CPAN"},{"id":118,"title":"ngx_memc: an extended version of ngx_memcached that supports set, add, delete, and many more commands"},{"id":117,"title":"Major updates to ngx_chunkin: lots of bug fixes and beginning of keep-alive support"},{"id":116,"title":"The \"headers more\" module: scripting input and output filters in your Nginx config file"},{"id":115,"title":"The \"chunkin\" module: Experimental chunked input support for Nginx"},{"id":114,"title":"Hacking on the Nginx echo module"}],
[{"id":179,"post":101,"sender":"agentzh","title":"生活搜基于 Firefox 3.1 的 List Hunter 集群"},{"id":178,"post":101,"sender":"Winter","title":"生活搜基于 Firefox 3.1 的 List Hunter 集群"},{"id":177,"post":100,"sender":"Mountain","title":"漂在北京"},{"id":176,"post":106,"sender":"agentzh","title":"Text::SmartLinks: The Perl 6 love for Perl 5"},{"id":175,"post":106,"sender":"gosber","title":"Text::SmartLinks: The Perl 6 love for Perl 5"},{"id":174,"post":105,"sender":"cnangel","title":"SSH::Batch: Treating clusters as maths sets and intervals"}],
[{"year_month":"2009-12-01","count":3},{"year_month":"2009-11-01","count":2},{"year_month":"2009-10-01","count":1},{"year_month":"2009-09-01","count":5},{"year_month":"2009-05-01","count":2},{"year_month":"2009-04-01","count":3},{"year_month":"2009-02-01","count":2},{"year_month":"2008-12-01","count":3},{"year_month":"2008-11-01","count":2},{"year_month":"2008-10-01","count":2},{"year_month":"2008-09-01","count":4},{"year_month":"2008-08-01","count":2}]]
);



=== TEST 7: GetFullPost
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /=/batch/GetFullPost/~/~?id=116&_user=agentzh.Public&_callback=OpenResty.callbackMap%5B1264354204389%5D
--- response_headers
Content-Type: application/x-javascript
--- response_body chop
--- SKIP



=== TEST 8: RowCount
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /=/view/RowCount/~/~?model=Post&_callback=foo
--- response_headers
Content-Type: application/x-javascript
--- response_body chop
foo([{"count":118}]);



=== TEST 9: GetFullPost bug
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /=/model/Comment/post/67
--- response_headers
Content-Type: application/json
--- response_body_like: laser
--- error_code: 200



=== TEST 10: more field data error
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /=/model/Post/~/~?_limit=5&_order_by=id%3Adesc&_offset=100
--- response_headers
Content-Type: application/json
--- response_body_like: 测试
--- error_code: 200
--- SKIP



=== TEST 11: default arguments
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /=/view/RecentComments/~/~
--- response_headers
Content-Type: application/json
--- response_body chop
[{"id":179,"post":101,"sender":"agentzh","title":"生活搜基于 Firefox 3.1 的 List Hunter 集群"},{"id":178,"post":101,"sender":"Winter","title":"生活搜基于 Firefox 3.1 的 List Hunter 集群"},{"id":177,"post":100,"sender":"Mountain","title":"漂在北京"},{"id":176,"post":106,"sender":"agentzh","title":"Text::SmartLinks: The Perl 6 love for Perl 5"},{"id":175,"post":106,"sender":"gosber","title":"Text::SmartLinks: The Perl 6 love for Perl 5"},{"id":174,"post":105,"sender":"cnangel","title":"SSH::Batch: Treating clusters as maths sets and intervals"},{"id":173,"post":106,"sender":"cnangel","title":"Text::SmartLinks: The Perl 6 love for Perl 5"},{"id":172,"post":104,"sender":"agentzh","title":"My VDOM.pm & WebKit Cluster Talk at the April Meeting of Beijing Perl Workshop"},{"id":171,"post":104,"sender":"kindy","title":"My VDOM.pm & WebKit Cluster Talk at the April Meeting of Beijing Perl Workshop"},{"id":170,"post":104,"sender":"cnangel","title":"My VDOM.pm & WebKit Cluster Talk at the April Meeting of Beijing Perl Workshop"}]
--- error_code: 200
--- timeout: 3



=== TEST 12: post a comment with empty body
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /=/batch/NewComment/~/~?sender=agentzh&email=agentzh@gmail.com&body=&post_id=3
--- response_headers
Content-Type: application/json
--- response_body chop
{"errcode":400,"errstr":"Bad \"body\" argument"}
--- error_code: 200



=== TEST 13: post a comment with valid gmail
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /=/batch/NewComment/~/~?sender=agentzh&email=agentzh%40gmail.com&body=hi&post_id=
--- response_headers
Content-Type: application/json
--- response_body chop
{"errcode":400,"errstr":"Bad \"post_id\" argument"}
--- error_code: 200



=== TEST 14: try to run the internal location
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /=/action/RunSQL/~/~?select
--- response_headers
Content-Type: application/json
--- response_body chop
{"errcode":404,"errstr":"Not Found"}
--- error_code: 200



=== TEST 15: auth
--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /auth?user=agentzh
--- response_headers
Content-Type: application/json
--- response_body
pass
--- error_code: 200
--- SKIP



=== TEST 16: auth

init db:

create table users (
    id serial primary key,
    name text not null,
    password text not null
);

insert into users
    (name, password)
    values ('agentzh', 'some_pass');

--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /auth?user=john
--- response_headers
Content-Type: application/json
--- response_body chop
{"errcode":403,"errstr":"Forbidden"}
--- error_code: 200
--- SKIP



=== TEST 17: auth

db init:

create table users (
    id serial primary key,
    name text not null,
    password text not null
);

insert into users
    (name, password)
    values ('agentzh', 'some_pass');


--- http_config eval: $::http_config
--- config eval: $::config
--- request
GET /test
--- response_headers
Content-Type: application/json
--- response_body
pass
--- error_code: 200
--- SKIP



=== TEST 18: default arguments (small pagesize)
--- http_config eval: $::http_config
--- config eval
"rds_json_buffer_size 1;
$::config"
--- request
GET /=/view/RecentComments/~/~
--- response_headers
Content-Type: application/json
--- response_body chop
[{"id":179,"post":101,"sender":"agentzh","title":"生活搜基于 Firefox 3.1 的 List Hunter 集群"},{"id":178,"post":101,"sender":"Winter","title":"生活搜基于 Firefox 3.1 的 List Hunter 集群"},{"id":177,"post":100,"sender":"Mountain","title":"漂在北京"},{"id":176,"post":106,"sender":"agentzh","title":"Text::SmartLinks: The Perl 6 love for Perl 5"},{"id":175,"post":106,"sender":"gosber","title":"Text::SmartLinks: The Perl 6 love for Perl 5"},{"id":174,"post":105,"sender":"cnangel","title":"SSH::Batch: Treating clusters as maths sets and intervals"},{"id":173,"post":106,"sender":"cnangel","title":"Text::SmartLinks: The Perl 6 love for Perl 5"},{"id":172,"post":104,"sender":"agentzh","title":"My VDOM.pm & WebKit Cluster Talk at the April Meeting of Beijing Perl Workshop"},{"id":171,"post":104,"sender":"kindy","title":"My VDOM.pm & WebKit Cluster Talk at the April Meeting of Beijing Perl Workshop"},{"id":170,"post":104,"sender":"cnangel","title":"My VDOM.pm & WebKit Cluster Talk at the April Meeting of Beijing Perl Workshop"}]
--- error_code: 200
--- timeout: 3

