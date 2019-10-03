package com.example.serviceb.controller;

import com.alibaba.fastjson.JSON;
import com.example.serviceb.entity.Student;
import com.example.serviceb.util.RedisUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description: redis服务
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/10/3 11:13
 */
@RestController
public class StudentRedisController {

    @Resource
    private RedisUtil redisUtil;

    @RequestMapping(value = "/set/{id}/{name}", method = RequestMethod.GET)
    public String set(@PathVariable("id") Integer id, @PathVariable("name") String name) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        //写入redis，有效期是30秒
        redisUtil.set("student:"+id, student, 30);
        return "set successful, id ["+id+"], name ["+name+"]";
    }

    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    public String get(@PathVariable("id") Integer id) {
        Object object = redisUtil.get("student:"+id);
        return null==object ? "" : JSON.toJSONString(object);
    }
}
