package com.bolingcavalry.service.impl;

import com.bolingcavalry.interceptor.define.TrackClass;
import com.bolingcavalry.interceptor.define.TrackMethod;
import io.quarkus.arc.NoClassInterceptors;
import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/5/2 17:03
 * @description 演示NoClassInterceptors注解的作用
 */
@ApplicationScoped
@TrackClass
public class ExcludeInterceptorDemo {

    public void test0() {
        Log.info("from test0");
    }

    @NoClassInterceptors
    @TrackMethod
    public void test1() {
        Log.info("from test1");
    }
}
