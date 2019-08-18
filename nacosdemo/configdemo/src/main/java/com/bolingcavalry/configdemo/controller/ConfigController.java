package com.bolingcavalry.configdemo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 提供web服务的controller
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/08/18 18:23
 */
@RestController
@RefreshScope
public class ConfigController {

    @Value("${bolingcavalry.desc:desc from code}")
    private String desc;

    @RequestMapping("/test")
    public String test(){
        return desc + new SimpleDateFormat(" [yyyy-mm-dd  HH:mm:ss]").format(new Date());
    }
}
