#!/bin/bash
echo '正在创建namespace'
kubectl create namespace indexer-tutorials
echo '正在创建PV'
kubectl apply -f mysql-pv.yaml
echo '正在创建PVC'
kubectl apply -f mysql-pvc.yaml
echo '正在部署MySQL'
kubectl apply -f mysql-deployment-service.yaml
echo '正在部署Tomcat'
kubectl apply -f tomcat-deployment-service.yaml
echo '正在部署Nginx'
kubectl apply -f nginx-deployment-service.yaml