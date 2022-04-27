package com.bolingcavalry;

import com.bolingcavalry.service.impl.HelloDependent;
import io.quarkus.arc.WithCaching;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@QuarkusTest
public class WithCachingTest {

    @Inject
    @WithCaching
    Instance<HelloDependent> instance;

    @Test
    public void test() {
        // 第一次调用Instance#get方法
        HelloDependent helloDependent = instance.get();
        helloDependent.hello();

        // 第二次调用Instance#get方法
        helloDependent = instance.get();
        helloDependent.hello();
    }
}
