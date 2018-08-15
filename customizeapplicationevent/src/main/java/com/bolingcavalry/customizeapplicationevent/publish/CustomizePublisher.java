package com.bolingcavalry.customizeapplicationevent.publish;

import com.bolingcavalry.customizeapplicationevent.event.CustomizeEvent;
import com.bolingcavalry.customizeapplicationevent.util.Utils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

/**
 * @Description : 自定义的广播发送器
 * @Author : zq2599@gmail.com
 * @Date : 2018-08-16 06:09
 */
@Service
public class CustomizePublisher implements ApplicationEventPublisherAware, ApplicationContextAware {

    private ApplicationEventPublisher applicationEventPublisher;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        Utils.printTrack("applicationEventPublisher is set : " + applicationEventPublisher);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 发送一条广播
     */
    public void publishEvent(){
        applicationEventPublisher.publishEvent(new CustomizeEvent(applicationContext));
    }
}
