package com.bolingcavalry.customizeimportselector.service.impl;

import com.bolingcavalry.customizeimportselector.service.CustomizeService2;

/**
 * @Description: 测试实现类2
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2018/9/9 13:21
 */
public class CustomizeServiceImpl2 implements CustomizeService2 {

    public CustomizeServiceImpl2() {
        System.out.println("construct : " + this.getClass().getSimpleName());
    }

    @Override
    public void execute() {
        System.out.println("execute : " + this.getClass().getSimpleName());
    }
}
