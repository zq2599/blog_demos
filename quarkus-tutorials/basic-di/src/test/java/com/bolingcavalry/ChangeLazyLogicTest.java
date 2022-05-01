package com.bolingcavalry;

import com.bolingcavalry.service.impl.NormalApplicationScoped;
import com.bolingcavalry.service.impl.NormalSingleton;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
class ChangeLazyLogicTest {

    @Inject
    NormalSingleton normalSingleton;

    @Inject
    NormalApplicationScoped normalApplicationScoped;

    @Test
    void ping() {
        Log.info("start invoke normalSingleton.ping\n");
        normalSingleton.ping();
        Log.info("start invoke normalApplicationScoped.ping\n");
        normalApplicationScoped.ping();
    }
}