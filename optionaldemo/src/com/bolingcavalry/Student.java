package com.bolingcavalry;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 一个普通的bean
 * @email zq2599@gmail.com
 * @Date 2017/8/26 下午11:23
 */
public class Student {
    private int id;

    private String name;

    private int age;

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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Student(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
}
