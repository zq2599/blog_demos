package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.*;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.Setter;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 方法实现
 * @date 2021/5/23 11:05
 */
@Service("multiProducerService")
public class MultiProducerServiceImpl extends ConsumeModeService {

    /**
     * 第二个生产者
     */
    @Setter
    protected OrderEventProducer producer2;

    @PostConstruct
    @Override
    protected void init() {
        // 实例化
        disruptor = new Disruptor<>(new OrderEventFactory(),
                BUFFER_SIZE,
                new CustomizableThreadFactory("event-handler-"),
                // 生产类型是多生产者
                ProducerType.MULTI,
                // BlockingWaitStrategy是默认的等待策略
                new BlockingWaitStrategy());

        // 留给子类实现具体的事件消费逻辑
        disruptorOperate();

        // 启动
        disruptor.start();

        // 第一个生产者
        setProducer(new OrderEventProducer(disruptor.getRingBuffer()));

        // 第二个生产者
        setProducer2(new OrderEventProducer(disruptor.getRingBuffer()));
    }

    @Override
    protected void disruptorOperate() {
        // 一号消费者
        MailEventHandler c1 = new MailEventHandler(eventCountPrinter);

        // 二号消费者
        MailEventHandler c2 = new MailEventHandler(eventCountPrinter);

        // handleEventsWith，表示创建的多个消费者以共同消费的模式消费
        disruptor.handleEventsWith(c1, c2);
    }

    @Override
    public void publishWithProducer2(String value) throws Exception {

        producer2.onData(value);
    }
}
