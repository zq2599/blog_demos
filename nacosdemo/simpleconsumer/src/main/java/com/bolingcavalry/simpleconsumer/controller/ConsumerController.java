package com.bolingcavalry.simpleconsumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 提供web服务的controller
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/7/28 11:08
 */
@RestController
public class ConsumerController {

    @Autowired
    LoadBalancerClient loadBalancerClient;

    @RequestMapping("/test")
    public String test(){
        //根据应用名称取得实例对象
        ServiceInstance serviceInstance = loadBalancerClient.choose("simple-provider");
        //根据实例对象取得地址
        String uri = serviceInstance.getUri().toString();
        String result = new RestTemplate().getForObject(uri + "/hello/bolingcavalry", String.class);
        return "provider uri : " + uri + "<br>" + "response :" + result;
    }
}
