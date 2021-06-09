package com.bolingcavalry.service;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 产生事件
 * @date 2021/5/23 14:48
 */
public class OrderEventProducerWithTranslator {

    // 存储数据的环形队列
    private final RingBuffer<OrderEvent> ringBuffer;

    public OrderEventProducerWithTranslator(RingBuffer<OrderEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    /**
     * 内部类
     */
    private static final EventTranslatorOneArg<OrderEvent, String> TRANSLATOR = new EventTranslatorOneArg<OrderEvent, String>() {
        @Override
        public void translateTo(OrderEvent event, long sequence, String arg0) {
            event.setValue(arg0);
        }
    };

    public void onData(String content) {
        ringBuffer.publishEvent(TRANSLATOR, content);
    }
}