package com.bolingcavalry.service.impl;

import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author will
 */
@ApplicationScoped
public class NormalApplicationScoped {

    public NormalApplicationScoped() {
        Log.info("Construction from " + this.getClass().getSimpleName());
    }

    public String ping() {
        return "ping from NormalApplicationScoped";
    }
}
