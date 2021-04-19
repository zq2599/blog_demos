package com.bolingcavalry.grpctutorials;

import com.bolingcavalry.grpctutorials.lib.Buyer;
import com.bolingcavalry.grpctutorials.lib.Order;
import com.bolingcavalry.grpctutorials.lib.OrderQueryGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 对外提供Grpc服务的类
 * @date 2021/4/19 08:19
 */
@GrpcService
public class GrpcServerService extends OrderQueryGrpc.OrderQueryImplBase {
    @Override
    public void listOrders(Buyer request, StreamObserver<Order> responseObserver) {
        super.listOrders(request, responseObserver);
    }
}
