package com.bolingcavalry.customizebeandefinitionregistrypostprocessor.service.impl;

import com.bolingcavalry.customizebeandefinitionregistrypostprocessor.service.CalculateService;

public class CalculateServiceImpl implements CalculateService {

    private String desc = "desc from class";

    public void setDesc(String desc) {
        this.desc = desc;
    }


    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public String getServiceDesc() {
        return desc;
    }
}
