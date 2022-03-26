package com.bolingcavalry.service.impl;

import io.quarkus.logging.Log;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.InjectionPoint;

@Dependent
public class HelloDependent {

    public HelloDependent(InjectionPoint injectionPoint) {
        Log.info("injecting from bean "+ injectionPoint.getMember().getDeclaringClass());
    }

    public String hello() {
        return this.getClass().getSimpleName();
    }
}
