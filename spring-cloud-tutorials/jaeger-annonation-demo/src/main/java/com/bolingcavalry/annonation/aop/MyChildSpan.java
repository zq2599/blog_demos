package com.bolingcavalry.annonation.aop;

import java.lang.annotation.*;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2021/9/23 8:25 上午
 * @description 基于注解的jaeger操作(子span)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface MyChildSpan {
    String spanName() default "";
}
