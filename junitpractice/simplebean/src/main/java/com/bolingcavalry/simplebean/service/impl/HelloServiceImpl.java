package com.bolingcavalry.simplebean.service.impl;

import com.bolingcavalry.simplebean.service.HelloService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service()
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "Hello " + name;
    }
}
