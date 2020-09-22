package com.bolingcavalry.api.service;

import com.bolingcavalry.api.exception.MinusException;

/**
 * @author wilzhao
 * @description 减法服务
 * @email zq2599@gmail.com
 * @time 2018/10/13 12:07
 */
public interface MinusService {
    /**
     * 普通减法
     * @param minuend 减数
     * @param subtraction 被减数
     * @return 差
     */
    int minus(int minuend, int subtraction) throws MinusException;
}
