package com.bolingcavalry;

import com.bolingcavalry.context.CustomApplicationContext;

/**
 * @Description :
 * @Author : zq2599@gmail.com
 * @Date : 2018-06-19 15:31
 */
public class CheckApplication {

    public static void main(String[] args) {
        CustomApplicationContext context = new CustomApplicationContext("classpath:applicationContext.xml");
        context.close();
    }
}
