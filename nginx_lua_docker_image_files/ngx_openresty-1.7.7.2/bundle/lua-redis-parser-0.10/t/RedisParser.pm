package t::RedisParser;

use Test::Base -Base;
use IPC::Run3;
use Cwd;

use Test::LongString;

our @EXPORT = qw( run_tests );

$ENV{LUA_CPATH} = "?.so;" . ($ENV{LUA_CPATH} || "") . ';' . "/usr/local/openresty-debug/lualib/?.so;/usr/local/openresty/lualib/?.so;;";
#$ENV{LUA_PATH} = ($ENV{LUA_PATH} || "" ) . ';' . getcwd . "/runtime/?.lua" . ';;';

sub run_test ($) {
    my $block = shift;
    #print $json_xs->pretty->encode(\@new_rows);
    #my $res = #print $json_xs->pretty->encode($res);
    my $name = $block->name;

    my $lua = $block->lua or
        die "No --- lua specified for test $name\n";

    open my $fh, ">test_case.lua";
    print $fh $lua;
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
        is_string $res, $block->out, "$name - output ok";
    }

    unlink 'test_case.lua' or warn "could not delete \'test_case.lua\':$!";
}

sub run_tests () {
    for my $block (blocks()) {
        run_test($block);
    }
}

1;
