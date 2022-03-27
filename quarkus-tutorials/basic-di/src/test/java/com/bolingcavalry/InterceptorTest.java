package com.bolingcavalry;

import com.bolingcavalry.interceptor.demo.HandleErrorDemo;
import com.bolingcavalry.interceptor.demo.HandleonstructionDemo;
import com.bolingcavalry.interceptor.demo.TrackParamsDemo;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class InterceptorTest {

    @Inject
    HandleErrorDemo handleErrorDemo;

    @Inject
    HandleonstructionDemo handleonstructionDemo;

    @Inject
    TrackParamsDemo trackParamsDemo;

    @Test
    public void testHandleError() {
        handleErrorDemo.executeThrowError();
    }

    @Test
    public void testHandleonstruction() {
        handleonstructionDemo.hello();
    }

    @Test
    public void testTrackParams() {
        trackParamsDemo.hello("Tom", 101);
    }
}
