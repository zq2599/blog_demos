# Docker image for Anaconda3-2019.03
# VERSION 0.0.1
# Author: bolingcavalry

### 基础镜像是最新的debian
FROM debian:latest

#作者
MAINTAINER BolingCavalry <zq2599@gmail.com>

#系统编码
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
#path
ENV PATH /opt/conda/bin:$PATH

#安装必要的软件
RUN apt-get update --fix-missing && apt-get install -y wget bzip2

ENV ANACONDA_FILE_NAME Anaconda3-2019.03-Linux-x86_64.sh

#下载和安装anaconda3，设置环境变量，再通过conda命令安装jupyter
RUN wget https://repo.anaconda.com/archive/$ANACONDA_FILE_NAME -O ~/anaconda.sh && \
    /bin/bash ~/anaconda.sh -b -p /opt/conda && \
    rm ~/anaconda.sh && \
    ln -s /opt/conda/etc/profile.d/conda.sh /etc/profile.d/conda.sh && \
    echo ". /opt/conda/etc/profile.d/conda.sh" >> ~/.bashrc && \
    echo "conda activate base" >> ~/.bashrc && \
    /bin/bash -c "source ~/.bashrc" && \
    conda update conda && \
    conda install jupyter -y --quiet && \
    mkdir /opt/notebooks

#把启动时用到的文件准备好
COPY ./docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
EXPOSE 8888
