package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.KafkaConsumer;
import com.bolingcavalry.service.KafkaProducer;
import com.bolingcavalry.service.KafkaService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author willzhao
 * @version V1.0
 * @Description: kafka服务的实现
 * @email zq2599@gmail.com
 * @Date 17/4/23 下午5:06
 */
@Service
public class KafkaServiceImpl implements KafkaService{

    static Set<String> runningTopicSet = new HashSet<String>();


    @Override
    public void produce(String topic, String message) {
        KafkaProducer.getInstance().send(topic, message);
    }

    @Override
    public void startConsume(String topic) {
        if(runningTopicSet.contains(topic)){
            System.out.println("topic [" + topic + "]'s consumer is running");
            return;
        }

        //如果该topic对应的consumer没有启动，就立即启动
        synchronized (runningTopicSet){
            if(!runningTopicSet.contains(topic)){
                System.out.println("start topic [" + topic + "]");
                runningTopicSet.add(topic);
                KafkaConsumer.getInstance().startConsume(topic);
            }
        }

    }
}
