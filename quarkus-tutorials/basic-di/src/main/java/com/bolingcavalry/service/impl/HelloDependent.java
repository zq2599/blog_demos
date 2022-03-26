package com.bolingcavalry.service.impl;

import javax.enterprise.context.Dependent;

@Dependent
public class HelloDependent {
    public String hello() {
        return this.getClass().getSimpleName();
    }
}
