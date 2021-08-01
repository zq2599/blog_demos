package com.bolingcavalry.consumer.controller;

import com.bolingcavalry.client.HelloResponse;
import com.bolingcavalry.consumer.service.HelloService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author zq2599@gmail.com
 * @Title: 服务消费者
 * @Package
 * @Description:
 * @date 7/31/21 7:54 上午
 */
@RestController
public class RemoteHello {
//    @Autowired
//    HelloService helloService;
//
//    @GetMapping("/remote-str")
//    public HelloResponse hello() throws IOException {
//        return helloService.hello();
//    }
}