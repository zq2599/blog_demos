# Docker file from bolingcavalry # VERSION 0.0.1
# Author: bolingcavalry

#基础镜像
FROM openjdk:8-jdk-stretch

#作者
MAINTAINER BolingCavalry <zq2599@gmail.com>

#健康检查参数设置，每5秒检查一次，接口超时时间2秒，连续10次返回1就判定该容器不健康
HEALTHCHECK --interval=5s --timeout=2s --retries=10 \
  CMD curl --silent --fail localhost:8080/getstate || exit 1