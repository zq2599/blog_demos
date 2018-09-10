package com.bolingcavalry.customizeimport.controller;

import com.bolingcavalry.customizeimport.service.CustomizeService1;
import com.bolingcavalry.customizeimport.service.CustomizeService2;
import com.bolingcavalry.customizeimport.service.CustomizeService3;
import com.bolingcavalry.customizeimport.service.CustomizeService4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2018/9/9 13:31
 */
@RestController
public class HelloController {
    @Autowired(required = false)
    CustomizeService1 customizeServiceImpl1;

    @Autowired(required = false)
    CustomizeService2 customizeServiceImpl2;

    @Autowired(required = false)
    CustomizeService3 customizeServiceImpl3;

    @Autowired(required = false)
    CustomizeService4 customizeServiceImpl4;

    @GetMapping("hello")
    public String hello(){
        customizeServiceImpl1.execute();
        customizeServiceImpl2.execute();
        customizeServiceImpl3.execute();
        customizeServiceImpl4.execute();
        return "finish";
    }
}
