package com.bolingcavalry.springcloudk8sdiscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
public class Springcloudk8sdiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(Springcloudk8sdiscoveryApplication.class, args);
    }
}
