package com.bolingcavalry.jacksondemo.core;

import com.bolingcavalry.jacksondemo.beans.TwitterEntry;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * @Description: jackson低阶方法的使用
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/7/4 15:50
 */
public class StreamingDemo {

    private static final Logger logger = LoggerFactory.getLogger(StreamingDemo.class);

    JsonFactory jsonFactory = new JsonFactory();

    /**
     * 该字符串的值是个网络地址，该地址对应的内容是个JSON
     */
    final static String TEST_JSON_DATA_URL = "https://raw.githubusercontent.com/zq2599/blog_demos/master/files/twitteer_message.json";

    /**
     * 用来验证反序列化的JSON字符串
     */
    final static String TEST_JSON_STR = "{\n" +
            "  \"id\":1125687077,\n" +
            "  \"text\":\"@stroughtonsmith You need to add a \\\"Favourites\\\" tab to TC/iPhone. Like what TwitterFon did. I can't WAIT for your Twitter App!! :) Any ETA?\",\n" +
            "  \"fromUserId\":855523, \n" +
            "  \"toUserId\":815309,\n" +
            "  \"languageCode\":\"en\"\n" +
            "}";

    /**
     * 用来验证序列化的TwitterEntry实例
     */
    final static TwitterEntry TEST_OBJECT = new TwitterEntry();

    /**
     * 准备好TEST_OBJECT对象的各个参数
     */
    static {
        TEST_OBJECT.setId(123456L);
        TEST_OBJECT.setFromUserId(101);
        TEST_OBJECT.setToUserId(102);
        TEST_OBJECT.setText("this is a message for serializer test");
        TEST_OBJECT.setLanguageCode("zh");
    }


    /**
     * 反序列化测试(JSON -> Object)，入参是JSON字符串
     * @param json JSON字符串
     * @return
     * @throws IOException
     */
    public TwitterEntry deserializeJSONStr(String json) throws IOException {

        JsonParser jsonParser = jsonFactory.createParser(json);

        if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
            jsonParser.close();
            logger.error("起始位置没有大括号");
            throw new IOException("起始位置没有大括号");
        }

        TwitterEntry result = new TwitterEntry();

        try {
            // Iterate over object fields:
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {

                String fieldName = jsonParser.getCurrentName();

                logger.info("正在解析字段 [{}]", jsonParser.getCurrentName());

                // 解析下一个
                jsonParser.nextToken();

                switch (fieldName) {
                    case "id":
                        result.setId(jsonParser.getLongValue());
                        break;
                    case "text":
                        result.setText(jsonParser.getText());
                        break;
                    case "fromUserId":
                        result.setFromUserId(jsonParser.getIntValue());
                        break;
                    case "toUserId":
                        result.setToUserId(jsonParser.getIntValue());
                        break;
                    case "languageCode":
                        result.setLanguageCode(jsonParser.getText());
                        break;
                    default:
                        logger.error("未知字段 '" + fieldName + "'");
                        throw new IOException("未知字段 '" + fieldName + "'");
                }
            }
        } catch (IOException e) {
            logger.error("反序列化出现异常 :", e);
        } finally {
            jsonParser.close(); // important to close both parser and underlying File reader
        }

        return result;
    }

    /**
     * 反序列化测试(JSON -> Object)，入参是JSON字符串
     * @param url JSON字符串的网络地址
     * @return
     * @throws IOException
     */
    public TwitterEntry deserializeJSONFromUrl(String url) throws IOException {
        // 从网络上取得JSON字符串
        String json = IOUtils.toString(new URL(TEST_JSON_DATA_URL), JsonEncoding.UTF8.name());

        logger.info("从网络取得JSON数据 :\n{}", json);

        if(StringUtils.isNotBlank(json)) {
            return deserializeJSONStr(json);
        } else {
            logger.error("从网络获取JSON数据失败");
            return null;
        }
    }


    /**
     * 序列化测试(Object -> JSON)
     * @param twitterEntry
     * @return 由对象序列化得到的JSON字符串
     */
    public String serialize(TwitterEntry twitterEntry) throws IOException{
        String rlt = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        JsonGenerator jsonGenerator = jsonFactory.createGenerator(byteArrayOutputStream, JsonEncoding.UTF8);

        try {
            jsonGenerator.useDefaultPrettyPrinter();

            jsonGenerator.writeStartObject();
            jsonGenerator.writeNumberField("id", twitterEntry.getId());
            jsonGenerator.writeStringField("text", twitterEntry.getText());
            jsonGenerator.writeNumberField("fromUserId", twitterEntry.getFromUserId());
            jsonGenerator.writeNumberField("toUserId", twitterEntry.getToUserId());
            jsonGenerator.writeStringField("languageCode", twitterEntry.getLanguageCode());
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            logger.error("序列化出现异常 :", e);
        } finally {
            jsonGenerator.close();
        }

        // 一定要在
        rlt = byteArrayOutputStream.toString();

        return rlt;
    }


    public static void main(String[] args) throws Exception {

        StreamingDemo streamingDemo = new StreamingDemo();

        // 执行一次对象转JSON操作
        logger.info("********************执行一次对象转JSON操作********************");
        String serializeResult = streamingDemo.serialize(TEST_OBJECT);
        logger.info("序列化结果是JSON字符串 : \n{}\n\n", serializeResult);

        // 用本地字符串执行一次JSON转对象操作
        logger.info("********************执行一次本地JSON反序列化操作********************");
        TwitterEntry deserializeResult = streamingDemo.deserializeJSONStr(TEST_JSON_STR);
        logger.info("\n本地JSON反序列化结果是个java实例 : \n{}\n\n", deserializeResult);

        // 用网络地址执行一次JSON转对象操作
        logger.info("********************执行一次网络JSON反序列化操作********************");
        deserializeResult = streamingDemo.deserializeJSONFromUrl(TEST_JSON_DATA_URL);
        logger.info("\n网络JSON反序列化结果是个java实例 : \n{}", deserializeResult);

        ObjectMapper a;
    }
}
