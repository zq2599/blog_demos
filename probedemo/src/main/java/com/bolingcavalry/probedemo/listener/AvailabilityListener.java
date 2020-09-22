package com.bolingcavalry.probedemo.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.AvailabilityState;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * description: 监听系统事件的类 <br>
 * date: 2020/6/4 下午12:57 <br>
 * author: willzhao <br>
 * email: zq2599@gmail.com <br>
 * version: 1.0 <br>
 */
@Component
@Slf4j
public class AvailabilityListener {

    /**
     * 监听系统消息，
     * AvailabilityChangeEvent类型的消息都从会触发此方法被回调
     * @param event
     */
    @EventListener
    public void onStateChange(AvailabilityChangeEvent<? extends AvailabilityState> event) {
        log.info(event.getState().getClass().getSimpleName() + " : " + event.getState());
    }
}