package com.bolingcavalry.jacksondemo.annotation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @Description: 学校实体类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/29 12:05
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
@JsonPropertyOrder({"age", "name", "city"})
public class College {

    private String name;
    private String city;
    protected int age = 100;


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return "College{" +
                "name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", age=" + age +
                '}';
    }
}
