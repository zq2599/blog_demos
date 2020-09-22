package com.bolingcavalry.jacksondemo.annotation;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description: 演示方法注解
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/29 11:32
 */
public class ClassAnnotationDeserializer {

    private static final Logger logger = LoggerFactory.getLogger(ClassAnnotationDeserializer.class);

    public static void main(String[] args) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
//        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);


        String jsonStr = "{\n" +
                "    \"firstName\" : \"Bill\",\n" +
                "    \"age\" : 11,\n" +
                "    \"lastName1\" : \"112233\",\n" +
                "    \"grade\" : 9,\n" +
                "    \"father\" : {\n" +
                "      \"relation\" : \"父子\"\n" +
                "    },\n" +
                "    \"college\" : {\n" +
                "      \"age\" : 100,\n" +
                "      \"name\" : \"北京大学\",\n" +
                "      \"city\" : \"北京\"\n" +
                "    }\n" +
                "  }";

        Student student = mapper.readValue(jsonStr, Student.class);
        logger.info("反序列化后得到的实例：\n{}", student);
    }
}
