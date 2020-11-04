package com.bolingcavalry.hiveudf.udaf;

import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.ql.util.JavaDataModel;

/**
 * @Description: 合并值缓冲区大小，这里是用来保存字符串长度，因此设为8byte
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/11/4 10:08
 */
public class FieldLengthAggregationBuffer extends GenericUDAFEvaluator.AbstractAggregationBuffer {

    private Integer value = 0;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public void add(int addValue) {
        synchronized (value) {
            value += addValue;
        }
    }

    @Override
    public int estimate() {
        return JavaDataModel.PRIMITIVES1;
    }
}
