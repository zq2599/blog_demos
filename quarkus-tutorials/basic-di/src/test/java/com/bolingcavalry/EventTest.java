package com.bolingcavalry;

import com.bolingcavalry.event.consumer.MyConsumer;
import com.bolingcavalry.event.producer.MyProducer;
import com.bolingcavalry.service.HelloInstance;
import com.bolingcavalry.service.impl.HelloInstanceA;
import com.bolingcavalry.service.impl.HelloInstanceB;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@QuarkusTest
public class EventTest {

    @Inject
    MyProducer myProducer;


    @Test
    public void testSync() {
        Assertions.assertEquals(1, myProducer.syncProduce("testSync"));
    }


    @Test
    public void testAsync() throws InterruptedException {
        Assertions.assertEquals(0, myProducer.asyncProduce("testAsync"));
        // 如果不等待的话，主线程结束的时候会中断正在消费事件的子线程，导致子线程报错
        Thread.sleep(150);
    }
}
