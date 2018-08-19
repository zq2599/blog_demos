package com.bolingcavalry.springclouddeepprovider.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description : 提供普通的http服务
 * @Author : za2599@gmail.com
 * @Date : 2018-08-18 18:25
 */
@RestController
public class HelloService {

    @GetMapping("hello/{name}")
    public String hello(@PathVariable String name){
        return "Hello " + name + ", " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
    }
}
