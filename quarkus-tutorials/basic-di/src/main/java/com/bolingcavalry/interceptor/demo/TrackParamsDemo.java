package com.bolingcavalry.interceptor.demo;

import com.bolingcavalry.interceptor.define.TrackParams;
import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/26 23:52
 * @description 模拟业务逻辑中的异常抛出，这里方法中直接抛出一个异常
 */
@ApplicationScoped
@TrackParams
public class TrackParamsDemo {

    public void hello(String name, int id) {
        Log.infov("Hello {0}, your id is {1}", name, id);
    }

    public static void staticHello(String name) {
        Log.infov("Hello {0}, from static method", name);
    }
}
