# vi:filetype=

use lib 'lib';
use Test::Nginx::Socket;

repeat_each(2);

plan tests => repeat_each() * 2 * blocks();

no_long_string();

run_tests();

#no_diff();

__DATA__

=== TEST 1: rds in a single buf (empty result set)
--- config
location = /single {
    default_type 'application/x-resty-dbd-stream';
    set_unescape_uri $rds $arg_rds;
    echo_duplicate 1 $rds;
    rds_json on;
}
--- request eval
my $rds = "\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{02}\x{00}".  # col count
"\x{01}\x{00}".  # std col type (bigint/int)
"\x{03}\x{00}".  # drizzle col type
"\x{02}\x{00}".     # col name len
"id".   # col name data
"\x{13}\x{80}".  # std col type (blob/str)
"\x{fc}\x{00}".  # drizzle col type
"\x{04}\x{00}".  # col name len
"name".  # col name data
"\x{00}";  # row list terminator

use URI::Escape;
$rds = uri_escape($rds);
"GET /single?rds=$rds"
--- response_body chop
[]



=== TEST 2: rds in a single buf (non-empty result set)
--- config
location = /single {
    default_type 'application/x-resty-dbd-stream';
    set_unescape_uri $rds $arg_rds;
    echo_duplicate 1 $rds;
    rds_json on;
}
--- request eval
my $rds =
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{02}\x{00}".  # col count
"\x{01}\x{00}".  # std col type (bigint/int)
"\x{03}\x{00}".  # drizzle col type
"\x{02}\x{00}".     # col name len
"id".   # col name data
"\x{13}\x{80}".  # std col type (blob/str)
"\x{fc}\x{00}".  # drizzle col type
"\x{04}\x{00}".  # col name len
"name".  # col name data
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"2".  # field data
"\x{ff}\x{ff}\x{ff}\x{ff}".  # field len
"".  # field data
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"3".  # field data
"\x{03}\x{00}\x{00}\x{00}".  # field len
"bob".  # field data
"\x{00}";  # row list terminator

use URI::Escape;
$rds = uri_escape($rds);
"GET /single?rds=$rds"
--- response_body chop
[{"id":2,"name":null},{"id":3,"name":"bob"}]



=== TEST 3: rds in a single buf (non-empty result set, and each row in a single buf)
--- config
location = /single {
    default_type 'application/x-resty-dbd-stream';

    set_unescape_uri $a $arg_a;
    set_unescape_uri $b $arg_b;
    set_unescape_uri $c $arg_c;
    set_unescape_uri $d $arg_d;

    echo_duplicate 1 $a;
    echo_duplicate 1 $b;
    echo_duplicate 1 $c;
    echo_duplicate 1 $d;

    rds_json on;
}
--- request eval
my $a =
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{02}\x{00}".  # col count
"\x{01}\x{00}".  # std col type (bigint/int)
"\x{03}\x{00}".  # drizzle col type
"\x{02}\x{00}".     # col name len
"id".   # col name data
"\x{13}\x{80}".  # std col type (blob/str)
"\x{fc}\x{00}".  # drizzle col type
"\x{04}\x{00}".  # col name len
"name";  # col name data

my $b =
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"2".  # field data
"\x{ff}\x{ff}\x{ff}\x{ff}".  # field len
"";  # field data

my $c =
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"3".  # field data
"\x{03}\x{00}\x{00}\x{00}".  # field len
"bob";  # field data

my $d =
"\x{00}";  # row list terminator

use URI::Escape;

$a = uri_escape($a);
$b = uri_escape($b);
$c = uri_escape($c);
$d = uri_escape($d);

"GET /single?a=$a&b=$b&c=$c&d=$d"
--- response_body chop
[{"id":2,"name":null},{"id":3,"name":"bob"}]



=== TEST 4: rds in a single buf (non-empty result set, and each row in a single buf)
--- config
location = /single {
    default_type 'application/x-resty-dbd-stream';

    set_unescape_uri $a $arg_a;
    set_unescape_uri $b $arg_b;
    set_unescape_uri $c $arg_c;

    echo -n $a;
    echo -n $b;
    echo -n $c;

    rds_json on;
}
--- request eval
my $a =
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{02}\x{00}".  # col count
"\x{01}\x{00}".  # std col type (bigint/int)
"\x{03}\x{00}".  # drizzle col type
"\x{02}\x{00}".     # col name len
"id".   # col name data
"\x{13}\x{80}".  # std col type (blob/str)
"\x{fc}\x{00}".  # drizzle col type
"\x{04}\x{00}".  # col name len
"name";  # col name data

my $b =
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"2".  # field data
"\x{ff}\x{ff}\x{ff}\x{ff}".  # field len
"";  # field data

my $c =
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}\x{00}".  # field len
"3".  # field data
"\x{03}\x{00}\x{00}\x{00}".  # field len
"bob".  # field data
"\x{00}";  # row list terminator

use URI::Escape;

$a = uri_escape($a);
$b = uri_escape($b);
$c = uri_escape($c);

"GET /single?a=$a&b=$b&c=$c"
--- response_body chop
[{"id":2,"name":null},{"id":3,"name":"bob"}]

