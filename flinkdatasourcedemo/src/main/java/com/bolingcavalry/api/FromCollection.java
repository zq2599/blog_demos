package com.bolingcavalry.api;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-03-14 22:08
 * @description 通过集合创建的DataSource
 */
public class FromCollection {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //并行度为1
        env.setParallelism(1);

        //创建一个List，里面有两个Tuple2元素
        List<Tuple2<String, Integer>> list = new ArrayList<>();
        list.add(new Tuple2("aaa", 1));
        list.add(new Tuple2("bbb", 1));

        //通过List创建DataStream
        DataStream<Tuple2<String, Integer>> fromCollectionDataStream = env.fromCollection(list);

        //通过多个Tuple2元素创建DataStream
        DataStream<Tuple2<String, Integer>> fromElementDataStream = env.fromElements(
                new Tuple2("ccc", 1),
                new Tuple2("ddd", 1),
                new Tuple2("aaa", 1)
        );

        //通过union将两个DataStream合成一个
        DataStream<Tuple2<String, Integer>> unionDataStream = fromCollectionDataStream.union(fromElementDataStream);

        //统计每个单词的数量
        unionDataStream
                .keyBy(0)
                .sum(1)
                .print();

        env.execute("API DataSource demo : collection");
    }
}
