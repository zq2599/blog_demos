package com.bolingcavalry.jaeger.provider.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2021/9/23 8:25 上午
 * @description 基于注解的jaeger操作
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface MySpan {
}
