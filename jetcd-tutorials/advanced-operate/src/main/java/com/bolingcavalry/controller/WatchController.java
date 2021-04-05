package com.bolingcavalry.controller;

import com.bolingcavalry.dao.AdvancedEtcdService;
import com.bolingcavalry.dao.EtcdService;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.api.Event;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @Description: 和watch相关的web服务
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2021/4/4 14:54
 */
@RestController
@Slf4j
public class WatchController {

    @Autowired
    EtcdService etcdService;

    @Autowired
    AdvancedEtcdService advancedEtcdService;

    private Map<String, Watch.Watcher> watcherMap = new ConcurrentHashMap<>();

    @RequestMapping(value = "/watch/{key}", method = RequestMethod.GET)
    public String watch(@PathVariable("key") String key) throws Exception {
        // 先检查指定的key在etcd中是否存在

        // 查询条件中指定只返回key
        GetOption getOption = GetOption.newBuilder().withCountOnly(true).build();
        // 如果数量小于1,表示指定的key在etcd中不存在
        if (etcdService.getRange(key, getOption).getCount()<1) {
            String errorDesc = String.format("[%s] not exists", key);
            log.error(errorDesc);
            return errorDesc + " " + new Date();
        }

        final String watchKey = key;

        // 实例化一个监听对象，当监听的key发生变化时会被调用
        Watch.Listener listener = Watch.listener(watchResponse -> {
            log.info("收到[{}]的事件", watchKey);

            // 被调用时传入的是事件集合，这里遍历每个事件
            watchResponse.getEvents().forEach(watchEvent -> {
                // 操作类型
                WatchEvent.EventType eventType = watchEvent.getEventType();

                // 操作的键值对
                KeyValue keyValue = watchEvent.getKeyValue();

                log.info("type={}, key={}, value={}",
                            eventType,
                            keyValue.getKey().toString(UTF_8),
                            keyValue.getValue().toString(UTF_8));

                // 如果是删除操作，就把该key的Watcher找出来close掉
                if (WatchEvent.EventType.DELETE.equals(eventType)
                && watcherMap.containsKey(watchKey)) {
                    Watch.Watcher watcher = watcherMap.remove(watchKey);
                    watcher.close();
                }
            });
        });

        // 添加监听
        Watch.Watcher watcher = advancedEtcdService.watch(watchKey, listener);

        // 将这个Watcher放入内存中保存，如果该key被删除就要将这个Watcher关闭
        watcherMap.put(key, watcher);

        return "watch success " + new Date();
    }
}
