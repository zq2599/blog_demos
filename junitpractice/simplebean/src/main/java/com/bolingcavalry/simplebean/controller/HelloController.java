package com.bolingcavalry.simplebean.controller;

import com.bolingcavalry.simplebean.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 接口类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/9/20 16:43
 */
@RestController
public class HelloController {

    @Autowired
    private HelloService helloService;

    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public String logExtend(@PathVariable String name){
        return helloService.hello(name);
    }

}
