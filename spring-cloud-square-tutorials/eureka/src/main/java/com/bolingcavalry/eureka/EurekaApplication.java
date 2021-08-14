package com.bolingcavalry.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author zq2599@gmail.com
 * @Title: eureka服务
 * @Package
 * @Description:
 * @date 7/30/21 3:39 下午
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(EurekaApplication.class, args);
    }

}

