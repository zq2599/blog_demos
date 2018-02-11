# Copyright (C) Yichun Zhang (agentzh)
# Copyright (C) Guanlan Dai

package Test::Resty;

use Test::Base -Base;
use POSIX ();
use IPC::Run ();

our @EXPORT = qw( run_tests blocks plan );

our $UseValgrind = $ENV{TEST_RESTY_USE_VALGRIND};

sub run_tests () {
    for my $block (Test::Base::blocks()) {
        run_test($block);
    }
}

sub bail_out (@) {
    Test::More::BAIL_OUT(@_);
}

sub parse_cmd ($) {
    my $cmd = shift;
    my @cmd;
    while (1) {
        if ($cmd =~ /\G\s*"(.*?)"/gmsc) {
            push @cmd, $1;

        } elsif ($cmd =~ /\G\s*'(.*?)'/gmsc) {
            push @cmd, $1;

        } elsif ($cmd =~ /\G\s*(\S+)/gmsc) {
            push @cmd, $1;

        } else {
            last;
        }
    }
    return @cmd;
}

sub run_test ($) {
    my $block = shift;
    my $name = $block->name;

    my $timeout = $block->timeout() || 10;
    my $opts = $block->opts;
    my $args = $block->args;

    my $cmd = "./resty";

    if ($UseValgrind) {
        my $val_opts = " --num-callers=100 -q --gen-suppressions=all";

        my $sup_file = 'valgrind.suppress';
        if (-f $sup_file) {
            $val_opts .= " --suppressions=$sup_file";
        }

        my $extra_opts = "--valgrind '--valgrind-opts=$val_opts'";
        if (!defined $opts) {
            $opts = $extra_opts;

        } else {
            $opts .= " $extra_opts";
        }

        warn "$name\n";
    }

    if (defined $opts) {
        $cmd .= " $opts";
    }

    my $luafile;
    if (defined $block->src) {
        $luafile = POSIX::tmpnam() . ".lua";
        open my $out, ">$luafile" or
            bail_out("cannot open $luafile for writing: $!");
        print $out ($block->src);
        close $out;
        $cmd .= " $luafile"
    }

    if (defined $args) {
        $cmd .= " $args";
    }

    #warn "CMD: $cmd\n";

    my @cmd = parse_cmd($cmd);

    my ($out, $err);

    eval {
        IPC::Run::run(\@cmd, \undef, \$out, \$err,
                      IPC::Run::timeout($timeout));
    };
    if ($@) {
        # timed out
        if ($@ =~ /timeout/) {
            if (!defined $block->expect_timeout) {
                fail("$name: resty process timed out");
            }
	} else {
            fail("$name: failed to run command [$cmd]: $@");
        }
    }

    my $ret = ($? >> 8);

    if (defined $luafile) {
        unlink $luafile;
    }

    if (defined $block->out) {
        is $out, $block->out, "$name - stdout eq okay";
    }

    my $regex = $block->out_like;
    if (defined $regex) {
        if (!ref $regex) {
            $regex = qr/$regex/s;
        }
        like $out, $regex, "$name - stdout like okay";
    }

    if (defined $block->err) {
        is $err, $block->err, "$name - stderr eq okay";
    }

    $regex = $block->err_like;
    if (defined $regex) {
        if (!ref $regex) {
            $regex = qr/$regex/ms;
        }
        like $err, $regex, "$name - stderr like okay";
    }

    my $exp_ret = $block->ret;
    if (!defined $exp_ret) {
        $exp_ret = 0;

    } elsif ($UseValgrind) {
        $ret &= 0x3;
    }
    is $ret, $exp_ret, "$name - exit code okay";
}

1;
# vi: et
