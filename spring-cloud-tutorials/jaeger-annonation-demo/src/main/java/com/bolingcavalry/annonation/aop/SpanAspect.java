package com.bolingcavalry.annonation.aop;

import io.opentracing.Span;
import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SpanAspect {

    private Tracer tracer;

    public SpanAspect(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * 取得真实方法的相关信息
     * @param proceedingJoinPoint
     * @return
     */
    private static String getOperationDesc(ProceedingJoinPoint proceedingJoinPoint) {
        Signature signature = proceedingJoinPoint.getSignature();
        // 提取类名
        return StringUtils.substringAfterLast(signature.getDeclaringTypeName(), ".")
                + ":"
                + signature.getName();
    }

    @Around("@annotation(mySpan)")
    public Object traceSpan(ProceedingJoinPoint proceedingJoinPoint, MySpan mySpan) throws Throwable {

        // 类名:方法名
        String operationDesc = getOperationDesc(proceedingJoinPoint);

        // 看方法的注解中有没有设置name
        String name = mySpan.spanName();

        // 如果没有设置name，就给span一个默认name
        if (StringUtils.isEmpty(name)) {
            name = "span-aspect-" + operationDesc;
        }

        // 创建一个span，在创建的时候就添加一个tag
        Span span = tracer.buildSpan(name).start();

        // span日志
        span.log("span log of " + operationDesc);

        // 增加一个tag
        span.setTag("operation-desc", operationDesc);

        // span结束
        span.finish();

        return proceedingJoinPoint.proceed();
    }

    @Around("@annotation(myChildSpan)")
    public Object traceChildSpan(ProceedingJoinPoint proceedingJoinPoint, MyChildSpan myChildSpan) throws Throwable {
        // 类名:方法名
        String operationDesc = getOperationDesc(proceedingJoinPoint);

        // 看方法的注解中有没有设置name
        String name = myChildSpan.spanName();

        // 如果没有设置name，就给span一个默认name
        if (StringUtils.isEmpty(name)) {
            name = "child-span-aspect-" + operationDesc;
        }

        // 从上下文中取得已存在的span
        Span parentSpan = tracer.activeSpan();

        if (null==parentSpan) {
            log.error("can not get span from context");
        } else {
            Span span = tracer.buildSpan(name).asChildOf(parentSpan).start();

            // span日志
            span.log("child span log of " + operationDesc);

            // 增加一个tag
            span.setTag("child-operation-desc", operationDesc);

            // span结束
            span.finish();
        }

        return proceedingJoinPoint.proceed();
    }

}
