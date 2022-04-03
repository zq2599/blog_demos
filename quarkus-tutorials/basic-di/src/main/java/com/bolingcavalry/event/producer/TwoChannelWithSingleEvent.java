package com.bolingcavalry.event.producer;

import com.bolingcavalry.annonation.Admin;
import com.bolingcavalry.annonation.Normal;
import com.bolingcavalry.event.bean.TwoChannelEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/4/3 10:16
 * @description 用同一个事件结构体TwoChannelEvent，分别发送不同业务类型的事件
 */
@ApplicationScoped
public class TwoChannelWithSingleEvent {

    @Inject
    Event<TwoChannelEvent> singleEvent;

    /**
     * 管理员消息
     * @param source
     * @return
     */
    public int produceAdmin(String source) {
        TwoChannelEvent event = new TwoChannelEvent(source);

        singleEvent.select(new AnnotationLiteral<Admin>() {})
                   .fire(event);

        return event.getNum();
    }

    /**
     * 普通消息
     * @param source
     * @return
     */
    public int produceNormal(String source) {
        TwoChannelEvent event = new TwoChannelEvent(source);

        singleEvent.select(new AnnotationLiteral<Normal>() {})
                .fire(event);

        return event.getNum();
    }
}
