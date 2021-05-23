package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.BasicEventService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BasicEventServiceImplTest {

    @Autowired
    BasicEventService basicEventService;

    @Test
    public void publish() throws InterruptedException {
        int count = 1000;

        for(int i=0;i<count;i++) {
            basicEventService.publish(String.valueOf(i));
        }

        // 异步消费，因此需要延时等待
        Thread.sleep(1000);
        assertEquals(count, basicEventService.eventCount());
    }
}