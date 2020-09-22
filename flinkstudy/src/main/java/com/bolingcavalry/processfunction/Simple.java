package com.bolingcavalry.processfunction;


import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-05-16 16:57
 * @description 最简单的ProcessFunctin实现，过滤值为奇数的元素
 */
public class Simple {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        // 并行度为1
        env.setParallelism(1);

        // 设置数据源，一共三个元素
        DataStream<Tuple2<String,Integer>> dataStream = env.addSource(new SourceFunction<Tuple2<String, Integer>>() {
            @Override
            public void run(SourceContext<Tuple2<String, Integer>> ctx) throws Exception {
                for(int i=1; i<4; i++) {

                    String name = "name" + i;
                    Integer value = i;
                    long timeStamp = System.currentTimeMillis();

                    // 将将数据和时间戳打印出来，用来验证数据
                    System.out.println(String.format("source，%s, %d, %d\n",
                            name,
                            value,
                            timeStamp));

                    // 发射一个元素，并且戴上了时间戳
                    ctx.collectWithTimestamp(new Tuple2<String, Integer>(name, value), timeStamp);

                    // 为了让每个元素的时间戳不一样，每发射一次就延时10毫秒
                    Thread.sleep(10);
                }
            }

            @Override
            public void cancel() {

            }
        });


        // 过滤值为奇数的元素
        SingleOutputStreamOperator<String> mainDataStream = dataStream
                .process(new ProcessFunction<Tuple2<String, Integer>, String>() {
                    @Override
                    public void processElement(Tuple2<String, Integer> value, Context ctx, Collector<String> out) throws Exception {
                        // f1字段为奇数的元素不会进入下一个算子
                        if(0 == value.f1 % 2) {
                            out.collect(String.format("process，%s, %d, %d\n",
                                    value.f0,
                                    value.f1,
                                    ctx.timestamp()));
                        }
                    }
                });

        // 打印结果，证明每个元素的timestamp确实可以在ProcessFunction中取得
        mainDataStream.print();

        env.execute("processfunction demo : simple");
    }
}