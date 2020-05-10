package com.bolingcavalry.processfunction;


import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;


/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-03-14 22:08
 * @description 通过集合创建的DataSource
 */
public class ProcessFunctionDemo {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        //并行度为1
        env.setParallelism(1);

        //创建一个List，里面有两个Tuple2元素
        List<Tuple2<String, Integer>> list = new ArrayList<>();
        list.add(new Tuple2("aaa", 1));
        list.add(new Tuple2("bbb", 2));
        list.add(new Tuple2("ccc", 3));
        list.add(new Tuple2("ddd", 4));
        list.add(new Tuple2("eee", 5));
        list.add(new Tuple2("fff", 6));

        //通过List创建DataStream
        DataStream<Tuple2<String, Integer>> fromCollectionDataStream = env.fromCollection(list);

        //统计每个单词的数量
        fromCollectionDataStream
                .process(new ProcessFunction<Tuple2<String, Integer>, String>() {
                    @Override
                    public void processElement(Tuple2<String, Integer> value, Context ctx, Collector<String> out) throws Exception {
                        out.collect(value.f1 + "个【" + value.f0 + "】");
                    }
                })
                .print();


        env.execute("processfunction demo : processfunction");
    }
}


