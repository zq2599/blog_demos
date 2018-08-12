package com.bolingcavalry.bean;

/**
 * @Description :
 * @Author : zq2599@gmail.com
 * @Date : 2018-06-19 15:31
 */
public class Simple {


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public void doInit(){
        System.out.println("init execute");
    }

    public void execute() {
        System.out.println("Simple execute method");
    }
}
