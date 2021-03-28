package com.bolingcavalry;

import io.etcd.jetcd.kv.PutResponse;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import java.util.concurrent.ExecutionException;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HelloWorldTest {
    // 用与测试的键
    private static final String KEY = "/abc/foo-" + System.currentTimeMillis();

    // 用于测试的值
    private static final String VALUE = "/abc/foo";

    @org.junit.jupiter.api.Test
    @Order(2)
    void get() throws ExecutionException, InterruptedException {
        String getResult = new HelloWorld().get(KEY);
        assertEquals(VALUE, getResult);
    }

    @Test
    @Order(1)
    void put() throws ExecutionException, InterruptedException {
        PutResponse putResponse = new HelloWorld().put(KEY, VALUE);
        assertNotNull(putResponse);
        assertNotNull(putResponse.getHeader());
    }
}