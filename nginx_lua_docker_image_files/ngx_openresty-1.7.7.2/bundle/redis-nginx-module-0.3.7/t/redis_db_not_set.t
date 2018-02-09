#!/usr/bin/perl

# (C) Maxim Dounin
# (C) Sergey A. Osokin

# Test for redis backend.

###############################################################################

use warnings;
use strict;

use Test::More;

BEGIN { use FindBin; chdir($FindBin::Bin); }

use lib 'lib';
use Test::Nginx;

###############################################################################

select STDERR; $| = 1;
select STDOUT; $| = 1;

eval { require Redis; };
plan(skip_all => 'Redis not installed') if $@;

my $t = Test::Nginx->new()->has(qw/http redis/)
	->has_daemon('redis-server')->plan(1)
	->write_file_expand('nginx.conf', <<'EOF');

%%TEST_GLOBALS%%

daemon         off;

events {
}

http {
    %%TEST_GLOBALS_HTTP%%

    upstream redisbackend {
        server 127.0.0.1:8081;
    }

    server {
        listen       127.0.0.1:8080;
        server_name  localhost;

        location / {
            set $redis_key $uri;
            redis_pass redisbackend;
        }
    }
}

EOF

$t->run();

