package main

import (
	"context"
	"google.golang.org/grpc"
	"io"
	"log"
	"time"

	pb "grpcstream"
)

const (
	address     = "localhost:50051"
	defaultId = "666"
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
	client := pb.NewIGrpcStremServiceClient(conn)

	// 超时设置
	ctx, cancel := context.WithTimeout(context.Background(), time.Second)

	defer cancel()

	log.Println("测试单一请求应答，一对一")
	singleReqSingleResp(ctx, client)

	log.Println("测试服务端流式应答，一对多")
	singleReqMultiResp(ctx, client)

	log.Println("测试客户端流式请求，多对一")
	multiReqSingleResp(ctx, client)

	log.Println("测试双向流式请求应答，多对多")
	multiReqMultiResp(ctx, client)

	log.Println("测试完成")
}


func singleReqSingleResp(ctx context.Context, client pb.IGrpcStremServiceClient) error {
	// 远程调用
	r, err := client.SingleReqSingleResp(ctx, &pb.SingleRequest{Id: 101})

	if err != nil {
		log.Fatalf("1. 远程调用异常 : %v", err)
		return err
	}

	// 将服务端的返回信息打印出来
	log.Printf("response, id : %d, name : %s", r.GetId(), r.GetName())

	return nil
}


func singleReqMultiResp(ctx context.Context, client pb.IGrpcStremServiceClient) error {
	// 远程调用
	recvStream, err := client.SingleReqMultiResp(ctx, &pb.SingleRequest{Id: 201})

	if err != nil {
		log.Fatalf("2. 远程调用异常 : %v", err)
		return err
	}

	for {
		singleResponse, err := recvStream.Recv()
		if err == io.EOF {
			log.Printf("2. 获取数据完毕")
			break
		}

		log.Printf("2. 收到服务端响应, id : %d, name : %s", singleResponse.GetId(), singleResponse.GetName())
	}

	return nil
}

func multiReqSingleResp(ctx context.Context, client pb.IGrpcStremServiceClient) error {
	// 远程调用
	sendStream, err := client.MultiReqSingleResp(ctx)

	if err != nil {
		log.Fatalf("3. 远程调用异常 : %v", err)
		return err
	}

	// 发送多条记录到服务端
	for i:=0; i<10; i++ {
		if err = sendStream.Send(&pb.SingleRequest{Id: int32(300+i)}); err!=nil {
			log.Fatalf("3. 通过流发送数据异常 : %v", err)
			return err
		}
	}

	singleResponse, err := sendStream.CloseAndRecv()

	if err != nil {
		log.Fatalf("3. 服务端响应异常 : %v", err)
		return err
	}

	// 将服务端的返回信息打印出来
	log.Printf("response, id : %d, name : %s", singleResponse.GetId(), singleResponse.GetName())

	return nil
}

func multiReqMultiResp(ctx context.Context, client pb.IGrpcStremServiceClient) error {
	// 远程调用
	intOutStream, err := client.MultiReqMultiResp(ctx)

	if err != nil {
		log.Fatalf("4. 远程调用异常 : %v", err)
		return err
	}

	// 发送多条记录到服务端
	for i:=0; i<10; i++ {
		if err = intOutStream.Send(&pb.SingleRequest{Id: int32(400+i)}); err!=nil {
			log.Fatalf("4. 通过流发送数据异常 : %v", err)
			return err
		}
	}

	// 服务端一直在接收，直到收到io.EOF为止
	// 因此，这里必须发送io.EOF到服务端，让服务端知道发送已经结束(很重要)
	intOutStream.CloseSend()

	// 接收服务端发来的数据
	for {
		singleResponse, err := intOutStream.Recv()
		if err == io.EOF {
			log.Printf("4. 获取数据完毕")
			break
		} else if err != nil {
			log.Fatalf("4. 接收服务端数据异常 : %v", err)
			break
		}

		log.Printf("4. 收到服务端响应, id : %d, name : %s", singleResponse.GetId(), singleResponse.GetName())
	}

	return nil
}