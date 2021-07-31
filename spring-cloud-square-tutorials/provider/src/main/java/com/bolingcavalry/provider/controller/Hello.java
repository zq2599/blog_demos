package com.bolingcavalry.provider.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author zq2599@gmail.com
 * @Title: web服务
 * @Package
 * @Description:
 * @date 7/31/21 2:53 上`午
 */
@RestController
public class Hello {

    @Autowired
    DiscoveryClient client;

    @GetMapping("/hello-str")
    public String hellostr() {
        List<ServiceInstance> instances = client.getInstances("provider");
        ServiceInstance selectedInstance = instances
                .get(new Random().nextInt(instances.size()));
        return "Hello World: " + selectedInstance.getServiceId() + ":" + selectedInstance
                .getHost() + ":" + selectedInstance.getPort()
                + new SimpleDateFormat(":yyyy-MM-dd hh:mm:ss").format(new Date());
    }
}