#!/bin/bash

echo "download node-exporter.yaml"
wget https://raw.githubusercontent.com/zq2599/blog_demos/master/prometheusgrafana/prometheus/node-exporter.yaml

echo "download rbac-setup.yaml"
wget https://raw.githubusercontent.com/zq2599/blog_demos/master/prometheusgrafana/prometheus/rbac-setup.yaml

echo "download configmap.yaml"
wget https://raw.githubusercontent.com/zq2599/blog_demos/master/prometheusgrafana/prometheus/configmap.yaml

echo "download prometheus.yaml"
wget https://raw.githubusercontent.com/zq2599/blog_demos/master/prometheusgrafana/prometheus/prometheus.yaml

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

if [ ! -f "prometheus.yaml" ];then
  echo "download prometheus.yaml fail, please retry!"
  exit 1
fi

if [ ! -f "grafana.yaml" ];then
  echo "download grafana.yaml fail, please retry!"
  exit 1
fi

kubectl create -f node-exporter.yaml
kubectl create -f rbac-setup.yaml
kubectl create -f configmap.yaml
kubectl create -f prometheus.yaml
kubectl create -f grafana.yaml

echo "create prometheus and grafana successful"