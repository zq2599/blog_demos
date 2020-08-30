package com.bolingcavalry.jacksondemo.annotation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * @Description: 实体类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/29 11:35
 */
@JsonIgnoreProperties({"lastName", "grade"})
public class Student {


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    private String firstName;

    private String lastName;

    private int age;

    private int grade;

    public Parent getFather() {
        return father;
    }

    public void setFather(Parent father) {
        this.father = father;
    }

    private Parent father;

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public College getCollege() {
        return college;
    }

    public void setCollege(College college) {
        this.college = college;
    }

    private Address address;

    private College college;


    @Override
    public String toString() {
        return "Student{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", age=" + age +
                ", grade=" + grade +
                ", father=" + father +
                ", address=" + address +
                ", college=" + college +
                '}';
    }
}
