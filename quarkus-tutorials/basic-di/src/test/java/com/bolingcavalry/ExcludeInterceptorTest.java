package com.bolingcavalry;

import com.bolingcavalry.service.impl.ExcludeInterceptorDemo;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class ExcludeInterceptorTest {

    @Inject
    ExcludeInterceptorDemo excludeInterceptorDemo;

    @Test
    public void test() {
        excludeInterceptorDemo.test0();
        Log.info("*****************************");
        excludeInterceptorDemo.test1();
    }
}


