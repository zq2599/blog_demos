package com.bolingcavalry.grpctutorials;

import com.bolingcavalry.grpctutorials.lib.AddCartReply;
import com.bolingcavalry.grpctutorials.lib.CartServiceGrpc;
import com.bolingcavalry.grpctutorials.lib.ProductOrder;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 业务服务类，调用gRPC服务
 * @date 2021/4/17 10:05
 */
@Service
@Slf4j
public class GrpcClientService {

    @GrpcClient("client-stream-server-side")
    private CartServiceGrpc.CartServiceStub cartServiceStub;

    public String addToCart() {
        /*
        StreamObserver<RouteSummary> responseObserver =
        (StreamObserver<RouteSummary>) mock(StreamObserver.class);
    RouteGuideGrpc.RouteGuideStub stub = RouteGuideGrpc.newStub(inProcessChannel);
    ArgumentCaptor<RouteSummary> routeSummaryCaptor = ArgumentCaptor.forClass(RouteSummary.class);

    StreamObserver<Point> requestObserver = stub.recordRoute(responseObserver);

    requestObserver.onNext(p1);
    requestObserver.onNext(p2);
    requestObserver.onNext(p3);
    requestObserver.onNext(p4);

    verify(responseObserver, never()).onNext(any(RouteSummary.class));

    requestObserver.onCompleted();
        */


        ExtendResponseObserver<AddCartReply> responseObserver = new ExtendResponseObserver<AddCartReply>() {

            @Override
            public String getExtra() {
                return String.format("返回码[%d]，返回信息[%s]" , code, message);
            }

            private int code;

            private String message;

            @Override
            public void onNext(AddCartReply value) {
                code = value.getCode();
                message = value.getMessage();
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };

        StreamObserver<ProductOrder> requestObserver = cartServiceStub.addToCart(responseObserver);
        requestObserver.onNext(build(101, 1));
        requestObserver.onNext(build(102, 2));
        requestObserver.onNext(build(103, 3));

        requestObserver.onCompleted();

        return responseObserver.getExtra();

    }

    /**
     * 创建ProductOrder对象
     * @param productId
     * @param num
     * @return
     */
    private static ProductOrder build(int productId, int num) {
        return ProductOrder.newBuilder().setProductId(productId).setNumber(num).build();
    }


}
