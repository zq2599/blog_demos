# vi:ft= et ts=4 sw=4

use lib 't/lib';
use Test::Resty;

plan tests => blocks() * 3;

run_tests();

__DATA__

=== TEST 1: bad --nginx value
--- opts: --nginx=/tmp/no/such/file
--- src
print("arg 0: ", arg[0])
print("arg 1: ", arg[1])
print("arg 2: ", arg[2])
print("arg 3: ", arg[3])

--- out
--- err_like chop
(?:Can't exec|valgrind:) "?/tmp/no/such/file"?: No such file or directory
--- ret: 2



=== TEST 2: -V
--- opts: -V
--- out
--- err_like eval
qr/^resty \d+.\d{2}
nginx version: /s
--- ret: 0



=== TEST 3: -h
--- opts: -h
--- out
resty [options] [lua-file [args]]

Options:
    -c num      set maximal connection count (default: 64).
    -e prog     run the inlined Lua code in "prog".
    --help      print this help.
    -I dir      Add dir to the search paths for Lua libraries.
    --nginx     specify the nginx path (this option might be removed in the future).
    -V          print version numbers and nginx configurations.
    --valgrind  use valgrind to run the underyling nginx

    --valgrind-opts     pass extra options to valgrind

For bug reporting instructions, please see:
<http://openresty.org/#Community>
--- err
--- ret: 0

