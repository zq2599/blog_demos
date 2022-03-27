package com.bolingcavalry;

import com.bolingcavalry.interceptor.demo.HandleErrorDemo;
import com.bolingcavalry.interceptor.demo.HandleonstructionDemo;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class InterceptorTest {

    @Inject
    HandleErrorDemo handleErrorDemo;

    @Inject
    HandleonstructionDemo handleonstructionDemo;

    @Test
    public void testHandleError() {
        handleErrorDemo.executeThrowError();
    }

    @Test
    public void testHandleonstruction() {
        handleonstructionDemo.hello();
    }

}
