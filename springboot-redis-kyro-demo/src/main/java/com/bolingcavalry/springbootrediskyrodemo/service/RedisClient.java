package com.bolingcavalry.springbootrediskyrodemo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

/**
 * @Description : 封装了redis的操作
 * @Author : zq2599@gmail.com
 * @Date : 2018-06-10 22:52
 */
@Service
public class RedisClient {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    private RedisConnection getConnection() {
        return redisTemplate.getConnectionFactory().getConnection();
    }

    /**
     * 获取缓存的key
     * @param id
     * @return
     */
    private <T> byte[] getKey(T id) {
        RedisSerializer serializer = redisTemplate.getKeySerializer();
        return serializer.serialize(id);
    }

    /**
     * 更新缓存中的对象，也可以在redis缓存中存入新的对象
     *
     * @param key
     * @param t
     * @param <T>
     */
    public <T> void set(String key, T t) {
        byte[] keyBytes = getKey(key);
        RedisSerializer serializer = redisTemplate.getValueSerializer();
        byte[] val = serializer.serialize(t);
        getConnection().set(keyBytes, val);
    }

    public long del(String key){
        return getConnection().del(getKey(key));
    }

    /**
     * 从缓存中取对象
     *
     * @param key
     * @param <T>
     * @return
     */
    public <T> T getObject(String key) {
        byte[] keyBytes = getKey(key);
        byte[] result = getConnection().get(keyBytes);
        return (T) redisTemplate.getValueSerializer().deserialize(result);
    }
}
