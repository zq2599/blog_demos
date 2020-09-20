package com.bolingcavalry.simplebean.service;

import com.bolingcavalry.simplebean.SimpleBeanApplication;
import com.bolingcavalry.simplebean.service.impl.TestEnvHelloServiceImpl;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/9/20 23:41
 */
@RunWith(SpringRunner.class)
//@SpringBootTest(classes = SimpleBeanApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringBootTest(classes = {TestEnvHelloServiceImpl.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloServiceTest {

    @LocalServerPort
    private int port;

    @Qualifier("helloService")
    @Autowired
    HelloService helloService;

    @Qualifier("testEnvHelloSerivce")
    @Autowired
    HelloService testEnvHelloSerivce;

    @Test
    void hello() {
        String name = "Tom";
        TestCase.assertEquals("Hello " + name, helloService.hello(name));

        name = "Jack";
        TestCase.assertEquals("Hello " + name + " from test env", testEnvHelloSerivce.hello(name));
    }
}