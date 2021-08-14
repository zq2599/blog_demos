package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.*;
import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
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
 * @date 2021/5/23 11:05
 */
@Service("oneConsumer")
@Slf4j
public class OneConsumerServiceImpl implements LowLevelOperateService {

    private RingBuffer<StringEvent> ringBuffer;

    private StringEventProducer producer;

    /**
     * 统计消息总数
     */
    private final AtomicLong eventCount = new AtomicLong();

    private ExecutorService executors;

    @PostConstruct
    private void init() {

        // 准备一个匿名类，传给disruptor的事件处理类，
        // 这样每次处理事件时，都会将已经处理事件的总数打印出来
        Consumer<?> eventCountPrinter = new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                long count = eventCount.incrementAndGet();
                log.info("receive [{}] event", count);
            }
        };


        // 创建环形队列实例
        ringBuffer = RingBuffer.createSingleProducer(new StringEventFactory(), BUFFER_SIZE);

        // 准备线程池
        executors = Executors.newFixedThreadPool(1);

        //创建SequenceBarrier
        SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();

        // 创建事件处理的工作类，里面执行StringEventHandler处理事件
        BatchEventProcessor<StringEvent> batchEventProcessor = new BatchEventProcessor<>(
                ringBuffer,
                sequenceBarrier,
                new StringEventHandler(eventCountPrinter));

        // 将消费者的sequence传给环形队列
        ringBuffer.addGatingSequences(batchEventProcessor.getSequence());

        // 在一个独立线程中取事件并消费
        executors.submit(batchEventProcessor);

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
