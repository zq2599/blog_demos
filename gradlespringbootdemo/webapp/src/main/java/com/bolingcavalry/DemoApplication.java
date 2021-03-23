package com.bolingcavalry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2021/3/21 8:12
 */
@SpringBootApplication
@RestController
public class DemoApplication {

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello() {
        return "Hello " + new Date();
    }


    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}