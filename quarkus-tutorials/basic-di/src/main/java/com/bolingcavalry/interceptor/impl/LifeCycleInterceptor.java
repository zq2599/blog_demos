package com.bolingcavalry.interceptor.impl;

import com.bolingcavalry.interceptor.define.TrackLifeCycle;
import io.quarkus.arc.Priority;
import io.quarkus.logging.Log;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.AroundConstruct;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/26 22:36
 * @description 拦截bean初始化
 */
@TrackLifeCycle
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 1)
public class LifeCycleInterceptor {

    @AroundConstruct
    void execute(InvocationContext context) throws Exception {
        Log.info("start AroundConstruct");
        try {
            context.proceed();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.info("end AroundConstruct");
    }

    @PostConstruct
    public void doPostConstruct(InvocationContext ctx) {
        Log.info("life cycle PostConstruct");
    }

    @PreDestroy
    public void doPreDestroy(InvocationContext ctx) {
        Log.info("life cycle PreDestroy");
    }
}
