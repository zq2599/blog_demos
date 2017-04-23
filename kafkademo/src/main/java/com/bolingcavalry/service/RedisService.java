package com.bolingcavalry.service;

import java.util.List;

/**
 * @author willzhao
 * @version V1.0
 * @Description: Redis服务类
 * @email zq2599@gmail.com
 * @Date 17/4/22 下午8:33
 */
public interface RedisService {
    /**
     * string操作，常规设置key－value
     * @param key
     * @param value
     */
    void strSet(String key, String value);

    /**
     * string操作，常规通过key获取value
     * @param key
     * @return
     */
    String setGet(String key);

    /**
     * list操作，尾部追加数据
     * @param key
     * @param value
     */
    void listAppend(String key, String value);

    /**
     * list操作，获取说有数据
     */
    List<String> listGetAll(String key);

    /**
     * 指定键值在redis中是否存在
     * @param key
     * @return
     */
    boolean exists(String key);
}
