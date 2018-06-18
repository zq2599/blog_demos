package com.bolingcavalry.redisperformancedemokryo.controller;

import com.alibaba.fastjson.JSONObject;
import com.bolingcavalry.redisperformancedemokryo.Helper;
import com.bolingcavalry.redisperformancedemokryo.bean.Person;
import com.bolingcavalry.redisperformancedemokryo.service.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description : web响应
 * @Author : zq2599@gmail.com
 * @Date : 2018-06-10 22:525
 */
@Controller
public class RedisController {

    private static final Logger logger = LoggerFactory.getLogger(RedisController.class);

    private static AtomicInteger addPersionIdGenerator = new AtomicInteger(0);

    private static AtomicInteger checkPersionIdGenerator = new AtomicInteger(0);

    private static final String PREFIX = "person_";

    private static final int TIMES = 100;

    @Autowired
    private RedisClient redisClient;

    /**
     * 检查指定id的记录
     * @param id
     * @param response
     */
    @RequestMapping(value = "/checksingle/{id}", method = RequestMethod.GET)
    public void check(@PathVariable("id") final int id, HttpServletResponse response) {
        checkPerson(id, response);
    }

    /**
     * 将最后一次检查的id加一，然后根据最新id检查记录
     * @param response
     */
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

    /**
     * 向redis增加一条记录
     * @param response
     */
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public void add(HttpServletResponse response) {
        for(int i=0;i<TIMES;i++) {
            Person person = Helper.buildPerson(addPersionIdGenerator);
            try {
                redisClient.set(PREFIX + person.getId(), person);
            } catch (Exception e) {
                logger.error("save redis error, ", e);
                Helper.error(response, "save redis error!");
                return;
            }
        }

        Helper.success(response, "save success");
    }

    /**
     * 将id清零
     * @param response
     */
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
        Person person = null;
        try{
            person = redisClient.getObject(PREFIX + id);
        }catch(Exception e){
            logger.error("get from redis error, ", e);
        }

        if(null==person){
            Helper.error( response, "[" + id + "] not exist!");
            return false;
        }

        String error = Helper.checkPerson(person);

        if(null==error){
            //Helper.success(response, "[" + id + "] check success, object :\n" + JSONObject.toJSONString(person));
            return true;
        }else {
            Helper.error(response, "[" + id + "] " + error);
            return false;
        }
    }


}