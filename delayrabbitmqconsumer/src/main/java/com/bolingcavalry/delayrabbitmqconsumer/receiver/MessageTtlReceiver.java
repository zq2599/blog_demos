package com.bolingcavalry.delayrabbitmqconsumer.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Description : 消息接受类，接收第一类延时消息(在每条消息中指定过期时间)的转发结果
 * @Author : zq2599@gmail.com
 * @Date : 2018-06-03 9:52
 */
@Component
@RabbitListener(queues = "${message.ttl.queue.process}")
public class MessageTtlReceiver {
    private static final Logger logger = LoggerFactory.getLogger(MessageTtlReceiver.class);

    @RabbitHandler
    public void process(String message) {
        logger.info("receive message : " + message);
    }
}
