# Docker image for Anaconda3-2019.03
# VERSION 0.0.1
# Author: bolingcavalry

### 基础镜像，使用alpine操作系统，openjkd使用8u201
FROM openjdk:8u201-jdk-alpine3.9

#作者
MAINTAINER BolingCavalry <zq2599@gmail.com>

#系统编码
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
#path
ENV PATH /opt/conda/bin:$PATH

#安装必要的软件
#RUN apt-get update --fix-missing && apt-get install -y wget
RUN apk update && apk add wget

#下载下来的压缩文件名称
ENV NACOS_FILE_NAME nacos-server-1.1.0.tar.gz

#把启动时用到的文件准备好
COPY ./docker-entrypoint.sh /docker-entrypoint.sh

#解压后的文件夹名称
ENV NACOS_FOLDER_NAME nacos

RUN wget https://github.com/alibaba/nacos/releases/download/1.1.0/nacos-server-1.1.0.tar.gz -O ~/$NACOS_FILE_NAME && \
    tar -zxf ~/$NACOS_FILE_NAME -C ~/ && \
    rm ~/$NACOS_FILE_NAME && \
    chmod a+x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]

EXPOSE 8848
