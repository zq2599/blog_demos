package com.bolingcavalry.dao;

/**
 * @Description: Etcd高级操作的服务接口
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2021/4/4 8:21
 */
public interface AdvancedEtcdService {

    /**
     * 乐观锁，指定key的当前值如果等于expectValue，就设置成updateValue
     * @param key           键
     * @param expectValue   期望值
     * @param updateValue   达到期望值时要设置的值
     */
    boolean cas(String key, String expectValue, String updateValue) throws Exception;

    /**
     * 关闭，释放资源
     */
    void close();
}
