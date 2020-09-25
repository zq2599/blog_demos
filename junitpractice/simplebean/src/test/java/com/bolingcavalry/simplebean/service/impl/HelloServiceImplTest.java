package com.bolingcavalry.simplebean.service.impl;

import com.bolingcavalry.simplebean.service.HelloService;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class HelloServiceImplTest {

    private static final String NAME = "Tom";

    @Test
    void hello(@Autowired HelloService helloService) {
        TestCase.assertEquals("Hello " + NAME, helloService.hello(NAME));
    }

    @Test
    void testApplicationContext(@Autowired ApplicationContext applicationContext) {
        // 通过applicationContext从spring环境取得helloService实例
        HelloService helloService = applicationContext.getBean(HelloService.class);
        // 非空
        TestCase.assertNotNull(helloService);
        // 相等
        TestCase.assertEquals("Hello " + NAME, helloService.hello(NAME));
    }
}