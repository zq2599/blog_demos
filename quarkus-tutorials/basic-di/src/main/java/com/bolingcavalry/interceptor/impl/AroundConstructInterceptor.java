package com.bolingcavalry.interceptor.impl;

import com.bolingcavalry.interceptor.define.TrackConstruct;
import com.bolingcavalry.interceptor.define.TrackParams;
import io.quarkus.arc.ArcInvocationContext;
import io.quarkus.arc.Priority;
import io.quarkus.logging.Log;

import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/26 22:36
 * @description 拦截bean初始化
 */
@TrackConstruct
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 1)
public class AroundConstructInterceptor {

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
}
