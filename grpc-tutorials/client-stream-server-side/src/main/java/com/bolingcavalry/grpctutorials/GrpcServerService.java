package com.bolingcavalry.grpctutorials;

import com.bolingcavalry.grpctutorials.lib.AddCartReply;
import com.bolingcavalry.grpctutorials.lib.CartServiceGrpc;
import com.bolingcavalry.grpctutorials.lib.ProductOrder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 对外提供Grpc服务的类
 * @date 2021/4/19 08:19
 */
@GrpcService
@Slf4j
public class GrpcServerService extends CartServiceGrpc.CartServiceImplBase {


    @Override
    public StreamObserver<ProductOrder> addToCart(StreamObserver<AddCartReply> responseObserver) {
        // 返回匿名类，给上层框架使用
        return new StreamObserver<ProductOrder>() {

            // 记录处理产品的总量
            private int totalCount = 0;

            @Override
            public void onNext(ProductOrder value) {
                log.info("正在处理商品[{}]，数量为[{}]",
                        value.getProductId(),
                        value.getNumber());

                // 增加总量
                totalCount += value.getNumber();
            }

            @Override
            public void onError(Throwable t) {
                log.error("添加购物车异常", t);
            }

            @Override
            public void onCompleted() {
                log.info("添加购物车完成，共计[{}]件商品", totalCount);
                responseObserver.onNext(AddCartReply.newBuilder()
                                                    .setCode(10000)
                                                    .setMessage(String.format("添加购物车完成，共计[%d]件商品", totalCount))
                                                    .build());
                responseObserver.onCompleted();

            }
        };
    }
}
