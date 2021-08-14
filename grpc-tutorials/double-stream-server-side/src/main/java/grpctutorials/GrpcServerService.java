package grpctutorials;

import com.bolingcavalry.grpctutorials.lib.DeductReply;
import com.bolingcavalry.grpctutorials.lib.ProductOrder;
import com.bolingcavalry.grpctutorials.lib.StockServiceGrpc;
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
public class GrpcServerService extends StockServiceGrpc.StockServiceImplBase {

    @Override
    public StreamObserver<ProductOrder> batchDeduct(StreamObserver<DeductReply> responseObserver) {
        // 返回匿名类，给上层框架使用
        return new StreamObserver<ProductOrder>() {

            private int totalCount = 0;

            @Override
            public void onNext(ProductOrder value) {
                log.info("正在处理商品[{}]，数量为[{}]",
                        value.getProductId(),
                        value.getNumber());

                // 增加总量
                totalCount += value.getNumber();

                int code;
                String message;

                // 假设单数的都有库存不足的问题
                if (0 == value.getNumber() % 2) {
                    code = 10000;
                    message = String.format("商品[%d]扣减库存数[%d]成功", value.getProductId(), value.getNumber());
                } else {
                    code = 10001;
                    message = String.format("商品[%d]扣减库存数[%d]失败", value.getProductId(), value.getNumber());
                }

                responseObserver.onNext(DeductReply.newBuilder()
                        .setCode(code)
                        .setMessage(message)
                        .build());
            }

            @Override
            public void onError(Throwable t) {
                log.error("批量减扣库存异常", t);
            }

            @Override
            public void onCompleted() {
                log.info("批量减扣库存完成，共计[{}]件商品", totalCount);
                responseObserver.onCompleted();
            }
        };
    }
}
