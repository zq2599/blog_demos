package com.bolingcavalry.grpctutorials;

import com.bolingcavalry.grpctutorials.lib.Buyer;
import com.bolingcavalry.grpctutorials.lib.Order;
import com.bolingcavalry.grpctutorials.lib.OrderQueryGrpc;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 业务服务类，调用gRPC服务
 * @date 2021/4/17 10:05
 */
@Service
@Slf4j
public class GrpcClientService {

    @GrpcClient("server-stream-server-side")
    private OrderQueryGrpc.OrderQueryBlockingStub orderQueryBlockingStub;

    public List<DispOrder> listOrders(final String name) {
        // gRPC的请求参数
        Buyer buyer = Buyer.newBuilder().setBuyerId(101).build();

        // gRPC的响应
        Iterator<Order> orderIterator;

        // 当前方法的返回值
        List<DispOrder> orders = new ArrayList<>();

        // 通过stub发起远程gRPC请求
        try {
            orderIterator = orderQueryBlockingStub.listOrders(buyer);
        } catch (final StatusRuntimeException e) {
            log.error("error grpc invoke", e);
            return new ArrayList<>();
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        log.info("start put order to list");
        while (orderIterator.hasNext()) {
            Order order = orderIterator.next();

            orders.add(new DispOrder(order.getOrderId(),
                                    order.getProductId(),
                                    // 使用DateTimeFormatter将时间戳转为字符串
                                    dtf.format(LocalDateTime.ofEpochSecond(order.getOrderTime(), 0, ZoneOffset.of("+8"))),
                                    order.getBuyerRemark()));
            log.info("");
        }

        log.info("end put order to list");

        return orders;
    }


}
