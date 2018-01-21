package com.bolingcavalry.threadpooldemoserver.controller;

import com.bolingcavalry.threadpooldemoserver.service.AsyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description : 接收请求向线程池提交任务的controller
 * @Author : zq2599@gmail.com
 * @Date : 2018-01-21 21:10
 */
@RestController
public class Hello {

    private static final Logger logger = LoggerFactory.getLogger(Hello.class);

    @Autowired
    private AsyncService asyncService;

    @RequestMapping("/")
    public String submit(){
        logger.info("start submit");

        //启动一个异步任务
        asyncService.executeAsync();

        logger.info("end submit");

        return "success";
    }
}
