#!/bin/bash

mkdir ~/temp-grpc-install

echo "clear old files"
rm -rf $GOPATH/src/google.golang.org/grpc
rm -rf $GOPATH/src/golang.org/x
rm -rf $GOPATH/src/google.golang.org/protobuf
rm -rf $GOPATH/src/github.com/golang/protobuf
rm -rf $GOPATH/src/google.golang.org/genproto

echo "create directory"
mkdir -p $GOPATH/src/google.golang.org/
mkdir -p $GOPATH/src/golang.org/x
mkdir -p $GOPATH/src/github.com/golang/

echo "1. grpc"
cd ~/temp-grpc-install
wget https://github.com/grpc/grpc-go/archive/master.zip -O grpc-go.zip
unzip grpc-go.zip -d $GOPATH/src/google.golang.org/
cd $GOPATH/src/google.golang.org/ 
mv grpc-go-master grpc

echo "2. x/net"
cd ~/temp-grpc-install
wget https://github.com/golang/net/archive/master.zip -O net.zip
unzip net.zip -d $GOPATH/src/golang.org/x/
cd $GOPATH/src/golang.org/x/ 
mv net-master net

echo "3. x/text"
cd ~/temp-grpc-install
wget https://github.com/golang/text/archive/master.zip -O text.zip
unzip text.zip -d $GOPATH/src/golang.org/x/
cd $GOPATH/src/golang.org/x/ 
mv text-master text

echo "4. protobuf-go"
cd ~/temp-grpc-install
wget https://github.com/protocolbuffers/protobuf-go/archive/master.zip -O protobuf-go.zip
unzip protobuf-go.zip -d $GOPATH/src/google.golang.org/ 
cd $GOPATH/src/google.golang.org/
mv protobuf-go-master protobuf

echo "5. protobuf"
cd ~/temp-grpc-install
wget https://github.com/golang/protobuf/archive/master.zip -O protobuf.zip
unzip protobuf.zip -d $GOPATH/src/github.com/golang/
cd $GOPATH/src/github.com/golang/
mv protobuf-master protobuf

echo "6. go-genproto"
cd ~/temp-grpc-install
wget https://github.com/google/go-genproto/archive/master.zip -O go-genproto.zip
unzip go-genproto.zip -d $GOPATH/src/google.golang.org/
cd $GOPATH/src/google.golang.org/
mv go-genproto-master genproto

echo "7. x/sys"
cd ~/temp-grpc-install
wget https://github.com/golang/sys/archive/master.zip -O sys.zip
unzip sys.zip -d $GOPATH/src/golang.org/x/
cd $GOPATH/src/golang.org/x
mv sys-master sys

echo "install protoc-gen-go"
cd $GOPATH/src/github.com/golang/protobuf/protoc-gen-go/
go build
go install

echo "install grpc"

cd $GOPATH/src/
go install google.golang.org/grpc

echo "clear resource"
cd ~/
rm -rf ~/temp-grpc-install

echo "install finish"
