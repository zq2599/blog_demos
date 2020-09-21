package com.bolingcavalry.testenvironment.service;

import com.bolingcavalry.testenvironment.TestEnvironmentApplication;
import com.bolingcavalry.testenvironment.service.impl.TestEnvHelloServiceImpl;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/9/20 23:41
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestEnvironmentApplication.class, TestEnvHelloServiceImpl.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloServiceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ApplicationContext applicationContext;

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

    @Test
    void testApplicationContext() {
        HelloService helloService = applicationContext.getBean(HelloService.class);
        TestCase.assertNotNull(helloService);
    }
}