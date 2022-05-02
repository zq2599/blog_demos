package com.bolingcavalry.interceptor.impl;

import com.bolingcavalry.interceptor.define.SendMessage;
import com.bolingcavalry.interceptor.define.TrackClass;
import io.quarkus.arc.runtime.InterceptorBindings;
import io.quarkus.logging.Log;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/5/2 17:01
 * @description TrackClass拦截器的实现
 */
@TrackClass
@Interceptor
public class TrackClassInterceptor {

    @AroundInvoke
    Object execute(InvocationContext context) throws Exception {
        Log.info("from TrackClass");
        return context.proceed();
    }
}
