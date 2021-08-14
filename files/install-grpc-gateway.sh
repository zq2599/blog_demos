#!/bin/bash

echo "1. downloading grpc-gateway release package v1.16.0"

rm -rf $GOPATH/src/github.com/grpc-ecosystem/grpc-gateway
mkdir -p $GOPATH/src/github.com/grpc-ecosystem
cd $GOPATH/src/github.com/grpc-ecosystem
wget https://github.com/grpc-ecosystem/grpc-gateway/archive/v1.16.0.zip -O grpc-gateway.zip
unzip grpc-gateway.zip
rm -f grpc-gateway.zip
mv grpc-gateway-1.16.0 grpc-gateway

echo "2. building grpc gateway"
cd $GOPATH/src/github.com/grpc-ecosystem/grpc-gateway/protoc-gen-grpc-gateway
go build
go install

echo "3. building swagger"
cd $GOPATH/src/github.com/grpc-ecosystem/grpc-gateway/protoc-gen-swagger
go build
go install

echo "4. downloading annonation files"
cd ~
wget https://github.com/protocolbuffers/protobuf/archive/v3.14.0.zip -O annonations.zip
unzip annonations.zip
rm -f annonations.zip
cp -r protobuf-3.14.0/src/google/ $GOPATH/src/
rm -rf protobuf-3.14.0

echo "finish"
