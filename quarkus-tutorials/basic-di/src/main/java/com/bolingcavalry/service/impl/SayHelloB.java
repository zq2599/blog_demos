package com.bolingcavalry.service.impl;

import com.bolingcavalry.interceptor.define.SendMessage;
import com.bolingcavalry.service.SayHello;
import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/5/2 07:53
 * @description 普通业务接口实现类
 */
@ApplicationScoped
@Named("B")
public class SayHelloB implements SayHello {

    @SendMessage(sendType = "email")
    @Override
    public void hello() {
        Log.info("hello from B");
    }
}

