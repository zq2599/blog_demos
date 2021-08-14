package com.bolingcavalry.grpctutorials;

import io.grpc.ClientInterceptor;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 配置类，用于生成日志拦截类的bean
 * @date 2021/4/17 10:16
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@Configuration(proxyBeanMethods = false)
public class GlobalClientInterceptorConfiguration {

    @GrpcGlobalClientInterceptor
    ClientInterceptor logClientInterceptor() {
        return new LogGrpcInterceptor();
    }

}
