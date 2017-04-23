package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.RedisPool;
import com.bolingcavalry.service.RedisService;
import org.springframework.stereotype.Service;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

/**
 * @author willzhao
 * @version V1.0
 * @Description: redis服务的实现类
 * @email zq2599@gmail.com
 * @Date 17/4/22 下午9:42
 */

@Service
public class RedisServiceImpl implements RedisService{

    private RedisPool redisPool = RedisPool.getInstance();

    /**
     * 取出资源
     * @return
     */
    private Jedis borrowJedis(){
        if(null!=redisPool){
            return redisPool.getJedis();
        }

        return null;
    }

    /**
     * 归还资源
     * @param jedis
     */
    private void returnJedis(Jedis jedis){
        if(null!=jedis && null!=redisPool){
            redisPool.returnResource(jedis);
        }
    }

    @Override
    public void strSet(String key, String value) {
        Jedis jedis = borrowJedis();

        if(null!=jedis){
            jedis.set(key, value);
        }

        returnJedis(jedis);
    }

    @Override
    public String setGet(String key) {
        Jedis jedis = borrowJedis();

        if(null!=jedis){
            String value = jedis.get(key);
            returnJedis(jedis);
            return value;
        }

        return null;
    }

    @Override
    public void listAppend(String key, String value) {
        Jedis jedis = borrowJedis();

        if(null!=jedis){
            jedis.rpush(key, value);
            returnJedis(jedis);
        }
    }

    @Override
    public List<String> listGetAll(String key) {
        List<String> list = null;

        Jedis jedis = borrowJedis();

        if(null!=jedis){
            list = jedis.lrange(key, 0, -1);
            returnJedis(jedis);
        }

        return null==list ? new ArrayList() : list;
    }

    @Override
    public boolean exists(String key) {
        Jedis jedis = borrowJedis();

        if(null!=jedis){
            boolean exists = jedis.exists(key);
            returnJedis(jedis);
            return exists;
        }

        return false;
    }
}
