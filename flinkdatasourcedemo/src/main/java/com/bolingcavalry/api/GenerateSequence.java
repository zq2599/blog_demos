package com.bolingcavalry.api;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-03-14 22:08
 * @description 通过集合创建的DataSource
 */
public class GenerateSequence {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //并行度为1
        env.setParallelism(1);

        //通过generateSequence得到Long类型的DataSource
        DataStream<Long> dataStream = env.generateSequence(1, 10);

        //做一次过滤，只保留偶数，然后打印
        dataStream.filter(new FilterFunction<Long>() {
            @Override
            public boolean filter(Long aLong) throws Exception {
                return 0L==aLong.longValue()%2L;
            }
        }).print();


        env.execute("API DataSource demo : collection");
    }
}
