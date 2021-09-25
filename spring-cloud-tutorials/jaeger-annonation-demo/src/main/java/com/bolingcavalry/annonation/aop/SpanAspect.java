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

    @Around("@annotation(com.bolingcavalry.annonation.aop.MySpan)")
    public Object traceSpan(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // 类名:方法名
        String operationDesc = getOperationDesc(proceedingJoinPoint);

        // 创建一个span，在创建的时候就添加一个tag
        Span span = tracer.buildSpan("span-aspect-" + operationDesc).start();

        // span日志
        span.log("span log of " + operationDesc);

        // 增加一个tag
        span.setTag("operation-desc", operationDesc);

        // span结束
        span.finish();

        return proceedingJoinPoint.proceed();
    }

    @Around("@annotation(com.bolingcavalry.annonation.aop.MyChildSpan)")
    public Object traceChildSpan(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // 类名:方法名
        String operationDesc = getOperationDesc(proceedingJoinPoint);

        // 从上下文中取得已存在的span
        Span parentSpan = tracer.activeSpan();

        if (null==parentSpan) {
            log.error("can not get span from context");
        } else {
            Span span = tracer.buildSpan("child-span-aspect-" + operationDesc).asChildOf(parentSpan).start();

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
