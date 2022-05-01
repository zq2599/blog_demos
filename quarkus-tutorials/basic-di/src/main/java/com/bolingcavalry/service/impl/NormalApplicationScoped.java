package com.bolingcavalry.service.impl;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author will
 */
@ApplicationScoped
@Startup
public class NormalApplicationScoped {

    public NormalApplicationScoped() {
        Log.info("Construction from " + this.getClass().getSimpleName() + "\n");
    }

    public String ping() {
        return "ping from NormalApplicationScoped\n";
    }
}
