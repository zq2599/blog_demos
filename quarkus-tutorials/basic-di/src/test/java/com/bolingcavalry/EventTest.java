package com.bolingcavalry;

import com.bolingcavalry.event.consumer.MyConsumer;
import com.bolingcavalry.event.producer.MyProducer;
import com.bolingcavalry.event.producer.TwoChannelWithTwoEvent;
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

    @Inject
    TwoChannelWithTwoEvent twoChannelWithTwoEvent;


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


    @Test
    public void testTwoChnnelWithTwoEvent() {
        // 对管理员来说，
        // TwoChannelConsumer.adminEvent消费时计数加2，
        // TwoChannelConsumer.allEvent消费时计数加1，
        // 所以最终计数是3
        Assertions.assertEquals(3, twoChannelWithTwoEvent.produceAdmin("admin"));

        // 对普通人员来说，
        // TwoChannelConsumer.normalEvent消费时计数加1，
        // TwoChannelConsumer.allEvent消费时计数加1，
        // 所以最终计数是2
        Assertions.assertEquals(2, twoChannelWithTwoEvent.produceNormal("normal"));

    }
}
