package com.bolingcavalry.event.producer;

import com.bolingcavalry.event.bean.MyEvent;
import io.quarkus.logging.Log;

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
public class MyProducer {

    @Inject
    Event<MyEvent> event;

    /**
     * 发送同步消息
     * @param source 消息源
     * @return 被消费次数
     */
    public int syncProduce(String source) {
        MyEvent myEvent = new MyEvent("syncEvent");
        Log.infov("before sync fire, {0}", myEvent);
        event.fire(myEvent);
        Log.infov("after sync fire, {0}", myEvent);
        return myEvent.getNum();
    }

}
