package com.bolingcavalry.coprocessfunction;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.StringUtils;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-03-14 20:27
 * @description 将字符串用逗号分割后转为Tuple2类型
 */
public class WordCountMap implements MapFunction<String, Tuple2<String, Integer>> {
    @Override
    public Tuple2<String, Integer> map(String s) throws Exception {

        if(StringUtils.isNullOrWhitespaceOnly(s)) {
            System.out.println("invalid line");
            return null;
        }

        String[] array = s.split(",");

        if(null==array || array.length<2) {
            System.out.println("invalid line for array");
            return null;
        }


        return new Tuple2<>(array[0], Integer.valueOf(array[1]));
    }
}
