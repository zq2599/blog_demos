package com.bolingcavalry.api;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-03-14 22:08
 * @description 最简单的sink
 */
public class Print {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //创建一个List，里面有两个Tuple2元素
        List<Tuple2<String, Integer>> list = new ArrayList<>();
        list.add(new Tuple2("aaa", 1));
        list.add(new Tuple2("aaa", 1));
        list.add(new Tuple2("bbb", 1));

        //统计每个单词的数量
        env.fromCollection(list)
                .keyBy(0)
                .sum(1)
                .print();

        env.execute("sink api demo : print");
    }
}
