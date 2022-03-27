package com.bolingcavalry.interceptor.impl;

import com.bolingcavalry.interceptor.define.HandleConstruction;
import io.quarkus.arc.Priority;
import io.quarkus.logging.Log;

import javax.interceptor.AroundConstruct;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/26 22:36
 * @description HandleConstruction的实现
 */
@HandleConstruction
@Interceptor
@Priority(Interceptor.Priority.APPLICATION +1)
public class HandleConstructionInterceptor {

    @AroundConstruct
    void execute(InvocationContext context) throws Exception {
        // 执行业务逻辑可以在此
        Log.infov("start construction interceptor");

        // 执行bean的构造方法
        context.proceed();

        // 注意，对于context.getTarget()的返回值，此时不是null，如果在context.proceed()之前，则是null
        Log.infov("bean instance of {0}", context.getTarget().getClass().getSimpleName());
    }
}
