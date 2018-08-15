package com.bolingcavalry.customizeapplicationevent.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

/**
 * @Description : 自定义的消息类型
 * @Author : zq2599@gmail.com
 * @Date : 2018-08-16 06:09
 */
public class CustomizeEvent extends ApplicationContextEvent {

    public CustomizeEvent(ApplicationContext source) {
        super(source);
    }

}