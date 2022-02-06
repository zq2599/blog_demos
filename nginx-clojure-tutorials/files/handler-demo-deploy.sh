#! /bin/bash

PROJECT_PATH=`pwd`/..
# NGINX路径
NGINX_PATH='/Users/zhaoqin1/temp/202202/02/001/nginx-clojure-0.5.2'

# 构建文件名
JAR_FILE_NANE='handler-demo-1.0-SNAPSHOT.jar'

cd ${PROJECT_PATH} && mvn clean compile package -U

rm -f ${NGINX_PATH}/jars/${JAR_FILE_NANE}

cp handler-demo/target/${JAR_FILE_NANE} ${NGINX_PATH}/jars/