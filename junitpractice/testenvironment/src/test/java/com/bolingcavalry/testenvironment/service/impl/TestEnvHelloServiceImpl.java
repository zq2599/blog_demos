package com.bolingcavalry.testenvironment.service.impl;

import com.bolingcavalry.testenvironment.service.HelloService;
import org.springframework.boot.test.context.TestComponent;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/9/21 0:33
 */
@TestComponent("testEnvHelloSerivce")
public class TestEnvHelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "Hello " + name + " from test env";
    }
}
