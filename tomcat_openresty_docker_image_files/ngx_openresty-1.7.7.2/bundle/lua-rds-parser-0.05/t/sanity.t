# vi:ft=

use strict;
use warnings;

use t::RdsParser;
plan tests => 1 * blocks();

#no_long_string();

run_tests();

__DATA__

=== TEST 1: simple select
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
"bob".  # field data
"\x{00}"  # row list terminator
--- out
{"errcode":0,"resultset":[{"id":2,"name":null},{"id":3,"name":"bob"}]}



=== TEST 2: update
--- rds eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{28}\x{00}".  # driver errstr len
"Rows matched: 1  Changed: 0  Warnings: 0".  # driver errstr data
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{00}\x{00}"  # col count
--- out
{"errstr":"Rows matched: 1  Changed: 0  Warnings: 0","errcode":0}



=== TEST 3: select empty resultset
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
"\x{00}"  # row list terminator
--- out
{"errcode":0,"resultset":{}}



=== TEST 4: update, insert id, and affected rows
--- rds eval
"\x{00}". # endian
"\x{03}\x{00}\x{00}\x{00}". # format version 0.0.3
"\x{00}". # result type
"\x{00}\x{00}".  # std errcode
"\x{00}\x{00}" . # driver errcode
"\x{28}\x{00}".  # driver errstr len
"Rows matched: 1  Changed: 0  Warnings: 0".  # driver errstr data
"\x{03}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # rows affected
"\x{10}\x{01}\x{00}\x{00}\x{00}\x{00}\x{00}\x{00}".  # insert id
"\x{00}\x{00}"  # col count
--- out
{"affected_rows":3,"errstr":"Rows matched: 1  Changed: 0  Warnings: 0","errcode":0,"insert_id":272}

