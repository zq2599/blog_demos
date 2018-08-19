package com.bolingcavalry.springclouddeepconsumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @Description : 远程调用测试的
 * @Author : qin_zhao@kingdee.com
 * @Date : 2018-08-18 19:10
 */
@RestController
public class ConsumerServiceController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @GetMapping("serviceinfo")
    public String serviceinfo(){
        ServiceInstance serviceInstance = loadBalancerClient.choose("springcloud-deep-provider");
        return null==serviceInstance ? "service not found" : serviceInstance.toString();
    }

    @GetMapping("consume/{name}")
    public String consume(@PathVariable String name){
               return "From remote : " + restTemplate.getForObject("http://springcloud-deep-provider/hello/" + name, String.class);
    }
}