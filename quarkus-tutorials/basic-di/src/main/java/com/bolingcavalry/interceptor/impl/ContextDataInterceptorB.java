package com.bolingcavalry.interceptor.impl;

import com.bolingcavalry.interceptor.define.ContextData;
import com.bolingcavalry.interceptor.define.TrackParams;
import io.quarkus.arc.Priority;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/26 22:36
 * @description ContextData的实现
 */
@ContextData
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 2)
public class ContextDataInterceptorB extends BaseContextDataInterceptor {

    @AroundInvoke
    Object execute(InvocationContext context) throws Exception {
        return super.execute(context);
    }
}
