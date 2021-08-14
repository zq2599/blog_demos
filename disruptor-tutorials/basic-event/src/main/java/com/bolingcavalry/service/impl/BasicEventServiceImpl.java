package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.*;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 方法实现
 * @date 2021/5/23 11:05
 */
@Service
@Slf4j
public class BasicEventServiceImpl implements BasicEventService {

    private static final int BUFFER_SIZE = 16;

    private Disruptor<StringEvent> disruptor;

    private StringEventProducer producer;

    /**
     * 统计消息总数
     */
    private final AtomicLong eventCount = new AtomicLong();

    @PostConstruct
    private void init() {
        // 实例化
        disruptor = new Disruptor<>(new StringEventFactory(),
                BUFFER_SIZE,
                new CustomizableThreadFactory("event-handler-"));

        // 准备一个匿名类，传给disruptor的事件处理类，
        // 这样每次处理事件时，都会将已经处理事件的总数打印出来
        Consumer<?> eventCountPrinter = new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                long count = eventCount.incrementAndGet();
                log.info("receive [{}] event", count);
            }
        };

        // 指定处理类
        disruptor.handleEventsWith(new StringEventHandler(eventCountPrinter));

        // 启动
        disruptor.start();

        // 生产者
        producer = new StringEventProducer(disruptor.getRingBuffer());
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
