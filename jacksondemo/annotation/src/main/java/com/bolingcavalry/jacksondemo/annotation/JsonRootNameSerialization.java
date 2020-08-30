package com.bolingcavalry.jacksondemo.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/30 20:52
 */
public class JsonRootNameSerialization {

    private static final Logger logger = LoggerFactory.getLogger(JsonRootNameSerialization.class);

    public static void main(String[] args) throws Exception {
        // 实例化Order1和Order2
        Order1 order1 = new Order1();
        order1. setId(1);
        order1.setName("book");

        Order2 order2 = new Order2();
        order2. setId(2);
        order2.setName("food");

        // 没有开启WRAP_ROOT_VALUE的时候
        logger.info("没有开启WRAP_ROOT_VALUE\n");
        ObjectMapper mapper1 = new ObjectMapper();
        // 美化输出
        mapper1.enable(SerializationFeature.INDENT_OUTPUT);

        logger.info("没有JsonRootName注解类，序列化结果：\n\n{}\n\n", mapper1.writeValueAsString(order1));
        logger.info("有JsonRootName注解的类，序列化结果：\n\n{}\n\n\n\n", mapper1.writeValueAsString(order2));


        // 开启了WRAP_ROOT_VALUE的时候
        logger.info("开启了WRAP_ROOT_VALUE\n");
        ObjectMapper mapper2 = new ObjectMapper();
        // 美化输出
        mapper2.enable(SerializationFeature.INDENT_OUTPUT);
        // 序列化的时候支持JsonRootName注解
        mapper2.enable(SerializationFeature.WRAP_ROOT_VALUE);

        logger.info("没有JsonRootName注解类，序列化结果：\n\n{}\n\n", mapper2.writeValueAsString(order1));
        logger.info("有JsonRootName注解的类，序列化结果：\n\n{}", mapper2.writeValueAsString(order2));
    }
}
