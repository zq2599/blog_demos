#!/bin/bash
echo "停止docker-compose"
cd jaeger-service-provider && docker-compose down && cd ..

echo "编译构建"
mvn clean package -U -DskipTests

echo "创建provider镜像"
cd jaeger-service-provider && docker build -t bolingcavalry/jaeger-service-provider:0.0.1 . && cd ..

echo "创建consumer镜像"
cd jaeger-service-consumer && docker build -t bolingcavalry/jaeger-service-consumer:0.0.1 . && cd ..

echo "清理无效资源"
docker system prune --volumes -f

echo "启动docker-compose"
cd jaeger-service-provider && docker-compose up -d && cd ..