package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.HelloQualifier;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HelloQualifierC implements HelloQualifier {
    @Override
    public String hello() {
        return this.getClass().getSimpleName();
    }
}