package com.bolingcavalry.service.impl;

import com.bolingcavalry.annonation.MyQualifier;
import com.bolingcavalry.service.HelloQualifier;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@MyQualifier("")
public class HelloQualifierA implements HelloQualifier {
    @Override
    public String hello() {
        return this.getClass().getSimpleName();
    }
}