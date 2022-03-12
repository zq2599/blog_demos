package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.HelloService;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/12 5:09 PM
 * @description 功能介绍
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello() {
        return "from " + this.getClass().getSimpleName();
    }
}
