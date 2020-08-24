package com.bolingcavalry.jacksondemo.databind;

import com.bolingcavalry.jacksondemo.beans.Constant;
import com.bolingcavalry.jacksondemo.beans.TwitterEntry;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * @Description: jackson数据绑定方法的使用
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/7/4 15:50
 */
public class DatabindDemo {

    private static final Logger logger = LoggerFactory.getLogger(DatabindDemo.class);

    /**
     * ObjectMapper是线程安全的
     */
    final ObjectMapper mapper = new ObjectMapper();

    /**
     * 反序列化测试(JSON -> Object)，入参是JSON字符串
     * @param json JSON字符串
     * @return
     * @throws IOException
     */
    public TwitterEntry deserializeJSONStr(String json) throws IOException {
        return mapper.readValue(json, TwitterEntry.class);
    }

    /**
     * 反序列化测试(JSON -> Object)，入参是JSON字符串
     * @param url JSON字符串的网络地址
     * @return
     * @throws IOException
     */
    public TwitterEntry deserializeJSONFromUrl(String url) throws IOException {
        return mapper.readValue(new URL(url), TwitterEntry.class);
    }

    /**
     * 序列化测试(Object -> JSON)
     * @param twitterEntry
     * @return 由对象序列化得到的JSON字符串
     */
    public String serialize(TwitterEntry twitterEntry) throws IOException{
        return mapper.writeValueAsString(twitterEntry);
    }

    public static void main(String[] args) throws Exception {

        DatabindDemo databindDemo = new DatabindDemo();

        // 执行一次对象转JSON操作
        logger.info("********************执行一次对象转JSON操作********************");
        String serializeResult = databindDemo.serialize(Constant.TEST_OBJECT);
        logger.info("序列化结果是JSON字符串 : \n{}\n\n", serializeResult);

        // 用本地字符串执行一次JSON转对象操作
        logger.info("********************执行一次本地JSON反序列化操作********************");
        TwitterEntry deserializeResult = databindDemo.deserializeJSONStr(Constant.TEST_JSON_STR);
        logger.info("\n本地JSON反序列化结果是个java实例 : \n{}\n\n", deserializeResult);

        // 用网络地址执行一次JSON转对象操作
        logger.info("********************执行一次网络JSON反序列化操作********************");
        deserializeResult = databindDemo.deserializeJSONFromUrl(Constant.TEST_JSON_DATA_URL);
        logger.info("\n网络JSON反序列化结果是个java实例 : \n{}", deserializeResult);
    }
}
