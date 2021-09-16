package com.bolingcavalry.jaeger.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zq2599@gmail.com
 * @Title: 服务提供者demo
 * @Package
 * @Description:
 * @date 8/8/21 04:21 下午
 */
@SpringBootApplication
public class JaegerConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(JaegerConsumerApplication.class, args);
    }
}