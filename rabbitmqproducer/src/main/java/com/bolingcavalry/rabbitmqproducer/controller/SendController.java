package com.bolingcavalry.rabbitmqproducer.controller;

import com.bolingcavalry.rabbitmqproducer.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description : 发送消息的controller
 * @Author : zq2599@gmail.com
 * @Date : 2018-05-06 15:15
 */
@RestController
public class SendController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${mq.rabbit.exchange.name}")
    String exchangeName;

    @RequestMapping(value = "/send/{name}/{message}", method = RequestMethod.GET)
    public @ResponseBody String send(@PathVariable("name") final String name, @PathVariable("message") final String message) {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = simpleDateFormat.format(new Date());
        String sendMessage = "hello, " + name + ", " + message  + ", " + timeStr;
        rabbitTemplate.convertAndSend(exchangeName,"", sendMessage);
        return "send message to [" +  name + "] success (" + timeStr + ")";
    }
}
