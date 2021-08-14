package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.*;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 方法实现
 * @date 2021/5/23 11:05
 */
@Service("lambdaService")
@Slf4j
public class LambdaServiceImpl extends ConsumeModeService {

    @PostConstruct
    @Override
    protected void init() {
        // lambda类型的实例化
        disruptor = new Disruptor<OrderEvent>(OrderEvent::new, BUFFER_SIZE, DaemonThreadFactory.INSTANCE);

        // 留给子类实现具体的事件消费逻辑
        disruptorOperate();

        // 启动
        disruptor.start();

        // 第一个生产者
        setProducerWithTranslator(new OrderEventProducerWithTranslator(disruptor.getRingBuffer()));

    }

    @Override
    protected void disruptorOperate() {
        // lambda表达式指定具体消费逻辑
        disruptor.handleEventsWith((event, sequence, endOfBatch) -> {
            log.info("lambda操作, sequence [{}], endOfBatch [{}], event : {}", sequence, endOfBatch, event);

            // 这里延时100ms，模拟消费事件的逻辑的耗时
            Thread.sleep(100);
            // 计数
            eventCountPrinter.accept(null);
        });
    }
}
