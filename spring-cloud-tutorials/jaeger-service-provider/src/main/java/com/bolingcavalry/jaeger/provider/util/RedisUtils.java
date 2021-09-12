package com.bolingcavalry.jaeger.provider.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RedisUtils {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * @param key
     * @return 获得值
     * redis有五种数据类型 opsForValue表示是操作字符串类型
     */
    public Object get(String key){
        return  key == null ? null : redisTemplate.opsForValue().get(key);
    }
    //本来只可以放入string类型，但是我们配置了自动序列化所以这儿可以传入object
    public boolean set(String key,Object value){
        try{
            redisTemplate.opsForValue().set(key,value);
            return true;
        }catch (Exception e){
            log.error("redis set value exception:{}",e);
            return false;
        }
    }

    /**
     * 原子操作
     * @param key
     * @param value
     * @param expire 过期时间 秒
     * @return
     */
    public boolean setex(String key,Object value,long expire){
        try{//TimeUnit.SECONDS指定类型为秒
            redisTemplate.opsForValue().set(key,value,expire, TimeUnit.SECONDS);
            return true;
        }catch (Exception e){
            log.error("redis set value and expire exception:{}",e);
            return false;
        }
    }

    /**
     * 非原子操作
     * @param key
     * @param expire
     * @return
     */
    public boolean expire(String key,long expire){
        try{//这儿没有ops什么的是因为每种数据类型都能设置过期时间
            redisTemplate.expire(key,expire,TimeUnit.SECONDS);
            return true;
        }catch (Exception e){
            log.error("redis set key expire exception:{}",e);
            return false;
        }
    }

    /**
     *
     * @param key
     * @return 获取key的过期时间
     */
    public long ttl(String key){
        return redisTemplate.getExpire(key);
    }

    /**
     *
     * @param keys 删除key 可变参数
     */
    public void del(String ...keys){
        if(keys!=null&&keys.length>0) {
            redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(keys));
        }
    }

    /**
     *
     * @param key
     * @param step  传入正数 就是加多少 传入负数就是减多少
     * @return
     */
    public long incrBy(String key,long step){
        return redisTemplate.opsForValue().increment(key,step);
    }

    /**
     *
     * @param key
     * @param value
     * @return  如果该key存在就返回false 设置不成功 key不存在就返回ture设置成功
     */
    public boolean setnx(String key,Object value){
        return redisTemplate.opsForValue().setIfAbsent(key,value);
    }

    /**
     *  原子操作
     * @param key
     * @param value
     * @param expire  在上面方法加上过期时间设置
     * @return
     */
    public boolean setnxAndExpire(String key,Object value,long expire){
        return redisTemplate.opsForValue().setIfAbsent(key,value,expire,TimeUnit.SECONDS);
    }

    /**
     *
     * @param key
     * @param value
     * @return  如果该key存在就返回之前的value  不存在就返回null
     */
    public Object getAndSet(String key,Object value){
        return redisTemplate.opsForValue().getAndSet(key,value);
    }

    /**
     *
     * @param key
     * @return 判断key是否存在
     */
    public boolean hasKey(String key){
        return redisTemplate.hasKey(key);
    }

    /***list的长度**/
    public long llen(String key){
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 获取key中index位置的值，负数就反过来数，-1为最后一个
     * @param key
     * @param index
     * @return
     */
    public Object lgetByIndex(String key,long index){
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error("redis lgetByIndex error,key:{},index:{}exception:{}",key,index,e);
            return null;
        }
    }
    /**
     * 将list放入缓存
     * @param key 键
     * @param value 值
     * @return
     */
    public boolean lrpush(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            log.error("redis lrpush error,key:{},value:{}exception:{}",key,value,e);
            return false;
        }
    }
    /**
     * 将list放入缓存
     * @param key 键
     * @param value 值
     * @param time 时间(秒)
     * @return
     */
    public boolean lrpush(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0)
                expire(key, time);
            return true;
        } catch (Exception e) {
            log.error("redis lrpush error,key:{},value:{},timeL{},exception:{}",key,value,time,e);
            return false;
        }
    }
    /**
     * 将list放入缓存
     * @param key 键
     * @param value 值
     * @return
     */
    public boolean lrpush(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            log.error("redis lrpush error,key:{},value:{},exception:{}",key,value,e);
            return false;
        }
    }
    /**
     * 将list放入缓存
     *
     * @param key 键
     * @param value 值
     * @param time 时间(秒)
     * @return
     */
    public boolean lrpush(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0)
                expire(key, time);
            return true;
        } catch (Exception e) {
            log.error("redis lrpush error,key:{},value:{},time:{},exception:{}",key,value,time,e);
            return false;
        }
    }
    /**
     * 根据索引修改list中的某条数据
     * @param key 键
     * @param index 索引
     * @param value 值
     * @return
     */
    public boolean lUpdateByIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            log.error("redis lUpdateByIndex error,key:{},index:{},value:{},exception:{}",key,index,value,e);
            return false;
        }
    }
    /**
     * 移除N个值为value
     * @param key 键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lrem(String key, long count, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            log.error("redis lrem error,key:{},count:{},value:{},exception:{}",key,count,value,e);
            return 0;
        }
    }
    /*****hash数据类型方法   opsForHash表示是操作字符串类型*****/

    /**
     * @param key 健
     * @param field 属性
     * @param value 值
     * @return
     */
    public boolean hset(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            return true;
        }catch (Exception e){
            log.error("redis hset eror,key:{},field:{},value:{}",key,field,value);
            return false;
        }
    }

    /**
     *
     * @param key
     * @param field
     * @param value
     * @param seconds(秒) 过期时间
     * @return
     */
    public boolean hset(String key, String field, Object value,long seconds) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            expire(key,seconds);//调用通用方法设置过期时间
            return true;
        }catch (Exception e){
            log.error("redis hset and expire eror,key:{},field:{},value:{},exception:{}",key,field,value,e);
            return false;
        }
    }

    /**
     * 获取key中field属性的值
     * @param key
     * @param field
     * @return
     */
    public Object hget(String key,String field){
        return redisTemplate.opsForHash().get(key,field);
    }

    /**
     * 获取key中多个属性的键值对，这儿使用map来接收
     * @param key
     * @param fields
     * @return
     */
    public Map<String,Object> hmget(String key, String...fields){
        Map<String,Object> map =  new HashMap<>();
        for (String field :fields){
            map.put(field,hget(key,field));
        }
        return map;
    }

    /**
     * @param key 获得该key下的所有键值对
     * @return
     */
    public Map<Object, Object> hmget(String key){
        return redisTemplate.opsForHash().entries(key);
    }



    /**
     * @param key 键
     * @param map 对应多个键值
     * @return
     */
    public boolean hmset(String key,Map<Object,Object> map){
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        }catch (Exception e){
            log.error("redis hmset eror,key:{},value:{},exception:{}",key,map,e);
            return false;
        }
    }

    /**
     * @param key 键
     * @param map 对应多个键值
     *  @param seconds 过期时间(秒)
     * @return
     */
    public boolean hmset(String key,Map<String,Object> map,long seconds){
        try {
            redisTemplate.opsForHash().putAll(key, map);
            expire(key,seconds);
            return true;
        }catch (Exception e){
            log.error("redis hmset eror,key:{},value:{},expireTime,exception:{}",key,map,seconds,e);
            return false;
        }
    }

    /**
     *删除key中的属性
     * @param key
     * @param fields
     */
    public void hdel(String key,Object...fields){
        redisTemplate.opsForHash().delete(key,fields);
    }

    /**
     * 判断key中是否存在某属性
     * @param key
     * @param field
     * @return
     */
    public boolean hHashKey(String key,String field){
        return redisTemplate.opsForHash().hasKey(key,field);
    }

    /**
     * 对key中filed的value增加多少 如果是减少就传入负数
     * @param key
     * @param field
     * @param step 正数增加，负数减少
     * @return
     */
    public double hincr(String key,String field,double step){
        return redisTemplate.opsForHash().increment(key,field,step);
    }

    /**
     * key中多少个
     * @param key
     * @return
     */
    public long hlen(String key){
        return redisTemplate.opsForHash().size(key);
    }
    /******其他方法用到在增加********/

    /***set集合***/
    /**
     * 获取key中所有元素
     * @param key
     * @return
     */
    public Set<Object> sgetAll(String key){
        try {
            return redisTemplate.opsForSet().members(key);
        }catch (Exception e){
            log.error("redis sgetAll error,key:{},exception:{}",key,e);
            return null;
        }
    }

    /**
     * 判断value是否在key中
     * @param key
     * @param value
     * @return
     */
    public boolean sexists(String key,Object value){
        try {
            return redisTemplate.opsForSet().isMember(key,value);
        }catch (Exception e){
            log.error("redis sexists error,key:{},value:{},exception:{}",key,value,e);
            return false;
        }
    }

    /**
     * 插入多个元素
     * @param key
     * @param values
     * @return  成功的个数
     */
    public long sset(String key ,Object...values){
        try {
            return redisTemplate.opsForSet().add(key,values);
        }catch (Exception e){
            log.error("redis sset error,key:{},value:{},values:{},exception:{}",key,values,e);
            return 0;
        }
    }

    /**
     * 添加元素并设置过期时间  （非原子操作）
     * @param key
     * @param time
     * @param values
     * @return
     */
    public long sset(String key ,long time,Object...values){
        try {
            long count = redisTemplate.opsForSet().add(key,values);
            expire(key,time);
            return count;
        }catch (Exception e){
            log.error("redis sset error,key:{},value:{},values:{},exception:{}",key,values,e);
            return 0;
        }
    }

    /**
     * 获取set的长度
     * @param key
     * @return
     */
    public long sgetSize(String key){
        try {
            return redisTemplate.opsForSet().size(key);
        }catch (Exception e){
            log.error("redis sgetSize error,key:{},exception:{}",key,e);
            return 0;
        }
    }
    /**
     * 移除值为value的
     * @param key 键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long sRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            log.error("redis sRemove error,key:{},values:{},exception:{}",key,values,e);
            return 0;
        }
    }

    /**
     * 随机取count个元素  count为正数就取不重复的  负数就有可能重复
     * @param key
     * @param count
     * @return
     */
    public List<Object> sRandom(String key,long count) {
        try {
            return  redisTemplate.opsForSet().randomMembers(key,count);
        } catch (Exception e) {
            log.error("redis sRandom error,key:{},count:{},exception:{}",key,count,e);
            return null;
        }
    }
    /****zset工具类***/
    /**
     * 添加元素
     * @param key
     * @param member
     * @param score
     * @return
     */
    public  boolean zadd(String key,Object member,double score){
        try {
            return  redisTemplate.opsForZSet().add(key,member,score);
        } catch (Exception e) {
            log.error("redis zadd error,key:{},value:{},score:{},exception:{}",key,member,score,e);
            return false;
        }
    }

    public  Set<String> zrange(String key,int start,int end){

        try {
            Set<Object> range = redisTemplate.opsForZSet().
                    range(key, start, end);
            if(range==null||range.size()==0) return null;
            return   range.stream().
                    map(o->(String)o).collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("redis zrange error,key:{},start:{},end:{},exception:{}",
                    key,start,end,e);
            return null;
        }
    }
}
