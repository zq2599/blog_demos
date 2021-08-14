package com.bolingcavalry.service;

import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.Setter;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import javax.annotation.PostConstruct;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 定义服务接口
 * @date 2021/5/23 11:05
 */
public abstract class ConsumeModeService {
    /**
     * 独立消费者数量
     */
    public static final int INDEPENDENT_CONSUMER_NUM = 2;

    /**
     * 环形缓冲区大小
     */
    protected int BUFFER_SIZE = 16;

    protected Disruptor<OrderEvent> disruptor;

    @Setter
    private OrderEventProducer producer;

    @Setter
    private OrderEventProducerWithTranslator producerWithTranslator;

    /**
     * 统计消息总数
     */
    protected final AtomicLong eventCount = new AtomicLong();

    /**
     * 这是辅助测试用的，
     * 测试的时候，完成事件发布后，测试主线程就用这个countDownLatch开始等待，
     * 在消费到指定的数量(countDownLatchGate)后，消费线程执行countDownLatch的countDown方法，
     * 这样测试主线程就可以结束等待了
     */
    private CountDownLatch countDownLatch;

    /**
     * 这是辅助测试用的，
     * 测试的时候，完成事件发布后，测试主线程就用这个countDownLatch开始等待，
     * 在消费到指定的数量(countDownLatchGate)后，消费线程执行countDownLatch的countDown方法，
     * 这样测试主线程就可以结束等待了
     */
    private int countDownLatchGate;

    /**
     * 准备一个匿名类，传给disruptor的事件处理类，
     * 这样每次处理事件时，都会将已经处理事件的总数打印出来
     */
    protected Consumer<?> eventCountPrinter = new Consumer<Object>() {
        @Override
        public void accept(Object o) {
            long count = eventCount.incrementAndGet();

            /**
             * 这是辅助测试用的，
             * 测试的时候，完成事件发布后，测试主线程就用这个countDownLatch开始等待，
             * 在消费到指定的数量(countDownLatchGate)后，消费线程执行countDownLatch的countDown方法，
             * 这样测试主线程就可以结束等待了
             */
            if (null!=countDownLatch && count>=countDownLatchGate) {
                countDownLatch.countDown();
            }
        }
    };

    /**
     * 发布一个事件
     * @param value
     * @return
     */
    public void publish(String value) {
        // 两种producer都支持 ，使用非空的那个
        if (null!= producer) {
            producer.onData(value);
        } else if (null!=producerWithTranslator) {
            producerWithTranslator.onData(value);
        }
    }

    /**
     * 基于lambda发布
     * @param translator
     */
    public void publistEvent(EventTranslatorOneArg<OrderEvent, String> translator, String value) {
        disruptor.getRingBuffer().publishEvent(translator, value);
    }

    /**
     * 子类重写后再调用，该方法的作用是多生产者模式下，用另一个生产者发布一个事件
     * @param value
     * @return
     */
    public void publishWithProducer2(String value) throws Exception {
        throw new Exception("父类未实现此方法，请在子类中重写此方法后再调用");
    }

    /**
     * 返回已经处理的任务总数
     * @return
     */
    public long eventCount() {
        return eventCount.get();
    }

    /**
     * 这是辅助测试用的，
     * 测试的时候，完成事件发布后，测试主线程就用这个countDownLatch开始等待，
     * 在消费到指定的数量(countDownLatchGate)后，消费线程执行countDownLatch的countDown方法，
     * 这样测试主线程就可以结束等待了
     * @param countDownLatch
     * @param countDownLatchGate
     */
    public void setCountDown(CountDownLatch countDownLatch, int countDownLatchGate) {
        this.countDownLatch = countDownLatch;
        this.countDownLatchGate = countDownLatchGate;
    }

    /**
     * 留给子类实现具体的事件消费逻辑
     */
    protected abstract void disruptorOperate();

    @PostConstruct
    protected void init() {
        // 实例化
        disruptor = new Disruptor<>(new OrderEventFactory(),
                BUFFER_SIZE,
                new CustomizableThreadFactory("event-handler-"));

        // 留给子类实现具体的事件消费逻辑
        disruptorOperate();

        // 启动
        disruptor.start();

        // 生产者
        setProducer(new OrderEventProducer(disruptor.getRingBuffer()));
    }
}
