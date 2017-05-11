package com.bolingcavalry.service;

/**
 * @author willzhao
 * @version V1.0
 * @Description: Kafka基础服务的封装
 * @email zq2599@gmail.com
 * @Date 17/4/23 下午5:03
 */
public interface KafkaService {
    /**
     * 启动消费者
     * @param topic
     */
    void startConsume(String topic);
}
