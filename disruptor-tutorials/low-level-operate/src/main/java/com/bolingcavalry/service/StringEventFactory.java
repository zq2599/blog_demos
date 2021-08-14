package com.bolingcavalry.service;

import com.lmax.disruptor.EventFactory;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 定义如何创建事件
 * @date 2021/5/23 11:50
 */
public class StringEventFactory implements EventFactory<StringEvent> {

    @Override
    public StringEvent newInstance() {
        return new StringEvent();
    }
}
