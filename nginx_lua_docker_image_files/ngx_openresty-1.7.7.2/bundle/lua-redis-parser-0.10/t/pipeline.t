# vi:ft=

use strict;
use warnings;

use t::RedisParser;
plan tests => 1 * blocks();

run_tests();

__DATA__

=== TEST 1: single reply parsed by parse_replies
--- lua
parser = require("redis.parser")
replies = '+OK\r\n'
results = parser.parse_replies(replies, 1)
print("res count == " .. #results)
print("res[1] count == " .. #results[1])
local res = results[1]
print("res[1][1] == " .. res[1])
print("res[1][2] == " .. res[2] .. ' == ' .. parser.STATUS_REPLY)
--- out
res count == 1
res[1] count == 2
res[1][1] == OK
res[1][2] == 1 == 1



=== TEST 2: single bad reply parsed by parse_replies
--- lua
parser = require("redis.parser")
replies = '+OK'
results = parser.parse_replies(replies, 1)
print("res count == " .. #results)
print("res[1] count == " .. #results[1])
local res = results[1]
print("res[1][1] == " .. res[1])
print("res[1][2] == " .. res[2] .. ' == ' .. parser.BAD_REPLY)
--- out
res count == 1
res[1] count == 2
res[1][1] == bad status reply
res[1][2] == 0 == 0



=== TEST 3: multiple status replies
--- lua
parser = require("redis.parser")
replies = '+OK\r\n+DONE\r\n'
results = parser.parse_replies(replies, 2)
print("res count == " .. #results)
print("res[1] count == " .. #results[1])
print("res[2] count == " .. #results[2])

local res = results[1]

print("res[1][1] == " .. res[1])
print("res[1][2] == " .. res[2] .. ' == ' .. parser.STATUS_REPLY)

res = results[2]

print("res[2][1] == " .. res[1])
print("res[2][2] == " .. res[2] .. ' == ' .. parser.STATUS_REPLY)

--- out
res count == 2
res[1] count == 2
res[2] count == 2
res[1][1] == OK
res[1][2] == 1 == 1
res[2][1] == DONE
res[2][2] == 1 == 1



=== TEST 4: multiple integer replies
--- lua
parser = require("redis.parser")
replies = ':-32\r\n:532\r\n'
results = parser.parse_replies(replies, 2)
print("res count == " .. #results)
print("res[1] count == " .. #results[1])
print("res[2] count == " .. #results[2])

local res = results[1]

print("res[1][1] == " .. res[1])
print("res[1][2] == " .. res[2] .. ' == ' .. parser.INTEGER_REPLY)

res = results[2]

print("res[2][1] == " .. res[1])
print("res[2][2] == " .. res[2] .. ' == ' .. parser.INTEGER_REPLY)

--- out
res count == 2
res[1] count == 2
res[2] count == 2
res[1][1] == -32
res[1][2] == 3 == 3
res[2][1] == 532
res[2][2] == 3 == 3



=== TEST 5: multiple error replies
--- lua
parser = require("redis.parser")
replies = '-ERROR\r\n-BAD\r\n'
results = parser.parse_replies(replies, 2)
print("res count == " .. #results)
print("res[1] count == " .. #results[1])
print("res[2] count == " .. #results[2])

local res = results[1]

print("res[1][1] == " .. res[1])
print("res[1][2] == " .. res[2] .. ' == ' .. parser.ERROR_REPLY)

res = results[2]

print("res[2][1] == " .. res[1])
print("res[2][2] == " .. res[2] .. ' == ' .. parser.ERROR_REPLY)

--- out
res count == 2
res[1] count == 2
res[2] count == 2
res[1][1] == ERROR
res[1][2] == 2 == 2
res[2][1] == BAD
res[2][2] == 2 == 2



=== TEST 6: multiple bad replies (invalid reply)
--- lua
parser = require("redis.parser")
replies = '\r\n-BAD\r\n'
results = parser.parse_replies(replies, 2)
print("res count == " .. #results)
print("res[1] count == " .. #results[1])
print("res[2] count == " .. #results[2])

local res = results[1]

print("res[1][1] == " .. res[1])
print("res[1][2] == " .. res[2] .. ' == ' .. parser.BAD_REPLY)

res = results[2]

print("res[2][1] == " .. res[1])
print("res[2][2] == " .. res[2] .. ' == ' .. parser.BAD_REPLY)

--- out
res count == 2
res[1] count == 2
res[2] count == 2
res[1][1] == invalid reply
res[1][2] == 0 == 0
res[2][1] == invalid reply
res[2][2] == 0 == 0



=== TEST 7: multiple bad replies (empty reply)
--- lua
parser = require("redis.parser")
replies = ''
results = parser.parse_replies(replies, 2)
print("res count == " .. #results)
print("res[1] count == " .. #results[1])
print("res[2] count == " .. #results[2])

local res = results[1]

print("res[1][1] == " .. res[1])
print("res[1][2] == " .. res[2] .. ' == ' .. parser.BAD_REPLY)

res = results[2]

print("res[2][1] == " .. res[1])
print("res[2][2] == " .. res[2] .. ' == ' .. parser.BAD_REPLY)

--- out
res count == 2
res[1] count == 2
res[2] count == 2
res[1][1] == empty reply
res[1][2] == 0 == 0
res[2][1] == empty reply
res[2][2] == 0 == 0



=== TEST 8: multiple bulk replies
--- lua
parser = require("redis.parser")
replies = '$-1\r\n$5\r\nhello\r\n'
results = parser.parse_replies(replies, 2)
print("res count == " .. #results)
print("res[1] count == " .. #results[1])
print("res[2] count == " .. #results[2])

local res = results[1]

print("res[1][1] == " .. (res[1] or "nil"))
print("res[1][2] == " .. res[2] .. ' == ' .. parser.BULK_REPLY)

res = results[2]

print("res[2][1] == " .. res[1])
print("res[2][2] == " .. res[2] .. ' == ' .. parser.BULK_REPLY)

--- out
res count == 2
res[1] count == 2
res[2] count == 2
res[1][1] == nil
res[1][2] == 4 == 4
res[2][1] == hello
res[2][2] == 4 == 4



=== TEST 9: multiple multi-bulk replies
--- lua
cjson = require('cjson')
parser = require("redis.parser")
replies = '*2\r\n$-1\r\n$5\r\nhello\r\n*1\r\n$1\r\na\r\n$2\r\nef\r\n'
results = parser.parse_replies(replies, 2)
print("res count == " .. #results)
print("res[1] count == " .. #results[1])
print("res[2] count == " .. #results[2])

local res = results[1]

print("res[1][1] == " .. cjson.encode(res[1]))
print("res[1][2] == " .. res[2] .. ' == ' .. parser.MULTI_BULK_REPLY)

res = results[2]

print("res[2][1] == " .. cjson.encode(res[1]))
print("res[2][2] == " .. res[2] .. ' == ' .. parser.MULTI_BULK_REPLY)

--- out
res count == 2
res[1] count == 2
res[2] count == 2
res[1][1] == [null,"hello"]
res[1][2] == 5 == 5
res[2][1] == ["a"]
res[2][2] == 5 == 5



=== TEST 10: nil multi bulk reply and status reply
--- lua
parser = require("redis.parser")
replies = '*-1\r\n+DONE\r\n'
results = parser.parse_replies(replies, 2)
print("res count == " .. #results)
print("res[1] count == " .. #results[1])
print("res[2] count == " .. #results[2])

local res = results[1]

print("res[1][1] == " .. (res[1] or "nil"))
print("res[1][2] == " .. res[2] .. ' == ' .. parser.MULTI_BULK_REPLY)

res = results[2]

print("res[2][1] == " .. res[1])
print("res[2][2] == " .. res[2] .. ' == ' .. parser.STATUS_REPLY)

--- out
res count == 2
res[1] count == 2
res[2] count == 2
res[1][1] == nil
res[1][2] == 5 == 5
res[2][1] == DONE
res[2][2] == 1 == 1

