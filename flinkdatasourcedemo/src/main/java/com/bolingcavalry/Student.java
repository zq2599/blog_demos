package com.bolingcavalry;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-03-15 23:42
 * @description 一个普通bean
 */
public class Student {

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
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
