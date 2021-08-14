package com.bolingcavalry.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author zq2599@gmail.com
 * @Title: 服务提供者demo
 * @Package
 * @Description:
 * @date 8/8/21 04:21 下午
 */
@SpringBootApplication
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}