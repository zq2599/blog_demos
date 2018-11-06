package com.bolingcavalry.springbootsidecardemo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @Description: 一个最普通的Controller，hello接口返回一个字符串并带上当前时间
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2018/11/6 14:15
 */
@RestController
public class HelloController {

    @RequestMapping(value = "/hello")
    public String hello(){
        return "Hello version 1.0 " + new Date();
    }
}
