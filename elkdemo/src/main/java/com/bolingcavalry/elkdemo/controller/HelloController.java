package com.bolingcavalry.elkdemo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class HelloController {
    protected static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @RequestMapping(value="/hello/{username}",method= RequestMethod.GET)
    public String hello(@PathVariable String username) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(new Date());
        logger.info("execute hello from {} - {}", username, time);
        return "hello " + username + ", " + time;
    }
}
