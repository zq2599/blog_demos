package com.bolingcavalry.jacksondemo.annotation;

/**
 * @Description: 实体类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/30 20:31
 */
public class Order1 {

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

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Order1{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
