package com.bolingcavalry.interceptor.impl;

import com.bolingcavalry.interceptor.define.HandleError;
import io.quarkus.arc.Priority;
import io.quarkus.logging.Log;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/26 22:36
 * @description HandleError的实现
 */
@HandleError
@Interceptor
@Priority(Interceptor.Priority.APPLICATION +1)
public class HandleErrorInterceptor {

    @AroundInvoke
    Object execute(InvocationContext context) {

        try {
            // 注意proceed方法的含义：调用下一个拦截器，直到最后一个才会执行被拦截的方法
            return context.proceed();
        } catch (Exception exception) {
            Log.errorf(exception,
                    "method error from %s.%s\n",
                    context.getTarget().getClass().getSimpleName(),
                    context.getMethod().getName());
        }

        return null;
    }
}
