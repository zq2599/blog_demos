package com.bolingcavalry.jacksondemo.databind;

import com.bolingcavalry.jacksondemo.beans.Constant;
import com.bolingcavalry.jacksondemo.beans.TwitterEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * @Description: jackson常用配置演示
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/24 07:58
 */
public class ConfigDemo {

    private static final Logger logger = LoggerFactory.getLogger(ConfigDemo.class);

    /**
     * ObjectMapper是线程安全的
     */
    final ObjectMapper mapper = new ObjectMapper();



    public static void main(String[] args) throws Exception {
        ConfigDemo databindDemo = new ConfigDemo();

        TwitterEntry twitterEntry = new TwitterEntry();
        twitterEntry.setId(1);
        twitterEntry.setText("aabbcc");
        twitterEntry.setFromUserId(123);
        twitterEntry.setFromUserId(456);
        twitterEntry.setLanguageCode("zh");


        logger.info("默认无缩放\n{}", databindDemo.mapper.writeValueAsString(twitterEntry));
        databindDemo.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        logger.info("有缩放\n{}", databindDemo.mapper.writeValueAsString(twitterEntry));



    }
}
