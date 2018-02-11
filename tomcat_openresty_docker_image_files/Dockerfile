# Docker image for OpenResty study
# VERSION 0.0.1
# Author: bolingcavalry

#基础镜像使用ubuntu:16.04
FROM ubuntu:16.04

#作者
MAINTAINER BolingCavalry <zq2599@gmail.com>

#定义工作目录
ENV WORK_PATH /usr/local/work

#定义安装目录
ENV INSTALL_PATH /usr/servers

#定义nginx-openresty文件夹名称
ENV NGX_OPENRESTY_PACKAGE_NAME ngx_openresty-1.7.7.2

#定义ngx_cache_purge文件夹名称，该模块用于清理nginx缓存
ENV NGX_CACHE_PURGE_PACKAGE_NAME ngx_cache_purge-2.3

#定义nginx_upstream_check_module文件夹名称，该模块用于ustream健康检查
ENV NGX_UPSTREAM_CHECK_PACKAGE_NAME nginx_upstream_check_module-0.3.0

#创建工作目录
RUN mkdir -p $WORK_PATH

#创建安装目录
RUN mkdir -p $INSTALL_PATH

#apt换源，用阿里云的源，删除原有的源
RUN rm /etc/apt/sources.list

#apt换源，用阿里云的源，复制新的源
COPY ./sources.list /etc/apt/sources.list

#apt更新
RUN apt-get update

#创建安装目录
RUN apt-get install -y make vim gcc libreadline-dev libncurses5-dev libpcre3-dev libssl-dev perl 

#修改vim的配置信息，修复中文乱码问题
RUN sed -i '$a\set fileencodings=utf-8,ucs-bom,gb18030,gbk,gb2312,cp936' /etc/vim/vimrc
RUN sed -i '$a\set termencoding=utf-8' /etc/vim/vimrc
RUN sed -i '$a\set encoding=utf-8' /etc/vim/vimrc

#把nginx-openresty文件夹复制到工作目录
COPY ./$NGX_OPENRESTY_PACKAGE_NAME $INSTALL_PATH/$NGX_OPENRESTY_PACKAGE_NAME

######luajit start######
#进入make目录，执行编译luajit
RUN cd $INSTALL_PATH/$NGX_OPENRESTY_PACKAGE_NAME/bundle/LuaJIT-2.1-20150120/ && make clean && make && make install

#软连接
RUN ln -sf luajit-2.1.0-alpha /usr/local/bin/luajit
######luajit end######


######module start######
#把ngx_cache_purge文件夹复制到工作目录
COPY ./$NGX_CACHE_PURGE_PACKAGE_NAME $INSTALL_PATH/$NGX_OPENRESTY_PACKAGE_NAME/bundle/$NGX_CACHE_PURGE_PACKAGE_NAME

#把nginx_upstream_check_module文件夹复制到工作目录
COPY ./$NGX_UPSTREAM_CHECK_PACKAGE_NAME $INSTALL_PATH/$NGX_OPENRESTY_PACKAGE_NAME/bundle/$NGX_UPSTREAM_CHECK_PACKAGE_NAME
######module end######


######ngx_openresty start######
#进入ngx_openresty目录，执行configure，执行构建
RUN cd $INSTALL_PATH/$NGX_OPENRESTY_PACKAGE_NAME && ./configure --prefix=$INSTALL_PATH --with-http_realip_module  --with-pcre  --with-luajit --add-module=./bundle/$NGX_CACHE_PURGE_PACKAGE_NAME/ --add-module=./bundle/$NGX_UPSTREAM_CHECK_PACKAGE_NAME/ -j2 && make && make install  
######ngx_openresty end######


######复制配置文件 start######
#删除原有的nginx.conf
RUN rm $INSTALL_PATH/nginx/conf/nginx.conf
#用定制的nginx.conf
COPY ./nginx.conf $INSTALL_PATH/nginx/conf/ 
#将新的conf文件放入指定位置，nginx.conf中对此文件有include
COPY ./boling_cavalry.conf $WORK_PATH/
#将http库文件赋值到默认库
COPY ./http.lua $INSTALL_PATH/lualib/resty
COPY ./http_headers.lua $INSTALL_PATH/lualib/resty
#创建放置lua库的目录(扩展库)
RUN mkdir $WORK_PATH/lualib
#复制一个lua库文件(扩展库)
COPY ./sequare.lua $WORK_PATH/lualib
#创建放置lua脚本的目录
RUN mkdir $WORK_PATH/lua
#复制一个lua的demo脚本
COPY ./test_request.lua $WORK_PATH/lua
COPY ./get_sequare.lua $WORK_PATH/lua
COPY ./test_http.lua $WORK_PATH/lua
######复制配置文件 end######


#暴露8080端口
EXPOSE 80

#启动NGINX
CMD ["/usr/servers/nginx/sbin/nginx", "-g", "daemon off;"]

