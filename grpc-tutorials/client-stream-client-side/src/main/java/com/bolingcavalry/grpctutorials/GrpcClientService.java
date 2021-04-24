package com.bolingcavalry.grpctutorials;

import com.bolingcavalry.grpctutorials.lib.AddCartReply;
import com.bolingcavalry.grpctutorials.lib.CartServiceGrpc;
import com.bolingcavalry.grpctutorials.lib.ProductOrder;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    public String addToCart(int count) {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        ExtendResponseObserver<AddCartReply> responseObserver = new ExtendResponseObserver<AddCartReply>() {

            String extraStr;

            @Override
            public String getExtra() {
                return extraStr;
            }

            private int code;

            private String message;

            @Override
            public void onNext(AddCartReply value) {
                log.info("on next");
                code = value.getCode();
                message = value.getMessage();
            }

            @Override
            public void onError(Throwable t) {
                log.error("gRPC request error", t);
                extraStr = "gRPC error, " + t.getMessage();
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                log.info("on complete");
                extraStr = String.format("返回码[%d]，返回信息:%s" , code, message);
                countDownLatch.countDown();
            }
        };

        StreamObserver<ProductOrder> requestObserver = cartServiceStub.addToCart(responseObserver);

        for(int i=0; i<count; i++) {
            requestObserver.onNext(build(101 + i, 1 + i));
        }

        requestObserver.onCompleted();

        try {
            countDownLatch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("countDownLatch await error", e);
        }

        log.info("service finish");
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
