package com.bolingcavalry.customizeapplicationevent.controller;

import com.bolingcavalry.customizeapplicationevent.publish.CustomizePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @Description : 收到web请求后发送一条广播
 * @Author : zq2599@gmail.com
 * @Date : 2018-08-16 06:45
 */
@RestController
public class CustomizePublishEventController {

    @Autowired
    private CustomizePublisher customizePublisher;

    @RequestMapping("/publish")
    public String publish(){

        customizePublisher.publishEvent();

        return "publish finish, "
                + new Date();
    }


}