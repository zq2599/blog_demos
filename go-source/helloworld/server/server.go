package main

import (
	"context"
	"log"
	"net"

	"google.golang.org/grpc"
	pb "helloworld"
)

const (
	port = ":50051"
)

// 定义结构体，在调用注册api的时候作为入参，
// 该结构体会带上SayHello方法，里面是业务代码
// 这样远程调用时就执行了业务代码了
type server struct {
	// pb.go中自动生成的，是个空结构体
	pb.UnimplementedGreeterServer
}

// 业务代码在此写，客户端远程调用SayHello时，
// 会执行这里的代码
func (s *server) SayHello(ctx context.Context, in *pb.HelloRequest) (*pb.HelloReply, error) {
	// 打印请求参数
	log.Printf("Received: %v", in.GetName())
	// 实例化结构体HelloReply，作为返回值
	return &pb.HelloReply{Message: "Hello " + in.GetName()}, nil
}

func main() {
	// 要监听的协议和端口
	lis, err := net.Listen("tcp", port)
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}

	// 实例化gRPC server结构体
	s := grpc.NewServer()

	// 服务注册
	pb.RegisterGreeterServer(s, &server{})

	log.Println("开始监听，等待远程调用...")

	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
