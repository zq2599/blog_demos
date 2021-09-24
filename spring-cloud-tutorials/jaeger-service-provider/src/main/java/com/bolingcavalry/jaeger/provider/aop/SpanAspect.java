package com.bolingcavalry.jaeger.provider.aop;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2021/9/24 8:10 上午
 * @description 功能介绍
 */
@Aspect
@Component
public class SpanAspect {

    private Tracer tracer;

    public SpanAspect(Tracer tracer) {
        this.tracer = tracer;
    }

    @Around("@annotation(com.bolingcavalry.jaeger.provider.aop.MySpan)")
    public Object traceMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Signature signature = proceedingJoinPoint.getSignature();
        String className = StringUtils.substringAfterLast(signature.getDeclaringTypeName(), ".");
        String operationName = className + ":" + signature.getName();

        // 创建一个span，在创建的时候就添加一个tag
        Span span = tracer.buildSpan("span-aspect-" + operationName).start();

        // span日志
        span.log("span log of " + signature.getName());

        // 增加一个tag
        span.setTag("class-name", signature.getDeclaringTypeName());
        span.setTag("operation-name", operationName);

        // span结束
        span.finish();

        return proceedingJoinPoint.proceed();
    }

}
