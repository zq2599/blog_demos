package com.bolingcavalry.lifecycle;

import com.bolingcavalry.interceptor.define.TrackConstruct;
import io.quarkus.logging.Log;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.AroundConstruct;

@ApplicationScoped
@TrackConstruct
public class Hello {

    public Hello() {
        Log.info(this.getClass().getSimpleName() + " at instance");
    }

    @PostConstruct
    public void doPostConstruct() {
        Log.info("at doPostConstruct");
    }

    @PreDestroy
    public void doPreDestroy() {
        Log.info("at PreDestroy");
    }

    public void helloWorld() {
        Log.info("Hello world!");
    }
}
