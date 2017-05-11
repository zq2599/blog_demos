package com.bolingcavalry.service;


import com.bolingcavalry.Constants;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Properties;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 单例模式的kafka底层基础服务
 * @email zq2599@gmail.com
 * @Date 17/4/22 下午8:35
 */
public class KafkaProducer {
    /**
     * 单例
     */
    private volatile static KafkaProducer instance = null;

    private Producer<Integer, String> producer = null;

    /**
     * 禁止被外部实例化
     */
    private KafkaProducer(){
        super();

        try {
            Properties props = new Properties();
            props.put("serializer.class", "kafka.serializer.StringEncoder");
            props.put("zk.connect", Constants.ZK_HOST + ":2181");
            props.put("metadata.broker.list", Constants.BROKER_HOST + ":9092");
            producer = new kafka.javaapi.producer.Producer<Integer, String>(new ProducerConfig(props));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static KafkaProducer getInstance(){
        if(null==instance) {
            synchronized (KafkaProducer.class){
                if(null==instance){
                    instance = new KafkaProducer();
                }
            }
        }

        return instance;
    }

    /**
     * 发送一条消息
     * @param topic
     * @param message
     */
    public void send(String topic, String message){
        //producer的内部实现中，已经考虑了线程安全，所以此处不用加锁了
        producer.send(new KeyedMessage<Integer, String>(topic, message));
    }
}
