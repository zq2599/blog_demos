package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.*;
import com.lmax.disruptor.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 方法实现
 * @date 2021/5/28 23:45
 */
@Service("workerPoolConsumer")
@Slf4j
public class WorkerPoolConsumerServiceImpl implements LowLevelOperateService {

    private RingBuffer<StringEvent> ringBuffer;

    private StringEventProducer producer;

    /**
     * 统计消息总数
     */
    private final AtomicLong eventCount = new AtomicLong();


    @PostConstruct
    private void init() {

        ringBuffer = RingBuffer.createSingleProducer(new StringEventFactory(), BUFFER_SIZE);

        ExecutorService executorService = Executors.newFixedThreadPool(CONSUMER_NUM);

        StringWorkHandler[] handlers = new StringWorkHandler[CONSUMER_NUM];

        // 创建多个StringWorkHandler实例，放入一个数组中
        for (int i=0;i < CONSUMER_NUM;i++) {
            handlers[i] = new StringWorkHandler(o -> {
                long count = eventCount.incrementAndGet();
                log.info("receive [{}] event", count);
            });
        }

        // 创建WorkerPool实例，将StringWorkHandler实例的数组传进去，代表共同消费者的数量
        WorkerPool<StringEvent> workerPool = new WorkerPool<>(ringBuffer, ringBuffer.newBarrier(), new IgnoreExceptionHandler(), handlers);

        // 这一句很重要，去掉就会出现重复消费同一个事件的问题
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());

        workerPool.start(executorService);

        // 生产者
        producer = new StringEventProducer(ringBuffer);
    }

    @Override
    public void publish(String value) {
        producer.onData(value);
    }

    @Override
    public long eventCount() {
        return eventCount.get();
    }
}
