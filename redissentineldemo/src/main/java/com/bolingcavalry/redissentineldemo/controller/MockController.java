package com.bolingcavalry.redissentineldemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
public class MockController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping(value = "/testredis/{key}/{value}", method = RequestMethod.GET)
    @ResponseBody
    public String testRedis(@PathVariable("key") final String key, @PathVariable("value") final String value) {
        try{
            stringRedisTemplate.opsForValue().set(key, value);
        }catch(Exception e){
            e.printStackTrace();
        }
        return "1. success";
    }
}
