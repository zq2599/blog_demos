package com.bolingcavalry.grpctutorials;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 启动类
 * @date 2021/5/11 06:58
 */
@EnableEurekaClient
@EnableDiscoveryClient
@SpringBootApplication
public class CloudClientSideApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudClientSideApplication.class, args);
    }

}
