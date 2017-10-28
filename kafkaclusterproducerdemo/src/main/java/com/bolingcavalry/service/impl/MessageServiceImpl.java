package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.MessageService;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 实现消息服务
 * @email zq2599@gmail.com
 * @Date 2017/10/28 上午9:58
 */
@Service
public class MessageServiceImpl implements MessageService{

    private Producer<Integer, String> producer = null;

    @PostConstruct
    public void init(){
        try {
            Properties props = new Properties();
            props.put("serializer.class", "kafka.serializer.StringEncoder");
            props.put("zk.connect", "broker1:2181,broker2:2181,broker3:2181");
            props.put("metadata.broker.list", "broker1:9092,broker2:9092,broker3:9092");
            producer = new kafka.javaapi.producer.Producer<Integer, String>(new ProducerConfig(props));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendSimpleMsg(String topic, String message) {
        //producer的内部实现中，已经考虑了线程安全，所以此处不用加锁了
        producer.send(new KeyedMessage<Integer, String>(topic, message));
    }
}
