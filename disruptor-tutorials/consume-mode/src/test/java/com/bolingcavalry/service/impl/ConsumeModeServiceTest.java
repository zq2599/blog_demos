package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.ConsumeModeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ConsumeModeServiceTest {

    @Autowired
    @Qualifier("independentModeService")
    ConsumeModeService independentModeService;

    @Autowired
    @Qualifier("shareModeService")
    ConsumeModeService shareModeService;

    @Autowired
    @Qualifier("independentAndShareModeService")
    ConsumeModeService independentAndShareModeService;

    /**
     * 测试时生产的消息数量
     */
    private static final int EVENT_COUNT = 100;

    private void testConsumeModeService(ConsumeModeService service, int eventCount, int expectEventCount) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        // 告诉service，等消费到expectEventCount个消息时，就执行countDownLatch.countDown方法
        service.setCountDown(countDownLatch, expectEventCount);

        for(int i=0;i<eventCount;i++) {
            log.info("publich {}", i);
            service.publish(String.valueOf(i));
        }

        // 当前线程开始等待，前面的service.setCountDown方法已经告诉过service，
        // 等消费到expectEventCount个消息时，就执行countDownLatch.countDown方法
        // 千万注意，要调用await方法，而不是wait方法！
        countDownLatch.await();

        // 消费的事件总数应该等于发布的事件数
        assertEquals(expectEventCount, service.eventCount());
    }

    @Test
    public void testIndependentModeService() throws InterruptedException {
        log.info("start testIndependentModeService");
        testConsumeModeService(independentModeService,
                EVENT_COUNT,
                EVENT_COUNT * ConsumeModeService.INDEPENDENT_CONSUMER_NUM);
    }

    @Test
    public void testShareModeService() throws InterruptedException {
        log.info("start testShareModeService");
        testConsumeModeService(shareModeService, EVENT_COUNT, EVENT_COUNT);
    }

    @Test
    public void independentAndShareModeService() throws InterruptedException {
        log.info("start independentAndShareModeService");
        testConsumeModeService(independentAndShareModeService,
                EVENT_COUNT,
                EVENT_COUNT * ConsumeModeService.INDEPENDENT_CONSUMER_NUM);
    }
}