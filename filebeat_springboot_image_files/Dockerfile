# Docker file for filebeat and springboot from bolingcavalry # VERSION 0.0.1
# Author: bolingcavalry

#基础镜像
FROM java:8u111-jdk

#作者
MAINTAINER BolingCavalry <zq2599@gmail.com>

#定义日志文件存放目录，这个要和web应用的日志配置一致
ENV APP_LOG_PATH /applog

#定义证书文件目录，这个要和filebeat.yml的配置一致
ENV FILE_BEAT_CRT_PATH /etc/pki/tls/certs

#定义filebeat文件夹名称
ENV FILE_BEAT_PACKAGE_NAME filebeat-6.2.2-linux-x86_64

#定义证书文件名称
ENV FILE_BEAT_CRT_NAME logstash-beats.crt

#定义启动文件名称
ENV ENTRYPOINT_FILE_NAME filebeat-springboot-entrypoint.sh

#定义时区参数
ENV TZ=Asia/Shanghai

#设置时区
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

#使之生效
RUN . /etc/profile

#创建日志目录文件夹
RUN mkdir $APP_LOG_PATH

#存放证书的文件夹
RUN mkdir -p $FILE_BEAT_CRT_PATH

#从当前目录將证书文件复制到镜像中
COPY ./$FILE_BEAT_CRT_NAME $FILE_BEAT_CRT_PATH/

#从当前目录將filebeat文件复制到镜像中
COPY ./$FILE_BEAT_PACKAGE_NAME /opt/$FILE_BEAT_PACKAGE_NAME

#复制启动文件
COPY ./$ENTRYPOINT_FILE_NAME /$ENTRYPOINT_FILE_NAME

#赋写权限
RUN chmod a+x /$ENTRYPOINT_FILE_NAME
