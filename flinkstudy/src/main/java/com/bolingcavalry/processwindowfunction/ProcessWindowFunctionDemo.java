package com.bolingcavalry.processwindowfunction;


import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-05-16 16:57
 * @description ProcessWindowFunction实现，统计每个窗口内每个key的元素数量，并在backend保存每个key的元素总数
 */
public class ProcessWindowFunctionDemo {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 使用事件时间
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        // 并行度为1
        env.setParallelism(1);

        // 设置数据源，一共三个元素
        DataStream<Tuple2<String,Integer>> dataStream = env.addSource(new SourceFunction<Tuple2<String, Integer>>() {
            @Override
            public void run(SourceContext<Tuple2<String, Integer>> ctx) throws Exception {
                int aaaNum = 0;
                int bbbNum = 0;

                for(int i=1; i<Integer.MAX_VALUE; i++) {
                    // 只有aaa和bbb两种name
                    String name = 0==i%2 ? "aaa" : "bbb";

                    //更新aaa和bbb元素的总数
                    if(0==i%2) {
                        aaaNum++;
                    } else {
                        bbbNum++;
                    }

                    // 使用当前时间作为时间戳
                    long timeStamp = System.currentTimeMillis();

                    // 将数据和时间戳打印出来，用来验证数据
                    System.out.println(String.format("source，%s, %s,    aaa total : %d,    bbb total : %d\n",
                            name,
                            time(timeStamp),
                            aaaNum,
                            bbbNum));

                    // 发射一个元素，并且戴上了时间戳
                    ctx.collectWithTimestamp(new Tuple2<String, Integer>(name, 1), timeStamp);

                    // 每发射一次就延时1秒
                    Thread.sleep(1000);
                }
            }

            @Override
            public void cancel() {

            }
        });

        // 将数据用5秒的滚动窗口做划分，再用ProcessWindowFunction
        SingleOutputStreamOperator<String> mainDataStream = dataStream
                // 以Tuple2的f0字段作为key，本例中实际上key只有aaa和bbb两种
                .keyBy(value -> value.f0)
                // 5秒一次的滚动窗口
                .timeWindow(Time.seconds(5))
                // 统计每个key当前窗口内的元素数量，然后把key、数量、窗口起止时间整理成字符串发送给下游算子
                .process(new ProcessWindowFunction<Tuple2<String, Integer>, String, String, TimeWindow>() {

                    // 自定义状态
                    private ValueState<KeyCount> state;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        // 初始化状态，name是myState
                        state = getRuntimeContext().getState(new ValueStateDescriptor<>("myState", KeyCount.class));
                    }

                    @Override
                    public void process(String s, Context context, Iterable<Tuple2<String, Integer>> iterable, Collector<String> collector) throws Exception {

                        // 从backend取得当前单词的myState状态
                        KeyCount current = state.value();

                        // 如果myState还从未没有赋值过，就在此初始化
                        if (current == null) {
                            current = new KeyCount();
                            current.key = s;
                            current.count = 0;
                        }

                        int count = 0;

                        // iterable可以访问该key当前窗口内的所有数据，
                        // 这里简单处理，只统计了元素数量
                        for (Tuple2<String, Integer> tuple2 : iterable) {
                            count++;
                        }

                        // 更新当前key的元素总数
                        current.count += count;

                        // 更新状态到backend
                        state.update(current);

                        // 将当前key及其窗口的元素数量，还有窗口的起止时间整理成字符串
                        String value = String.format("window, %s, %s - %s, %d,    total : %d\n",
                                // 当前key
                                s,
                                // 当前窗口的起始时间
                                time(context.window().getStart()),
                                // 当前窗口的结束时间
                                time(context.window().getEnd()),
                                // 当前key在当前窗口内元素总数
                                count,
                                // 当前key出现的总数
                                current.count);

                        // 发射到下游算子
                        collector.collect(value);
                    }
                });

        // 打印结果，通过分析打印信息，检查ProcessWindowFunction中可以处理所有key的整个窗口的数据
        mainDataStream.print();

        env.execute("processfunction demo : processwindowfunction");
    }

    public static String time(long timeStamp) {
        return new SimpleDateFormat("hh:mm:ss").format(new Date(timeStamp));
    }

    static class KeyCount {
        /**
         * 分区key
         */
        public String key;

        /**
         * 元素总数
         */
        public long count;
    }

}