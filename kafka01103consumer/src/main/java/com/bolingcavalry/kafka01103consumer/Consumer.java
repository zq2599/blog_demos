package com.bolingcavalry.kafka01103consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2018/12/31 10:46
 */
@Component
public class Consumer {

    @KafkaListener(topics = {"topic001"})
    public void listen(ConsumerRecord<?, ?> record) {



        Optional<?> kafkaMessage = Optional.ofNullable(record.value());

        if (kafkaMessage.isPresent()) {

            Object message = kafkaMessage.get();

            System.out.println("----------------- record =" + record);
            System.out.println("------------------ message =" + message);
        }

    }
}
