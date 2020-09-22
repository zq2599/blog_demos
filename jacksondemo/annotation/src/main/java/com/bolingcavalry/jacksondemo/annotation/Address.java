package com.bolingcavalry.jacksondemo.annotation;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

/**
 * @Description: 地址实体类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/29 12:00
 */
@JsonIgnoreType
public class Address {

    private String city;

    private String street;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    @Override
    public String toString() {
        return "Address{" +
                "city='" + city + '\'' +
                ", street='" + street + '\'' +
                '}';
    }
}
