package com.bolingcavalry.provider;

import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zq2599@gmail.com
 * @Title: 服务提供者demo
 * @Package
 * @Description:
 * @date 7/30/21 2:53 下午
 */
@SpringBootApplication
@RestController
public class ProviderApplication {

    @Autowired
    DiscoveryClient client;

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }

    @GetMapping("/")
    public String hello() {
        List<ServiceInstance> instances = client.getInstances("provider");
        ServiceInstance selectedInstance = instances
                .get(new Random().nextInt(instances.size()));
        return "Hello World: " + selectedInstance.getServiceId() + ":" + selectedInstance
                .getHost() + ":" + selectedInstance.getPort();
    }
}