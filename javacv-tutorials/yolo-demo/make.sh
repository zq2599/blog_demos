#! /bin/bash

cd yolo-demo

echo '清理容器'
docker rm -f yolodemo

echo '编译构建'
mvn clean package -U

echo '提取镜像所需内容'
mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

echo '制造镜像'
docker build -t bolingcavalry/yolodemo:0.0.1 .

echo '清理残余镜像'
docker image prune -f

echo '运行容器'
docker run \
--rm \
--name yolodemo \
-p 18080:8080 \
-v /Users/zhaoqin/temp/202110/16/images:/app/images \
-v /Users/zhaoqin/temp/202110/16/model:/app/model \
bolingcavalry/yolodemo:0.0.1


