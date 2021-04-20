package com.bolingcavalry.grpctutorials;

import com.bolingcavalry.grpctutorials.lib.Buyer;
import com.bolingcavalry.grpctutorials.lib.Order;
import com.bolingcavalry.grpctutorials.lib.OrderQueryGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 对外提供Grpc服务的类
 * @date 2021/4/19 08:19
 */
@GrpcService
public class GrpcServerService extends OrderQueryGrpc.OrderQueryImplBase {

    /**
     * mock一批数据
     * @return
     */
    private static List<Order> mockOrders(){
        List<Order> list = new ArrayList<>();
        Order.Builder builder = Order.newBuilder();

        for (int i = 0; i < 10; i++) {
            list.add(builder
                    .setOrderId(i)
                    .setProductId(1000+i)
                    .setOrderTime(System.currentTimeMillis()/1000)
                    .setBuyerRemark(("remark-" + i))
                    .build());
        }

        return list;
    }

    @Override
    public void listOrders(Buyer request, StreamObserver<Order> responseObserver) {
        // 持续输出到client
        for (Order order : mockOrders()) {
            responseObserver.onNext(order);
        }
        // 结束输出
        responseObserver.onCompleted();
    }
}
