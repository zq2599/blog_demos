package com.bolingcavalry.service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author willzhao
 * @version V1.0
 * @Description: Redis连接池
 * @email zq2599@gmail.com
 * @Date 17/4/22 下午8:35
 */
public class RedisPool {
    /**
     * redis服务器ip
     */
    private final static String ADDR = "127.0.0.1";

    /**
     * redis服务器端口
     */
    private final static int PORT = 6379;

    /**
     * 可用连接最大数目
     */
    private final static int MAX_TOTAL = 1024;

    /**
     * 最大空闲实例数目
     */
    private final static int MAX_IDLE = 200;

    /**
     * 等待连接的最长时间,超出会抛出JedisConnectionException
     */
    private final static int MAX_WAIT_MILLIS = 10000;

    private final static int TIMEOUT = 10000;

    /**
     * 取一个jedis实例时，是否提前进行validate操作
     */
    private static boolean TEST_ON_BORROW = true;

    /**
     * 执行
     */
    private volatile static RedisPool instance = null;

    private JedisPool jedisPool = null;

    /**
     * 禁止被外部实例化
     */
    private RedisPool(){
        super();

        try {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxTotal(MAX_TOTAL);
            jedisPoolConfig.setMaxIdle(MAX_IDLE);
            jedisPoolConfig.setMaxWaitMillis(MAX_WAIT_MILLIS);
            jedisPoolConfig.setTestOnBorrow(TEST_ON_BORROW);
            jedisPool = new JedisPool(jedisPoolConfig, ADDR, PORT, TIMEOUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static RedisPool getInstance(){
        if(null==instance) {
            synchronized (RedisPool.class){
                if(null==instance){
                    instance = new RedisPool();
                }
            }
        }

        return instance;
    }

    /**
     * 获取Jedis实例
     * @return
     */
    public synchronized Jedis getJedis(){
        try {
            if(null!=jedisPool){
                Jedis jedis = jedisPool.getResource();
                return jedis;
            }else{
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 归还资源给pool
     * @param jedis
     */
    public void returnResource(final Jedis jedis){
        if(jedis!=null){
            //jedis.close()取代jedisPool.returnResource(jedis)方法将3.0版本开始
            jedisPool.returnResource(jedis);
        }
    }




}
