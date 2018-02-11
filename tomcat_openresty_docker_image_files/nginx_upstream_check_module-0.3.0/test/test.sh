#!/bin/sh

TEST_NGINX_USE_HUP=1 PATH=/home/yaoweibin/nginx/sbin:$PATH prove -r t
