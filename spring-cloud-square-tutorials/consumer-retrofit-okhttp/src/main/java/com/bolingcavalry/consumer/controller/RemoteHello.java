package com.bolingcavalry.consumer.controller;

import com.bolingcavalry.client.HelloResponse;
import com.bolingcavalry.consumer.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    @Autowired(required = false)
    HelloService helloService;

    @GetMapping("/remote-obj")
    public HelloResponse hello(@RequestParam("name") String name) throws IOException {
        return helloService.hello(name).execute().body();
    }
}