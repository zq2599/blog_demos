package com.bolingcavalry.simpleeureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @Description: 启动eureka
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/17 8:21
 */
@EnableEurekaServer
@SpringBootApplication
public class SimpleEurekaApplication {
    public static void main(String[] args) {
        SpringApplication.run(SimpleEurekaApplication.class, args);
    }
}
