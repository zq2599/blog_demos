package com.bolingcavalry.webdemo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * @Description: 提供web服务
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/7/7 19:24
 */
@RestController
@RequestMapping("/hello")
public class HelloController {

    @RequestMapping(value = "time", method = RequestMethod.GET)
    public String hello(HttpServletRequest request){

        return "hello, "
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                + ", extendtag ["
                + request.getHeader("extendtag")
                + "]";
    }
}
