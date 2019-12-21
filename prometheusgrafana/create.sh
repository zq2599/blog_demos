#!/bin/bash

echo "download node-exporter.yaml"
wget https://raw.githubusercontent.com/zq2599/blog_demos/master/prometheusgrafana/prometheus/node-exporter.yaml

echo "download rbac-setup.yaml"
wget https://raw.githubusercontent.com/zq2599/blog_demos/master/prometheusgrafana/prometheus/rbac-setup.yaml

echo "download configmap.yaml"
wget https://raw.githubusercontent.com/zq2599/blog_demos/master/prometheusgrafana/prometheus/configmap.yaml

echo "download promethues.yaml"
wget https://raw.githubusercontent.com/zq2599/blog_demos/master/prometheusgrafana/prometheus/promethues.yaml

echo "download grafana.yaml"
wget https://raw.githubusercontent.com/zq2599/blog_demos/master/prometheusgrafana/grafana/grafana.yaml

if [ ! -f "node-exporter.yaml" ];then
  echo "download node-exporter.yaml fail, please retry!"
  exit 1
fi

if [ ! -f "rbac-setup.yaml" ];then
  echo "download rbac-setup.yaml fail, please retry!"
  exit 1
fi

if [ ! -f "configmap.yaml" ];then
  echo "download configmap.yaml fail, please retry!"
  exit 1
fi

if [ ! -f "promethues.yaml" ];then
  echo "download promethues.yaml fail, please retry!"
  exit 1
fi

if [ ! -f "grafana.yaml" ];then
  echo "download grafana.yaml fail, please retry!"
  exit 1
fi

kubectl create -f node-exporter.yaml
kubectl create -f rbac-setup.yaml
kubectl create -f configmap.yaml
kubectl create -f promethues.yaml
kubectl create -f grafana.yaml

echo "create promethues and grafana successful"