package com.bolingcavalry.junit.service;

import com.bolingcavalry.junit.JunitApplication;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/9/20 23:41
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JunitApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloServiceTest {

    @LocalServerPort
    private int port;

    @Autowired
    HelloService helloService;

    @Test
    void hello() {
        String name = "Tom";
        TestCase.assertEquals("Hello " + name, helloService.hello(name));
    }
}