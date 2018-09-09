package com.bolingcavalry.customizeimportselector.service.impl;

import com.bolingcavalry.customizeimportselector.service.CustomizeService3;

/**
 * @Description: 测试实现类3
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2018/9/9 13:21
 */
public class CustomizeServiceImpl3 implements CustomizeService3 {

    public CustomizeServiceImpl3() {
        System.out.println("construct : " + this.getClass().getSimpleName());
    }

    @Override
    public void execute() {
        System.out.println("execute : " + this.getClass().getSimpleName());
    }
}
