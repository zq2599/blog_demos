package com.bolingcavalry.event.producer;

import com.bolingcavalry.annonation.Admin;
import com.bolingcavalry.annonation.Normal;
import com.bolingcavalry.event.bean.TwoChannelEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/4/3 10:16
 * @description 功能介绍
 */
@ApplicationScoped
public class TwoChannelWithTwoEvent {

    @Inject
    @Admin
    Event<TwoChannelEvent> adminEvent;

    @Inject
    @Normal
    Event<TwoChannelEvent> normalEvent;

    /**
     * 管理员消息
     * @param source
     * @return
     */
    public int produceAdmin(String source) {
        TwoChannelEvent event = new TwoChannelEvent(source);
        adminEvent.fire(event);
        return event.getNum();
    }

    /**
     * 普通消息
     * @param source
     * @return
     */
    public int produceNormal(String source) {
        TwoChannelEvent event = new TwoChannelEvent(source);
        normalEvent.fire(event);
        return event.getNum();
    }
}
