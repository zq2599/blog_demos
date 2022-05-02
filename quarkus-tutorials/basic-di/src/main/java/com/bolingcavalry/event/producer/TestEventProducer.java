package com.bolingcavalry.event.producer;

import com.bolingcavalry.event.bean.TestEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/29 07:34
 * @description 发送消息的bean
 */
@ApplicationScoped
public class TestEventProducer {

    @Inject
    Event<TestEvent> event;

    /**
     * 发送异步事件
     */
    public void asyncProduce() {
        event.fireAsync(new TestEvent());
    }

}
