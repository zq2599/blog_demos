# Docker file for rabbitmq single or cluster from bolingcavalry 
# VERSION 0.0.3
# Author: bolingcavalry

#基础镜像
FROM centos:7

#作者
MAINTAINER BolingCavalry <zq2599@gmail.com>

#定义时区参数
ENV TZ=Asia/Shanghai

#设置时区
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo '$TZ' > /etc/timezone

#设置编码为中文
RUN yum -y install kde-l10n-Chinese glibc-common

RUN localedef -c -f UTF-8 -i zh_CN zh_CN.utf8

ENV LC_ALL zh_CN.utf8 

#安装wget工具
RUN yum install -y wget unzip tar

#安装erlang
RUN rpm -Uvh https://github.com/rabbitmq/erlang-rpm/releases/download/v19.3.6.5/erlang-19.3.6.5-1.el7.centos.x86_64.rpm

RUN yum install -y erlang

#安装rabbitmq
RUN rpm --import http://www.rabbitmq.com/rabbitmq-signing-key-public.asc

RUN yum install -y https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.7.5-rc.1/rabbitmq-server-3.7.5.rc.1-1.el7.noarch.rpm

RUN /usr/sbin/rabbitmq-plugins list <<<'y'

#安装常用插件
RUN /usr/sbin/rabbitmq-plugins enable --offline rabbitmq_mqtt rabbitmq_stomp rabbitmq_management  rabbitmq_management_agent rabbitmq_federation rabbitmq_federation_management <<<'y'

#添加配置文件
ADD rabbitmq.config /etc/rabbitmq/

#添加cookie，使集群环境中的机器保持互通
ADD erlang.cookie /var/lib/rabbitmq/.erlang.cookie

#添加启动容器时执行的脚本，主要根据启动时的入参做集群设置
ADD startrabbit.sh /opt/rabbit/

#给相关资源赋予权限
RUN chmod u+rw /etc/rabbitmq/rabbitmq.config \
&& chown rabbitmq:rabbitmq /var/lib/rabbitmq/.erlang.cookie \
&& chmod 400 /var/lib/rabbitmq/.erlang.cookie \
&& mkdir -p /opt/rabbit \
&& chmod a+x /opt/rabbit/startrabbit.sh

#暴露常用端口
EXPOSE 5672
EXPOSE 15672
EXPOSE 25672
EXPOSE 4369
EXPOSE 9100
EXPOSE 9101
EXPOSE 9102
EXPOSE 9103
EXPOSE 9104
EXPOSE 9105

#设置容器创建时执行的脚本
CMD /opt/rabbit/startrabbit.sh
