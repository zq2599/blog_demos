package com.bolingcavalry.interceptor.impl;

import com.bolingcavalry.interceptor.define.TrackParams;
import io.quarkus.arc.Priority;
import io.quarkus.logging.Log;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/26 22:36
 * @description HandleError的实现
 */
@TrackParams
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 1)
public class TrackParamsInterceptor {

    @AroundInvoke
    Object execute(InvocationContext context) throws Exception {

        // context.getParameters()返回拦截方法的所有参数，
        // 用Optional处理非空时候的数组
        Optional.of(Arrays.stream(context.getParameters()))
                .ifPresent(stream -> {
                    stream.forEach(object -> Log.infov("parameter type [{0}], value [{1}]",
                                                       object.getClass().getSimpleName(),
                                                       object)
                    );
                });

        return context.proceed();
    }
}
