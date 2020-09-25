package com.bolingcavalry.simplebean.service;

import com.bolingcavalry.simplebean.SimpleBeanApplication;
import junit.framework.TestCase;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/9/20 23:41
 */
@SpringBootTest
//@AutoConfigureMockMvc
class Demo2Test {

    private static final Logger logger = LoggerFactory.getLogger(Demo2Test.class);



    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    HelloService helloService;


    @Test
    void hello() {
        String name = "Tom";
        TestCase.assertEquals("Hello " + name, helloService.hello(name));
    }

    @Test
    void testApplicationContext() {
        HelloService helloService = applicationContext.getBean(HelloService.class);
        TestCase.assertNotNull(helloService);
    }


}