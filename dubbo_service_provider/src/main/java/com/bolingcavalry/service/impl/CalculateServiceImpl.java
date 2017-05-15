package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.CalculateService;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 计算服务的实现
 * @email zq2599@gmail.com
 * @Date 17/5/15 下午2:54
 */
public class CalculateServiceImpl implements CalculateService{

    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
