package com.bolingcavalry.tag.service.impl;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: 自定义注解
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/10/4 10:43
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Tag("hard")
@Test
public @interface HardTest {
}