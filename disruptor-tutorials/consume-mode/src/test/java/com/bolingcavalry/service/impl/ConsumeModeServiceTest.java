package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.ConsumeModeService;
import com.bolingcavalry.service.OrderEvent;
import com.lmax.disruptor.EventTranslatorOneArg;
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

    @Autowired
    @Qualifier("multiProducerService")
    ConsumeModeService multiProducerService;

    @Autowired
    @Qualifier("scene5")
    ConsumeModeService scene5;

    @Autowired
    @Qualifier("scene6")
    ConsumeModeService scene6;

    @Autowired
    @Qualifier("scene7")
    ConsumeModeService scene7;

    @Autowired
    @Qualifier("scene8")
    ConsumeModeService scene8;

    @Autowired
    @Qualifier("scene9")
    ConsumeModeService scene9;

    @Autowired
    @Qualifier("scene10")
    ConsumeModeService scene10;

    @Autowired
    @Qualifier("translatorPublishService")
    ConsumeModeService translatorPublishService;

    @Autowired
    @Qualifier("lambdaService")
    ConsumeModeService lambdaService;

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

    @Test
    public void testMultiProducerService() throws InterruptedException {
        log.info("start testMultiProducerService");
        CountDownLatch countDownLatch = new CountDownLatch(1);

        // 两个生产者，每个生产100个事件，一共生产两百个事件
        // 两个独立消费者，每人消费200个事件，因此一共消费400个事件
        int expectEventCount = EVENT_COUNT*4;

        // 告诉service，等消费到400个消息时，就执行countDownLatch.countDown方法
        multiProducerService.setCountDown(countDownLatch, expectEventCount);

        // 启动一个线程，用第一个生产者生产事件
        new Thread(() -> {
            for(int i=0;i<EVENT_COUNT;i++) {
                log.info("publich {}", i);
                multiProducerService.publish(String.valueOf(i));
            }
        }).start();

        // 再启动一个线程，用第二个生产者生产事件
        new Thread(() -> {
            for(int i=0;i<EVENT_COUNT;i++) {
                log.info("publishWithProducer2 {}", i);
                try {
                    multiProducerService.publishWithProducer2(String.valueOf(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // 当前线程开始等待，前面的service.setCountDown方法已经告诉过service，
        // 等消费到expectEventCount个消息时，就执行countDownLatch.countDown方法
        // 千万注意，要调用await方法，而不是wait方法！
        countDownLatch.await();

        // 消费的事件总数应该等于发布的事件数
        assertEquals(expectEventCount, multiProducerService.eventCount());
    }

    @Test
    public void testScene5 () throws InterruptedException {
        log.info("start testScene5");
        testConsumeModeService(scene5,
                EVENT_COUNT,
                // 三个独立消费者，一共消费300个事件
                EVENT_COUNT * 3);
    }

    @Test
    public void testScene6 () throws InterruptedException {
        log.info("start testScene6");
        testConsumeModeService(scene6,
                EVENT_COUNT,
                // 四个独立消费者，一共消费400个事件
                EVENT_COUNT * 4);
    }

    @Test
    public void testScene7 () throws InterruptedException {
        log.info("start testScene7");
        testConsumeModeService(scene7,
                EVENT_COUNT,
                // 五个独立消费者，一共消费500个事件
                EVENT_COUNT * 5);
    }

    @Test
    public void testScene8 () throws InterruptedException {
        log.info("start testScene8");
        testConsumeModeService(scene8,
                EVENT_COUNT,
                // C1和C2共同消费，C3和C4共同消费，C5虽然只是一个，但也是共同消费模式,
                // 也就是一共有三组消费者，所以一共消费300个事件
                EVENT_COUNT * 3);
    }

    @Test
    public void testScene9 () throws InterruptedException {
        log.info("start testScene9");
        testConsumeModeService(scene9,
                EVENT_COUNT,
                // C1和C2共同消费（100个事件），
                // C3和C4独立消费（200个事件）,
                // C5独立消费（100个事件）,
                // 所以一共消费400个事件
                EVENT_COUNT * 4);
    }

    @Test
    public void testScene10 () throws InterruptedException {
        log.info("start testScene10");
        testConsumeModeService(scene10,
                EVENT_COUNT,
                // C1和C2独立消费（200个事件），
                // C3和C4独立消费（100个事件）,
                // C5独立消费（100个事件）,
                // 所以一共消费400个事件
                EVENT_COUNT * 4);
    }

    @Test
    public void testTranslatorPublishService() throws InterruptedException {
        log.info("start testTranslatorPublishService");
        testConsumeModeService(translatorPublishService,
                EVENT_COUNT,
                EVENT_COUNT * ConsumeModeService.INDEPENDENT_CONSUMER_NUM);
    }

    @Test
    public void testLambdaService() throws InterruptedException {
        log.info("start testLambdaService");

        CountDownLatch countDownLatch = new CountDownLatch(1);

        // 告诉service，等消费到expectEventCount个消息时，就执行countDownLatch.countDown方法
        lambdaService.setCountDown(countDownLatch, EVENT_COUNT);

        for(int i=0;i<EVENT_COUNT;i++) {
            log.info("publich {}", i);
            final String content = String.valueOf(i);
            lambdaService.publistEvent((event, sequence, value) -> event.setValue(value), content);
        }

        // 当前线程开始等待，前面的service.setCountDown方法已经告诉过service，
        // 等消费到expectEventCount个消息时，就执行countDownLatch.countDown方法
        // 千万注意，要调用await方法，而不是wait方法！
        countDownLatch.await();

        // 消费的事件总数应该等于发布的事件数
        assertEquals(EVENT_COUNT, lambdaService.eventCount());
    }
}