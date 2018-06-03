package com.bolingcavalry.messagettlproducer;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

/**
 * @Description :
 * @Author : zq2599@gmail.com
 * @Date : 2018-06-02 23:33
 */
public class ExpirationMessagePostProcessor implements MessagePostProcessor {
    private final Long ttl; // 毫秒

    public ExpirationMessagePostProcessor(Long ttl) {
        this.ttl = ttl;
    }

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        message.getMessageProperties() .setExpiration(ttl.toString()); // 设置per-message的失效时间
        return message;
    }
}