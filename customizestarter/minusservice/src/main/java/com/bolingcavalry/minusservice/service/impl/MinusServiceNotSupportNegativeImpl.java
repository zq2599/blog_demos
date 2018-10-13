package com.bolingcavalry.minusservice.service.impl;

import com.bolingcavalry.api.exception.MinusException;
import com.bolingcavalry.api.service.MinusService;

/**
 * @author wilzhao
 * @description 减法服务的实现，不支持负数
 * @email zq2599@gmail.com
 * @time 2018/10/13 14:24
 */
public class MinusServiceNotSupportNegativeImpl implements MinusService {

    /**
     * 减法运算，不支持负数结果，如果被减数小于减数，就跑出MinusException
     * @param minuend 被减数
     * @param subtraction 减数
     * @return
     * @throws MinusException
     */
    public int minus(int minuend, int subtraction) throws MinusException {
        if(subtraction>minuend){
            throw new MinusException("not support negative!");
        }

        return minuend-subtraction;
    }
}
