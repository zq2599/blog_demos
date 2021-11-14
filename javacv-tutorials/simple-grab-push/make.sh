#! /bin/bash

cd simple-grab-push

echo '清理容器'
docker rm -f simple-grab-push

echo '编译构建'
mvn clean package -U

echo '提取镜像所需内容'
mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

echo '制造镜像'
docker build -t bolingcavalry/simple-grab-push:0.0.1 .

echo '清理残余镜像'
docker image prune -f

echo '运行容器'
docker run \
--rm \
--name simple-grab-push \
-p 18080:8080 \
bolingcavalry/simple-grab-push:0.0.1


