package com.bolingcavalry.interceptor.demo;

import com.bolingcavalry.interceptor.define.HandleConstruction;
import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/26 23:52
 * @description 用于验证拦截构造方法的demo
 */
@ApplicationScoped
@HandleConstruction
public class HandleonstructionDemo {

    public HandleonstructionDemo() {
        super();
        Log.infov("construction of {0}", HandleonstructionDemo.class.getSimpleName());
    }

    public void hello() {
        Log.info("hello world!");
    }
}
