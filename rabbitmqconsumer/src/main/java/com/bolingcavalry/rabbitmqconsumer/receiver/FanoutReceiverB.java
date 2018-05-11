package com.bolingcavalry.rabbitmqconsumer.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Description : fanout消息的接收类
 * @Author : zq2599@gmail.com
 * @Date : 2018-05-06 18:02
 */
@Component
@RabbitListener(queues = "fanout.bolingcavalry.b")
public class FanoutReceiverB {
    private static final Logger logger = LoggerFactory.getLogger(FanoutReceiverB.class);

    @RabbitHandler
    public void process(String message) {
        logger.info("b. receive message : " + message);
    }
}
