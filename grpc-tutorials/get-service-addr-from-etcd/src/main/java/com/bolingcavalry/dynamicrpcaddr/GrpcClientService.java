package com.bolingcavalry.dynamicrpcaddr;

import com.bolingcavalry.grpctutorials.lib.HelloReply;
import com.bolingcavalry.grpctutorials.lib.HelloRequest;
import com.bolingcavalry.grpctutorials.lib.SimpleGrpc;
import io.grpc.StatusRuntimeException;
import lombok.Setter;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 业务服务类，调用gRPC服务
 * @date 2021/4/17 10:05
 */
@Service
public class GrpcClientService {

    @Autowired(required = false)
    @Setter
    private StubWrapper stubWrapper;

    public String sendMessage(final String name) {
        // 很有可能simpleStub对象为null
        if (null==stubWrapper) {
            return "invalid SimpleBlockingStub, please check etcd configuration";
        }

        try {
            final HelloReply response = stubWrapper.getSimpleBlockingStub().sayHello(HelloRequest.newBuilder().setName(name).build());
            return response.getMessage();
        } catch (final StatusRuntimeException e) {
            return "FAILED with " + e.getStatus().getCode().name();
        }
    }

}
