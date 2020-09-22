package com.bolingcavalry.simplebean.service;

import com.bolingcavalry.simplebean.SimpleBeanApplication;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@SpringBootTest(classes = {SimpleBeanApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceTest.class);

    @LocalServerPort
    private int port;

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
        logger.info("port is {}", port);
    }

    @BeforeEach
    void beforeEach() {
        logger.info("execute beforeEach");
    }

    @AfterEach
    void afterEach() {
        logger.info("execute afterEach");
    }

    @BeforeAll
    static void beforeAll() {
        logger.info("execute beforeAll");
    }

    @AfterAll
    static void afterAll() {
        logger.info("execute afterAll");
    }
}