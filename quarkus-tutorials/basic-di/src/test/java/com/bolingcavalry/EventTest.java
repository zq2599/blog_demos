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

    @Inject
    MyConsumer myConsumer;

    @Test
    public void testSync() {
        Assertions.assertEquals(1, myProducer.syncProduce("testSync"));
    }


}
