package com.bolingcavalry.junit5experience.service;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/9/20 16:08
 */
public interface HelloService {

    String hello(String name);

    int increase(int value);

    /**
     * 该方法会等待1秒后返回true，这是在模拟一个耗时的远程调用
     * @return
     */
    boolean remoteRequest();
}
