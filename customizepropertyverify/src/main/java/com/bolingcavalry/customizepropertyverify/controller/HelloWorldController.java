package com.bolingcavalry.customizepropertyverify.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @Description :
 * @Author : zq2599@gmail.com
 * @Date : 2018-08-11 16:30
 */
@RestController
public class HelloWorldController {
    @RequestMapping("/hello")
    public String hello(){
        return "hello, " + new Date();
    }


}
