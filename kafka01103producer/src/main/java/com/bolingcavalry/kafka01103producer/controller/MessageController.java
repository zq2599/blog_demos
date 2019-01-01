package com.bolingcavalry.kafka01103producer.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @Description: 接收web请求，发送消息到kafka
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/1/1 11:44
 */
@RestController
public class MessageController {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @RequestMapping(value = "/send/{name}/{message}", method = RequestMethod.GET)
    public @ResponseBody
    String send(@PathVariable("name") final String name, @PathVariable("message") final String message) {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = simpleDateFormat.format(new Date());

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("message", message);
        jsonObject.put("time", timeStr);
        jsonObject.put("timeLong", System.currentTimeMillis());
        jsonObject.put("bizID", UUID.randomUUID());

        String sendMessage = jsonObject.toJSONString();

        ListenableFuture future = kafkaTemplate.send("topic001", sendMessage);
        future.addCallback(o -> System.out.println("send message success : " + sendMessage),
                throwable -> System.out.println("send message fail : " + sendMessage));

        return "send message to [" +  name + "] success (" + timeStr + ")";
    }
}
