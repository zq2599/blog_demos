package com.bolingcavalry.service;


import com.bolingcavalry.Constants;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 单例模式的kafka底层基础服务
 * @email zq2599@gmail.com
 * @Date 17/4/22 下午8:35
 */
public class KafkaConsumer {

    final static String zkConnect = Constants.ZK_HOST + ":2181";
    final static String groupId = "group1";

    /**
     * 单例
     */
    private volatile static KafkaConsumer instance = null;

    /**
     * 禁止被外部实例化
     */
    private KafkaConsumer(){
        super();
    }

    public static KafkaConsumer getInstance(){
        if(null==instance) {
            synchronized (KafkaConsumer.class){
                if(null==instance){
                    instance = new KafkaConsumer();
                }
            }
        }

        return instance;
    }

    /**
     * 启动一个consumer
     * @param topic
     */
    public void startConsume(String topic){
        Properties props = new Properties();
        props.put("zookeeper.connect", zkConnect);
        props.put("group.id", groupId);
        props.put("zookeeper.session.timeout.ms", "40000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(props));


        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(1));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        KafkaStream<byte[], byte[]> stream = consumerMap.get(topic).get(0);
        final ConsumerIterator<byte[], byte[]> it = stream.iterator();

        Runnable executor = new Runnable() {
            @Override
            public void run() {
                while (it.hasNext()) {
                    System.out.println("************** receive：" + new String(it.next().message()));
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        new Thread(executor).start();
    }




}
