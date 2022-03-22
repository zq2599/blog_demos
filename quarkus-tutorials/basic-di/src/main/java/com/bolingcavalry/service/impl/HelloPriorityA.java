package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.HelloPriority;
import io.quarkus.arc.Priority;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

@ApplicationScoped
@Alternative
@Priority(1001)
public class HelloPriorityA implements HelloPriority {
    @Override
    public String hello() {
        return this.getClass().getSimpleName();
    }
}