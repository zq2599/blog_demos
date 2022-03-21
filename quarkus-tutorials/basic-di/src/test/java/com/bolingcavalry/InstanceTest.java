package com.bolingcavalry;

import com.bolingcavalry.service.HelloInstance;
import com.bolingcavalry.service.impl.HelloInstanceA;
import com.bolingcavalry.service.impl.HelloInstanceB;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@QuarkusTest
public class InstanceTest {

    @Inject
    Instance<HelloInstance> instance;

    @Test
    public void testSelectHelloInstanceA() {
        Class<HelloInstanceA> clazz = HelloInstanceA.class;

        Assertions.assertEquals(clazz.getSimpleName(),
                instance.select(clazz).get().hello());
    }

    @Test
    public void testSelectHelloInstanceB() {
        Class<HelloInstanceB> clazz = HelloInstanceB.class;

        Assertions.assertEquals(clazz.getSimpleName(),
                instance.select(clazz).get().hello());
    }
}
