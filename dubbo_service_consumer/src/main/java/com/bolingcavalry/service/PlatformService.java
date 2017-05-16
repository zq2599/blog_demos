package com.bolingcavalry.service;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 执行平台层次的服务
 * @email zq2599@gmail.com
 * @Date 17/5/15 下午2:51
 */
public interface PlatformService {

    /**
     * 返回rpc来源的身份信息
     * @return
     */
    String getRpcFrom();
}
