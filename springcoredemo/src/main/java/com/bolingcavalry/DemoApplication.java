package com.bolingcavalry;

import com.bolingcavalry.bean.Simple;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashSet;
import java.util.TreeSet;

/**
 * @Description :
 * @Author : zq2599@gmail.com
 * @Date : 2018-06-19 15:31
 */
public class DemoApplication {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("${CONFIG_PATH}");
        Simple bean = context.getBean(Simple.class);
        bean.execute();
        context.close();
    }
}
