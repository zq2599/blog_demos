package com.bolingcavalry.simpleconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class SimpleConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleConsumerApplication.class, args);
    }
}
