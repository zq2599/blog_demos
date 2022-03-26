package com.bolingcavalry;

import com.bolingcavalry.service.impl.*;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class DependentTest {

    @Inject
    DependentClientA dependentClientA;

    @Inject
    DependentClientB dependentClientB;

    @Test
    public void testSelectHelloInstanceA() {
        Class<HelloDependent> clazz = HelloDependent.class;

        Assertions.assertEquals(clazz.getSimpleName(), dependentClientA.doHello());
        Assertions.assertEquals(clazz.getSimpleName(), dependentClientB.doHello());
    }

}
