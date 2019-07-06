package com.bolingcavalry.provider.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

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
