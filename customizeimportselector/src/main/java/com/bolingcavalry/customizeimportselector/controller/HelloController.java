package com.bolingcavalry.customizeimportselector.controller;

import com.bolingcavalry.customizeimportselector.service.CustomizeService1;
import com.bolingcavalry.customizeimportselector.service.CustomizeService2;
import com.bolingcavalry.customizeimportselector.service.CustomizeService3;
import com.bolingcavalry.customizeimportselector.service.impl.CustomizeServiceImpl1;
import com.bolingcavalry.customizeimportselector.service.impl.CustomizeServiceImpl2;
import com.bolingcavalry.customizeimportselector.service.impl.CustomizeServiceImpl3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("hello")
    public String add(){
        customizeServiceImpl1.execute();
        customizeServiceImpl2.execute();
        customizeServiceImpl3.execute();
        return "finish";
    }
}
