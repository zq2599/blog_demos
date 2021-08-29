package com.bolingcavalry.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/8/29 9:11
 */
@SpringBootApplication
public class RequestRateLimiterApplication {
    public static void main(String[] args) {
        SpringApplication.run(RequestRateLimiterApplication.class,args);
    }
}
