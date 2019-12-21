#!/bin/bash

echo "undeploy grafana"
kubectl delete Service grafana -n kube-system
kubectl delete Deployment grafana-core -n kube-system

echo "undeploy prometheus"
kubectl delete Service prometheus -n kube-system
kubectl delete Deployment prometheus -n kube-system

echo "clear config"
kubectl delete ConfigMap prometheus-config -n kube-system

echo "clear rbac"
kubectl delete ClusterRoleBinding prometheus
kubectl delete ServiceAccount prometheus -n kube-system
kubectl delete ClusterRole prometheus

echo "undeploy node-exporter"
kubectl delete DaemonSet node-exporter -n kube-system

echo "undeploy successful"