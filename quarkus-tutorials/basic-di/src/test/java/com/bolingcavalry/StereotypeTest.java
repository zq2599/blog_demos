package com.bolingcavalry;

import com.bolingcavalry.service.impl.DemoA;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class StereotypeTest {

    @Inject
    DemoA demoA;

    @Test
    public void test() {
        demoA.hello("Tom");
    }
}
