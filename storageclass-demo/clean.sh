#!/bin/bash

helm del --purge tomcat001
kubectl delete storageclass managed-nfs-storage
kubectl delete deployment nfs-client-provisioner -n hello-storageclass
kubectl delete clusterrolebinding run-nfs-client-provisioner
kubectl delete serviceaccount nfs-client-provisioner -n hello-storageclass
kubectl delete role leader-locking-nfs-client-provisioner -n hello-storageclass
kubectl delete rolebinding leader-locking-nfs-client-provisioner -n hello-storageclass
kubectl delete clusterrole nfs-client-provisioner-runner
kubectl delete namespace hello-storageclass
