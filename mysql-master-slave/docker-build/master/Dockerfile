# Docker file from bolingcavalry # VERSION 0.0.1
# Author: bolingcavalry

#基础镜像
FROM mysql:5.7.21

#作者
MAINTAINER BolingCavalry <zq2599@gmail.com>

#定义配置文件存放目录
ENV BASE_CONF_PATH /etc/mysql

#定义存放外部配置文件的文件夹名称
ENV EXTEND_CONF_FILE_FOLDER_NAME extend.conf.d

#定义conf文件名
ENV CONF_FILE_NAME my.cnf

#定义entrypoint文件所在路径
ENV ENTRY_FILE_PATH /usr/local/bin

#定义entrypoint文件名
ENV ENTRY_FILE_NAME docker-entrypoint.sh

#定义entrypoint的软链接文件名
ENV ENTRY_FILE_SOFT_LINK_NAME entrypoint.sh

#删除原有的配置文件
RUN rm $BASE_CONF_PATH/$CONF_FILE_NAME

#复制新的配置文件
COPY ./$CONF_FILE_NAME $BASE_CONF_PATH/

#给shell文件赋读权限
RUN chmod a+r $BASE_CONF_PATH/$CONF_FILE_NAME

#创建存放外部配置文件的目录
RUN mkdir $BASE_CONF_PATH/$EXTEND_CONF_FILE_FOLDER_NAME

#删除原有的软链接
RUN rm -rf /$ENTRY_FILE_SOFT_LINK_NAME

#删除原有的文件
RUN rm -rf $ENTRY_FILE_PATH/$ENTRY_FILE_NAME

#将entrypoint文件复制到原有位置
COPY ./$ENTRY_FILE_NAME $ENTRY_FILE_PATH/

#给entrypoint文件赋读权限
RUN chmod a+x $ENTRY_FILE_PATH/$ENTRY_FILE_NAME

#建立软链接
RUN ln -s $ENTRY_FILE_PATH/$ENTRY_FILE_NAME /$ENTRY_FILE_SOFT_LINK_NAME

ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 3306
CMD ["mysqld"]
