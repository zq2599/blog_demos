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
public class ClassAnnotationSerialization {

    private static final Logger logger = LoggerFactory.getLogger(ClassAnnotationSerialization.class);

    public static void main(String[] args) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        // 美化输出
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        College college = new College();
        college.setCity("北京");
        college.setName("北京大学");

        Address address = new Address();
        address.setCity("深圳");
        address.setStreet("粤海");

        Parent father = new Parent();
        father.setRelation("父子");
        //father.setName("");

        Student student = new Student();
        student.setFirstName("Bill");
        student.setAge(11);

        student.setLastName("Gates");
        student.setGrade(5);

        student.setCollege(college);
        student.setAddress(address);
        student.setFather(father);

        logger.info("使用了JsonRootName注解的实例，序列化结果：\n{}",
                mapper.writeValueAsString(student));


    }
}
