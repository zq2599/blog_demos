package com.bolingcavalry.customize;

/**
 * @Description: 自定义sink示例用到的实体类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/4/12 20:54
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

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
