package com.bolingcavalry.service.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DependentClientB {

    @Inject
    HelloDependent hello;

    public String doHello() {
        return hello.hello();
    }
}
