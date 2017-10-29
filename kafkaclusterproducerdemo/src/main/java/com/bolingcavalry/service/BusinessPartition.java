package com.bolingcavalry.service;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;
import org.apache.commons.lang3.StringUtils;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 实现kafka消息的分区逻辑
 * @email zq2599@gmail.com
 * @Date 2017/10/29 上午1:56
 */
public class BusinessPartition implements Partitioner {

    /**
     * 构造函数的函数体没有东西，但是不能没有构造函数
     * @param props
     */
    public BusinessPartition(VerifiableProperties props) {
        super();
    }

    public int partition(Object o, int i) {
        int partitionValue = 0;

        if(o instanceof String && StringUtils.isNoneBlank((String)o)){
            partitionValue = Integer.valueOf((String)o);
        }

        return partitionValue;
    }
}
