package com.bolingcavalry.entity;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 学生类
 * @email zq2599@gmail.com
 * @Date 2017/10/5 上午9:26
 */
@Document(collection = "student")
public class Student {
    /**
     * 学号
     */
    private String id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 年龄
     */
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
