package com.bolingcavalry.interceptor.impl;

import com.bolingcavalry.interceptor.define.HandleMethod;
import io.quarkus.arc.Priority;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/26 22:36
 * @description HandleError的实现
 */
@HandleMethod
@Interceptor
@Priority(Interceptor.Priority.APPLICATION +1)
public class HandleMethodInterceptorA {

    @Inject
    Logger logger;

    @AroundInvoke
    Object execute(InvocationContext context) throws Exception {
        logger.infov("start intercepting {0}.{1}",
                context.getTarget().getClass().getSimpleName(),
                context.getMethod().getName());
        return context.proceed();
    }
}
