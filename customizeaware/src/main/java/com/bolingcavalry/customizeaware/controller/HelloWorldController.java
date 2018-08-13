package com.bolingcavalry.customizeaware.controller;

import com.bolingcavalry.customizeaware.aware.CustomizeApplicationContextAware;
import com.bolingcavalry.customizeaware.aware.CustomizeBeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @Description :
 * @Author : zq2599@gmail.com
 * @Date : 2018-08-13 19:04
 */
@RestController
public class HelloWorldController {

    @Autowired
    private CustomizeBeanNameAware customizeBeanNameAware;

    @Autowired
    private CustomizeApplicationContextAware customizeApplicationContextAware;

    @RequestMapping("/hello")
    public String hello(){

        String[] beanDefinitionNames = customizeApplicationContextAware.getApplicationContext().getBeanDefinitionNames();

        StringBuilder stringBuilder = new StringBuilder();

        int arrayLength = 0;

        if(null!=beanDefinitionNames){
            arrayLength = beanDefinitionNames.length;
            //将所有bean的名称拼接成字符串（带html的换行符号<br>）
            for(String name : beanDefinitionNames){
                stringBuilder.append(name).append("<br>");
            }
        }

        return "hello, "
                + new Date()
                + "<br><br>CustomizeBeanNameAware instance bean name : "
                + customizeBeanNameAware.getBeanName()
                + "<br><br>bean definition names, size "
                + arrayLength
                + ", detail :<br><br>"
                + stringBuilder;
    }


}