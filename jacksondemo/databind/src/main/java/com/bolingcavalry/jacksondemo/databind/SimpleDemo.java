package com.bolingcavalry.jacksondemo.databind;

import com.bolingcavalry.jacksondemo.beans.Constant;
import com.bolingcavalry.jacksondemo.beans.TwitterEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/24 8:25
 */
public class SimpleDemo {

    private static final Logger logger = LoggerFactory.getLogger(SimpleDemo.class);

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        logger.info("以下是序列化操作");

        // 对象 -> 字符串
        String jsonStr = mapper.writeValueAsString(Constant.TEST_OBJECT);

        logger.info("序列化的字符串：{}", jsonStr);
        // 对象 -> 文件
        mapper.writeValue(new File("twitter.json"), Constant.TEST_OBJECT);

        // 对象 -> byte数组
        byte[] array = mapper.writeValueAsBytes(Constant.TEST_OBJECT);


        logger.info("以下是反序列化操作");

        // 字符串 -> 对象
        TwitterEntry tFromStr = mapper.readValue(Constant.TEST_JSON_STR, TwitterEntry.class);
        logger.info("从字符串反序列化的对象：{}", tFromStr);

        // 字符串网络地址 -> 对象
        TwitterEntry tFromUrl = mapper.readValue(new URL(Constant.TEST_JSON_DATA_URL), TwitterEntry.class);
        logger.info("从网络地址反序列化的对象：{}", tFromUrl);
    }
}
