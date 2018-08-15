package com.bolingcavalry.customizeapplicationevent.listener;

import com.bolingcavalry.customizeapplicationevent.event.CustomizeEvent;
import com.bolingcavalry.customizeapplicationevent.util.Utils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

/**
 * @Description : 自定义的系统广播监听器，只接受CustomizeEvent类型的消息
 * @Author : zq2599@gmail.com
 * @Date : 2018-08-15 14:53
 */
@Service
public class CustomizeEventListener implements ApplicationListener<CustomizeEvent>{

    @Override
    public void onApplicationEvent(CustomizeEvent event) {
        Utils.printTrack("onApplicationEvent : " + event);
    }
}
