package com.bolingcavalry.messagettlproducer.controller;

import com.bolingcavalry.messagettlproducer.ExpirationMessagePostProcessor;
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

    @Value("${message.ttl.queue.source}")
    private String MESSAGE_TTL_QUEUE_SOURCE;

    /**
     * 生产一条消息，消息中带有过期时间
     * @param name
     * @param message
     * @param delaytime
     * @return
     */
    @RequestMapping(value = "/messagettl/{name}/{message}/{delaytime}", method = RequestMethod.GET)
    public @ResponseBody
    String messagettl(@PathVariable("name") final String name, @PathVariable("message") final String message, @PathVariable("delaytime") final int delaytime) {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = simpleDateFormat.format(new Date());
        String queueName = MESSAGE_TTL_QUEUE_SOURCE;
        String  sendMessage = String.format("hello, %s , %s, from queue [%s], delay %d's, %s", name, message, MESSAGE_TTL_QUEUE_SOURCE, delaytime, timeStr);
        rabbitTemplate.convertAndSend(MESSAGE_TTL_QUEUE_SOURCE,
                                        (Object)sendMessage,
                                        new ExpirationMessagePostProcessor(delaytime*1000L));

        return "send message to [" +  name + "] success , queue is : " + queueName + " (" + timeStr + ")";
    }
}
