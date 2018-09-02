package com.bolingcavalry.customizebeanpostprocessor.service.impl;


import com.bolingcavalry.customizebeanpostprocessor.service.CalculateService;
import org.springframework.stereotype.Service;

@Service("calculateService")
public class CalculateServiceImpl implements CalculateService {

    private String serviceDesc = "desc from class";

    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public String getServiceDesc() {
        return serviceDesc;
    }

    @Override
    public void setServiceDesc(String serviceDesc) {
        this.serviceDesc = serviceDesc;
    }
}