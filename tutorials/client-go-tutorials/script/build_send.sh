#!/bin/bash

OUTPUT_FILE='client-go-tutorials'
DEST_ADDR='192.168.50.76'
CUR_DIR=`pwd`


echo '当前目录：'$CUR_DIR
echo '清理残留文件'
rm -rf $CUR_DIR/$OUTPUT_FILE
echo '开始构建'
go build
echo '构建完成，推送到kubernetes机器'
scp $OUTPUT_FILE root@$DEST_ADDR:~/work/