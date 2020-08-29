package com.bolingcavalry.jacksondemo.databind;

import com.bolingcavalry.jacksondemo.beans.Constant;
import com.bolingcavalry.jacksondemo.beans.TwitterEntry;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: jackson常用配置演示
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/24 07:58
 */
public class ConfigDemo {

    private static final Logger logger = LoggerFactory.getLogger(ConfigDemo.class);


    public static void main(String[] args) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
//        // 美化输出
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
// 允许序列化空的POJO类
// （否则会抛出异常）
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
// 把java.util.Date, Calendar输出为数字（时间戳）
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

// 在遇到未知属性的时候不抛出异常
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
// 强制JSON 空字符串("")转换为null对象值:
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

// 在JSON中允许C/C++ 样式的注释(非标准，默认禁用)
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
// 允许没有引号的字段名（非标准）
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
// 允许单引号（非标准）
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
// 强制转义非ASCII字符
        mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
// 将内容包裹为一个JSON属性，属性名由@JsonRootName注解指定
//        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

//        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

        ConfigDemo databindDemo = new ConfigDemo();

        TwitterEntry twitterEntry = new TwitterEntry();
        twitterEntry.setId(1);
        twitterEntry.setText("aabbcc");
        twitterEntry.setFromUserId(123);
        twitterEntry.setFromUserId(456);
        twitterEntry.setLanguageCode("zh");


        //logger.info("默认无缩放\n{}", databindDemo.mapper.writeValueAsString(twitterEntry));

        System.out.println(mapper.writeValueAsString(twitterEntry));
        System.out.println("");
        System.out.println("");
        System.out.println("");
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

        System.out.println(mapper.writeValueAsString(twitterEntry));

        //Map<String, Object> map = new HashMap<>();
        //map.put("123", new Date());
        //System.out.println(mapper.writeValueAsString(map));

    }
}
