package com.bolingcavalry.springbootmulticastconsumer;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Description: (这里用一句话描述这个类的作用)
 *
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/10/17 19:04
 */
@SpringBootApplication
@EnableDubbo
public class SpringBootMulticastConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootMulticastConsumerApplication.class, args);
    }
}
