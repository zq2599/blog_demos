package com.bolingcavalry;

import com.bolingcavalry.lifecycle.Hello;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class LifeCycleTest {

    @Inject
    Hello hello;

    @Test
    public void testLifyCycle() {
        hello.helloWorld();
    }

}
