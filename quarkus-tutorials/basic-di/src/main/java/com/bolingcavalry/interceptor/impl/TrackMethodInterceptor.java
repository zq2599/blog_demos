package com.bolingcavalry.interceptor.impl;

import com.bolingcavalry.interceptor.define.TrackMethod;
import io.quarkus.logging.Log;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/5/2 17:02
 * @description TrackMethod拦截器的实现
 */
@TrackMethod
@Interceptor
public class TrackMethodInterceptor {

    @AroundInvoke
    Object execute(InvocationContext context) throws Exception {
        Log.info("from TrackMethod");
        return context.proceed();
    }
}
