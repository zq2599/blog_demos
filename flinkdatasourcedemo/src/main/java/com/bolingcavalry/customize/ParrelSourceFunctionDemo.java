package com.bolingcavalry.customize;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-03-21 16:35
 * @description 多并行度自定义数据源实战：实现ParallelSourceFunction接口
 */
public class ParrelSourceFunctionDemo {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //并行度为2
        env.setParallelism(2);

        DataStream<Tuple2<Integer,Integer>> dataStream = env.addSource(new ParallelSourceFunction<Tuple2<Integer, Integer>>() {

            private volatile boolean isRunning = true;

            @Override
            public void run(SourceContext<Tuple2<Integer, Integer>> ctx) throws Exception {
                int i = 0;
                while (isRunning) {
                    ctx.collect(new Tuple2<>(i++ % 5, 1));
                    Thread.sleep(1000);
                    if(i>9){
                        break;
                    }
                }
            }

            @Override
            public void cancel() {
                isRunning = false;
            }
        });

        dataStream
                .keyBy(0)
                .timeWindow(Time.seconds(2))
                .sum(1)
                .print();

        env.execute("Customize DataSource demo : ParallelSourceFunction");
    }
}
