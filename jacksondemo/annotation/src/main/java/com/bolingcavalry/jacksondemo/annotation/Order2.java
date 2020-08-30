package com.bolingcavalry.jacksondemo.annotation;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * @Description: 实体类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/30 20:31
 */
@JsonRootName(value = "aaabbbccc")
public class Order2 {

    private int id;

    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Order2{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public void setName(String name) {
        this.name = name;
    }
}
