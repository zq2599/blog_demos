package com.bolingcavalry.annonation;

import com.bolingcavalry.interceptor.define.HandleError;
import com.bolingcavalry.interceptor.define.TrackParams;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Stereotype;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Stereotype
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

// 下面这些注解，都包含在MyStereotype中了
@ApplicationScoped
@TrackParams
@HandleError
public @interface MyStereotype {
}
