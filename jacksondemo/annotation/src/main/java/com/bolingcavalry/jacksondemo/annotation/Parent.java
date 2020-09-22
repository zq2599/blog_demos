package com.bolingcavalry.jacksondemo.annotation;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @Description: 家长
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/29 12:50
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Parent {

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    @Override
    public String toString() {
        return "Parent{" +
                "relation='" + relation + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    private String relation;

    private String name="aaa";



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
