package com.bolingcavalry.interceptor.define;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@InterceptorBinding
@Target({TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackParams {
}
