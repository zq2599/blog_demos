package com.bolingcavalry.provider.controller;

import com.bolingcavalry.client.HelloResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    public static final String HELLO_PREFIX = "Hello World";

    @Autowired
    DiscoveryClient client;

    /**
     * 随机取一个provider实例，返回其描述信息，
     * 如果只有一个provider实例时，返回的就是当前服务信息
     * @return
     */
    private String providerDescription() {
        List<ServiceInstance> instances = client.getInstances("provider");
        ServiceInstance selectedInstance = instances
                .get(new Random().nextInt(instances.size()));

        return String.format("serviceId [%s], host [%s], port [%d]",
                selectedInstance.getServiceId(),
                selectedInstance.getHost(),
                selectedInstance.getPort());
    }

    private String dateStr(){
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
    }

    /**
     * 返回字符串类型
     * @return
     */
    @GetMapping("/hello-str")
    public String helloStr() {
        List<ServiceInstance> instances = client.getInstances("provider");
        ServiceInstance selectedInstance = instances
                .get(new Random().nextInt(instances.size()));
        return HELLO_PREFIX
                + " : "
                + providerDescription()
                + ", "
                + dateStr();
    }

    /**
     * 返回bean
     * @return
     */
    @GetMapping("/hello-obj")
    public HelloResponse helloObj(@RequestParam("name") String name) {
        return new HelloResponse(name, dateStr(), providerDescription());
    }
}