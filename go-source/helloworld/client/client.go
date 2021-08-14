package main

import (
	"context"
	"log"
	"os"
	"time"

	"google.golang.org/grpc"
	pb "helloworld"
)

const (
	address     = "localhost:50051"
	defaultName = "world"
)

func main() {
	// 远程连接服务端
	conn, err := grpc.Dial(address, grpc.WithInsecure(), grpc.WithBlock())
	if err != nil {
		log.Fatalf("did not connect: %v", err)
	}

	// main方法执行完毕后关闭远程连接
	defer conn.Close()

	// 实例化数据结构
	c := pb.NewGreeterClient(conn)

	// 远程调用的请求参数，如果没有从命令行传入，就用默认值
	name := defaultName
	if len(os.Args) > 1 {
		name = os.Args[1]
	}

	// 超时设置
	ctx, cancel := context.WithTimeout(context.Background(), time.Second)

	defer cancel()

	// 远程调用
	r, err := c.SayHello(ctx, &pb.HelloRequest{Name: name})
	if err != nil {
		log.Fatalf("could not greet: %v", err)
	}

	// 将服务端的返回信息打印出来
	log.Printf("Greeting: %s", r.GetMessage())
}
