package com.bolingcavalry.service.impl;

import com.bolingcavalry.annonation.MyStereotype;
import io.quarkus.logging.Log;

@MyStereotype
public class DemoA {
    public void hello(String name) {
        Log.info("hello " + name);
    }
}
