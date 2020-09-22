package com.bolingcavalry.addservice.service.impl;

import com.bolingcavalry.api.service.AddService;

/**
 * @author wilzhao
 * @description 加法服务的实现
 * @email zq2599@gmail.com
 * @time 2018/10/13 10:59
 */
public class AddServiceImpl implements AddService {
    public int add(int a, int b) {
        return a + b;
    }
}
