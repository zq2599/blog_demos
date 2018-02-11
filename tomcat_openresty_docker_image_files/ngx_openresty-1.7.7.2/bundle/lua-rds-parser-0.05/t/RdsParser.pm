package t::RdsParser;

use Test::Base -Base;
use IPC::Run3;
use Cwd;

use Test::LongString;

our @EXPORT = qw( run_tests );

$ENV{LUA_CPATH} = "?.so;" . ($ENV{LUA_CPATH} || "") . ';' . "/usr/local/openresty/lualib/?.so;;";
#$ENV{LUA_PATH} = ($ENV{LUA_PATH} || "" ) . ';' . getcwd . "/runtime/?.lua" . ';;';

sub run_test ($) {
    my $block = shift;
    #print $json_xs->pretty->encode(\@new_rows);
    #my $res = #print $json_xs->pretty->encode($res);
    my $name = $block->name;

    my $rds = $block->rds or
        die "No --- lua specified for test $name\n";

    my $rdsfile = "test.rds";
    open my $fh, ">$rdsfile" or
        die "Cannot open $rdsfile for writing: $!\n";
    print $fh $rds;
    close $fh;

    my $luafile = "test_case.lua";

    open $fh, ">$luafile" or
        die "Cannot open $luafile for writing: $!\n";

    print $fh <<'_EOC_';
local f, err = io.open("test.rds", "rb")
if f == nil then
    error("failed to open file test.rds: " .. err)
end
local rds = f:read("*a")
f:close()
-- print("RDS:" .. rds)
local parser = require "rds.parser"
local res, err = parser.parse(rds)
if res == nil then
    print(res)
    print(err)
    return
end
local cjson = require "cjson"
print(cjson.encode(res))
_EOC_

    close $fh;

    my ($res, $err);

    my @cmd;

    if ($ENV{TEST_LUA_USE_VALGRIND}) {
        @cmd =  ('valgrind', '-q', '--leak-check=full', 'lua', 'test_case.lua');
    } else {
        @cmd =  ('lua', 'test_case.lua');
    }

    run3 \@cmd, undef, \$res, \$err;

    #warn "res:$res\nerr:$err\n";

    if (defined $block->err) {
        $err =~ /.*:.*:.*: (.*\s)?/;
        $err = $1;
        is $err, $block->err, "$name - err expected";
    } elsif ($?) {
        die "Failed to execute --- lua for test $name: $err\n";

    } else {
        #is $res, $block->out, "$name - output ok";
        is $res, $block->out, "$name - output ok";
    }

    #unlink 'test_case.lua' or warn "could not delete \'test_case.lua\':$!";
}

sub run_tests () {
    for my $block (blocks()) {
        run_test($block);
    }
}

1;
