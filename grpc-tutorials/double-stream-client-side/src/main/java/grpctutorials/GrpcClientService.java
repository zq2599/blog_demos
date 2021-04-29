package grpctutorials;

import com.bolingcavalry.grpctutorials.lib.AddCartReply;
import com.bolingcavalry.grpctutorials.lib.CartServiceGrpc;
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

    public String batchDeduct(int count) {
        // TODO 等待实现
        return null;
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
