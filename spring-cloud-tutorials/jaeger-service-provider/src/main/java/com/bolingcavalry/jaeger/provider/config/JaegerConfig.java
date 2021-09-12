package com.bolingcavalry.jaeger.provider.config;

import io.jaegertracing.internal.MDCScopeManager;
import io.opentracing.contrib.java.spring.jaeger.starter.TracerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2021/9/12 6:30 下午
 * @description jaeger相关的配置
 */
@Configuration
public class JaegerConfig {

    @Bean
    public TracerBuilderCustomizer mdcBuilderCustomizer() {
        // 1.8新特性，函数式接口
        return builder -> builder.withScopeManager(new MDCScopeManager.Builder().build());
    }
}
