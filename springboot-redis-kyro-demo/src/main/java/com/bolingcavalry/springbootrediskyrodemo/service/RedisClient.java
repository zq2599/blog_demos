package com.bolingcavalry.springbootrediskyrodemo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
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

    private static final Logger logger = LoggerFactory.getLogger(RedisClient.class);

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    private RedisConnection getConnection() {
        return redisTemplate.getConnectionFactory().getConnection();
    }

    /**
     * 释放连接
     * @param redisConnection
     */
    private void releaseConnection(RedisConnection redisConnection){
        if(null!=redisConnection && null!=redisTemplate){
            RedisConnectionFactory redisConnectionFactory = redisTemplate.getConnectionFactory();

            if(null!=redisConnectionFactory){
                RedisConnectionUtils.releaseConnection(redisConnection, redisConnectionFactory);
            }
        }
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
        RedisConnection redisConnection = getConnection();

        if(null!=redisConnection){
            try {
                redisConnection.set(keyBytes, val);
            }finally {
                releaseConnection(redisConnection);
            }
        }else{
            logger.error("1. can not get valid connection");
        }
    }

    /**
     * 删除指定对象
     * @param key
     * @return
     */
    public long del(String key){
        RedisConnection redisConnection = getConnection();
        long rlt = 0L;

        if(null!=redisConnection){
            try {
                rlt = redisConnection.del(getKey(key));
            }finally {
                releaseConnection(redisConnection);
            }
        }else{
            logger.error("1. can not get valid connection");
        }
        return rlt;
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
        byte[] result = null;

        RedisConnection redisConnection = getConnection();

        if(null!=redisConnection){
            try {
                result = redisConnection.get(keyBytes);
            }finally {
                releaseConnection(redisConnection);
            }
        }else{
            logger.error("2. can not get valid connection");
        }

        return null!=redisConnection ? (T) redisTemplate.getValueSerializer().deserialize(result) : null;
    }
}
