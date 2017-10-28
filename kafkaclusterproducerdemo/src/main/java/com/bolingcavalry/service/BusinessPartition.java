package com.bolingcavalry.service;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 实现kafka消息的分区逻辑
 * @email zq2599@gmail.com
 * @Date 2017/10/29 上午1:56
 */
public class BusinessPartition implements Partitioner {

    public BusinessPartition(VerifiableProperties props) {
        //注意 ： 构造函数的函数体没有东西，但是不能没有构造函数
    }

    public int partition(Object o, int i) {
        return 0;
    }
}
