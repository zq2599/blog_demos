package com.bolingcavalry;

import com.bolingcavalry.service.impl.ArroundInvokeDemo;
import com.bolingcavalry.service.impl.DependentClientA;
import com.bolingcavalry.service.impl.DependentClientB;
import com.bolingcavalry.service.impl.HelloDependent;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class ArroundInvokeTest {

    @Inject
    ArroundInvokeDemo arroundInvokeDemo;

    @Test
    public void testSelectHelloInstanceA() {
        arroundInvokeDemo.executeThrowError();
    }

}
