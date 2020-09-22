package com.bolingcavalry.processfunction;


import com.bolingcavalry.Splitter;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;


/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-03-14 22:08
 * @description 通过集合创建的DataSource
 */
public class KeyedProcessFunctionDemo {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

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
}


