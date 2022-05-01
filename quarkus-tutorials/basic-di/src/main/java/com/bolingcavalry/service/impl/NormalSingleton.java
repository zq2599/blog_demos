package com.bolingcavalry.service.impl;

import io.quarkus.logging.Log;

import javax.inject.Singleton;

/**
 * @author will
 */
@Singleton
public class NormalSingleton {

    public NormalSingleton() {
        Log.info("Construction from " + this.getClass().getSimpleName() + "\n");
    }

    public String ping() {
        return "ping from NormalSingleton\n";
    }
}
