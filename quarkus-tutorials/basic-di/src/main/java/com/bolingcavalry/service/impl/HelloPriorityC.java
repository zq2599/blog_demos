package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.HelloPriority;
import io.quarkus.arc.Priority;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

@ApplicationScoped
@Alternative
@Priority(1003)
public class HelloPriorityC implements HelloPriority {
    @Override
    public String hello() {
        return this.getClass().getSimpleName();
    }
}