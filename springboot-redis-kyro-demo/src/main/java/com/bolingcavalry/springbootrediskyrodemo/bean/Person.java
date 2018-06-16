package com.bolingcavalry.springbootrediskyrodemo.bean;

import java.io.Serializable;

public class Person implements Serializable{

    private static final long serialVersionUID = 1L;

    private int id;

    private String name;

    private int age;

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
