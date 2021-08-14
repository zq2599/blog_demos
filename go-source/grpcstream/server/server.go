package main

import (
	"context"
	"google.golang.org/grpc"
	pb "grpcstream"
	"io"
	"log"
	"net"
	"strconv"
)

// 常量：监听端口
const (
	port = ":50051"
)

// 定义结构体，在调用注册api的时候作为入参，
// 该结构体会带上proto中定义的方法，里面是业务代码
// 这样远程调用时就执行了业务代码了
type server struct {
	// pb.go中自动生成的，是个空结构体
	pb.UnimplementedIGrpcStremServiceServer
}

// 单项流式 ：单个请求，单个响应
func (s *server) SingleReqSingleResp(ctx context.Context, req *pb.SingleRequest) (*pb.SingleResponse, error) {
	id := req.GetId()

	// 打印请求参数
	log.Println("1. 收到请求:", id)
	// 实例化结构体SingleResponse，作为返回值
	return &pb.SingleResponse{Id: id, Name: "1. name-" + strconv.Itoa(int(id))}, nil
}

// 服务端流式 ：单个请求，集合响应
func (s *server) SingleReqMultiResp(req *pb.SingleRequest, stream pb.IGrpcStremService_SingleReqMultiRespServer) error {
	// 取得请求参数
	id := req.GetId()

	// 打印请求参数
	log.Println("2. 收到请求:", id)

	// 返回多条记录
	for i := 0; i < 10; i++ {
		stream.Send(&pb.SingleResponse{Id: int32(i), Name: "2. name-" + strconv.Itoa(i)})
	}

	return nil
}

// 客户端流式 ：集合请求，单个响应
func (s *server) MultiReqSingleResp(reqStream pb.IGrpcStremService_MultiReqSingleRespServer) error {
	var addVal int32 = 0

	// 在for循环中接收流式请求
	for {
		// 一次接受一条记录
		singleRequest, err := reqStream.Recv()

		// 不等于io.EOF表示这是条有效记录
		if err == io.EOF {
			log.Println("3. 客户端发送完毕")
			break
		} else if err != nil {
			log.Fatalln("3. 接收时发生异常", err)
			break
		} else {
			log.Println("3. 收到请求:", singleRequest.GetId())
			// 收完之后，执行SendAndClose返回数据并结束本次调用
			addVal += singleRequest.GetId()
		}
	}

	return reqStream.SendAndClose(&pb.SingleResponse{Id: addVal, Name: "3. name-" + strconv.Itoa(int(addVal))})
}

// 双向流式 ：集合请求，集合响应
func (s *server) MultiReqMultiResp(reqStream pb.IGrpcStremService_MultiReqMultiRespServer) error {
	// 简单处理，对于收到的每一条记录都返回一个响应
	for {
		singleRequest, err := reqStream.Recv()

		// 不等于io.EOS表示这是条有效记录
		if err == io.EOF {
			log.Println("4. 接收完毕")
			return nil
		} else if err != nil {
			log.Fatalln("4. 接收时发生异常", err)
			return err
		} else {
			log.Println("4. 接收到数据", singleRequest.GetId())

			id := singleRequest.GetId()

			if sendErr := reqStream.Send(&pb.SingleResponse{Id: id, Name: "4. name-" + strconv.Itoa(int(id))}); sendErr != nil {
				log.Println("4. 返回数据异常数据", sendErr)
				return sendErr
			}
		}
	}
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
	pb.RegisterIGrpcStremServiceServer(s, &server{})

	log.Println("开始监听，等待远程调用...")

	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}