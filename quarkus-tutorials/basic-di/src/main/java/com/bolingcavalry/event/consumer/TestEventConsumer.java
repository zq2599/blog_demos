package com.bolingcavalry.event.consumer;

import com.bolingcavalry.event.bean.TestEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/29 07:39
 * @description 功能介绍
 */
@ApplicationScoped
public class TestEventConsumer {

    /**
     * 消费异步事件，这里故意抛出异常
     */
    public void aSyncConsume(@ObservesAsync TestEvent testEvent) throws Exception {
       throw new Exception("exception from aSyncConsume");
    }
}
