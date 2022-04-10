package com.bolingcavalry.service.impl;

import io.quarkus.logging.Log;

/**
 * @author zq2599@gmail.com
 * @Title: 资源管理类
 * @Package
 * @Description:
 * @date 4/10/22 10:20 AM
 */
public class ResourceManager {

    public ResourceManager () {
        Log.info("create instance, " + this.getClass().getSimpleName());
    }

    /**
     * 假设再次方法中打开资源，如网络、文件、数据库等
     */
    public void open() {
        Log.info("open resource here");
    }

    /**
     * 假设在此方法中关闭所有已打开的资源
     */
    public void closeAll() {
        Log.info("close all resource here");
    }
}