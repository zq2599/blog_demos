#!/bin/bash

# 判断是否安装了sshpass
if ! [ -x "$(command -v sshpass)" ]; then
  echo '请安装sshpass后再使用此脚本！'
  exit 1
fi

# 镜像名
IMAGE_NAME='bolingcavalry/probedemo'

# TAG名
TAG_NAME='0.0.1'

# 配置了deployment和service的yaml文件名
DEPLOY_SERVICE_YAML='probedemo.yaml'

# K8S环境的IP地址
K8S_IP_ADDRESS='192.168.50.135'

# K8S环境的SSH账号
K8S_SSH_ACCOUNT='root'

# 8S环境的SSH密码
K8S_SSH_PSWD='888888'

# K8S上存放tar和yaml文件的位置
K8S_FILE_PATH='~/deploy_temp'

# 当前名目录
CURRENT_DIR=`pwd`

echo '开始自动构建和部署，当前目录是：'${CURRENT_DIR}

# 执行maven命令构建项目
mvn clean package -U -DskipTests

echo "构建镜像文件："${IMAGE_NAME}/${TAG_NAME}
docker build -t ${IMAGE_NAME}/${TAG_NAME} .

echo "将镜像导出为tar文件："${IMAGE_NAME}/${TAG_NAME}
docker save ${IMAGE_NAME}/${TAG_NAME} > ${CURRENT_DIR}/image.tar

echo "在K8S服务器创建存放文件的目录："${K8S_FILE_PATH}
sshpass -p ${K8S_SSH_PSWD} ssh ${K8S_SSH_ACCOUNT}@${K8S_IP_ADDRESS} "mkdir -p ${K8S_FILE_PATH}"

echo "将yaml文件发送到K8S服务器："${IMAGE_NAME}/${TAG_NAME}
sshpass -p ${K8S_SSH_PSWD} scp ${CURRENT_DIR}/${DEPLOY_SERVICE_YAML} ${K8S_SSH_ACCOUNT}@${K8S_IP_ADDRESS}:${K8S_FILE_PATH}/

echo "将镜像tar文件发送到K8S服务器："${IMAGE_NAME}/${TAG_NAME}
sshpass -p ${K8S_SSH_PSWD} scp ${CURRENT_DIR}/image.tar ${K8S_SSH_ACCOUNT}@${K8S_IP_ADDRESS}:${K8S_FILE_PATH}/

echo "如果K8S环境之前已经部署过，就先清理："${IMAGE_NAME}/${TAG_NAME}
sshpass -p ${K8S_SSH_PSWD} ssh ${K8S_SSH_ACCOUNT}@${K8S_IP_ADDRESS} "kubectl delete -f ${K8S_FILE_PATH}/${DEPLOY_SERVICE_YAML}"

echo "等待10秒"
sleep 10

echo "清理之前加载到本地仓库的镜像："${IMAGE_NAME}/${TAG_NAME}
sshpass -p ${K8S_SSH_PSWD} ssh ${K8S_SSH_ACCOUNT}@${K8S_IP_ADDRESS} "docker rmi ${IMAGE_NAME}/${TAG_NAME}"

echo "从tar文件加载镜像："${IMAGE_NAME}/${TAG_NAME}
sshpass -p ${K8S_SSH_PSWD} ssh ${K8S_SSH_ACCOUNT}@${K8S_IP_ADDRESS} "docker load < ${K8S_FILE_PATH}/image.tar"

echo "部署："${IMAGE_NAME}/${TAG_NAME}
sshpass -p ${K8S_SSH_PSWD} ssh ${K8S_SSH_ACCOUNT}@${K8S_IP_ADDRESS} "kubectl apply -f ${K8S_FILE_PATH}/${DEPLOY_SERVICE_YAML}"

echo "删除tar文件："${CURRENT_DIR}/image.tar
rm -rf ${CURRENT_DIR}/image.tar

echo "删镜像："${IMAGE_NAME}/${TAG_NAME}
docker rmi ${IMAGE_NAME}/${TAG_NAME}