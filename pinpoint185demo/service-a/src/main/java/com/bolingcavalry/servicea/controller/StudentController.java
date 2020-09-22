package com.bolingcavalry.servicea.controller;

import com.alibaba.fastjson.JSON;
import com.bolingcavalry.servicea.entity.Student;
import com.bolingcavalry.servicea.mapper.StudentMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @Description: 提供学生服务相关的web接口
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/10/3 8:51
 */
@RestController
public class StudentController {

    @Autowired
    StudentMapper studentMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${serviceb.baseurl}")
    private String baseUrl;

    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    public String get(@PathVariable("id") Integer id) {

        String rlt = restTemplate.getForObject(baseUrl + "get/" + id,String.class);

        if(StringUtils.isNotBlank(rlt)){
            return "From redis :" + rlt;
        }

        //redis中查不到，就去数据库里面查
        Student student = studentMapper.getById(id);
        //mysql里面也没有就报错
        if(null==student) {
            return "student with id["+id+"] not exist";
        }
        //调用service-b的接口，写入redis
        restTemplate.getForObject(baseUrl + "set/"+id+"/"+student.getName(),String.class);

        return "From mysql :"+JSON.toJSONString(student);
    }

    @RequestMapping(value = "/add/{id}/{name}", method = RequestMethod.GET)
    public String add(@PathVariable("id") Integer id, @PathVariable("name") String name) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        //写入数据库
        studentMapper.insert(student);
        //调用service-b的接口，写入redis
        restTemplate.getForObject(baseUrl + "set/"+id+"/"+name,String.class);
        return "insert success";
    }

}
