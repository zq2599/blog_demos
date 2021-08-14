package com.bolingcavalry.advanced.service.impl;

import com.bolingcavalry.advanced.service.HelloService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service()
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "Hello " + name;
    }
}
