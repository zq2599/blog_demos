package com.bolingcavalry.springcloudcustomizelistener;

import com.alibaba.fastjson.JSON;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description: 自定义监听器，接收缓存刷新的广播
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2018/9/23 10:35
 */
@Component
public class EurekaCacheRefreshListener implements ApplicationListener<HeartbeatEvent> {

    private static final Logger logger = LoggerFactory.getLogger(EurekaCacheRefreshListener.class);

    @Override
    public void onApplicationEvent(HeartbeatEvent event) {
        Object count = event.getValue();
        Object source = event.getSource();

        logger.info("start onApplicationEvent, count [{}], source :\n{}", count, JSON.toJSON(source));
    }
}
