package com.bolingcavalry.springbootrediskyrodemo.controller;

import com.alibaba.fastjson.JSONObject;
import com.bolingcavalry.springbootrediskyrodemo.bean.Person;
import com.bolingcavalry.springbootrediskyrodemo.service.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @Description : web响应
 * @Author : zq2599@gmail.com
 * @Date : 2018-06-10 22:525
 */
@Controller
public class RedisController {

    private static final Logger logger = LoggerFactory.getLogger(RedisController.class);

    private static final String PREFIX = "person_";

    @Autowired
    private RedisClient redisClient;

    /**
     * 返回指定id的记录
     * @param id
     */
    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String check(@PathVariable("id") final int id) {
        Person person = null;
        try{
            person = redisClient.getObject(PREFIX + id);
        }catch(Exception e){
            logger.error("get from redis error, ", e);
        }

        return null==person ? ("can not get person by id [" + id + "]") : JSONObject.toJSONString(person);
    }

    /**
     * 向redis增加一条记录
     */
    @RequestMapping(value = "/add/{id}/{name}/{age}", method = RequestMethod.GET)
    @ResponseBody
    public String add(@PathVariable("id") int id, @PathVariable("id") String name, @PathVariable("id") int age) {
        Person person = new Person();
        person.setId(id);
        person.setName(name);
        person.setAge(age);

        String rlt = null;

        try{
            redisClient.set(PREFIX + person.getId(), person);
            rlt = "save to redis success!";
        }catch(Exception e){
            rlt = "save to redis fail, " + e;
            logger.error("save redis error, ", e);
        }

        return rlt + "(" + new Date() + ")";
    }
}