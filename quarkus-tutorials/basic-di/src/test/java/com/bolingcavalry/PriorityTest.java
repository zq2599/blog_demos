package com.bolingcavalry;

import com.bolingcavalry.service.HelloPriority;
import com.bolingcavalry.service.impl.HelloPriorityC;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class PriorityTest {

    @Inject
    HelloPriority helloPriority;

    @Test
    public void testSelectHelloInstanceA() {
        Assertions.assertEquals(HelloPriorityC.class.getSimpleName(),
                                helloPriority.hello());
    }
}
