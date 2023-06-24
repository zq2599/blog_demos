#!/bin/bash
# echo '正在创建namespace'
# kubectl create namespace client-go-tutorials
# echo 'namespace创建成功'
echo '正在部署Nginx'
kubectl apply -f script/kubernetes/nginx-deployment-service.yaml
echo 'Nginx部署成功'