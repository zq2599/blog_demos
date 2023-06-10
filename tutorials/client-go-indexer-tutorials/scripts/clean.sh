#!/bin/bash
echo '正在删除MySQL的部署'
kubectl delete -f mysql-deployment-service.yaml
echo '正在删除MySQL的PVC'
kubectl delete -f mysql-pvc.yaml
echo '正在删除MySQL的PV'
kubectl delete -f mysql-pv.yaml
echo '正在删除Tomcat的部署'
kubectl delete -f tomcat-deployment-service.yaml
echo '正在删除Nginx的部署'
kubectl delete -f nginx-deployment-service.yaml
echo '正在删除namespace'
kubectl delete namespace indexer-tutorials