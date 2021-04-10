package com.bolingcavalry.grpctutorials;

import com.bolingcavalry.grpctutorials.lib.HelloReply;
import com.bolingcavalry.grpctutorials.lib.SimpleGrpc;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Date;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 对外提供Grpc服务的类
 * @date 2021/4/10 22:26
 */
@GrpcService
public class GrpcServerService extends SimpleGrpc.SimpleImplBase {

    @Override
    public void sayHello(com.bolingcavalry.grpctutorials.lib.HelloRequest request,
                         io.grpc.stub.StreamObserver<com.bolingcavalry.grpctutorials.lib.HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + request.getName() + ", " + new Date()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
