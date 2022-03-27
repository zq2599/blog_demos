package com.bolingcavalry.interceptor.demo;

import com.bolingcavalry.interceptor.define.ContextData;
import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/26 23:52
 * @description 模拟业务逻辑中的异常抛出，这里方法中直接抛出一个异常
 */
@ApplicationScoped
@ContextData
public class ContextDataDemo {

    public void hello() {
        Log.info("Hello world!");
    }
}
