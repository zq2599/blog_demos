package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.PlatformService;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 计算服务的实现
 * @email zq2599@gmail.com
 * @Date 17/5/15 下午2:54
 */
public class PlatformServiceImpl implements PlatformService {

    @Override
    public String getRpcFrom() {
        return System.getenv().get("TOMCAT_SERVER_ID");
    }
}
