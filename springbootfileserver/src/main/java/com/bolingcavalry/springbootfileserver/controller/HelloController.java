package com.bolingcavalry.springbootfileserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description : hello demo
 * @Author : zq2599@gmail.com
 * @Date : 2018-02-24 22:42
 */
@RestController
public class HelloController {

    @RequestMapping(value="/hello")
    public String hello(){
        return "Hello, " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
