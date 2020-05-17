package com.bolingcavalry.keyedprocessfunction;

import com.bolingcavalry.Splitter;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.util.Collector;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-05-17 13:43
 * @description 体验KeyedProcessFunction类(时间类型是处理时间)
 */
public class ProcessTime {

    static class CountWithTimeoutFunction extends KeyedProcessFunction<Tuple, Tuple2<String, Integer>, Tuple2<String, Long>> {

        // 自定义状态
        private ValueState<CountWithTimestamp> state;

        @Override
        public void open(Configuration parameters) throws Exception {
            // 初始化状态，name是myState
            state = getRuntimeContext().getState(new ValueStateDescriptor<>("myState", CountWithTimestamp.class));
        }

        @Override
        public void processElement(
                Tuple2<String, Integer> value,
                Context ctx,
                Collector<Tuple2<String, Long>> out) throws Exception {

            Tuple currentKey = ctx.getCurrentKey();

            // 从backend取得当前单词的myState状态
            CountWithTimestamp current = state.value();

            // 如果myState还从未没有赋值过，就在此初始化
            if (current == null) {
                current = new CountWithTimestamp();
                current.key = value.f0;
            }

            // 单词数量加一
            current.count++;

            // 取当前元素的时间戳，作为该单词最后一次出现的时间
            current.lastModified = ctx.timestamp();

            // 重新保存到backend，包括该单词出现的次数，以及最后一次出现的时间
            state.update(current);

            // 为当前单词创建定时器，十秒后后触发
            long timer = current.lastModified + 10000;
            ctx.timerService().registerProcessingTimeTimer(timer);

            System.out.println(String.format("process, %s, %d, lastModified : %d (%s), timer : %d (%s),",
                    currentKey.getField(0),
                    current.count,
                    current.lastModified,
                    time(current.lastModified),
                    timer,
                    time(timer)));

        }

        /**
         * 定时器触发后执行的方法
         * @param timestamp
         * @param ctx
         * @param out
         * @throws Exception
         */
        @Override
        public void onTimer(
                long timestamp,
                OnTimerContext ctx,
                Collector<Tuple2<String, Long>> out) throws Exception {

            // 取得当前单词
            Tuple currentKey = ctx.getCurrentKey();

            // 取得该单词的myState状态
            CountWithTimestamp result = state.value();

            boolean isTimeout = false;

            // 如果时间戳等于
            if (timestamp == result.lastModified + 10000) {
                // emit the state on timeout
                out.collect(new Tuple2<String, Long>(result.key, result.count));

                isTimeout = true;
            }

            System.out.println(String.format("ontimer, %s, %d, lastModified : %d (%s), stamp : %d (%s), isTimeout : %s, lastModified + 10000 = %d",
                    currentKey.getField(0),
                    result.count,
                    result.lastModified,
                    time(result.lastModified),
                    timestamp,
                    time(timestamp),
                    String.valueOf(isTimeout),
                    result.lastModified + 10000));
        }
    }



    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        //监听本地9999端口，读取字符串
        DataStream<String> socketDataStream = env.socketTextStream("localhost", 9999);

        socketDataStream
                .flatMap(new Splitter())
                //一定要设置watermark，这样才能触发processfunction的onTime操作
                .assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks<Tuple2<String, Integer>>() {


                    @Override
                    public long extractTimestamp(Tuple2<String, Integer> element, long previousElementTimestamp) {
                        return System.currentTimeMillis();
                    }

                    @Override
                    public Watermark getCurrentWatermark() {
                        // return the watermark as current time minus the maximum time lag
                        return new Watermark(System.currentTimeMillis());
                    }
                })
                .keyBy(0)
                .process(new CountWithTimeoutFunction());
        //.print();

        env.execute("API DataSource demo : socket");
    }


    public static String time(long timeStamp) {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(timeStamp));
    }
}
