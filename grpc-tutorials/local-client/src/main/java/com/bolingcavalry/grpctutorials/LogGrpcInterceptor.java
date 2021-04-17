package com.bolingcavalry.grpctutorials;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 拦截请求打印日志
 * @date 2021/4/17 10:11
 */
public class LogGrpcInterceptor implements ClientInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LogGrpcInterceptor.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions, Channel next) {
        log.info(method.getFullMethodName());
        return next.newCall(method, callOptions);
    }

}
