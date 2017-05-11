package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.KafkaProducer;
import com.bolingcavalry.service.KafkaService;
import org.springframework.stereotype.Service;

/**
 * @author willzhao
 * @version V1.0
 * @Description: kafka服务的实现
 * @email zq2599@gmail.com
 * @Date 17/4/23 下午5:06
 */
@Service
public class KafkaServiceImpl implements KafkaService{

    @Override
    public void produce(String topic, String message) {
        KafkaProducer.getInstance().send(topic, message);
    }
}
