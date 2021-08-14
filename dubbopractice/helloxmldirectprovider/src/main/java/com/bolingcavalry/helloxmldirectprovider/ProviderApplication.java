package com.bolingcavalry.helloxmldirectprovider;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ProviderApplication {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/dubbo-provider.xml");
        context.start();
        System.in.read();
    }
}
