package com.bolingcavalry.queuettlproducer.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description : 用于生产消息的web接口类
 * @Author : zq2599@gmail.com
 * @Date : 2018-06-02 23:00
 */
@RestController
public class SendMessageController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${queue.ttl.queue.source}")
    private String QUEUE_TTL_QUEUE_SOURCE;

    /**
     * 生产一条消息，消息中不带过期时间，但是对应的队列中已经配置了过期时间
     * @param name
     * @param message
     * @return
     */
    @RequestMapping(value = "/queuettl/{name}/{message}", method = RequestMethod.GET)
    public @ResponseBody
    String queuettl(@PathVariable("name") final String name, @PathVariable("message") final String message) {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = simpleDateFormat.format(new Date());
        String queueName = QUEUE_TTL_QUEUE_SOURCE;
        String  sendMessage = String.format("hello, %s , %s, from queue [%s], %s", name, message, queueName, timeStr);
        rabbitTemplate.convertAndSend(queueName, sendMessage);

        return "send message to [" +  name + "] success , queue is : " + queueName + " (" + timeStr + ")";
    }
}
