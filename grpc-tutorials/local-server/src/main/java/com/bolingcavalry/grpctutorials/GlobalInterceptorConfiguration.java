package com.bolingcavalry.grpctutorials;

import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Configuration;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 配置类，用于生成日志拦截类的bean
 * @date 2021/4/10 22:20
 */
@Configuration(proxyBeanMethods = false)
public class GlobalInterceptorConfiguration {
    @GrpcGlobalServerInterceptor
    ServerInterceptor logServerInterceptor() {
        return new LogGrpcInterceptor();
    }
}
