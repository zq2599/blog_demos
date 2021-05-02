package grpctutorials;

import com.bolingcavalry.grpctutorials.lib.DeductReply;
import com.bolingcavalry.grpctutorials.lib.ProductOrder;
import com.bolingcavalry.grpctutorials.lib.StockServiceGrpc;
import io.grpc.stub.StreamObserver;
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

    @GrpcClient("double-stream-server-side")
    private StockServiceGrpc.StockServiceStub stockServiceStub;

    /**
     * 批量减库存
     * @param count
     * @return
     */
    public String batchDeduct(int count) {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        // responseObserver的onNext和onCompleted会在另一个线程中被执行，
        // ExtendResponseObserver继承自StreamObserver
        ExtendResponseObserver<DeductReply> responseObserver = new ExtendResponseObserver<DeductReply>() {

            StringBuilder stringBuilder = new StringBuilder();

            @Override
            public String getExtra() {
                return stringBuilder.toString();
            }

            @Override
            public void onNext(DeductReply value) {
                log.info("batch deduct on next");
                stringBuilder.append(String.format("返回码[%d]，返回信息:%s<br>" , value.getCode(), value.getMessage()));
            }

            @Override
            public void onError(Throwable t) {
                log.error("batch deduct gRPC request error", t);
                stringBuilder.append("batch deduct gRPC error, " + t.getMessage());
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                log.info("batch deduct on complete");
                countDownLatch.countDown();
            }
        };

        // 远程调用，此时数据还没有给到服务端
        StreamObserver<ProductOrder> requestObserver = stockServiceStub.batchDeduct(responseObserver);

        for(int i=0; i<count; i++) {
            // 发送一笔数据到服务端
            requestObserver.onNext(build(101 + i, 1 + i));
        }

        // 客户端告诉服务端：数据已经发完了
        requestObserver.onCompleted();

        try {
            // 开始等待，如果服务端处理完成，那么responseObserver的onCompleted方法会在另一个线程被执行，
            // 那里会执行countDownLatch的countDown方法，一但countDown被执行，下面的await就执行完毕了，
            // await的超时时间设置为2秒
            countDownLatch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("countDownLatch await error", e);
        }

        log.info("service finish");
        // 服务端返回的内容被放置在requestObserver中，从getExtra方法可以取得
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
