package com.bolingcavalry.grpctutorials;

import com.bolingcavalry.grpctutorials.lib.HelloReply;
import com.bolingcavalry.grpctutorials.lib.HelloRequest;
import com.bolingcavalry.grpctutorials.lib.SimpleGrpc;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 业务服务类，调用gRPC服务
 * @date 2021/5/11 07:09
 */
@Service
public class GrpcClientService {

    @GrpcClient("cloud-server-side")
    private SimpleGrpc.SimpleBlockingStub simpleStub;

    public String sendMessage(final String name) {
        try {
            final HelloReply response = this.simpleStub.sayHello(HelloRequest.newBuilder().setName(name).build());
            return response.getMessage();
        } catch (final StatusRuntimeException e) {
            return "FAILED with " + e.getStatus().getCode().name();
        }
    }

}
