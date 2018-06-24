package com.bolingcavalry.redisperformancedemostring.controller;

import com.alibaba.fastjson.JSONObject;
import com.bolingcavalry.redisperformancedemostring.Helper;
import com.bolingcavalry.redisperformancedemostring.bean.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class RedisController {

    private static final Logger logger = LoggerFactory.getLogger(RedisController.class);

    private static AtomicInteger addPersionIdGenerator = new AtomicInteger(0);

    private static AtomicInteger checkPersionIdGenerator = new AtomicInteger(0);

    private static final String PREFIX = "person_";

    private static final int TIMES = 100;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping(value = "/save/{key}/{value}", method = RequestMethod.GET)
    @ResponseBody
    public String save(@PathVariable("key") final String key, @PathVariable("value") final String value) {
        try{
            stringRedisTemplate.opsForValue().set(key, value);
        }catch(Exception e){
            e.printStackTrace();
        }
        return "1. success";
    }

    @RequestMapping(value = "/checksingle/{id}", method = RequestMethod.GET)
    public void check(@PathVariable("id") final int id, HttpServletResponse response) {
        checkPerson(id, response);
    }

    @RequestMapping(value = "/check", method = RequestMethod.GET)
    public void check(HttpServletResponse response) {
        boolean hasError = false;
        for(int i=0;i<TIMES;i++) {
            boolean rlt = checkPerson(checkPersionIdGenerator.incrementAndGet(), response);
            if(!rlt){
                hasError = true;
                break;
            }
        }

        if(!hasError){
            Helper.success(response, "check success");
        }
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public void add(HttpServletResponse response) {
        boolean isSuccess;
        for(int i=0;i<TIMES;i++) {
            Person person = Helper.buildPerson(addPersionIdGenerator);

            while (true) {
                isSuccess = false;
                try {
                    stringRedisTemplate.opsForValue().set(PREFIX + person.getId(), JSONObject.toJSONString(person));
                    isSuccess = true;
                } catch (Exception e) {
                    logger.error("save redis error");
                    return;
                }

                if (isSuccess) {
                    break;
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        logger.error("1. sleep error, ", e);
                    }
                }
            }
        }

        Helper.success(response, "save success");
    }

    @RequestMapping(value = "/reset", method = RequestMethod.GET)
    public void reset(HttpServletResponse response){
          addPersionIdGenerator.set(0);
          checkPersionIdGenerator.set(0);
          Helper.success(response, "id generator reset success!");
    }

    /**
     * 检查指定id的数据是否正常
     * @param id
     * @param response
     */
    private boolean checkPerson(int id, HttpServletResponse response){
        String raw = null;

        boolean isSuccess;

        while (true) {
            isSuccess = false;
            try {
                raw = stringRedisTemplate.opsForValue().get(PREFIX + id);
                isSuccess = true;
            } catch (Exception e) {
                logger.error("get from redis error");
            }

            if (isSuccess) {
                break;
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logger.error("1. sleep error, ", e);
                }
            }
        }

        if(null==raw){
            Helper.error( response, "[" + id + "] not exist!");
            return false;
        }

        Person person = JSONObject.parseObject(raw, Person.class);

        String error = Helper.checkPerson(person);

        if(null==error){
            //Helper.success(response, "[" + id + "] check success!");
            return true;
        }else {
            Helper.error(response, "[" + id + "] " + error);
            return false;
        }
    }


}