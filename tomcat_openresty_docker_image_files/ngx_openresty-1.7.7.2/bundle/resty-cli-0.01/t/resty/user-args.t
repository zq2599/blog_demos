# vi:ft= et ts=4 sw=4

use lib 't/lib';
use Test::Resty;

plan tests => blocks() * 3;

run_tests();

__DATA__

=== TEST 1: simplest
--- args: 1 testing "2 3 4"
--- src
print("arg 0: ", arg[0])
print("arg 1: ", arg[1])
print("arg 2: ", arg[2])
print("arg 3: ", arg[3])

--- out_like chop
^arg 0: /tmp/\S+\.lua
arg 1: 1
arg 2: testing
arg 3: 2 3 4$

--- err



=== TEST 2: minus indexes
--- args: 1 testing "2 3 4"
--- src
print("arg -1: ", arg[-1])
print("arg 0: ", arg[0])
print("arg 1: ", arg[1])
print("arg 2: ", arg[2])
print("arg 3: ", arg[3])

--- out_like chop
^arg -1: ./resty
arg 0: /tmp/\S+\.lua
arg 1: 1
arg 2: testing
arg 3: 2 3 4$

--- err



=== TEST 3: with quote and options
--- opts: -I /tmp
--- args: 1 2 \"
--- src
print("arg -3: ", arg[-3])
print("arg -2: ", arg[-2])
print("arg -1: ", arg[-1])
print("arg 0: ", arg[0])
print("arg 1: ", arg[1])
print("arg 2: ", arg[2])
print("arg 3: ", arg[3])

--- out_like chop
^arg -3: ./resty
arg -2: -I
arg -1: /tmp
arg 0: /tmp/\S+\.lua
arg 1: 1
arg 2: 2
arg 3: \"$

--- err



=== TEST 4: with long brackets
--- args: 1 ] ]] ]]] ]=] ]==] ]====] [===[ [[ [=[
--- src
print("arg 0: ", arg[0])
print("arg 1: ", arg[1])
print("arg 2: ", arg[2])
print("arg 3: ", arg[3])
print("arg 4: ", arg[4])
print("arg 5: ", arg[5])
print("arg 6: ", arg[6])
print("arg 7: ", arg[7])
print("arg 8: ", arg[8])
print("arg 9: ", arg[9])
print("arg 10: ", arg[10])

--- out_like chop
^arg 0: /tmp/\S+\.lua
arg 1: 1
arg 2: ]
arg 3: ]]
arg 4: ]]]
arg 5: ]=]
arg 6: ]==]
arg 7: ]====]
arg 8: \[===\[
arg 9: \[\[
arg 10: \[=\[$

--- err



=== TEST 5: with complex long brackets
--- args: 1]] 1]=]3 4]===] ]====]5 ]==2!] [=[]=]
--- src
print("arg 0: ", arg[0])
print("arg 1: ", arg[1])
print("arg 2: ", arg[2])
print("arg 3: ", arg[3])
print("arg 4: ", arg[4])
print("arg 5: ", arg[5])
print("arg 6: ", arg[6])

--- out_like chop
^arg 0: /tmp/\S+\.lua
arg 1: 1]]
arg 2: 1]=]3
arg 3: 4]===]
arg 4: ]====]5
arg 5: ]==2!]
arg 6: \[=\[]=]$

--- err
