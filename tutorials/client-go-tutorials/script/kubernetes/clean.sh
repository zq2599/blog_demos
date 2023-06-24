#!/bin/bash
kubectl delete -f script/kubernetes/nginx-deployment-service.yaml
echo 'Nginx删除成功'
# kubectl delete namespace client-go-tutorials
# echo 'namespace删除成功'