# Docker file from bolingcavalry # VERSION 0.0.1
# Author: bolingcavalry

#基础镜像
FROM mysql:5.7.21

#作者
MAINTAINER BolingCavalry <zq2599@gmail.com>

#定义配置文件存放目录
ENV EXTEND_CONF_PATH /etc/mysql/conf.d

#定义conf文件名
ENV CONF_FILE_NAME master.cnf

#定义entrypoint文件所在路径
ENV ENTRY_FILE_PATH /usr/local/bin/

#定义entrypoint文件名
ENV ENTRY_FILE_NAME docker-entrypoint.sh

#定义entrypoint的软链接文件名
ENV ENTRY_FILE_SOFT_LINK_NAME entrypoint.sh

#复制新的配置文件
COPY ./$CONF_FILE_NAME $EXTEND_CONF_PATH/

#给shell文件赋读权限
RUN chmod a+r $EXTEND_CONF_PATH/$CONF_FILE_NAME

#删除原有的软链接
RUN rm -rf /$ENTRY_FILE_SOFT_LINK_NAME

#删除原有的文件
RUN rm -rf $ENTRY_FILE_PATH/$ENTRY_FILE_NAME

#将entrypoint文件复制到原有位置
COPY ./$ENTRY_FILE_NAME $ENTRY_FILE_PATH/

#给entrypoint文件赋读权限
RUN chmod a+r $ENTRY_FILE_PATH/$ENTRY_FILE_NAME

#建立软链接
RUN ln -s $ENTRY_FILE_PATH/$ENTRY_FILE_NAME /$ENTRY_FILE_SOFT_LINK_NAME

ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 3306
CMD ["mysqld"]
