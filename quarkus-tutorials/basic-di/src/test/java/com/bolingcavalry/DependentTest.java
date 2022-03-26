package com.bolingcavalry;

import com.bolingcavalry.service.impl.DependentClientA;
import com.bolingcavalry.service.impl.HelloDependent;
import com.bolingcavalry.service.impl.HelloInstanceA;
import com.bolingcavalry.service.impl.HelloInstanceB;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class DependentTest {

    @Inject
    DependentClientA dependentClientA;

    @Test
    public void testSelectHelloInstanceA() {
        Class<HelloDependent> clazz = HelloDependent.class;

        Assertions.assertEquals(clazz.getSimpleName(),
                dependentClientA.doHello());
    }

}
