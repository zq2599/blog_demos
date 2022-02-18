#! /bin/bash

PROJECT_PATH=`pwd`/..
# NGINX路径
#NGINX_PATH='/Users/zhaoqin1/temp/202202/02/001/nginx-clojure-0.5.2'
NGINX_PATH='/Users/zhaoqin/baidusync/temp/202202/07/001/nginx-clojure-0.5.2'

# simple-hello构建文件名
SIMPLE_HELLO_JAR_FILE_NANE='simple-hello-1.0-SNAPSHOT.jar'

# handler-demo构建文件名
HANDLER_DEMO_JAR_FILE_NANE='handler-demo-1.0-SNAPSHOT.jar'

# filter-demo构建文件名
FILTER_DEMO_JAR_FILE_NANE='filter-demo-1.0-SNAPSHOT.jar'

# shared-map-demo构建文件名
FILTER_DEMO_JAR_FILE_NANE='shared-map-demo-1.0-SNAPSHOT.jar'

echo '开始编译'
cd ${PROJECT_PATH} && mvn clean compile package -U

echo '删除旧的jar'
rm -f ${NGINX_PATH}/jars/${SIMPLE_HELLO_JAR_FILE_NANE}
rm -f ${NGINX_PATH}/jars/${HANDLER_DEMO_JAR_FILE_NANE}
rm -f ${NGINX_PATH}/jars/${FILTER_DEMO_JAR_FILE_NANE}
rm -f ${NGINX_PATH}/jars/${FILTER_DEMO_JAR_FILE_NANE}

echo '删除旧的nginx.conf'
rm -f ${NGINX_PATH}/conf/nginx.conf
echo '将新的nginx.conf复制到nginx目录'
cp ${PROJECT_PATH}/files/nginx.conf ${NGINX_PATH}/conf/

echo '将新的jar复制到nginx目录'
cp simple-hello/target/${SIMPLE_HELLO_JAR_FILE_NANE} ${NGINX_PATH}/jars/
cp handler-demo/target/${HANDLER_DEMO_JAR_FILE_NANE} ${NGINX_PATH}/jars/
cp filter-demo/target/${FILTER_DEMO_JAR_FILE_NANE} ${NGINX_PATH}/jars/
cp shared-map-demo/target/${FILTER_DEMO_JAR_FILE_NANE} ${NGINX_PATH}/jars/

echo '进入nginx目录'
cd ${NGINX_PATH}

echo '停止nginx'
./nginx -s stop

sleep 2

echo '启动nginx'
./nginx

echo '重新部署完成'