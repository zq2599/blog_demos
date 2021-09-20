package com.bolingcavalry.jaeger.consumer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2021/9/12 7:26 上午
 * @description 功能介绍
 */
@RestController
@Slf4j
public class HelloConsumerController {

    @Autowired
    RestTemplate restTemplate;

    /**
     * 返回字符串类型
     * @return
     */
    @GetMapping("/hello")
    public String hello() {
        String url = "http://jaeger-service-provider:8080/hello";

        log.info("try http request");

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        StringBuffer sb = new StringBuffer();
        HttpStatus statusCode = responseEntity.getStatusCode();
        String body = responseEntity.getBody();

        // 返回
        return "response from jaeger-service-provider \nstatus : " + statusCode + "\nbody : " + body;
    }

}
