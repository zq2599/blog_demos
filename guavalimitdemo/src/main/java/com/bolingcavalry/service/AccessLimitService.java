package com.bolingcavalry.service;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Service;

/**
 * Description:限流服务
 * @author willzhao
 * @email zq2599@gmail.com
 * @date 2017/7/9 11:48
 */
@Service
public class AccessLimitService {

    //每秒只发出5个令牌
    RateLimiter rateLimiter = RateLimiter.create(1.0);

    /**
     * 尝试获取令牌
     * @return
     */
    public boolean tryAcquire(){
        return rateLimiter.tryAcquire();
    }
}
