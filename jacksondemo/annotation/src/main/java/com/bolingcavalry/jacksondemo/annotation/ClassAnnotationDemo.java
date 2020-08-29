package com.bolingcavalry.jacksondemo.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description: 演示方法注解
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/29 11:32
 */
public class ClassAnnotationDemo {

    private static final Logger logger = LoggerFactory.getLogger(ClassAnnotationDemo.class);

    public static void main(String[] args) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        // 美化输出
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//
//
//
//
//
//        College college = new College();
//        college.setCity("北京");
//        college.setName("北京大学");
//
//        Address address = new Address();
//        address.setCity("深圳");
//        address.setStreet("粤海");
//
//        Parent father = new Parent();
//        father.setRelation("父子");
//        father.setName("");
//
//        Student student = new Student();
//        student.setFirstName("Bill");
//        student.setLastName("Gates");
//        student.setAge(11);
//        student.setGrade(5);
//
//        student.setCollege(college);
//        student.setAddress(address);
//        student.setFather(father);
//
//
//        logger.info("使用了JsonRootName注解的实例，序列化结果：\n{}", mapper.writeValueAsString(student));




//        String jsonStr = "{\n" +
//                "  \"aaabbbccc\" : {\n" +
//                "    \"firstName\" : \"Bill\",\n" +
//                "    \"age\" : 11,\n" +
//                "    \"father\" : {\n" +
//                "      \"relation\" : \"父子\",\n" +
//                "      \"name\" : \"\"\n" +
//                "    },\n" +
//                "    \"college\" : {\n" +
//                "      \"age\" : 100,\n" +
//                "      \"name\" : \"北京大学\",\n" +
//                "      \"city\" : \"北京\"\n" +
//                "    }\n" +
//                "  }\n" +
//                "}";

//        String jsonStr = "{\n" +
//                "    \"firstName\" : \"Bill\",\n" +
//                "    \"age\" : 11,\n" +
//                "    \"father\" : {\n" +
//                "      \"relation\" : \"父子\",\n" +
//                "      \"name\" : \"\"\n" +
//                "    },\n" +
//                "    \"college\" : {\n" +
//                "      \"age\" : 100,\n" +
//                "      \"name\" : \"北京大学\",\n" +
//                "      \"city\" : \"北京\"\n" +
//                "    }\n" +
//                "  }";

        String jsonStr = "{\n" +
                "    \"firstName\" : \"Bill\",\n" +
                "    \"age\" : 11,\n" +
                "    \"lastName\" : \"Abc\",\n" +
                "    \"father\" : {\n" +
                "      \"relation\" : \"父子\",\n" +
                "      \"name\" : \"\"\n" +
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
