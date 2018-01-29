package com.bolingcavalry.serviceconsumer.controller;

import com.bolingcavalry.serviceconsumer.feign.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description : 用户服务接口
 * @Author : zq2599@gmail.com
 * @Date : 2018-01-25 14:09
 */
@RestController
public class UserFacadeController {

    @Autowired
    private UserFeignClient userFeignClient;

    @GetMapping("/user/{id}/{name}")
    public String getUserInfo(@PathVariable("id") final String id, @PathVariable("name") final String name) {
        return "1. ---" + userFeignClient.getUserInfoWithRequestParam(id, name);
    }
}