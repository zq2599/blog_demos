package com.bolingcavalry.minusservice.service.impl;

import com.bolingcavalry.api.exception.MinusException;
import com.bolingcavalry.api.service.MinusService;

/**
 * @author wilzhao
 * @description 支持负数结果的减法服务
 * @email zq2599@gmail.com
 * @time 2018/10/13 14:30
 */
public class MinusServiceSupportNegativeImpl implements MinusService {

    /**
     * 减法实现，支持负数
     * @param minuend 减数
     * @param subtraction 被减数
     * @return
     * @throws MinusException
     */
    public int minus(int minuend, int subtraction) throws MinusException {
        return minuend - subtraction;
    }
}
