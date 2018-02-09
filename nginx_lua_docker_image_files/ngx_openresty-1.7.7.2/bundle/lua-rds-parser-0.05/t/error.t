# vi:ft=

use strict;
use warnings;

use t::RdsParser;
plan tests => 1 * blocks();

#no_long_string();

run_tests();

__DATA__

=== TEST 1: truncate at row terminator
--- rds eval
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
"\x{09}\x{00}".  # std col type (integer)
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
"bob"  # field data
--- out
nil
row flag is incomplete



=== TEST 2: truncate at field data
--- rds eval
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
"\x{09}\x{00}".  # std col type (integer)
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
"bo"  # field data
--- out
nil
field value is incomplete at offset 68, row 2, col 2



=== TEST 3: truncate at field len
--- rds eval
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
"\x{09}\x{00}".  # std col type (integer)
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
"\x{03}\x{00}\x{00}"  # field len
--- out
nil
field size is incomplete at offset 64, row 2, col 2



=== TEST 4: truncate at field len (a different cell)
--- rds eval
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
"\x{09}\x{00}".  # std col type (integer)
"\x{03}\x{00}".  # drizzle col type
"\x{02}\x{00}".     # col name len
"id".   # col name data
"\x{13}\x{80}".  # std col type (blob/str)
"\x{fc}\x{00}".  # drizzle col type
"\x{04}\x{00}".  # col name len
"name".  # col name data
"\x{01}".  # valid row flag
"\x{01}\x{00}\x{00}"  # field len
--- out
nil
field size is incomplete at offset 49, row 1, col 1



=== TEST 5: truncate at field len
--- rds eval
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
"\x{09}\x{00}".  # std col type (integer)
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
"\x{01}"  # valid row flag
--- out
nil
field size is incomplete at offset 59, row 2, col 1



=== TEST 6: truncate at col name data
--- rds eval
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
"\x{09}\x{00}".  # std col type (integer)
"\x{03}\x{00}".  # drizzle col type
"\x{02}\x{00}".     # col name len
"id".   # col name data
"\x{13}\x{80}".  # std col type (blob/str)
"\x{fc}\x{00}".  # drizzle col type
"\x{04}\x{00}".  # col name len
"nam"  # col name data
--- out
nil
column name string is incomplete



=== TEST 7: truncate at col name len
--- rds eval
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
"\x{09}\x{00}".  # std col type (integer)
"\x{03}\x{00}".  # drizzle col type
"\x{02}\x{00}".     # col name len
"id".   # col name data
"\x{13}\x{80}".  # std col type (blob/str)
"\x{fc}\x{00}".  # drizzle col type
"\x{04}"  # col name len
--- out
nil
column spec is incomplete



=== TEST 8: truncate at col type
--- rds eval
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
"\x{09}\x{00}".  # std col type (integer)
"\x{03}\x{00}".  # drizzle col type
"\x{02}\x{00}".     # col name len
"id".   # col name data
"\x{13}\x{80}".  # std col type (blob/str)
"\x{fc}"  # drizzle col type
--- out
nil
column spec is incomplete



=== TEST 9: truncate at col count
--- rds eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{02}"  # col count
--- out
nil
header part is incomplete



=== TEST 10: truncate at insert id
--- rds eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}"  # insert id
--- out
nil
header part is incomplete



=== TEST 11: truncate at rows affected
--- rds eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{00}\x{00}".  # driver errstr len
"".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}"  # rows affected
--- out
nil
header part is incomplete



=== TEST 12: truncate at errstr data
--- rds eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{03}\x{00}".  # driver errstr len
"he"  # driver errstr data
--- out
nil
header part is incomplete



=== TEST 13: truncate at errstr len
--- rds eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{03}"  # driver errstr len
--- out
nil
header part is incomplete



=== TEST 14: truncate at driver errcode
--- rds eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}"  # driver errcode
--- out
nil
header part is incomplete



=== TEST 15: truncate at std errcode
--- rds eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}"  # std errcode
--- out
nil
header part is incomplete



=== TEST 16: bad result type
--- rds eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{03}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{28}\x{00}".  # driver errstr len
"Rows matched: 1  Changed: 0  Warnings: 0".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{00}\x{00}"  # col count
--- out
nil
RDS result type must be 0 for now but got 3



=== TEST 17: bad format version
--- rds eval
"\x{00}". # endian
"\x{01}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{03}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{28}\x{00}".  # driver errstr len
"Rows matched: 1  Changed: 0  Warnings: 0".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{00}\x{00}"  # col count
--- out
nil
found RDS format version 1, but we can only handle version 3

